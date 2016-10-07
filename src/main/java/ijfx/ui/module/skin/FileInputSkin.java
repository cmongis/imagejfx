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
import java.io.File;
import java.nio.file.Path;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import org.apache.commons.io.FilenameUtils;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = InputSkinPlugin.class)
public class FileInputSkin extends AbstractInputSkinPlugin<File> {

    HBox hbox = new HBox();
    Label label = new Label();
    Button button = new Button();

    ObjectProperty<File> selectedFolder = new SimpleObjectProperty<File>();

    public static final String NO_FILE_SELECTED = "No file selected";
    public static final String SELECT_FILE = "Select file";

    public FileInputSkin() {

        hbox.getChildren().addAll(button);
        selectedFolder.addListener(this::onSelectedFileChanged);

        button.setOnAction(this::onButtonClick);
        button.setText(SELECT_FILE);
        button.onMouseEnteredProperty().addListener(event -> {
            button.setText(SELECT_FILE);
        });

        button.onMouseExitedProperty().addListener(event -> {
            updateLabel(selectedFolder.getValue());
        });
    }

    @Override
    public Property<File> valueProperty() {
        return selectedFolder;
    }

    

    @Override
    public Node getNode() {
        return hbox;
    }

    @Override
    public void dispose() {
    }

    public void updateLabel(File file) {
        if (file == null) {
            button.setText(NO_FILE_SELECTED);
        } else {
            button.setText(file.getName());
        }
    }

    public void onSelectedFileChanged(ObservableValue<? extends File> observable, File oldValue, File newValue) {
        updateLabel(newValue);
    }

    public void onButtonClick(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        Input moduleItem = getSkinnable().getInput();
        boolean save = moduleItem.getLabel().toLowerCase().contains("output") || moduleItem.getName().toLowerCase().contains("output")
                || moduleItem.getName().toLowerCase().contains("save") || moduleItem.getLabel().toLowerCase().contains("save");

        File selected;
        if (save) {
            selected = chooser.showSaveDialog(null);
        } else {
            selected = chooser.showOpenDialog(null);
        }
        if (selected != null) {
            /*
            if(selected.getName().endsWith(".csv") == false) {
                selected = new File(selected.getParentFile(),selected.getName()+".csv");
            }
            */
            selectedFolder.setValue(selected);
        }
    }
    
    public boolean canHandle(Class<?> clazz) {
        return clazz == File.class;
    }

    @Override
    public void init(Input input) {
    }

}
