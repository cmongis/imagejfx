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
import ijfx.ui.module.input.AbstractInputSkin;
import ijfx.ui.module.input.Input;
import ijfx.ui.plugin.LUTComboBox;
import ijfx.ui.plugin.LUTView;
import ijfx.service.ui.FxImageService;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import net.imglib2.display.ColorTable;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = InputSkinPlugin.class)
public class ColorTableInputSkin extends AbstractInputSkinPlugin<ColorTable>{

    LUTComboBox lutComboBox;
    ObjectProperty<ColorTable> value = new SimpleObjectProperty<>();
    public ColorTableInputSkin() {
        super();
        
       
        
    }
    
    public void init(Input input) {
        lutComboBox = new LUTComboBox();
        
         lutComboBox.getItems().addAll(FxImageService.getLUTViewMap().values());
        
        
        value.addListener((obs,oldValue,newValue)->{
            lutComboBox.getSelectionModel().select(getLUTView(newValue));
        });
        
        lutComboBox.getSelectionModel().selectedItemProperty().addListener((obs,oldValue,newValue)->{

            value.setValue(newValue.getColorTable());
        });
        
    }
    
    
    public LUTView getLUTView(ColorTable table) {
        return FxImageService.getLUTViewMap().values().stream().filter(view->view.getColorTable() == table).findFirst().orElse(lutComboBox.getItems().get(0));
    }
    
    @Override
    public Property<ColorTable> valueProperty() {
        return value;
    }

   

    @Override
    public Node getNode() {
        return lutComboBox;
        
    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean canHandle(Class<?> clazz) {
        return ColorTable.class.isAssignableFrom(clazz);
    }
    
}
