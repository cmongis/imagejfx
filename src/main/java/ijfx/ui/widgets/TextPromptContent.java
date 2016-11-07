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
package ijfx.ui.widgets;

import ijfx.ui.main.ImageJFX;
import java.io.IOException;
import java.util.function.Predicate;
import java.util.logging.Level;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import mongis.utils.FXUtilities;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class TextPromptContent extends VBox {

    @FXML
    private Label titleLabel;

    @FXML
    private Label subtitleLabel;

    @FXML
    private TextField textField;

    private Predicate<String> textVerifier;

    private final BooleanProperty validProperty = new SimpleBooleanProperty();

    public TextPromptContent() {
        try {
            FXUtilities.injectFXML(this, "/ijfx/ui/widgets/TextPromptContent.fxml");
            validProperty.bind(Bindings.createBooleanBinding(this::isTextValid, textField.textProperty()));
        } catch (IOException ex) {
            ImageJFX.getLogger().log(Level.SEVERE, null, ex);
        }
    }

    public TextPromptContent setTitle(String title) {
        titleProperty().setValue(title);
        return this;
    }

    public TextPromptContent setSubtitle(String subtitle) {
        subtitleProperty().setValue(subtitle);
        return this;
    }

    public StringProperty titleProperty() {
        return titleLabel.textProperty();
    }

    public StringProperty subtitleProperty() {
        return subtitleLabel.textProperty();
    }

    public boolean isTextValid() {

        if (textVerifier != null) {
            return textVerifier.test(textField.getText());
        } else {
            return textField.getText() != null && textField.getText().trim().equals("") == false;
        }
    }

    public ReadOnlyBooleanProperty validProperty() {
        return validProperty;
    }

    public String getText() {
        return textField.getText();
    }

}
