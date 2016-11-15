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
import mongis.utils.bindings.TextToNumberBinding;
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

            toggleGroupValue.valueProperty().addListener(this::onAutoThresholdChanged);
            
            methodComboBox.disableProperty().bind(autoThreshold.not());
            rangeSlider.disableProperty().bind(autoThreshold);
            minValueTextField.disableProperty().bind(autoThreshold);
            maxValueTextField.disableProperty().bind(autoThreshold);
            
            
            
             // binding text field to the low/high values
        //Bindings.bindBidirectional(minValueTextField.textProperty(), lowValue, converter);
        //Bindings.bindBidirectional(maxValueTextField.textProperty(), highValue, converter);
            new TextToNumberBinding(minValueTextField, lowValue);
            new TextToNumberBinding(maxValueTextField, highValue);
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
            autoThreshold.unbindBidirectional(oldValue.autoThreshold);
            methodComboBox.valueProperty().unbindBidirectional(oldValue.methodProperty);
        }
        
       
        
        if (newValue != null) {
            
            double range  = newValue.maxValue.get() - newValue.minValue.get();
            double tick;
            if(range < 2) {
                tick = 0.01;
            }
            else if (range > 2 && range < 10) {
                tick = 0.1;
            }
            else {
                tick = 1;
            }
            
            rangeSlider.setMajorTickUnit(tick);
            rangeSlider.setMinorTickCount(1);
            
            maxValue.bind(newValue.maxValue);
            minValue.bind(newValue.minValue);
            
            highValue.bindBidirectional(newValue.highValue);
            lowValue.bindBidirectional(newValue.lowValue);
           
            toggleGroupValue.setValue(newValue.autoThreshold.getValue());
            autoThreshold.bindBidirectional(newValue.autoThreshold);
            
            methodComboBox.setValue(newValue.methodProperty.getValue());
            methodComboBox.valueProperty().bindBidirectional(newValue.methodProperty);
        }

    }
    
    private void onAutoThresholdChanged(Observable obs, Boolean oldValue, Boolean newValue) {
        
        if(autoThreshold.getValue() != newValue) autoThreshold.setValue(newValue);
        
    }
}
