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
package mongis.utils.bindings;
import java.awt.Event;
import java.time.Duration;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.StringConverter;
import mongis.utils.FXUtilities;
import mongis.utils.SmartNumberStringConverter;
import org.reactfx.EventStreams;

/**
 *
 * @author cyril
 */
public class TextToNumberBinding {

    final TextField textField;
    final Property<Number> numberProperty;

    StringConverter<Number> converter = new SmartNumberStringConverter();

    public TextToNumberBinding(TextField textField, Property<Number> numberProperty) {
        this.textField = textField;
        this.numberProperty = numberProperty;

        EventStreams
                .valuesOf(textField.textProperty())
                .successionEnds(Duration.ofMillis(2000))
                .subscribe(event->updateText());
        numberProperty.addListener(this::onNumberChanged);
        
        textField.addEventHandler(KeyEvent.KEY_PRESSED, this::onKeyTyped);
    }

    public TextToNumberBinding setConverter(StringConverter<Number> string) {

        this.converter = converter;
        return this;

    }

    public void onNumberChanged(Observable obs, Number oldValue, Number newValue) {

        String conversion = converter.toString(newValue);
        if (conversion.equals(textField.getText())) {
            return;
        }
        Platform.runLater(() -> textField.setText(conversion));
    }

    private void updateText() {
        String text = textField.getText();
        System.out.println("new value = "+text.trim());
        if (text.trim().equals("")) {
             text = "0";
        }
        try {
            Number number = converter.fromString(text);

            if (number.doubleValue() == numberProperty.getValue().doubleValue()) {
                return;
            }
            numberProperty.setValue(number.doubleValue());
            FXUtilities.toggleCssStyle(textField, "warning",false);
        } catch (Exception e) {

        }
    }

    public void onKeyTyped(KeyEvent event) {
        FXUtilities.toggleCssStyle(textField, "warning",true);
        if(event.getCode() == KeyCode.ENTER) {
            Platform.runLater(this::updateText);
        }
    }

}
