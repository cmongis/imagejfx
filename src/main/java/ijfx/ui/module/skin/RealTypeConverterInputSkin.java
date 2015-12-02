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
package ijfx.ui.module.skin;

import ijfx.ui.module.InputSkinPlugin;
import ijfx.ui.module.input.Input;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import net.imagej.ops.OpService;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.ShortType;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = InputSkinPlugin.class)
public class RealTypeConverterInputSkin extends AbstractInputSkinPlugin<RealType>{

    ObjectProperty<RealType> valueProperty = new SimpleObjectProperty<RealType>(); 
    
    String TYPE_8_BIT = "8-bits";
    String TYPE_16_BIT = "16-bits";
    String TYPE_INTEGER = "Integer";
    String TYPE_FLOAT = "Float";
    String TYPE_DOUBLE = "Double";
    
    ComboBox<String> combobox = new ComboBox<String>();
    
    @Parameter
    OpService opsService;
    
    
    
    public RealTypeConverterInputSkin() {
        super();
        combobox.getItems().setAll("Please choose...",TYPE_8_BIT,TYPE_16_BIT,TYPE_FLOAT,TYPE_DOUBLE);
        
        
        
    }
    
    public void setConverter(String type) {
        if(type == TYPE_8_BIT) {
          
            //valueProperty.setValue(new ConvertPixCopy<ShortType,ByteType>());
            valueProperty.setValue(new ByteType());
        }
    }
    
    @Override
    public Property<RealType> valueProperty() {
        return valueProperty;
    }

   

    @Override
    public Node getNode() {
        
        return combobox;
        
        
    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean canHandle(Class<?> clazz) {
        return RealType.class == clazz;
    }

    @Override
    public void init(Input input) {
    }
    
}
