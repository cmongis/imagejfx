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
package ijfx.ui.module.skin;

import ijfx.ui.module.InputSkinPlugin;
import ijfx.ui.module.input.Input;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import net.imagej.DatasetService;
import net.imagej.ImgPlus;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imglib2.type.numeric.RealType;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = InputSkinPlugin.class)
public class ImgPlusInputSkin extends AbstractInputSkinPlugin<ImgPlus>{

    private final ObjectProperty<ImgPlus> valueProperty = new SimpleObjectProperty();
    
    private final ComboBox<ImageDisplay> comboBox = new ComboBox<>();
    
    
    
    
    @Parameter
    DatasetService datasetService;
    
    
    @Parameter
    ImageDisplayService imageDisplayService;
    
    @Override
    public Property valueProperty() {
        return valueProperty;
    }

    @Override
    public Node getNode() {
        return comboBox;
    }

    @Override
    public void dispose() {
        
        valueProperty.setValue(null);
        
    }

    @Override
    public boolean canHandle(Class<?> clazz) {
       return ImgPlus.class.isAssignableFrom(clazz);
    }

    @Override
    public void init(Input<ImgPlus> input) {
        
        comboBox.getItems().addAll(imageDisplayService.getImageDisplays());
        
        
        if(input.getValue() !=null) {
            
        }
    }
    
    
   
    
    
}
