/*
 * /*
 *     This file is part of ImageJ FX.
 *
 *     ImageJ FX is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     ImageJ FX is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with ImageJ FX.  If not, see <http://www.gnu.org/licenses/>. 
 *
 * 	Copyright 2015,2016 Cyril MONGIS, Michael Knop
 *
 */
package ijfx.ui.plugin.panel;

import com.google.common.collect.Lists;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import ijfx.core.stats.IjfxStatisticService;
import ijfx.core.utils.DimensionUtils;
import ijfx.plugins.commands.AutoContrast;
import ijfx.plugins.commands.SimpleThreshold;
import ijfx.service.ImagePlaneService;
import ijfx.service.Timer;
import ijfx.service.TimerService;
import ijfx.service.batch.SegmentationService;
import ijfx.ui.main.ImageJFX;
import ijfx.ui.main.Localization;
import ijfx.ui.plugin.LUTComboBox;
import ijfx.ui.plugin.LUTView;
import ijfx.service.display.DisplayRangeService;
import ijfx.service.ui.FxImageService;
import ijfx.service.ui.LoadingScreenService;
import ijfx.service.uicontext.UiContextService;
import ijfx.service.workflow.WorkflowBuilder;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import net.imagej.DatasetService;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.event.DataViewUpdatedEvent;
import net.imagej.lut.LUTService;
import net.imglib2.display.ColorTable;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.RangeSlider;
import org.scijava.Context;
import org.scijava.command.CommandService;
import org.scijava.display.DisplayService;
import org.scijava.display.event.DisplayActivatedEvent;
import org.scijava.event.EventHandler;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.thread.ThreadService;
import ijfx.ui.UiPlugin;
import ijfx.ui.UiConfiguration;
import ijfx.ui.context.UiContextProperty;
import ijfx.ui.utils.ImageDisplayProperty;
import javafx.beans.Observable;
import javafx.concurrent.Task;
import mongis.utils.CallbackTask;
import mongis.utils.FXUtilities;
import net.imagej.display.DataView;
import ijfx.ui.widgets.PopoverToggleButton;
import java.text.NumberFormat;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.event.Event;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.StringConverter;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.NumberStringConverter;
import mongis.utils.SmartNumberStringConverter;
import net.imagej.Dataset;
import net.imagej.axis.Axes;
import net.imagej.display.ColorMode;
import net.imagej.display.DatasetView;
import net.imagej.display.ImageDisplay;
import net.imagej.overlay.Overlay;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;

import org.scijava.module.ModuleService;

/**
 *
 * @author Cyril MONGIS
 *
 * The LUT Panel display the minimum and maximum pixel value of the current
 * image. It also proposes a Range slider allowing the user to change the
 * minimum and maximum displayed values, along with the LUT.
 *
 */
@Plugin(type = UiPlugin.class)
@UiConfiguration(id = "lutPanel", context = "imagej+image-open -overlay-selected", localization = Localization.RIGHT, order = 0)
public class LUTPanel extends TitledPane implements UiPlugin {

    LUTComboBox lutComboBox;

    /*
     ImageJ Services (automatically injected)
     */
    @Parameter
    Context context;

    @Parameter
    ImageDisplayService imageDisplayService;

    @Parameter
    DisplayService displayService;

    @Parameter
    EventService eventService;

    @Parameter
    LUTService lutService;

    @Parameter
    ThreadService threadService;

    @Parameter
    DisplayRangeService displayRangeServ;

    @Parameter
    CommandService commandService;

    @Parameter
    FxImageService fxImageService;
    @Parameter
    DatasetService datasetService;

    @Parameter
    LoadingScreenService loadingService;

    @Parameter
    IjfxStatisticService statsService;

    @Parameter
    ImagePlaneService imagePlaneSrv;

    @Parameter
    TimerService timerService;

    @Parameter
    ModuleService moduleService;

    @Parameter
    SegmentationService segmentationService;

    @Parameter
    UiContextService uiContextService;

    Node lutPanelCtrl;

    @FXML
    VBox vbox;

    @FXML
    ToggleButton minMaxButton;

    RangeSlider rangeSlider = new RangeSlider();

    @FXML
    private ToggleButton mergedViewToggleButton;

    private ReadOnlyBooleanProperty isMultiChannelImage;

    private ImageDisplayProperty currentImageDisplayProperty;

    Button thresholdButton = new Button("Threshold using min-value.", new FontAwesomeIconView(FontAwesomeIcon.EYE));
    Button thresholdMoreButton = new Button("Advanced thresholding");
    // PopOver popOver;
    Logger logger = ImageJFX.getLogger();

    GridPane gridPane = new GridPane();

    TextField minTextField = new TextField();
    TextField maxTextField = new TextField();

    public static final String LUT_COMBOBOX_CSS_ID = "lut-panel-lut-combobox";

    protected DoubleProperty minValue = new SimpleDoubleProperty(0);
    protected DoubleProperty maxValue = new SimpleDoubleProperty(255);

    StringConverter stringConverter;

    public LUTPanel() {

        logger.info("Loading FXML");

        try {
            FXUtilities.injectFXML(this);

            logger.info("FXML loaded");

            // creating the panel that will be hold by the popover
            VBox vboxp = new VBox();
            vboxp.getChildren().addAll(rangeSlider, gridPane);

            // adding the vbox class to the vbox;
            // associating min and max value to properties
            rangeSlider.lowValueProperty().bindBidirectional(minValue);
            rangeSlider.highValueProperty().bindBidirectional(maxValue);

            // taking care of the range slider configuration
            rangeSlider.setShowTickLabels(true);
            rangeSlider.setPrefWidth(255);
            minMaxButton.setPrefWidth(200);

            // threshold button
            thresholdButton.setMaxWidth(Double.POSITIVE_INFINITY);
            thresholdButton.setOnAction(this::thresholdAndSegment);
            gridPane.add(thresholdButton, 0, 2, 2, 1);

            // advanced threshold button
            thresholdMoreButton.setMaxWidth(Double.POSITIVE_INFINITY);
            thresholdMoreButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.PLUS));
            thresholdMoreButton.setOnAction(this::onAdvancedThresholdButtonClicked);
            gridPane.add(thresholdMoreButton, 0, 3, 2, 1);

            // adding other stuffs
            gridPane.add(new Label("Min : "), 0, 0);
            gridPane.add(new Label("Max : "), 0, 1);
            gridPane.add(minTextField, 1, 0);
            gridPane.add(maxTextField, 1, 1);

            gridPane.setHgap(15);
            gridPane.setVgap(15);

            // Adding listeners
            logger.info("Adding listeners");

            maxValue.addListener(this::onHighValueChanging);
            minValue.addListener(this::onLowValueChanging);

            //NumberStringConverter converter = new NumberStringConverter(NumberFormat.getIntegerInstance());
            minTextField.addEventHandler(KeyEvent.KEY_TYPED, this::updateModelRangeFromView);
            maxTextField.addEventHandler(KeyEvent.KEY_TYPED, this::updateModelRangeFromView);
            // setting some insets... should be done int the FXML
            vboxp.setPadding(new Insets(15));

            PopoverToggleButton.bind(minMaxButton, vboxp, PopOver.ArrowLocation.RIGHT_TOP);

            minMaxButton.addEventHandler(ActionEvent.ACTION, event -> updateHistogramAsync());

        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        // System.out.println(label);
    }

    public void buildComboBox() {

        /*
        lutComboBox.setOnAction(event -> {
            applyLUT(lutComboBox.getSelectionModel().getSelectedItem().getColorTable());
        });*/
        lutComboBox.setId("lutPanel");

        lutComboBox.getSelectionModel().selectedItemProperty().addListener(this::onComboBoxChanged);

        lutComboBox.getItems().addAll(FxImageService.getLUTViewMap().values());

    }

    public void onComboBoxChanged(Observable observable, LUTView oldValue, LUTView newValue) {
        if (newValue != null && newValue.getColorTable() != null) {
            applyLUT(newValue.getColorTable());
        }
    }

    public void applyLUT(ColorTable table) {
        DatasetView datasetView = imageDisplayService.getActiveDatasetView();
        HashMap<String, Object> params = new HashMap<>();
        params.put("colorTable", table);
        int channel = imageDisplayService.getActiveDatasetView().getIntPosition(Axes.CHANNEL);
        channel = channel == -1 ? 0 : channel;
        datasetView.setColorTable(table, channel);
    }

    @Override
    public Node getUiElement() {
        return this;
    }

    @Override
    public UiPlugin init() {

        lutComboBox = new LUTComboBox();
        context.inject(lutComboBox);
        lutComboBox.init();
        lutComboBox.setPrefWidth(200);
        lutComboBox.setMinWidth(200);
        buildComboBox();
        vbox.getChildren().add(lutComboBox);

        isMultiChannelImage = new UiContextProperty(context, "multi-channel-img");

        currentImageDisplayProperty = new ImageDisplayProperty(context);

        mergedViewToggleButton.disableProperty().bind(isMultiChannelImage.not());

        mergedViewToggleButton.selectedProperty().addListener(this::onMergedViewToggleButtonChanged);

        
        // binding the min and max textfield so it can display float precision depending on the image
        SmartNumberStringConverter smartNumberStringConverter = new SmartNumberStringConverter();
        smartNumberStringConverter.floatingPointProperty().bind(Bindings.createBooleanBinding(this::isFloat, currentImageDisplayProperty, minValue));

        Bindings.bindBidirectional(minTextField.textProperty(), minValue, smartNumberStringConverter);
        Bindings.bindBidirectional(maxTextField.textProperty(), maxValue, smartNumberStringConverter);

        return this;
    }

    public void updateModelRangeFromView(Event event) {
        updateModelRangeFromView();
    }

    private void updateModelRangeFromView() {
        //System.out.println(minValue);
        //System.out.println(maxValue);
        // //if (rangeSlider.isLowValueChanging() || rangeSlider.isHighValueChanging()) {
        // return;
        // }
        logger.info("updating model from view");
        System.out.println("updating");

        displayRangeServ.updateCurrentDisplayRange(minValue.doubleValue(), maxValue.doubleValue());
        System.out.println("updating");

        // updateLabel();
    }

    private boolean isFloat() {
        return !imageDisplayService.getActiveDataset(currentImageDisplayProperty.getValue()).isInteger();
    }

    public void updateViewRangeFromModel() {
        logger.info("Updating view range from model");
        double min = displayRangeServ.getCurrentViewMinimum();
        double max = displayRangeServ.getCurrentViewMaximum();

        double range = max - min;

        if (rangeSlider.getMin() == min && rangeSlider.getMax() == max) {
            return;
        }
        if (range < 10 && isFloat()) {

            rangeSlider.setMajorTickUnit(0.1);
            rangeSlider.setMinorTickCount(10);
        } else {
            rangeSlider.setMajorTickUnit(displayRangeServ.getCurrentDatasetMaximum() - displayRangeServ.getCurrentDatasetMinimum());
        }
        rangeSlider.setMin(displayRangeServ.getCurrentDatasetMinimum() * .1);
        rangeSlider.setMax(displayRangeServ.getCurrentDatasetMaximum() * 1.1);

        minValue.set(min);
        maxValue.set(max);

        updateLabel();

    }

    public void updateLabel() {

        double min = displayRangeServ.getCurrentViewMinimum();
        double max = displayRangeServ.getCurrentViewMaximum();

        Platform.runLater(() -> {
            minMaxButton.setText(String.format("Min/Max : %.0f - %.0f", min, max));
        });
    }

    @EventHandler
    public void handleEvent(DisplayActivatedEvent event) {

        if (event.getDisplay() instanceof ImageDisplay == false) {
            return;
        }
        if (getCurrentDatasetView() == null) {
            return;
        }
        updateViewRangeFromModel();
        updateLabel();
        mergedViewToggleButton.selectedProperty().setValue(getCurrentDatasetView().getColorMode() == ColorMode.COMPOSITE);
    }

    @EventHandler
    public void handleEvent(DataViewUpdatedEvent event) {
        if (event.getView() != getCurrentDataView()) {
            return;
        }
        updateLabel();
        System.out.println("updated");
        updateViewRangeFromModel();

    }

    private DataView getCurrentDataView() {
        return imageDisplayService.getActiveDatasetView();
    }

    private void onHighValueChanging(Object notUsed) {
        System.out.println("state changed !" + minValue.getValue());
        updateModelRangeFromView();
        updateLabel();
    }

    private void onLowValueChanging(Object notUsed) {
        //System.out.println("state changed");
        updateModelRangeFromView();
        updateLabel();
    }

    @FXML
    private void autoRange(ActionEvent event) {

        Task task
                = new CallbackTask<DatasetView, DatasetView>()
                .setName("Auto-contrast...")
                .setInput(getCurrentDatasetView())
                .run(this::autoContrast)
                .then(o -> {
                    updateViewRangeFromModel();
                    updateLabel();
                }).start();

        loadingService.frontEndTask(task);
        // displayRangeServ.autoRange();

    }

    private DatasetView autoContrast(DatasetView view) {
        try {
            commandService.run(AutoContrast.class, true, "imageDisplay", view, "channelDependant", true).get();
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(LUTPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return view;
    }

    private void onMergedViewToggleButtonChanged(Observable obs) {

        if (getCurrentImageDisplay() != null) {
            if (getCurrentDatasetView().getColorMode() != booleanToColorMode(mergedViewToggleButton.isSelected())) {
                getCurrentDatasetView().setColorMode(mergedViewToggleButton.isSelected() ? ColorMode.COMPOSITE : ColorMode.COLOR);
            }
            getCurrentDatasetView().getProjector().map();
        }

    }

    private ColorMode booleanToColorMode(boolean isComposite) {
        return isComposite ? ColorMode.COMPOSITE : ColorMode.COLOR;
    }

    public ImageDisplay getCurrentImageDisplay() {
        return currentImageDisplayProperty.getValue();
    }

    public DatasetView getCurrentDatasetView() {
        return imageDisplayService.getActiveDatasetView(getCurrentImageDisplay());
    }

    public boolean isCurrentImageComposite() {

        ImageDisplay current = currentImageDisplayProperty.getValue();
        if (current == null) {
            return false;
        }
        Dataset dataset = imageDisplayService.getActiveDataset(current);
        return dataset.isRGBMerged();
    }

    public void toggleRGBView(MouseEvent event) {
        //Dataset dataset = imageDisplayService.getActiveDataset(currentImageDisplayProperty.getValue());
        //dataset.setRGBMerged(!dataset.isRGBMerged());
        // currentImageDisplayProperty.getValue().update();
        // event.consume();

    }

    private void updateHistogramAsync() {
        new CallbackTask<ImageDisplay, List<Number>>()
                .setInput(imageDisplayService.getActiveImageDisplay())
                .run(this::updateHistogram)
                //.then(numberFilter::setAllPossibleValue)
                .start();
    }

    private <T extends RealType<T>> List<Number> updateHistogram(ImageDisplay display) {

        Timer t = timerService.getTimer(this.getClass().getSimpleName());

        System.out.println("updating !");

        t.start();
        long[] position = new long[display.numDimensions()];
        display.localize(position);
        position = DimensionUtils.planarToNonPlanar(position);
        IntervalView<T> planeView = imagePlaneSrv.planeView(imageDisplayService.getActiveDataset(), position);

        t.elapsed("Isolation");
        Double[] values = statsService.getValues(planeView);
        t.elapsed("Value retrieving");
        return Lists.newArrayList(values);
    }

    private void thresholdAndSegment(ActionEvent event) {

        new WorkflowBuilder(context)
                .addInput(imageDisplayService.getActiveImageDisplay())
                .addStep(SimpleThreshold.class, "value", minValue.doubleValue())
                .thenUseDataset(this::segment)
                .start();

    }

    private void onAdvancedThresholdButtonClicked(ActionEvent event) {
        uiContextService.enter("segment segmentation");
        uiContextService.update();
    }

    private void segment(Dataset dataset) {

        new CallbackTask<Dataset, List<Overlay>>()
                .setInput(dataset)
                .setInitialProgress(50)
                .setName("Detecting objects")
                .submit(loadingService)
                .run(d -> {
                    return segmentationService.segmentAndAddToDisplay(dataset, imageDisplayService.getActiveImageDisplay(), true);
                })
                .start();
    }

}
