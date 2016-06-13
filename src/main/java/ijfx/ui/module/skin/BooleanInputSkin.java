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
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Cyril MONGIS, 2015
 * @author Tuan anh TRINH
 */
@Plugin(type = InputSkinPlugin.class)
public class BooleanInputSkin extends AbstractInputSkinPlugin<Boolean> {

    BooleanProperty value = new SimpleBooleanProperty();
    CheckBox choice;

    public static final String YES = "Yes";
    public static final String NO = "No";

    @Override
    public void init(Input<Boolean> input) {
        
        choice = new CheckBox();
         Bindings.bindBidirectional(choice.selectedProperty(), value);
         
         choice.textProperty().bind(Bindings.createStringBinding(this::getLabel, choice.selectedProperty()));
         
    }
      
    @Override
    public Property<Boolean> valueProperty() {
        return value;
    }
    
    @Override
    public Node getNode() {
        return choice;
    }

    @Override
    public void dispose() {
    }

    private String getLabel() {
        return choice.isSelected() ? YES : NO;
    }
    
    @Override
    public boolean canHandle(Class<?> clazz) {
        return clazz == boolean.class || clazz == Boolean.class;
    }

    

}
