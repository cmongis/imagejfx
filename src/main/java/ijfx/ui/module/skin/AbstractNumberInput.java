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

import ijfx.ui.module.input.Input;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public abstract class AbstractNumberInput<T extends Number> extends AbstractInputSkinPlugin<T> {

    private TextField field;

    public static String CLASS_INVALID = "danger";
    
    public AbstractNumberInput() {
        super();

    }

    protected void onKeyTyped(KeyEvent event) {
        onChange(field.getText());
    }

    public TextField getTextField() {
        return field;
    }

    @Override
    public Node getNode() {

        return field;

    }

    @Override
    public void dispose() {
    }

    Property<T> value = new SimpleObjectProperty<T>();

    abstract T convert(String newValue);

    public void onChange(String newValue) {
        try {
            T value = convert(newValue);
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
    public Property<T> valueProperty() {
        return (ObjectProperty<T>) value;
    }

    @Override
    public void init(Input input) {
        field = new TextField();
        field.setText(input.getValue().toString());
        value.setValue((T)input.getValue());
        Object t = input.getValue();
        field.addEventHandler(KeyEvent.KEY_RELEASED, this::onKeyTyped);
    }

}
