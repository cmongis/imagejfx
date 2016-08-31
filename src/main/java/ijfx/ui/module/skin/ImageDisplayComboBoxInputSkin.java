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
import java.awt.Rectangle;
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import net.imagej.DatasetService;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import org.scijava.display.DisplayService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Tuan anh TRINH
 */
@Plugin(type = InputSkinPlugin.class)
public class ImageDisplayComboBoxInputSkin extends AbstractInputSkinPlugin<ImageDisplay> {

    ObjectProperty<ImageDisplay> valueProperty = new SimpleObjectProperty<>();
    @Parameter
    ImageDisplayService imageDisplayService;

    @Parameter
    DatasetService datasetService;

    @Parameter
    DisplayService displayService;

    ComboBox<ImageDisplay> imageDisplayComboBox = new ComboBox<>();

    @Override
    public Property valueProperty() {

        return valueProperty;
    }

    @Override
    public Node getNode() {
        return imageDisplayComboBox;
    }

    @Override
    public void dispose() {

    }

    @Override
    public boolean canHandle(Class<?> clazz) {
        System.out.println("can i handle ?" + clazz);
        return clazz == ImageDisplay.class;
    }

    @Override
    public void init(Input<ImageDisplay> input) {

        //datasetComboBox.getItems().addAll(datasetService.getDatasets());
        List<ImageDisplay> toAdd = imageDisplayService.getImageDisplays();

        imageDisplayComboBox.getItems().addAll(toAdd);
        imageDisplayComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            valueProperty.setValue(newValue);
        });
        setCellFactory(imageDisplayComboBox);

        //input.getValue();
    }

    public void setCellFactory(ComboBox<ImageDisplay> cmb) {
        cmb.setCellFactory((ListView<ImageDisplay> p) -> new ListCell<ImageDisplay>() {
            @Override
            protected void updateItem(ImageDisplay item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    setText(item.getName());
                }
            }
        });
    }

}
