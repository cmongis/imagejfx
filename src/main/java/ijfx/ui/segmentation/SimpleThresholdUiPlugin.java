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

import ijfx.core.utils.DimensionUtils;
import ijfx.plugins.commands.SimpleThreshold;
import ijfx.service.ImagePlaneService;
import ijfx.service.display.DisplayRangeService;
import ijfx.service.workflow.Workflow;
import ijfx.service.workflow.WorkflowBuilder;
import ijfx.ui.context.UiContextProperty;
import ijfx.ui.main.ImageJFX;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import jfxtras.scene.control.ToggleGroupValue;
import mongis.utils.CallbackTask;
import mongis.utils.FXUtilities;
import mongis.utils.SmartNumberStringConverter;
import mongis.utils.TimedBuffer;
import net.imagej.Dataset;
import net.imagej.autoscale.AutoscaleService;
import net.imagej.autoscale.DataRange;
import net.imagej.display.DataView;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.event.AxisPositionEvent;
import net.imagej.threshold.ThresholdMethod;
import net.imagej.threshold.ThresholdService;
import net.imagej.widget.HistogramBundle;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.histogram.Histogram1d;
import net.imglib2.histogram.Real1dBinMapper;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.IntervalView;
import org.apache.commons.lang3.ArrayUtils;
import org.controlsfx.control.RangeSlider;
import org.scijava.Context;
import org.scijava.event.EventHandler;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Cyril MONGIS, 2016
 */
@Plugin(type = SegmentationUiPlugin.class, label = "Fixed / Automatic threshold",priority=0.1)
public class SimpleThresholdUiPlugin extends BorderPane implements SegmentationUiPlugin {

    @FXML
    ComboBox<String> methodComboBox;

    @FXML
    TextField minValueTextField;

    @FXML
    TextField maxValueTextField;

    @FXML
    VBox sliderVBox;

    @FXML
    ToggleButton autoThresholdButton;

    @FXML
    ToggleButton fixedThresholdButton;

    //@FXML
    //Label fixedThresholdLabel;

    //@FXML
    //Label autoThresholdLabel;

    private ImageDisplay imageDisplay = null;

    // ** min max representing the model
    private final DoubleProperty lowValue = new SimpleDoubleProperty();
    private final DoubleProperty highValue = new SimpleDoubleProperty();
    private final DoubleProperty minValue = new SimpleDoubleProperty();
    private final DoubleProperty maxValue = new SimpleDoubleProperty();

    private final Property<Double[]> position = new SimpleObjectProperty();

    private final Property<Img<BitType>> maskProperty = new SimpleObjectProperty();

    private final BooleanProperty activatedProperty = new SimpleBooleanProperty();
    
    BooleanProperty autoThreshold = new SimpleBooleanProperty();

    RangeSlider rangeSlider = new RangeSlider();

    Logger logger = ImageJFX.getLogger();

    Map<String, ThresholdMethod> thresholdMethods;

    /*
        SciJava classes
     */
    @Parameter
    private DisplayRangeService displayRangeService;

    @Parameter
    private ImagePlaneService imagePlaneService;

    @Parameter
    private ThresholdService thresholdService;

    @Parameter
    private ImageDisplayService imageDisplayService;

    @Parameter
    private HistogramBundle histogramService;

    @Parameter
    private AutoscaleService autoScaleService;

    @Parameter
    private Context context;

    @Parameter
    UiContextProperty isExplorer;
    
    
   
    ToggleGroupValue<Boolean> toggleGroupValue = new ToggleGroupValue<>();

    SmartNumberStringConverter converter = new SmartNumberStringConverter();

    TimedBuffer<Runnable> maskUpdateBuffer = new TimedBuffer(100);

    public SimpleThresholdUiPlugin() {

        try {
            FXUtilities.injectFXML(this, "/ijfx/ui/segmentation/SimpleThresholdUiPlugin.fxml");

            sliderVBox.getChildren().add(rangeSlider);
            rangeSlider.setId("thresholdSlider");
            toggleGroupValue.add(fixedThresholdButton, Boolean.FALSE);
            toggleGroupValue.add(autoThresholdButton, Boolean.TRUE);

            autoThreshold.bindBidirectional(toggleGroupValue.valueProperty());
            
        } catch (IOException ex) {
            Logger.getLogger(SimpleThresholdUiPlugin.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void setImageDisplay(ImageDisplay display) {

        if(display == imageDisplay) return;
        
        imageDisplay = display;
        if (display != null) {
            updatePosition(imageDisplay);
            if (isAutothreshold()) {
                
                calculateThresholdValues();
            }
            requestMaskUpdate();
        }
    }

    @Override
    public Node getContentNode() {

        if (thresholdMethods == null) {
            init();
        }

        return this;
    }

    @Override
    public Workflow getWorkflow() {
        return new WorkflowBuilder(context)
                .addStep(SimpleThreshold.class, "value", lowValue.getValue(), "makeBinary", true, "upperCut", false)
                .getWorkflow("Simple threshold");
    }

    @Override
    public Property<Img<BitType>> maskProperty() {
        return maskProperty;
    }

    /*
        Getters and setters
     */
    private boolean isAutothreshold() {
        return autoThreshold.getValue();
    }

    /*
         Listeners
     */
    private void initListeners() {

        // setting the autothreshold status to true
        autoThreshold.setValue(Boolean.TRUE);

        // disabling the rangeSlider in auto mode
        rangeSlider.disableProperty().bind(autoThreshold.or(isExplorer));

        // binding text field to the low/high values
        Bindings.bindBidirectional(minValueTextField.textProperty(), lowValue, converter);
        Bindings.bindBidirectional(maxValueTextField.textProperty(), highValue, converter);

        minValueTextField.disableProperty().bind(autoThreshold);
        maxValueTextField.disableProperty().bind(autoThreshold);
        
        // highlighting labels depending on the mode
        //BindingsUtils.bindNodeToClass(fixedThresholdLabel, autoThreshold.not(), "warning");
        //BindingsUtils.bindNodeToClass(autoThresholdLabel, autoThreshold, "warning");

        methodComboBox.disableProperty().bind(autoThreshold.not());
        
        // when changing the display position, the min and max values should be changed
        position.addListener(this::onPositionChanged);

        // the range slider is bound to local variables
        rangeSlider.lowValueProperty().bindBidirectional(lowValue);
        rangeSlider.highValueProperty().bindBidirectional(highValue);
        rangeSlider.maxProperty().bindBidirectional(maxValue);
        rangeSlider.minProperty().bindBidirectional(minValue);

        // listening the min and max values for updating the max
        lowValue.addListener(this::onMinMaxChanged);
        highValue.addListener(this::onMinMaxChanged);

        // listenening for the mode -> should calculate threshold values
        autoThreshold.addListener(this::onAutothresholdChanged);

        // a mask update should be only occurs every 50ms
        maskUpdateBuffer.setAction(list -> updateMask());

        // filling the threshold methods
        thresholdMethods = thresholdService.getThresholdMethods();

        //
        methodComboBox.getItems().addAll(thresholdMethods.keySet());
        methodComboBox.setValue("Default");

        // requesting refreshing the min/max value when refreshing the method
        methodComboBox.valueProperty().addListener(this::onMethodChanged);

    }

    private ImageDisplay getImageDisplay() {
        return imageDisplay;
    }

    private void setPosition(double[] position) {
        this.position.setValue(ArrayUtils.toObject(position));
    }

    private void onAutothresholdChanged(Observable obs, Boolean oldValue, Boolean newValue) {
        if (newValue) {
            calculateThresholdValues();
        }
    }

    private void onPositionChanged(Observable obs, Double[] oldValue, Double[] position) {
        int channel = displayRangeService.getCurrentChannelId();
        double low = displayRangeService.getChannelMinimum(getImageDisplay(), channel);
        double high = displayRangeService.getChannelMinimum(getImageDisplay(), channel);
        double min = displayRangeService.getDatasetMinimum(getImageDisplay(), channel);
        double max = displayRangeService.getDatasetMaximum(getImageDisplay(), channel);

        minValue.setValue(min);
        maxValue.setValue(max);
        requestMaskUpdate();
    }

    private void init() {
        if (thresholdMethods == null) {
            
            isExplorer = new UiContextProperty(context,"explorer");
            
            initListeners();
        }
    }

    private void onMethodChanged(Observable obs, String oldValue, String newValue) {
        if (!isAutothreshold()) {
            autoThreshold.setValue(Boolean.TRUE);

        } else {
            calculateThresholdValues();
        }
    }

    private void onMinMaxChanged(Observable obs) {
        if(isDisabled()) return;
        logger.info("min max request !");
        Platform.runLater(this::requestMaskUpdate);
    }

    private DataView getDatasetView() {
        if (imageDisplay != null) {
            return imageDisplay.getActiveView();
        } else {
            return null;
        }
    }

    @EventHandler
    public void onAxisEvent(AxisPositionEvent event) {
        if(!isActivated()) return;
        if (event.getDisplay() == imageDisplay) {
            updatePosition(imageDisplay);
        }
    }

    private void updatePosition(ImageDisplay imageDisplay) {
        if (imageDisplay == null) {
            return;
        }
        double[] position = new double[imageDisplay.numDimensions()];

        imageDisplay.localize(position);
        setPosition(position);
    }

    private void requestMaskUpdate() {
        maskUpdateBuffer.add(this::updateMask);
    }

    private void updateMask() {

        new CallbackTask<ImageDisplay, Img<BitType>>()
                .setInput(getImageDisplay())
                .run(this::generateMask)
                .then(maskProperty::setValue)
                .start();

    }

    private <T extends RealType<T>> IntervalView<T> getAccessInterval() {

        Dataset dataset = imageDisplayService.getActiveDataset(imageDisplay);

        // localizing
        long[] position = new long[imageDisplay.numDimensions()];
        imageDisplay.localize(position);

        IntervalView<T> planeView = imagePlaneService.planeView(dataset, DimensionUtils.absoluteToPlanar(position));

        return planeView;

    }

    private <T extends RealType<T>> void calculateThresholdValues() {

        
        if(imageDisplay == null) return;
        
        logger.info("Calculating the threshold values");

        IntervalView<T> planeView = getAccessInterval();

        String methodStr = methodComboBox.getValue() == null ? "Default" : methodComboBox.getValue();

        ThresholdMethod method = thresholdMethods.get(methodStr);

        DataRange range = autoScaleService.getDefaultIntervalRange(planeView);

        Histogram1d<T> histogram = new Histogram1d<T>(planeView, new Real1dBinMapper<>(range.getMin(), range.getMax(), getBins(range), true));

        long threshold = method.getThreshold(histogram);
        DoubleType val = new DoubleType();

        histogram.getLowerBound(threshold, (T) val);

        double min = val.getRealDouble();
        double max = range.getMax();

        lowValue.setValue(min);
        highValue.setValue(max);
    }

    private <T extends RealType<T>> Img<BitType> generateMask(ImageDisplay imageDisplay) {

        if(imageDisplay == null) return null;
        
        logger.info("Generating mask !");
        IntervalView<T> planeView = getAccessInterval();

        ImgFactory<BitType> factory = new ArrayImgFactory<>();
        long[] dimension = new long[2];
        planeView.dimensions(dimension);

        Img<BitType> img = factory.create(dimension, new BitType());

        final double min, max;

        min = lowValue.get();
        max = highValue.get();

        double value;

        Cursor<T> cursor = planeView.cursor();
        RandomAccess<BitType> randomAccess = img.randomAccess();
        cursor.reset();
        while (cursor.hasNext()) {
            cursor.fwd();
            value = cursor.get().getRealDouble();
            randomAccess.setPosition(cursor);
            randomAccess.get().set(value >= min && value <= max);
        }

        return img;

    }

    private int getBins(DataRange range) {

        if (range.getExtent() > 16000) {
            return 16000;
        } else {
            return new Double(range.getExtent()).intValue();
        }

    }

    @Override
    public BooleanProperty activatedProperty() {
        return activatedProperty;
    }

   

}
