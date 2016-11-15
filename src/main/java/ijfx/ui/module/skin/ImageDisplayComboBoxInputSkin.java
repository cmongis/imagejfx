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

import ijfx.service.thumb.ThumbService;
import ijfx.ui.module.InputSkinPlugin;
import ijfx.ui.module.input.Input;
import java.util.List;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import mongis.utils.CallbackTask;
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

    @Parameter
    ThumbService thumbService;
    
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
        imageDisplayComboBox.setButtonCell(new ImageDisplayListCell(null));
        //input.getValue();
    }

    public void setCellFactory(ComboBox<ImageDisplay> cmb) {
        cmb.setCellFactory(ImageDisplayListCell::new);
    }
    
    
    
    public class ImageDisplayListCell extends ListCell<ImageDisplay> {
        
        private BorderPane borderPane = new BorderPane();
        
        private Label label = new Label();
        
        private ImageView imageView = new ImageView();
        
        
        
        public ImageDisplayListCell(ListView<ImageDisplay> listView) {
            super();
            
            borderPane.setLeft(imageView);
            borderPane.setCenter(label);
            imageView.setFitWidth(64);
            imageView.setFitHeight(64);
            
            borderPane.getStyleClass().add("image-display-list-cell");
            itemProperty().addListener(this::onItemChanged);
        }
        
        
        
        public void onItemChanged(Observable obs, ImageDisplay oldValue, ImageDisplay newValue) {
            
            if(newValue == null) {
                setGraphic(null);
                return;
            }
            setGraphic(borderPane);
            
            label.setText(newValue.getName());
            
            new CallbackTask<ImageDisplay,Image>()
                    .setInput(newValue)
                    .run(display->thumbService.getThumb(display, 64, 64))
                    .then(imageView::setImage)
                    .start();
                    
        }
        
    }

}
