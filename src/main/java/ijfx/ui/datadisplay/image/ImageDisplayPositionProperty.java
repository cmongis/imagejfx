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

import java.util.Arrays;
import javafx.beans.property.ObjectPropertyBase;
import net.imagej.display.ImageDisplay;

/**
 *
 * @author cyril
 */
public class ImageDisplayPositionProperty extends ObjectPropertyBase<long[]>{
    
    final ImageDisplay imageDisplay;

    public ImageDisplayPositionProperty(ImageDisplay imageDisplay) {
        this.imageDisplay = imageDisplay;
    }

    public void setValue(long[] value) {
        if(getValue() != null && Arrays.equals(getValue(), value)) return;
        super.setValue(value);
    }
    
    public void check() {
        long[] position = new long[imageDisplay.numDimensions()];
        imageDisplay.localize(position);
        setValue(position);
    }
    
    @Override
    public Object getBean() {
       return imageDisplay;
    }

    @Override
    public String getName() {
       return "position";
    }
    
    
    
    
}
