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
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = InputSkinPlugin.class)
public class IntegerInputSkin extends AbstractInputSkinPlugin<Integer> {

    TextField field;

    EventHandler<KeyEvent> changeListener = (event) -> {
        onChange(field.getText());
    };

    public static String CLASS_INVALID = "danger";

    public IntegerInputSkin() {
        super();
    }
    
    
    @Override
    public Node getNode() {

        field.addEventHandler(KeyEvent.KEY_RELEASED, changeListener);

        return field;

    }

    @Override
    public void dispose() {
    }

    Property<Integer> value = new SimpleObjectProperty<>();

    public void onChange(String newValue) {
        try {
            Integer value = Integer.parseInt(newValue);
            valueProperty().setValue(value);

            validProperty().setValue(true);

            field.getStyleClass().remove(CLASS_INVALID);

        } catch (Exception e) {
            if (field.getStyleClass().contains(CLASS_INVALID) == false) {

                validProperty().setValue(false);
                field.getStyleClass().add(CLASS_INVALID);

            }
        }
    }

    @Override
    public Property<Integer> valueProperty() {
        return (ObjectProperty<Integer>) value;
    }

   

    @Override
    public boolean canHandle(Class<?> clazz) {
        return clazz == int.class || clazz == Integer.class;
    }

    @Override
    public void init(Input input) {
        
        field = new TextField();
        field.setText(input.getValue().toString());
    }

}

