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
package ijfx.ui.datadisplay.image;

import javafx.beans.Observable;
import javafx.scene.control.Slider;
import net.imagej.display.ImageDisplay;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class AxisSliderController {
    
    public final ImageDisplay imageDisplay;
    Slider slider;
    final int axis;

    public void updateFromModel() {   
        slider.setValue(imageDisplay.getDoublePosition(axis));
    }
    
    public AxisSliderController(ImageDisplay imageDisplay, int axis) {
        this.imageDisplay = imageDisplay;
        this.axis = axis;
        rebuild();
        
    }
    
    public void onAxisConfigurationChanged(AxisConfiguration newConfig) {
        rebuild();
        updateFromModel();
    }
    public void setSlider(Slider slider) {
        this.slider = slider;
    }
    
    public void rebuild() {
        slider.setMin(imageDisplay.min(axis)+1);
        slider.setMax(imageDisplay.max(axis)+1);
        
        slider.valueProperty().addListener(this::onValueChanged);
        
    }
    
    public void onValueChanged(Observable obs, Number oldValue, Number newValue) {
        imageDisplay.setPosition(newValue.intValue(), axis);
    }
    
}
