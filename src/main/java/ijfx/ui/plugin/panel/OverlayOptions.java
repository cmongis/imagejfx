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
package ijfx.ui.plugin.panel;

import java.io.IOException;

import javafx.beans.Observable;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.util.Callback;

import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.OverlayService;

import org.scijava.plugin.Parameter;
import org.scijava.util.ColorRGB;


/**
 *
 * @author Pierre BONNEAU
 */


public class OverlayOptions extends HBox{
    
    
    @FXML
    VBox widthOptionsBox;
    
    @FXML
    ComboBox<Double> widthComboBox;
    
    @FXML
    VBox colorOptionsBox;
    
    @FXML
    HBox lineColor;
    
    @Parameter
    OverlayService overlayService;
    
    @Parameter
    ImageDisplayService imageDisplayService;
    
    @Parameter
    OverlayOptionsService overlayOptionsService;
    
    ColorPicker colorPicker;
    
    double[] WIDTH = {1.0, 2.0, 4.0, 8.0};
        
    public OverlayOptions() throws IOException{

        FXMLLoader loader = new FXMLLoader(getClass().getResource("OverlayOptions.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        loader.load();
        
        colorPicker = new ColorPicker();
        
        colorPicker.valueProperty().addListener(this::onColorChanged);
        
        colorOptionsBox.getChildren().add(colorPicker);

        widthComboBox.setCellFactory(new Callback<ListView<Double>, ListCell<Double>>() {
            
            public ListCell<Double> call(ListView<Double> p) {
                return new ListCell<Double>() {
                    private final Line line;
                    {
                        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

                        line = new Line(0, 10, 40, 10);
                    }
                    
                    @Override
                    protected void updateItem(Double item, boolean empty) {
                        super.updateItem(item, empty);
                        
                        if (item == null || empty) {
                            setGraphic(null);
                        }
                        else {
                            line.setStrokeWidth(item);
                            setGraphic(line);
                        }
                    }
                };
            }
        });
        widthComboBox.setButtonCell(widthComboBox.cellFactoryProperty().getValue().call(null));
        
        for(double d: WIDTH){
            widthComboBox.getItems().add(d);
        }
        
        widthComboBox.setValue(widthComboBox.getItems().get(0));
        
        widthComboBox.valueProperty().addListener(this::onWidthChanged);
    }
    
    
    public void onColorChanged(Observable obs){
        
        ImageDisplay display = imageDisplayService.getActiveImageDisplay();
        
        Color color = colorPicker.getValue();
        ColorRGB colorRGB = toScijavaColor(color);
        overlayOptionsService.colorProperty().setValue(colorRGB);
        overlayService.getActiveOverlay(display).setLineColor(colorRGB);
        overlayService.getActiveOverlay(display).setFillColor(colorRGB);
        overlayService.getActiveOverlay(display).update();
        
    }
    
    
    public void onWidthChanged(Observable obs, Double oldValue, Double newValue){
        
        ImageDisplay display = imageDisplayService.getActiveImageDisplay();

        overlayOptionsService.widthProperty().setValue(newValue);
        overlayService.getActiveOverlay(display).setLineWidth(overlayOptionsService.widthProperty().getValue());
        overlayService.getActiveOverlay(display).update();
    }
    
    
    public ColorRGB toScijavaColor(Color color){
        
        int red = (int) (color.getRed()*255);
        int green = (int) (color.getGreen()*255);
        int blue = (int) (color.getBlue()*255);
        
        return new ColorRGB(red, green, blue);
    }
    
    
    public ColorRGB updateLinesColor(){
        return overlayOptionsService.colorProperty().getValue();
    }
}
