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
import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Cyril MONGIS, 2015
 */

@Plugin(type = InputSkinPlugin.class)
public class StringInputSkin extends AbstractInputSkinPlugin<String> {

   
    
    ComboBox<String> comboBox;
    TextField textField;
    Label label;
    StringProperty valueProperty = new SimpleStringProperty("");
    
    public boolean isMultipleChoice() {
        return getSkinnable().getInput().multipleChoices();
    }
    
    public boolean isMessage() {
        return getSkinnable().getInput().isMessage();
    }
    
    @Override
    public Property<String> valueProperty() {
        
        return valueProperty;
       
       
    }

    @Override
    public Node getNode() {
        
        if(isMultipleChoice()) {
            return comboBox;
        }
        else if(isMessage()) {
            return label;
        }
        
        else return textField;
    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean canHandle(Class<?> clazz) {
        return clazz == String.class;
    }

    @Override
    public void init(Input<String> input) {
        
        
        
        if(input.multipleChoices()) {
            comboBox = new ComboBox();
            comboBox.getItems().addAll(input.getChoices());
            comboBox.valueProperty().bindBidirectional(valueProperty);
            
          
            //String value = input.getDefaultValue();
            //comboBox.getSelectionModel().select(input.getValue());
            
        }
        else if(input.isMessage()) {
            label = new Label(input.getValue());
            label.textProperty().bind(valueProperty);
        }
        else {
            textField = new TextField();
            textField.setText(input.getValue());
            valueProperty.setValue(input.getValue());
            textField.textProperty().bindBidirectional(valueProperty);
            if(valueProperty.getValue() == null) valueProperty.setValue("");
            if(input.getValue() == null) input.setValue("");
        }
    }
    
}
