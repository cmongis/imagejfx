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

import ijfx.service.ImagePlaneService;
import ijfx.service.display.DisplayRangeService;
import ijfx.ui.context.UiContextProperty;
import ijfx.ui.segmentation.threshold.ThresholdSegmentation;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import jfxtras.scene.control.ToggleGroupValue;
import mongis.utils.FXUtilities;
import mongis.utils.SmartNumberStringConverter;
import net.imagej.autoscale.AutoscaleService;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.threshold.ThresholdService;
import net.imagej.widget.HistogramBundle;
import org.controlsfx.control.RangeSlider;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Cyril MONGIS, 2016
 */
@Plugin(type = SegmentationUiPlugin.class, label = "Fixed / Automatic threshold", priority = 0.1)
public class SimpleThresholdUiPlugin extends BorderPane implements SegmentationUiPlugin<ThresholdSegmentation> {

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
    /*
    private ImageDisplay imageDisplay = null;
    
    private ObjectProperty<ImageDisplay> imageDisplayProperty = new SimpleObjectProperty<>();
    
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

    RangeSlider rangeSlider = new RangeSlider();

    Property<ThresholdSegmentation> segmentationProperty = new SimpleObjectProperty();

    DoubleProperty minValue = rangeSlider.minProperty();
    DoubleProperty maxValue = rangeSlider.maxProperty();
    DoubleProperty highValue = rangeSlider.highValueProperty();
    DoubleProperty lowValue = rangeSlider.lowValueProperty();

    BooleanProperty autoThreshold = new SimpleBooleanProperty();

    Collection<? extends String> thresholdMethods;

    public SimpleThresholdUiPlugin() {

        try {
            FXUtilities.injectFXML(this, "/ijfx/ui/segmentation/SimpleThresholdUiPlugin.fxml");

            sliderVBox.getChildren().add(rangeSlider);
            rangeSlider.setId("thresholdSlider");
            toggleGroupValue.add(fixedThresholdButton, Boolean.FALSE);
            toggleGroupValue.add(autoThresholdButton, Boolean.TRUE);
            segmentationProperty.addListener(this::onSegmentationPropertyChanged);

            autoThreshold.bindBidirectional(toggleGroupValue.valueProperty());

            methodComboBox.disableProperty().bind(autoThreshold.not());

             // binding text field to the low/high values
        Bindings.bindBidirectional(minValueTextField.textProperty(), lowValue, converter);
        Bindings.bindBidirectional(maxValueTextField.textProperty(), highValue, converter);
            
        } catch (IOException ex) {
            Logger.getLogger(SimpleThresholdUiPlugin.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public Node getContentNode() {
        return this;
    }

    public ThresholdSegmentation createSegmentation(ImageDisplay imageDisplay) {
        return new ThresholdSegmentation(imageDisplay);
    }

    @Override
    public void bind(ThresholdSegmentation t) {
        segmentationProperty.setValue(t);
    }

    private void onSegmentationPropertyChanged(Observable obs, ThresholdSegmentation oldValue, ThresholdSegmentation newValue) {

        if (thresholdMethods == null) {
            // filling the threshold methods
            thresholdMethods = thresholdService.getThresholdMethods().keySet();

            //
            methodComboBox.getItems().addAll(thresholdMethods);
        }

        if (oldValue != null) {
            minValue.unbind();
            maxValue.unbind();

            lowValue.unbindBidirectional(oldValue.lowValue);
            highValue.unbindBidirectional(oldValue.highValue);
            autoThreshold.unbind();
            methodComboBox.valueProperty().unbindBidirectional(oldValue.methodProperty);
        }
        if (newValue != null) {
            highValue.bindBidirectional(newValue.highValue);
            lowValue.bindBidirectional(newValue.lowValue);
            maxValue.bind(newValue.maxValue);
            minValue.bind(newValue.minValue);
            autoThreshold.setValue(newValue.autoThreshold.getValue());
            newValue.autoThreshold.bind(autoThreshold);
            methodComboBox.setValue(newValue.methodProperty.getValue());
            methodComboBox.valueProperty().bindBidirectional(newValue.methodProperty);
        }

    }

    /*
    
    TimedBuffer<Runnable> maskUpdateBuffer = new TimedBuffer(100);

    ConfigurationMapper<ImageDisplay> configurationMapper;
    
    List<Property> properties = new ArrayList<>();
    
    public SimpleThresholdUiPlugin() {

        try {
            FXUtilities.injectFXML(this, "/ijfx/ui/segmentation/SimpleThresholdUiPlugin.fxml");

            sliderVBox.getChildren().add(rangeSlider);
            rangeSlider.setId("thresholdSlider");
            toggleGroupValue.add(fixedThresholdButton, Boolean.FALSE);
            toggleGroupValue.add(autoThresholdButton, Boolean.TRUE);

            autoThreshold.bind(toggleGroupValue.valueProperty());
            
            // adding the properties to listen
            Collections.addAll(properties, minValue,maxValue,autoThreshold,methodComboBox.valueProperty());
            
        } catch (IOException ex) {
            Logger.getLogger(SimpleThresholdUiPlugin.class.getName()).log(Level.SEVERE, null, ex);
        }

    }


    public void process(ImageDisplay display) {

        if(display == imageDisplay) return;
        
        imageDisplay = display;
        imageDisplayProperty.setValue(display);
        if (display != null) {
            updatePosition(imageDisplay);
            if (isAutothreshold()) {
                
                calculateThresholdValues(display);
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

  
    private boolean isAutothreshold() {
        return autoThreshold.getValue();
    }

   
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
       // autoThreshold.addListener(this::onAutothresholdChanged);

        // a mask update should be only occurs every 50ms
        //maskUpdateBuffer.setAction(list -> updateMask());

        // filling the threshold methods
        thresholdMethods = thresholdService.getThresholdMethods();

        //
        methodComboBox.getItems().addAll(thresholdMethods.keySet());
        methodComboBox.setValue("Default");

        // requesting refreshing the min/max value when refreshing the method
        methodComboBox.valueProperty().addListener(this::onMethodChanged);
        
        
        //configurationMapper = new ConfigurationMapper<>(imageDisplayProperty,minValue,maxValue,methodComboBox.valueProperty());

    }

    private ImageDisplay getImageDisplay() {
        return imageDisplay;
    }

    private void setPosition(double[] position) {
        this.position.setValue(ArrayUtils.toObject(position));
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

  

    private <T extends RealType<T>> Img<BitType> generateMask(ProgressHandler handler,ImageDisplay imageDisplay) {

        if(imageDisplay == null) return null;
        
        if(isAutothreshold()) {
            calculateThresholdValues(imageDisplay);
            
            return null;
            
        }
        
        
        logger.info("Generating mask !");
        IntervalView<T> planeView = getAccessInterval(imageDisplay);

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

   
    @Override
    public Segmentation createSegmentation() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void bind(Segmentation t) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
     */
}
