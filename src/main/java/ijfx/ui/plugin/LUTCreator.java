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
import java.util.stream.IntStream;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import mongis.utils.FXUtilities;
import net.imglib2.display.ColorTable;
import net.imglib2.display.ColorTable16;
import net.imglib2.display.ColorTable8;
import org.controlsfx.control.GridView;

/**
 *
 * @author Tuan anh TRINH
 */
public class LUTCreator extends BorderPane {

    public final static int SIZE_BIG_RECTANGLE = 100;
    public final static int SIZE_SMALL_RECTANGLE = 20;

    @FXML
    ListView<Shape> listViewSamples;
    @FXML
    FlowPane flowPane;
    @FXML
    Button addButton;

    @FXML
    CheckBox choiceCheckBox;

//    @FXML
//    TextField colorNumberField;
    List<Color> colors;

    List<Color> generatedColors;

    public LUTCreator(List<Color> colors) {
        try {
            FXUtilities.injectFXML(this, "/ijfx/ui/plugin/LUTCreator.fxml");
            listViewSamples.setItems(FXCollections.observableArrayList());

            listViewSamples.getItems().addListener((Observable e) -> updateResults());
            choiceCheckBox.selectedProperty().addListener((Observable e) -> updateResults());
            colors.stream()
                    .forEach(e -> {
                        Shape shape = new Rectangle(SIZE_BIG_RECTANGLE, SIZE_BIG_RECTANGLE, e);
                        addActionShape(shape);
                        listViewSamples.getItems().add(shape);

                    });
        } catch (IOException ex) {
            Logger.getLogger(GridIconView.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @FXML
    public void onclick() {
        Shape shape = new Rectangle(SIZE_BIG_RECTANGLE, SIZE_BIG_RECTANGLE, Color.RED);
        addActionShape(shape);
        listViewSamples.getItems().add(shape);
    }

    public void addActionShape(Shape shape) {
        shape.fillProperty().addListener(e -> updateResults());
        shape.setOnMouseClicked(e -> {
            if (e.getButton().equals(MouseButton.PRIMARY)) {
                changeColor(shape);
                Runnable runnable = () -> updateResults();
                new Thread(new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        updateResults();
                        return null;
                    }
                }).start();
            } else if (e.getButton().equals(MouseButton.SECONDARY)) {
                listViewSamples.getItems().remove(shape);
            }
        });
    }

    @FXML
    public void deleteColor() {
        listViewSamples.getSelectionModel().getSelectedItems().stream().forEach(e -> listViewSamples.getItems().remove(e));
    }

    private void updateResults() {
        flowPane.getChildren().clear();
        colors = listViewSamples.getItems()
                .stream()
                .map(e -> (Color) e.getFill())
                .collect(Collectors.toList());
        if (choiceCheckBox.isSelected()) {
            generatedColors = ColorGenerator.generateInterpolatedColor(colors, 256);
        } else {
            generatedColors = ColorGenerator.generateColor(colors, 256);
        }
        generatedColors.stream()
                .forEach(e -> {
                    Shape shape = new Rectangle(SIZE_SMALL_RECTANGLE, SIZE_SMALL_RECTANGLE, e);
                    shape.setOnMouseClicked((f) -> {
                        changeColor(shape);
                    });
                    flowPane.getChildren().add(shape);
                });

    }

    protected void changeColor(Shape shape) {
        long startTime = System.currentTimeMillis();

        MyCustomColorDialog customColorDialog = new MyCustomColorDialog(this.getScene().getWindow());
        customColorDialog.show();
        customColorDialog.setCurrentColor((Color) shape.getFill());
        customColorDialog.setCustomColor((Color) shape.getFill());
        shape.fillProperty().bind(customColorDialog.customColorProperty());
        shape.fillProperty().addListener(e -> {
            generatedColors.set(flowPane.getChildren().indexOf(shape), (Color) shape.getFill());
                });

        long estimatedTime = System.currentTimeMillis() - startTime;

        System.out.println(estimatedTime);
    }

    public List<Color> getColors() {
        return colors;
    }

    public List<Color> getGeneratedColors() {
        return generatedColors;
    }

    public static ColorTable colorsToColorTable(List<Color> colors) {
        long startTime = System.currentTimeMillis();

        if (colors.size() < 257) {
            byte[][] values = new byte[3][256];
            IntStream.range(0, colors.size())
                    .forEach(e -> {
                        Double red = ((Double) colors.get(e).getRed()) * 255;
                        Double green = ((Double) colors.get(e).getGreen()) * 255;
                        Double blue = ((Double) colors.get(e).getBlue()) * 255;

                        values[0][e] = red.byteValue();
                        values[1][e] = green.byteValue();
                        values[2][e] = blue.byteValue();

                    });
            return new ColorTable8(values);

        } else {
            short[][] values = new short[3][65536];
            Double redr = ((Double) colors.get(0).getRed()) * 255;
            Double greenr = ((Double) colors.get(0).getGreen()) * 255;
            Double bluer = ((Double) colors.get(0).getBlue()) * 255;

            values[0][0] = redr.shortValue();
            values[1][0] = greenr.shortValue();
            values[2][0] = bluer.shortValue();
            IntStream.range(0, colors.size())
                    .forEach(e -> {
                        Double red = ((Double) colors.get(e).getRed()) * 255;
                        Double green = ((Double) colors.get(e).getGreen()) * 255;
                        Double blue = ((Double) colors.get(e).getBlue()) * 255;

                        values[0][e] = red.shortValue();
                        values[1][e] = green.shortValue();
                        values[2][e] = blue.shortValue();

                    });
            long estimatedTime = System.currentTimeMillis() - startTime;

            System.out.println(estimatedTime);

            return new ColorTable16(values);
        }

    }
}
