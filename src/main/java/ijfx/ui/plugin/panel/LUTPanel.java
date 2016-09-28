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
import ijfx.plugins.commands.ApplyLUT;
import ijfx.plugins.commands.AutoContrast;
import ijfx.plugins.commands.SimpleThreshold;
import ijfx.plugins.commands.measures.MeasureAllOverlays;
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
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.Event;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.util.StringConverter;
import mongis.utils.SmartNumberStringConverter;
import mongis.utils.transition.TransitionBinding;
import net.imagej.Dataset;
import net.imagej.display.ColorMode;
import net.imagej.display.DatasetView;
import net.imagej.display.ImageDisplay;
import net.imagej.display.OverlayService;
import net.imagej.event.OverlayCreatedEvent;
import net.imagej.overlay.Overlay;
import net.imagej.overlay.ThresholdOverlay;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;

import org.scijava.module.ModuleService;
import org.scijava.util.Colors;

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

    @Parameter
            OverlayService overlayService;
    
    Node lutPanelCtrl;

    @FXML
    VBox vbox;

    @FXML
    ToggleButton minMaxButton;

    
    
    RangeSlider rangeSlider = new RangeSlider();

    @FXML
    private ToggleButton mergedViewToggleButton;

    private ReadOnlyBooleanProperty isMultiChannelImage;

    private ImageDisplayProperty currentDisplay;

    ToggleButton thresholdButton = new ToggleButton("Threshold using min-value.", new FontAwesomeIconView(FontAwesomeIcon.EYE));
    Button thresholdMoreButton = new Button("Advanced thresholding");
    // PopOver popOver;
    Logger logger = ImageJFX.getLogger();

    GridPane gridPane = new GridPane();

    TextField minTextField = new TextField();
    TextField maxTextField = new TextField();

    public static final String LUT_COMBOBOX_CSS_ID = "lut-panel-lut-combobox";

    protected DoubleProperty lowValue = new SimpleDoubleProperty(0);
    protected DoubleProperty highValue = new SimpleDoubleProperty(255);

    protected StringConverter stringConverter;

    protected DoubleProperty thresholdLevel = new SimpleDoubleProperty();
    
    protected Button analyseParticalButton = new Button("Analyze particles", new FontAwesomeIconView(FontAwesomeIcon.TABLE));
    
    protected final Property<ColorMode> colorMode = new SimpleObjectProperty(ColorMode.COLOR);
    
    protected final BooleanProperty isComposite = new SimpleBooleanProperty();
    
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
            rangeSlider.lowValueProperty().bindBidirectional(lowValue);
            rangeSlider.highValueProperty().bindBidirectional(highValue);

            // taking care of the range slider configuration
            rangeSlider.setShowTickLabels(true);
            rangeSlider.setPrefWidth(255);
            minMaxButton.setPrefWidth(200);

            // threshold button
            thresholdButton.setMaxWidth(Double.POSITIVE_INFINITY);
            //thresholdButton.setOnAction(this::thresholdAndSegment);
            gridPane.add(thresholdButton, 0, 2, 2, 1);

            // analyse particle button
            analyseParticalButton.setMaxWidth(Double.POSITIVE_INFINITY);
            analyseParticalButton.setOnAction(event->commandService.run(MeasureAllOverlays.class, true));
            analyseParticalButton.setOpacity(0.0);
            gridPane.add(analyseParticalButton, 0, 3,2,1);
            new TransitionBinding<Number>(0,1.0)
                    .bind(thresholdButton.selectedProperty(), analyseParticalButton.opacityProperty());
            
            
            // advanced threshold button
            thresholdMoreButton.setMaxWidth(Double.POSITIVE_INFINITY);
            thresholdMoreButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.PLUS));
            thresholdMoreButton.setOnAction(this::onAdvancedThresholdButtonClicked);
            gridPane.add(thresholdMoreButton, 0, 4, 2, 1);

            // adding other stuffs
            gridPane.add(new Label("Min : "), 0, 0);
            gridPane.add(new Label("Max : "), 0, 1);
            gridPane.add(minTextField, 1, 0);
            gridPane.add(maxTextField, 1, 1);

            gridPane.setHgap(15);
            gridPane.setVgap(15);

            // Adding listeners
            logger.info("Adding listeners");

            highValue.addListener(this::onHighValueChanging);
            lowValue.addListener(this::onLowValueChanging);

           
            minTextField.addEventHandler(KeyEvent.KEY_TYPED, this::updateModelRangeFromView);
            maxTextField.addEventHandler(KeyEvent.KEY_TYPED, this::updateModelRangeFromView);
            // setting some insets... should be done int the FXML
            vboxp.setPadding(new Insets(15));

            PopoverToggleButton.bind(minMaxButton, vboxp, PopOver.ArrowLocation.RIGHT_TOP);

            minMaxButton.addEventHandler(ActionEvent.ACTION, event -> updateHistogramAsync());

            isComposite.addListener(this::syncCompositeSettings);
            colorMode.addListener(this::syncCompositeSettings);
            
            colorMode.addListener(this::onColorModeChanged);
            
            mergedViewToggleButton.selectedProperty().bindBidirectional(isComposite);
            
            
            
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

    
    }
       @Override
    public UiPlugin init() {

        // creating the LUT Combobx
        lutComboBox = new LUTComboBox();
        context.inject(lutComboBox);
        lutComboBox.init();
        lutComboBox.setId("lutComboBox");
        lutComboBox.setPrefWidth(200);
        lutComboBox.setMinWidth(200);
        buildComboBox();
        vbox.getChildren().add(lutComboBox);

        // creating a property allowing to know if the current image is multi channel
        isMultiChannelImage = new UiContextProperty(context, "multi-channel-img");

        // property representing the current image display
        currentDisplay = new ImageDisplayProperty(context);

        // binding desabling the merged view button
        mergedViewToggleButton.disableProperty().bind(isMultiChannelImage.not());

      
        
        // binding the min and max textfield so it can display float precision depending on the image
        SmartNumberStringConverter smartNumberStringConverter = new SmartNumberStringConverter();
        smartNumberStringConverter.floatingPointProperty().bind(Bindings.createBooleanBinding(this::isFloat, currentDisplay, lowValue));

        Bindings.bindBidirectional(minTextField.textProperty(), lowValue, smartNumberStringConverter);
        Bindings.bindBidirectional(maxTextField.textProperty(), highValue, smartNumberStringConverter);

        
        thresholdLevel.bind(Bindings.createDoubleBinding(this::getThresholdValue, lowValue,highValue,thresholdButton.selectedProperty()));
        thresholdLevel.addListener(this::onThresholdButtonChanged);
        
        currentDisplay.addListener(this::onDisplayChanged);
        
        return this;
    }

    public void buildComboBox() {

      
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
        // applies the chosen LUT using the ImageJFX dedicated plugin
        commandService.run(ApplyLUT.class,true,"channelId",displayRangeServ.getCurrentChannelId(),"colorTable",table);
        
    }

    @Override
    public Node getUiElement() {
        return this;
    }

 

    public void updateModelRangeFromView(Event event) {
        updateModelRangeFromView();
    }

    private void updateModelRangeFromView() {
       
        logger.info("updating model from view");
        
        displayRangeServ.updateCurrentDisplayRange(lowValue.doubleValue(), highValue.doubleValue());
    }

    private boolean isFloat() {
        if(currentDisplay.getValue() == null) return false;
        return !imageDisplayService.getActiveDataset(currentDisplay.getValue()).isInteger();
    }
    
    private void syncCompositeSettings(Observable o, Object oldValue, Object newValue) {
        if(newValue instanceof ColorMode) {
            isComposite.setValue(ColorModeToBoolean((ColorMode)newValue));
        }
        else {
            if(newValue instanceof Boolean) {
                colorMode.setValue(booleanToColorMode((Boolean)newValue));
            }
        }
    }
    
    

    public void updateViewRangeFromModel() {
        logger.info("Updating view range from model");
        double low = displayRangeServ.getCurrentViewMinimum();
        double high = displayRangeServ.getCurrentViewMaximum();

        double range = high - low;

        if (rangeSlider.getMin() != low) {
             lowValue.setValue(low);
        }
        if(rangeSlider.getMax() != high) {
            highValue.setValue(high);
        }
        
        if(range > 0) {
        if (range < 10 && isFloat()) {

            rangeSlider.setMajorTickUnit(0.1);
            rangeSlider.setMinorTickCount(10);
        } else {
            rangeSlider.setMajorTickUnit(displayRangeServ.getCurrentDatasetMaximum() - displayRangeServ.getCurrentDatasetMinimum());
        }
        }
        
        
        rangeSlider.setMin(displayRangeServ.getCurrentDatasetMinimum() * .1);
        rangeSlider.setMax(displayRangeServ.getCurrentDatasetMaximum() * 1.1);


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
      
        
        
    }

    @EventHandler
    public void handleEvent(DataViewUpdatedEvent event) {
        if (event.getView() != getCurrentDataView()) {
            return;
        }
        updateLabel();
      
        updateViewRangeFromModel();

    }

    private DataView getCurrentDataView() {
        return imageDisplayService.getActiveDatasetView();
    }

    private void onHighValueChanging(Object notUsed) {
       
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
    }

    private DatasetView autoContrast(DatasetView view) {
        try {
            commandService.run(AutoContrast.class, true, "imageDisplay", view, "channelDependant", true).get();
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(LUTPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return view;
    }

    private void onColorModeChanged(Observable obs, ColorMode oldValue, ColorMode newValue) {  
        if (getCurrentImageDisplay() != null) {
            if (getCurrentDatasetView().getColorMode() != newValue) {
                getCurrentDatasetView().setColorMode(mergedViewToggleButton.isSelected() ? ColorMode.COMPOSITE : ColorMode.COLOR);
                getCurrentImageDisplay().update();
            }
        }
    }

    private ColorMode booleanToColorMode(boolean isComposite) {
        return isComposite ? ColorMode.COMPOSITE : ColorMode.COLOR;
    }
    private Boolean ColorModeToBoolean(ColorMode colorMode) {
        return colorMode == ColorMode.COMPOSITE;
    }
    private Boolean isComposite() {
        return colorMode.getValue() == ColorMode.COMPOSITE;
    }

    public ImageDisplay getCurrentImageDisplay() {
        return currentDisplay.getValue();
    }

    public DatasetView getCurrentDatasetView() {
        return imageDisplayService.getActiveDatasetView(getCurrentImageDisplay());
    }

    public boolean isCurrentImageComposite() {

        ImageDisplay current = currentDisplay.getValue();
        if (current == null) {
            return false;
        }
        Dataset dataset = imageDisplayService.getActiveDataset(current);
        return dataset.isRGBMerged();
    }

   

    private void updateHistogramAsync() {
        new CallbackTask<ImageDisplay, List<Number>>()
                .setInput(imageDisplayService.getActiveImageDisplay())
                .run(this::updateHistogram)
                //.then(numberFilter::setAllPossibleValue)
                .start();
    }

    private <T extends RealType<T>> List<Number> updateHistogram(ImageDisplay display) {

        final Timer t = timerService.getTimer(this.getClass().getSimpleName());

    
        t.start();
        long[] position = new long[display.numDimensions()];
        display.localize(position);
        position = DimensionUtils.absoluteToPlanar(position);
        final IntervalView<T> planeView = imagePlaneSrv.planeView(imageDisplayService.getActiveDataset(), position);

        t.elapsed("Isolation");
        final Double[] values = statsService.getValues(planeView);
        t.elapsed("Value retrieving");
        return Lists.newArrayList(values);
    }

    private void thresholdAndSegment(ActionEvent event) {

        new WorkflowBuilder(context)
                .addInput(imageDisplayService.getActiveImageDisplay())
                .addStep(SimpleThreshold.class, "value", lowValue.doubleValue())
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
    
    /*
     * Threshold releated functions
     * 
     */
      
    private Double getThresholdValue() {
        if(thresholdButton.isSelected()) {
            return lowValue.getValue();
        }
        else {
            return Double.NaN;
        }
    }
    
    private void deleteThresholdOverlay() {
        overlayService
                .getOverlays(currentDisplay.getValue())
                .stream()
                .filter(o->o instanceof ThresholdOverlay)
                .map(o->(ThresholdOverlay)o)
                .findFirst()
                .ifPresent(ovrl->overlayService.removeOverlay(getCurrentImageDisplay(), ovrl));
    }
    
    
    private void onDisplayChanged(Observable obs, ImageDisplay oldValue, ImageDisplay newValue) {
        if(newValue == null) return;
        colorMode.setValue(imageDisplayService.getActiveDatasetView(newValue).getColorMode());
        updateViewRangeFromModel();
        updateLabel();
        thresholdButton.setSelected(false);
    }
    
    private void onThresholdButtonChanged(Observable obs, Number oldValue, Number newValue) {
        
        
        if(getCurrentImageDisplay() == null) return;
        
        if(Double.isNaN(newValue.doubleValue()))  {
            deleteThresholdOverlay();
            return;
        }
        Timer t = timerService.getTimer(this.getClass());
        t.start();
        ThresholdOverlay thresholdOverlay = getThresholdOverlay();
        t.elapsed("getting overlay");
        logger.info("Threshold value " + getThresholdValue());
        
        thresholdOverlay.setRange(lowValue.getValue(), highValue.getValue());
        thresholdOverlay.setColorWithin(Colors.WHITE);
			thresholdOverlay.setColorLess(Colors.BLACK);
			thresholdOverlay.setColorGreater(Colors.BLACK);
        thresholdOverlay.update();
        t.elapsed("updating");
        logger.info("update over");
        //eventService.publish(new OverlayUpdatedEvent(thresholdOverlay));
        
        
    }
    
    private Dataset getCurrentDataset() {
        return imageDisplayService.getActiveDataset(getCurrentImageDisplay());
    }
    
    private ThresholdOverlay getThresholdOverlay() {
       logger.info("getting the overlay;");
        return overlayService
                .getOverlays(currentDisplay.getValue())
                .stream()
                .filter(o->o instanceof ThresholdOverlay)
                .map(o->(ThresholdOverlay)o)
                .findFirst()
                .orElseGet(this::createThresholdOverlay);
        
    }
    
    private ThresholdOverlay createThresholdOverlay() {
        logger.info("Creating an overlay");
        ThresholdOverlay overlay = new ThresholdOverlay(context, getCurrentDataset(), lowValue.getValue(), highValue.getValue());
        overlayService.addOverlays(getCurrentImageDisplay(), Lists.newArrayList(overlay));
        eventService.publish(new OverlayCreatedEvent(overlay));
        return overlay;
    }
    

}
