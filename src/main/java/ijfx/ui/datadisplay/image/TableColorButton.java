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
package ijfx.ui.datadisplay.image;

import ijfx.service.ui.ControlableProperty;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ToggleButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import net.imagej.axis.Axes;
import net.imagej.display.DatasetView;
import net.imagej.display.event.DataViewUpdatedEvent;
import net.imagej.display.event.LUTsChangedEvent;
import net.imglib2.display.ColorTable;
import net.imglib2.display.ColorTable8;
import org.scijava.event.EventHandler;

/**
 * Button displaying the brightest color of a color table in a rectangle
 * @author cyril
 */
public class TableColorButton extends ToggleButton{
    
   
    private Property<DatasetView> datasetViewProperty = new SimpleObjectProperty();
    
    private IntegerProperty channelProperty = new SimpleIntegerProperty();
    
    private Rectangle rectangle = new Rectangle(10, 10);
    
    
    private ControlableProperty<DatasetView,Boolean> channelActivatedProperty;
            
    
    public TableColorButton() {
        super();
        
        rectangle.getStyleClass().add("rectangle");
        getStyleClass().add("color-button");
        setGraphic(rectangle);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        
       
        rectangle.setWidth(20);
        rectangle.setHeight(20);
        
        channelProperty.addListener(this::onParameterChanged);
        datasetViewProperty.addListener(this::onParameterChanged);
     
        channelActivatedProperty = new ControlableProperty<DatasetView,Boolean>()
                .bindBeanTo(datasetViewProperty)
            .setCaller(this::isActivated)
            .setBiSetter(this::setActivated);
        
        selectedProperty().bindBidirectional(channelActivatedProperty);
        
        
    }
    
   
    
    public Property<DatasetView> datasetViewProperty() {
        return datasetViewProperty;
    }
    
    public IntegerProperty channelProperty() {
        return channelProperty;
    }
    
    
    
    
    private Color getCurrentBrightestColor() {
        
        if(channelProperty.get() == -1 || datasetViewProperty.getValue() == null) {
            return Color.BLACK;
        }
        
        
        return getBrighterColor(datasetViewProperty.getValue().getColorTables().get(channelProperty.get()));
    }
    
    
    private void onParameterChanged(Observable obs, Object o1, Object o2) {
        Platform.runLater(this::updateColor);
    }
    
   private void updateColor() {
       
      rectangle.setFill(getCurrentBrightestColor());
       
       
   } 
   
   
     public Boolean isActivated(DatasetView view) {

      

            return view.getProjector().isComposite(channelProperty().get());

        
    }

    public void setActivated(DatasetView view, Boolean activated) {

        if(activated)view.setPosition(channelProperty().get(), Axes.CHANNEL);

        view.getProjector().setComposite(channelProperty.get(), activated);
        view.getProjector().map();
        view.update();
    }
   
            
    private Color getBrighterColor(ColorTable colorTable) {
        
        
        if(colorTable instanceof ColorTable8) {
            return getBrighterColor((ColorTable8)colorTable);
        }
        else {
            return Color.BLACK;
        }
        
    }
    private Color getBrighterColor(ColorTable8 colorTable) {
        
      
        
        double red = colorTable.get(ColorTable.RED,255);
        double green=colorTable.get(ColorTable.GREEN,255);
        double blue = colorTable.get(ColorTable.BLUE,255);
        
        return new Color(red/255, green/255, blue/255, 1.0);
        
    }
    
    @EventHandler
    private void onDatasetViewUpdated(DataViewUpdatedEvent event) {
        
        channelActivatedProperty.checkFromGetter();
        
        if(event.getView() == datasetViewProperty.getValue()) {
            updateColor();
        }
    }
    
    @EventHandler
    public void onLutChangedEvent(LUTsChangedEvent event) {
        if (datasetViewProperty.getValue() == event.getView()) {
           updateColor();
        }
    }
    
}
