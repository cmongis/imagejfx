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

import ijfx.ui.UiConfiguration;
import ijfx.ui.UiPlugin;
import ijfx.ui.main.Localization;
import ijfx.ui.utils.ImageDisplayObserver;
import ijfx.ui.utils.ImageDisplayProperty;
import ijfx.ui.widgets.PopoverToggleButton;
import java.util.Optional;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.BeanPropertyUtils;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = UiPlugin.class)
@UiConfiguration(id = "observer-test",context = "imagej",localization = Localization.BOTTOM_CENTER)
public class ObserverTest extends PropertySheet implements UiPlugin{

    
    ToggleButton toSee = new ToggleButton("Show");
    
    @Parameter
    Context context;
    
    ImageDisplayProperty imageDisplayProperty;
    
    ImageDisplayObserver observer = new ImageDisplayObserver();
    
    public ObserverTest() {
        super();
        PopoverToggleButton.bind(toSee, this, PopOver.ArrowLocation.LEFT_TOP);
        
        
        
        
    }
            
    @Override
    public Node getUiElement() {
        return toSee;
    }

    @Override
    public UiPlugin init() {
        imageDisplayProperty = new ImageDisplayProperty(context);
        
        observer.imageDisplayProperty.bind(imageDisplayProperty);
        
       getItems().addAll(BeanPropertyUtils.getProperties(observer.positionProperty));
       
       return this;
    }
    
    
    
    
    
}
