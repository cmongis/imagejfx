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

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import ijfx.bridge.ImageJContainer;
import ijfx.core.imagedb.MetaDataExtractionService;
import ijfx.core.metadata.MetaData;
import ijfx.core.metadata.MetaDataSet;
import ijfx.core.metadata.MetaDataSetType;
import ijfx.core.utils.DimensionUtils;
import ijfx.plugins.commands.AxisUtils;
import ijfx.plugins.commands.BinaryToOverlay;
import ijfx.service.ImagePlaneService;
import ijfx.service.batch.SegmentationService;
import ijfx.service.batch.SegmentedObject;
import ijfx.service.batch.SilentImageDisplay;
import ijfx.service.batch.input.AbstractLoaderWrapper;
import ijfx.service.batch.input.DatasetPlaneWrapper;
import ijfx.service.batch.input.ExplorableBatchInputWrapper;
import ijfx.service.overlay.OverlayShapeStatistics;
import ijfx.service.overlay.OverlayStatService;
import ijfx.service.overlay.OverlayStatistics;

import ijfx.service.overlay.OverlayUtilsService;
import ijfx.service.ui.HintService;
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
import ijfx.ui.explorer.ExplorationMode;
import ijfx.ui.explorer.ExplorerActivity;
import ijfx.ui.explorer.ExplorerService;
import ijfx.ui.explorer.Folder;
import ijfx.ui.explorer.FolderManagerService;
import ijfx.ui.main.Localization;
import ijfx.ui.utils.ImageDisplayProperty;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import mongis.utils.CallbackTask;
import mongis.utils.FXUtilities;
import mongis.utils.ProgressHandler;
import mongis.utils.transition.OpacityTransitionBinding;
import net.imagej.Dataset;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.OverlayService;
import net.imagej.overlay.BinaryMaskOverlay;
import net.imagej.overlay.Overlay;
import net.imagej.overlay.PolygonOverlay;
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
 * Menu aimed to ease segmentation while offering segmentation options during
 * explorer mode and normal mode.
 *
 * Possibilities in Image mode: - Particle analysis of the current plane -
 * Particle analysis of the all the plane using the same mask - Particle
 * analysis of all the plane with segmentation of each - Object counting of the
 * current plane - Object counting of all the planes
 *
 * Possibilities in the Explorer Mode - Particles analysis of the selected item
 * - Segmentation of the selected item and use it to measure all the plane of
 * all the source - Count object of the selected items
 *
 * TODO: getting all the logic out of the controller
 *
 * @author Cyril MONGIS, 2016
 */
@Plugin(type = UiPlugin.class)
@UiConfiguration(id = "segmentation-panel-2", localization = Localization.RIGHT, context = "explorerActivity+segmentation segmentation+any-display-open -overlay-selected")
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

    @Parameter
    private OverlayStatService overlayStatService;

    @Parameter
    private HintService hintService;

    @Parameter
    private FolderManagerService folderManagerService;

    @FXML
    private Accordion accordion;

    @FXML
    private SplitMenuButton analyseParticlesButton;

    @FXML
    private SplitMenuButton countObjectsButton;

    @FXML
    private VBox actionVBox;

    @FXML
    private Button segmentMoreButton;

    Img<BitType> currentMask;

    private final Map<TitledPane, SegmentationUiPlugin> nodeMap = new HashMap<>();

    private Property<ImageDisplay> imageDisplayProperty;

    private ImageDisplayProperty imageJCurrentDisplay;

    private final BooleanProperty measureAllPlaneProperty = new SimpleBooleanProperty(false);

    private final Property<ImageDisplay> explorerImageDisplay = new SimpleObjectProperty();

    private final ObjectProperty<SegmentationUiPlugin> currentPlugin = new SimpleObjectProperty();

    private SilentImageDisplay fakeDisplay;

    private UiContextProperty segmentationContext;

    private static final String MEASURE_CURRENT_PLANE = "...only this plane using...";
    private static final String ALL_PLANE_ONE_MASK = "... for all planes using this mask";
    private static final String SEGMENT_AND_MEASURE = "... after segmenting each planes";

    private OpacityTransitionBinding opacityTransition;

    public SegmentationUiPanel() {

        try {
            FXUtilities.injectFXML(this, "/ijfx/ui/segmentation/SegmentationUiPanel.fxml");
            setPrefWidth(200);
            accordion.expandedPaneProperty().addListener(this::onExpandedPaneChanged);
            //measureAllPlaneProperty.bind(planeSettingCheckBox.selectedProperty());

            addAction(FontAwesomeIcon.COPY, ALL_PLANE_ONE_MASK, this::analyseParticlesFromAllPlanes, analyseParticlesButton);
            addAction(FontAwesomeIcon.TASKS, SEGMENT_AND_MEASURE, this::segmentAndAnalyseEachPlane, analyseParticlesButton);
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

    /*
        Init functions
     */
    @Override
    public UiPlugin init() {

        // initializing a fx property
        imageDisplayProperty = new SimpleObjectProperty<>();
        imageJCurrentDisplay = new ImageDisplayProperty(context);
        segmentationContext = new UiContextProperty(context, "segmentation");

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

        segmentationContext.addListener(this::onSegmentationContextChanged);

        onExplorerPropertyChanged(isExplorerProperty, Boolean.FALSE, isExplorer());

        actionVBox.disableProperty().bind(currentPlugin.isNull());

        segmentMoreButton.visibleProperty().bind(isExplorerProperty.not());
        
        //planeSettingCheckBox.textProperty().bind(Bindings.createStringBinding(this::getCheckBoxText, planeSettingCheckBox.selectedProperty()));
        return this;
    }

    private void addAction(FontAwesomeIcon icon, String label, Consumer<ProgressHandler> action, SplitMenuButton menuButton) {

        MenuItem item = new MenuItem(label, new FontAwesomeIconView(icon));
        item.setOnAction(
                event -> new CallbackTask<Void, Void>()
                        .run((progress, voiid) -> {
                            action.accept(progress);
                            return null;
                        })
                        .submit(loadingScreenService)
                        .start()
        );

        menuButton.getItems().add(item);

    }

    /*
     *  Getters 
     * 
     */
    private ImageDisplay getCurrentImageDisplay() {

        return imageDisplayProperty.getValue();
    }

    private SegmentationUiPlugin getCurrentPlugin() {
        return nodeMap.get(getExpandedPane());
    }

    //adds a wrapper
    private void addPlugin(PluginWrapper wrapper) {
        wrapper.activatedProperty().bind(segmentationContext);
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

    private void onExplorerPropertyChanged(Observable obs, Boolean oldValue, Boolean isExplorer) {

        if (isExplorer) {
            imageDisplayProperty.unbind();
            imageDisplayProperty.setValue(null);
        } else {
            imageDisplayProperty.bind(imageJCurrentDisplay);
        }

    }

    private SegmentationUiPlugin getActivePlugin() {
        return nodeMap.get(getExpandedPane());
    }

    private List<? extends Overlay> getOverlays(ProgressHandler progress) {
        // list of overlays to measure
        List<? extends Overlay> overlays;

        // first, let's check if polygon overlays have already been separated
        overlays = overlayUtilsService.findOverlaysOfType(getCurrentImageDisplay(), PolygonOverlay.class);

        // if none if found, we transformed the mask
        if (overlays.isEmpty()) {
            progress.setStatus("Transforming masks into ROIs...");

            overlays = measurementService
                    .extractOverlays(
                            overlayUtilsService
                                    .findOverlayOfType(getCurrentImageDisplay(), BinaryMaskOverlay.class
                                    ));

            // now we remove all the present overlays just in case
            overlayUtilsService.removeAllOverlay(getCurrentImageDisplay());
            overlayUtilsService.addOverlay(getCurrentImageDisplay(), overlays);
        }

        return overlays;
    }

    private BinaryMaskOverlay getBinaryMask(ImageDisplay imageDisplay) {
        return overlayUtilsService.findOverlayOfType(imageDisplay, BinaryMaskOverlay.class);
    }

    private BinaryMaskOverlay createBinaryMaskOverlay(ImageDisplay imageDisplay, Img<BitType> mask) {

        BinaryMaskOverlay overlay = new BinaryMaskOverlay(context, new BinaryMaskRegionOfInterest<>(mask));
        overlayService.addOverlays(imageDisplay, Arrays.asList(overlay));
        return overlay;

    }

    private Predicate<OverlayShapeStatistics> getShapeFilter() {
        return o -> true;
    }


    /*
        EVENT HANDLERS
     */
    private void onSegmentationContextChanged(Observable obs, Boolean oldValue, Boolean isSegmentationContext) {

        if (isSegmentationContext) {
            if (getCurrentPlugin() != null) {
                onMaskChanged(null, null, getCurrentPlugin().maskProperty().getValue());
            }
        } else {
            ImageDisplay display = getCurrentImageDisplay();
            Overlay overlay = overlayUtilsService.findOverlayOfType(getCurrentImageDisplay(), BinaryMaskOverlay.class);
            overlayService.removeOverlay(overlay);
        }

    }

    private void onExpandedPaneChanged(Observable obs, TitledPane oldPane, TitledPane newPane) {
        if (newPane == null) {
            currentPlugin.setValue(null);
            return;
        }

        currentPlugin.setValue(nodeMap.get(newPane));
        nodeMap.get(newPane).setImageDisplay(null);
        nodeMap.get(newPane).setImageDisplay(getCurrentImageDisplay());

        hintService.displayHints(getActivePlugin().getClass(), false);
        hintService.displayHints("/ijfx/ui/segmentation/SegmentationUiPanel-tutorial-hints.json", false);
    }

    private void onMaskChanged(Observable obs, Img<BitType> oldMask, Img<BitType> newMask) {

        if (segmentationContext.getValue() == false) {
            return;
        }

        if (newMask != null && getCurrentImageDisplay() != null) {

            new CallbackTask()
                    .run(() -> updateMask(getCurrentImageDisplay(), newMask))
                    .start();

        }

    }

    // 
    private void onImageDisplayChanged(Observable obs) {
        ImageDisplay display = imageDisplayProperty.getValue();
        if (getActivePlugin() != null) {
            getActivePlugin().setImageDisplay(display);

        }

    }

    /*
        PROCESSES
    
    
    TODO: move processes to an other classes somehow
     */
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

        if (isExplorer()) {
            new CallbackTask<Workflow, Boolean>().
                    setInput(getWorkflow())
                    .run(segmentationService::measureFromExplorer)
                    .submit(loadingScreenService)
                    .start();
        } else {

        }

        new CallbackTask<Object, Object>()
                .run(this::analyseParticleFromCurrentPlane)
                .submit(loadingScreenService)
                .start();
    }

    private Object analyseParticleFromCurrentPlane(ProgressHandler progress, Object object) {

        progress.setStatus("Getting ROIs...");
        progress.setProgress(1, 3);
        List<? extends Overlay> overlays = getOverlays(progress);

        progress.setStatus("Measuring...");
        progress.setProgress(2, 3);
        measurementService.measureOverlays(getCurrentImageDisplay(), overlays, this::filterSegmentedObject);
        return null;
    }

    private void analyseParticlesFromAllPlanes(ProgressHandler progress) {

        // getting the ROIs
        progress.setStatus("Getting ROIs...");
        progress.setProgress(1, 30);

        long[][] planePossibilities = DimensionUtils.allPossibilities(getCurrentImageDisplay());

        if (planePossibilities.length == 0) {
            analyseParticleFromCurrentPlane(progress, null);
            return;
        }

        List<? extends Overlay> overlays = getOverlays(progress);

        // progress parameters
        int total = overlays.size() * planePossibilities.length;
        int i = 0;
        progress.setTotal(total);

        Dataset dataset = imageDisplayService.getActiveDataset();
        List<MetaDataSet> result = new ArrayList<>();

        String displayName = getCurrentImageDisplay().getName();

        for (long[] position : planePossibilities) {

            position = DimensionUtils.planarToAbsolute(position);
            progress.setStatus(String.format("Measuring from plane %d/%d", i++, total));
            for (Overlay overlay : overlays) {
                MetaDataSet set = new MetaDataSet(MetaDataSetType.OBJECT);
                set.putGeneric(MetaData.NAME, getCurrentImageDisplay().getName());
                metaDataSrv.fillPositionMetaData(set, AxisUtils.getAxes(dataset), position);
                OverlayStatistics statistics = overlayStatService.getStatistics(overlay, dataset, position);
                set.merge(overlayStatService.getStatisticsAsMap(statistics));
                progress.increment(1.0);

                result.add(set);

            }

        }

        metaDataDisplayService.findDisplay(String.format("Measure per plane for %s", displayName)).addAll(result);
    }

    private void segmentAndAnalyseEachPlane(ProgressHandler handler) {
        final Dataset dataset = imageDisplayService.getActiveDataset();

        // DatasetPlaneWrapper copies the indicate position from the original dataset before
        // the beginning of the process;
        List<DatasetPlaneWrapper> collect = Stream.of(DimensionUtils.allPossibilities(getCurrentImageDisplay()))
                .map(position -> new DatasetPlaneWrapper(context, dataset, position))
                .collect(Collectors.toList());

        String displayName = String.format("Measures from %s (each plane segmented)", dataset.getName());

        new WorkflowBuilder(context)
                .addInput(collect)
                .execute(getWorkflow())
                .then(output -> {

                    // casting the original input/output
                    DatasetPlaneWrapper wrapper = (DatasetPlaneWrapper) output;

                    long[] position = DimensionUtils.absoluteToPlanar(wrapper.getPositinon());

                    // we duplicate the output
                    Dataset mask = output.getDataset();

                    // getting the source dataset
                    Dataset source = wrapper.getWrappedValue();

                    // getting the list of overlays
                    List<Overlay> overlays = Arrays.asList(BinaryToOverlay.transform(context, mask, true));

                    // getting a global metadata set from the dataset object
                    MetaDataSet set = metaDataSrv.extractMetaData(dataset);

                    // filling the position informations
                    metaDataSrv.fillPositionMetaData(set, AxisUtils.getAxes(dataset), position);

                    // getting back a list of measures;
                    List<? extends SegmentedObject> measures = measurementService.measureOverlays(overlays, source, position);

                    metaDataDisplayService.addMetaDataSetToDisplay(measures, displayName);

                })
                .runSync(handler);

    }

    @FXML
    public void countObjects() {
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

        } else if (!measureAllPlanes()) {

            MetaDataSet set = getDisplayMetaDataSet();
            handler.setProgress(1.5, 3);
            handler.setStatus("Transforming masks...");
            List<Overlay> overlayList = measurementService.transform(currentMask, true);

            measurementService.countObjects(overlayList, o -> true, set, true);

        } else {
            countParticlesFromPlane(handler, getCurrentImageDisplay());
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

            MetaDataSet set = new MetaDataSet(MetaDataSetType.OBJECT);
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

    private void iterateThroughPlanes(ProgressHandler handler, ImageDisplay imageDisplay, Consumer<Dataset> consumer) {
        Dataset dataset = imageDisplayService.getActiveDataset(imageDisplay);
        long[][] planes = DimensionUtils.allPossibilities(imageDisplay);
        handler.setTotal(planes.length);
        handler.setStatus("Process each plane...");
        for (long[] position : planes) {
            Dataset isolatedPlane = imagePlaneService.isolatePlane(dataset, position);
            handler.increment(0.5);
            consumer.accept(dataset);
            handler.increment(0.5);
        }
    }

    private void countParticles(AbstractLoaderWrapper<Explorable> output) {

        List<Overlay> overlays = measurementService.transform((RandomAccessibleInterval) output.getDataset(), true);

        measurementService.countObjects(overlays, getShapeFilter(), output.getWrappedValue().getMetaDataSet(), true);

        activityService.openByType(ImageJContainer.class);

    }

    private boolean measureAllPlanes() {
        return measureAllPlaneProperty.getValue();
    }

    private MetaDataSet getDisplayMetaDataSet() {
        return metaDataSrv.extractMetaData(getCurrentImageDisplay());
    }

    @FXML
    public void segmentMore() {
        ImageDisplay activeImageDisplay = imageDisplayService.getActiveImageDisplay();

        Dataset activeDataset = imageDisplayService.getActiveDataset(activeImageDisplay);

        if (activeDataset != null) {

            String source = activeDataset.getSource();

            Folder folderContainingFile = folderManagerService.getFolderContainingFile(new File(source));

            if (folderContainingFile == null) {
                folderContainingFile = folderManagerService.addFolder(new File(source).getParentFile());
            }

            folderManagerService.setCurrentFolder(folderContainingFile);
        }
        folderManagerService.setExplorationMode(ExplorationMode.FILE);

        activityService.openByType(ExplorerActivity.class);
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

        @Override
        public BooleanProperty activatedProperty() {
            return plugin.activatedProperty();
        }

    }

}
