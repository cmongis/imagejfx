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
package ijfx.ui.plugin.panel;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.SciJavaService;
import org.scijava.util.ColorRGB;

/**
 *
 * @author Pierre BONNEAU
 */
@Plugin(type = SciJavaService.class)
public class OverlayOptionsService extends AbstractService implements SciJavaService{
    
    private Property<ColorRGB> colorProperty;
    private Property<Double> widthProperty;
    
    public OverlayOptionsService(){
        colorProperty = new SimpleObjectProperty<>();
        colorProperty.setValue(new ColorRGB(255, 255, 0));
        
        widthProperty = new SimpleObjectProperty<>(1.0);
    }
    
    public Property<ColorRGB> colorProperty(){
        return colorProperty;
    }
    
    public Property<Double> widthProperty(){
        return widthProperty;
    }

}
