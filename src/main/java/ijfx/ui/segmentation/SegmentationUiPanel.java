/*
    This file is part of ImageJ FX.

    ImageJ FX is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    ImageJ FX is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with ImageJ FX.  If not, see <http://www.gnu.org/licenses/>. 
    
     Copyright 2015,2016 Cyril MONGIS, Michael Knop
	
 */
package ijfx.ui.segmentation;

import ijfx.bridge.ImageJContainer;
import ijfx.core.imagedb.MetaDataExtractionService;
import ijfx.core.metadata.MetaData;
import ijfx.core.metadata.MetaDataSet;
import ijfx.core.utils.DimensionUtils;
import ijfx.plugins.commands.AxisUtils;
import ijfx.service.ImagePlaneService;
import ijfx.service.batch.SegmentationService;
import ijfx.service.batch.SegmentedObject;
import ijfx.service.batch.SilentImageDisplay;
import ijfx.service.batch.input.AbstractLoaderWrapper;
import ijfx.service.batch.input.ExplorableBatchInputWrapper;
import ijfx.service.overlay.OverlayShapeStatistics;

import ijfx.service.overlay.OverlayUtilsService;
import ijfx.service.ui.LoadingScreenService;
import ijfx.service.ui.MeasurementService;
import ijfx.service.workflow.DefaultWorkflow;
import ijfx.service.workflow.Workflow;
import ijfx.service.workflow.WorkflowBuilder;
import ijfx.ui.UiConfiguration;
import ijfx.ui.UiPlugin;
import ijfx.ui.activity.ActivityService;
import ijfx.ui.context.UiContextProperty;
import ijfx.ui.datadisplay.metadataset.MetaDataSetDisplayService;
import ijfx.ui.explorer.Explorable;
import ijfx.ui.explorer.ExplorerService;
import ijfx.ui.main.Localization;
import ijfx.ui.utils.ImageDisplayProperty;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import mongis.utils.CallbackTask;
import mongis.utils.FXUtilities;
import mongis.utils.ProgressHandler;
import net.imagej.Dataset;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.OverlayService;
import net.imagej.overlay.BinaryMaskOverlay;
import net.imagej.overlay.Overlay;
import net.imagej.overlay.ThresholdOverlay;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.roi.BinaryMaskRegionOfInterest;
import net.imglib2.type.logic.BitType;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;

/**
 *
 * @author cyril
 */
@Plugin(type = UiPlugin.class)
@UiConfiguration(id = "segmentation-panel-2", localization = Localization.RIGHT, context = "explorerActivity+segment segment+any-display-open -overlay-selected")
public class SegmentationUiPanel extends BorderPane implements UiPlugin {

    @Parameter
    private Context context;

    @Parameter
    private PluginService pluginService;

    @Parameter
    private OverlayService overlayService;

    @Parameter
    private OverlayUtilsService overlayUtilsService;

    @Parameter
    private SegmentationService segmentationService;

    @Parameter
    private MeasurementService measurementService;

    @Parameter
    private LoadingScreenService loadingScreenService;

    @Parameter
    private ExplorerService explorerService;

    @Parameter
    private MetaDataExtractionService metaDataSrv;

    @Parameter
    private MetaDataSetDisplayService metaDataDisplayService;

    private UiContextProperty isExplorerProperty;

    @Parameter
    private ActivityService activityService;

    @Parameter
    private ImagePlaneService imagePlaneService;

    @Parameter
    private ImageDisplayService imageDisplayService;

    @FXML
    Accordion accordion;

    @FXML
    private CheckBox planeSettingCheckBox;

    Img<BitType> currentMask;

    private final Map<TitledPane, SegmentationUiPlugin> nodeMap = new HashMap<>();

    private Property<ImageDisplay> imageDisplayProperty;

    private ImageDisplayProperty imageJCurrentDisplay;

    private BooleanProperty measureAllPlaneProperty = new SimpleBooleanProperty(false);

    private Property<ImageDisplay> explorerImageDisplay = new SimpleObjectProperty();

    private SilentImageDisplay fakeDisplay;

    public SegmentationUiPanel() {

        try {
            FXUtilities.injectFXML(this, "/ijfx/ui/segmentation/SegmentationUiPanel.fxml");
            setPrefWidth(200);
            accordion.expandedPaneProperty().addListener(this::onExpandedPaneChanged);
            measureAllPlaneProperty.bind(planeSettingCheckBox.selectedProperty());
        } catch (IOException ex) {
            Logger.getLogger(SegmentationUiPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public Node getUiElement() {
        return this;
    }

    private boolean isExplorer() {
        return isExplorerProperty.getValue();
    }

    @Override
    public UiPlugin init() {

        // initializing a fx property
        imageDisplayProperty = new SimpleObjectProperty<>();
        imageJCurrentDisplay = new ImageDisplayProperty(context);

        // when the display is changed, we want to notify only the current wrapper
        imageDisplayProperty.addListener(this::onImageDisplayChanged);

        // adding all the UI Plugins
        pluginService
                .createInstancesOfType(SegmentationUiPlugin.class)
                .stream()
                .map(PluginWrapper::new)
                .forEach(this::addPlugin);

        isExplorerProperty = new UiContextProperty(context, "explorerActivity");

        isExplorerProperty.addListener(this::onExplorerPropertyChanged);

        return this;
    }

   

    private void onExplorerPropertyChanged(Observable obs, Boolean oldValue, Boolean isExplorer) {

        if (isExplorer) {
            imageDisplayProperty.unbind();
            imageDisplayProperty.setValue(null);
        } else {
            imageDisplayProperty.bind(imageDisplayProperty);
        }

    }

    private SegmentationUiPlugin getActivePlugin() {
        return nodeMap.get(getExpandedPane());
    }

    private void onExpandedPaneChanged(Observable obs, TitledPane oldPane, TitledPane newPane) {
        nodeMap.get(newPane).setImageDisplay(getCurrentImageDisplay());
    }

    private void onMaskChanged(Observable obs, Img<BitType> oldMask, Img<BitType> newMask) {

        if (newMask != null && getCurrentImageDisplay() != null) {

            new CallbackTask()
                    .run(() -> updateMask(getCurrentImageDisplay(), newMask))
                    .start();

        }

    }

    private ImageDisplay getCurrentImageDisplay() {
        return imageDisplayProperty.getValue();
    }

    private SegmentationUiPlugin getCurrentPlugin() {
        return nodeMap.get(getExpandedPane());
    }

    //adds a wrapper
    private void addPlugin(PluginWrapper wrapper) {

        wrapper.maskProperty().addListener(this::onMaskChanged);
        // put it in a map with the corresponding plugin
        nodeMap.put(wrapper, wrapper.getPlugin());
        getPanes().add(wrapper);

    }

    private List<TitledPane> getPanes() {
        return accordion.getPanes();
    }

    private TitledPane getExpandedPane() {
        return accordion.getExpandedPane();
    }

    // 
    private void onImageDisplayChanged(Observable obs, ImageDisplay oldValue, ImageDisplay newValue) {

        if (getActivePlugin() != null) {
            getActivePlugin().setImageDisplay(newValue);
        }

    }

    private BinaryMaskOverlay getBinaryMask(ImageDisplay imageDisplay) {
        return overlayUtilsService.findOverlayOfType(imageDisplay, BinaryMaskOverlay.class);
    }

    private BinaryMaskOverlay createBinaryMaskOverlay(ImageDisplay imageDisplay, Img<BitType> mask) {

        BinaryMaskOverlay overlay = new BinaryMaskOverlay(context, new BinaryMaskRegionOfInterest<>(mask));

        overlayService.addOverlays(imageDisplay, Arrays.asList(overlay));

        return overlay;

    }

    private synchronized void updateMask(ImageDisplay imageDisplay, Img<BitType> mask) {

        ThresholdOverlay findOverlayOfType = overlayUtilsService.findOverlayOfType(imageDisplay, ThresholdOverlay.class);

        if (findOverlayOfType != null) {
            overlayService.removeOverlay(findOverlayOfType);
        }

        BinaryMaskOverlay overlay = getBinaryMask(imageDisplay);
        if (overlay == null) {
            overlay = createBinaryMaskOverlay(imageDisplay, mask);
        } else {

            BinaryMaskRegionOfInterest regionOfInterest = (BinaryMaskRegionOfInterest) overlay.getRegionOfInterest();

            RandomAccessibleInterval<BitType> img = regionOfInterest.getImg();
            RandomAccess<BitType> randomAccess = img.randomAccess();
            Cursor<BitType> cursor = mask.cursor();

            cursor.reset();
            while (cursor.hasNext()) {
                cursor.fwd();
                randomAccess.setPosition(cursor);
                randomAccess.get().set(cursor.get());
            }

        }
        currentMask = mask;
        overlay.update();
    }

    @FXML
    public void analyseParticles() {

        new CallbackTask<Object, Object>()
                .run((progress, status) -> {

                    progress.setStatus("Transforming mask...");
                    progress.setProgress(1, 3);
                    List<Overlay> overlays = measurementService
                            .extractOverlays(
                                    overlayUtilsService
                                    .findOverlayOfType(getCurrentImageDisplay(), BinaryMaskOverlay.class
                                    ));
                    overlayUtilsService.removeAllOverlay(getCurrentImageDisplay());

                    overlayUtilsService.addOverlay(getCurrentImageDisplay(), overlays);
                    progress.setStatus("Measuring...");
                    progress.setProgress(2, 3);
                    measurementService.measureOverlays(getCurrentImageDisplay(), overlays, this::filterSegmentedObject);
                    return null;
                })
                .submit(loadingScreenService)
                .start();
    }

    private Object analyseParticles(ProgressHandler progress, Object object) {

        return null;
    }

    @FXML
    public void countParticles() {
        new CallbackTask<Void, Void>()
                .runLongCallable(this::countParticles)
                .submit(loadingScreenService)
                .start();
    }

    private Workflow getWorkflow() {
        return new DefaultWorkflow(getCurrentPlugin().getWorkflow().getStepList());
    }

    private Void countParticles(ProgressHandler handler) {

        if (isExplorer()) {

            List<AbstractLoaderWrapper<Explorable>> inputs = explorerService
                    .getSelectedItems()
                    .stream()
                    .map(explorable -> new ExplorableBatchInputWrapper(explorable).onSave(this::countParticles))
                    .collect(Collectors.toList());

            new WorkflowBuilder(context)
                    .addInput(inputs)
                    .execute(getWorkflow())
                    .startAndShow();

        } else {
            if (!measureAllPlanes()) {

                MetaDataSet set = getDisplayMetaDataSet();
                handler.setProgress(1.5, 3);
                handler.setStatus("Transforming masks...");
                List<Overlay> overlayList = measurementService.transform(currentMask, true);

                measurementService.countObjects(overlayList, o -> true, set, true);

            } else {
                countParticlesFromPlane(handler, getCurrentImageDisplay());
            }
        }
        return null;

    }

    private void countParticlesFromPlane(ProgressHandler handler, ImageDisplay imageDisplay) {

        Dataset dataset = imageDisplayService.getActiveDataset(imageDisplay);
        long[][] planes = DimensionUtils.allPossibilities(imageDisplay);
        handler.setTotal(planes.length);
        handler.setStatus("Process each plane...");
        for (long[] position : planes) {

            Dataset isolatedPlane = imagePlaneService.isolatePlane(dataset, position);

            handler.increment(0.5);

            MetaDataSet set = new MetaDataSet();
            set.putGeneric(MetaData.NAME, imageDisplay.getName());
            metaDataSrv.fillPositionMetaData(set, AxisUtils.getAxes(dataset), DimensionUtils.planarToAbsolute(position));

            boolean result = new WorkflowBuilder(context)
                    .addInput(isolatedPlane)
                    .execute(getWorkflow())
                    .thenUseDataset(mask -> {
                        measurementService.countObjects(mask, getShapeFilter(), set, true);

                    })
                    .runSync(null);

            handler.increment(0.5);
        }

    }

    private void countParticles(AbstractLoaderWrapper<Explorable> output) {

        List<Overlay> overlays = measurementService.transform((RandomAccessibleInterval) output.getDataset(), true);

        measurementService.countObjects(overlays, getShapeFilter(), output.getWrappedValue().getMetaDataSet(), true);

        activityService.openByType(ImageJContainer.class);

    }

    private Predicate<OverlayShapeStatistics> getShapeFilter() {
        return o -> true;
    }

    private boolean measureAllPlanes() {
        return measureAllPlaneProperty.getValue();
    }

    private MetaDataSet getDisplayMetaDataSet() {
        return metaDataSrv.extractMetaData(getCurrentImageDisplay());
    }

    @FXML
    public void segmentMore() {

    }

    private boolean filterSegmentedObject(SegmentedObject segmentedObject) {
        return segmentedObject.getMetaDataSet().getOrDefault(MetaData.LBL_AREA, MetaData.NULL).getDoubleValue() > 8;
    }

    // Wrapping class
    private class PluginWrapper extends TitledPane implements SegmentationUiPlugin {

        private final SegmentationUiPlugin plugin;

        public PluginWrapper(SegmentationUiPlugin plugin) {
            this.plugin = plugin;
            setText(plugin.getName());
            setContent(plugin.getContentNode());

        }

        public SegmentationUiPlugin getPlugin() {
            return plugin;
        }

        @Override
        public void setImageDisplay(ImageDisplay display) {
            plugin.setImageDisplay(display);
        }

        @Override
        public Node getContentNode() {
            return this;
        }

        @Override
        public Workflow getWorkflow() {
            return plugin.getWorkflow();
        }

        @Override
        public Property<Img<BitType>> maskProperty() {
            return plugin.maskProperty();
        }

        public String getName() {
            return plugin.getName();
        }

    }

}
