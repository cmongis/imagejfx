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
package ijfx.plugins.bunwarpJ;

import ijfx.ui.module.InputSkinPluginService;
import ijfx.ui.module.input.Input;
import ijfx.ui.module.input.InputControl;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.GridPane;
import org.scijava.plugin.Parameter;

/**
 *
 * @author cyril
 */

public class BUnwarpJConfigurator extends GridPane{
    
    
    Property<bunwarpj.Param> parameterProperty = new SimpleObjectProperty();
    
    int row = 0;
    
    @Parameter
    InputSkinPluginService inputSkinFactory;
    
    
    
    public BUnwarpJConfigurator() {
       
        
       
        
    }
    
    public void addRow(String name, Input input, Class<?> clazz) {
        
        
        InputControl control = new InputControl(inputSkinFactory, input);
    }
    
    
    
}
