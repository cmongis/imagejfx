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
package mongis.utils;

import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import java.io.File;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

/**
 *
 * A helper class to create buttons that allow the selection of the directory.
 * When clicking on the button, it opens a file saving dialog or open dialog
 *
 * Ex :
 *
 * Button aButton = new Button();
 *
 * FileButtonBinding fileButtonBinding = new FileButtonBinding(aButton)
 * .setSaveDialog(true) .
 *
 * fileButtonBinding
 *
 *
 * @author Cyril MONGIS, 2016
 */
public class FileButtonBinding {

    private final Button button;

    private final ObjectProperty<File> fileProperty = new SimpleObjectProperty<>(null);

    private String buttonDefaultText = "Choose a directory ...";

   

    private Mode mode;
    
    public enum Mode {
        SAVE
        ,OPEN
        ,FOLDER
    }
    
    public FileButtonBinding(Button button) {
        this(button, null);
    }

    public FileButtonBinding(Button b, File defaultFile) {
        this.button = b;

        button.setOnMouseClicked(this::onClick);

        fileProperty.setValue(defaultFile);
        fileProperty.addListener(this::onFileChanged);
        button.setTooltip(new Tooltip("Right click to reset the parameter."));
        onFileChanged(null, null, fileProperty.getValue());
        button.setGraphic(GlyphsDude.createIcon(FontAwesomeIcon.FOLDER_ALT));
    }

    protected void onClick(MouseEvent event) {

        if(event.getButton() != MouseButton.PRIMARY) {
            fileProperty.setValue(null);
            return;
        }
        
        
        if (mode == Mode.OPEN) {
            FileChooser chooser = new FileChooser();
            File file = chooser.showOpenDialog(null);
            if (file != null) {
                fileProperty.setValue(file);
            }
        }
        else if(mode == Mode.SAVE) {
            FileChooser chooser = new FileChooser();
            File file = chooser.showSaveDialog(null);
            if(file != null) {
                fileProperty.setValue(file);
            }
        }
            else {

            DirectoryChooser chooser = new DirectoryChooser();

            File saveFolder = chooser.showDialog(null);

            if (saveFolder != null) {
                fileProperty.setValue(saveFolder);
            }
        }
    }

    public String getButtonDefaultText() {
        return buttonDefaultText;
    }

    public FileButtonBinding setButtonDefaultText(String buttonDefaultText) {
        this.buttonDefaultText = buttonDefaultText;
        button.setText(buttonDefaultText);
        return this;
    }

    protected void onFileChanged(Observable obs, File oldValue, File newValue) {
        if (newValue == null) {
            button.setText(buttonDefaultText);
        } else if (newValue.getParentFile() == null) {
            button.setText(newValue.getName());
        } else {
            button.setText(newValue.getParentFile().getName() + " / " + newValue.getName());
        }
    }

    public ObjectProperty<File> fileProperty() {
        return fileProperty;
    }

    public FileButtonBinding setMode(Mode mode) {
        this.mode = mode;
        return this;
    }

}
