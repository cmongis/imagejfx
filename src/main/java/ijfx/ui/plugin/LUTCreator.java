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
package ijfx.ui.plugin;

import ijfx.ui.explorer.view.GridIconView;
import ijfx.ui.explorer.view.chartview.ColorGenerator;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import mongis.utils.FXUtilities;

/**
 *
 * @author Tuan anh TRINH
 */
public class LUTCreator extends BorderPane {

    public final static int SIZE_BIG_RECTANGLE = 100;
    public final static int SIZE_SMALL_RECTANGLE = 50;

    @FXML
    ListView<Shape> listViewSamples;
    @FXML
    TilePane resultTilePane;
    @FXML
    Button addButton;

    @FXML
    TextField colorNumberField;

    public LUTCreator() {
        try {
            FXUtilities.injectFXML(this, "/ijfx/ui/plugin/LUTCreator.fxml");
            listViewSamples.setItems(FXCollections.observableArrayList());
            listViewSamples.getItems().addListener((Observable e) -> updateResults());
            colorNumberField.textProperty().addListener(e -> updateResults());
        } catch (IOException ex) {
            Logger.getLogger(GridIconView.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @FXML
    public void onclick() {
        Shape shape = new Rectangle(SIZE_BIG_RECTANGLE, SIZE_BIG_RECTANGLE, Color.AQUA);
        shape.fillProperty().addListener(e -> updateResults());
        shape.setOnMouseClicked(e -> {
            if (e.getButton().equals(MouseButton.PRIMARY)) {
                changeableColor(shape);
                updateResults();
            } else if (e.getButton().equals(MouseButton.SECONDARY)) {
                listViewSamples.getItems().remove(shape);
            }
        });
        listViewSamples.getItems().add(shape);
    }

    private void updateResults() {
        resultTilePane.getChildren().clear();
        List<Color> colors = listViewSamples.getItems()
                .stream()
                .map(e -> (Color) e.getFill())
                .collect(Collectors.toList());
        colors = ColorGenerator.generateColor(colors, Integer.valueOf(colorNumberField.getText()));
        colors.stream()
                .forEach(e -> resultTilePane.getChildren().add(new Rectangle(SIZE_SMALL_RECTANGLE, SIZE_SMALL_RECTANGLE, e)));

    }

    protected void changeableColor(Shape shape) {
        MyCustomColorDialog customColorDialog = new MyCustomColorDialog(this.getScene().getWindow());
        customColorDialog.show();
        customColorDialog.setCurrentColor((Color) shape.getFill());
        customColorDialog.setCustomColor((Color) shape.getFill());
        shape.fillProperty().bind(customColorDialog.customColorProperty());
    }

}
