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

import ijfx.ui.service.ControlableProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import mongis.utils.FXUtilities;
import net.imagej.axis.CalibratedAxis;
import net.imagej.display.DatasetView;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.event.DataViewUpdatedEvent;
import org.scijava.event.EventHandler;
import org.scijava.plugin.Parameter;

/*
Helper Classes
 */
public class AxisSlider extends BorderPane {

    private final CalibratedAxis axis;
    private final ImageDisplay display;
    private final int axisId;
    private final ControlableProperty<ImageDisplay, Number> position;

    
    @FXML
    Slider slider;
    
    @FXML
    Label axisNameLabel;
    
    @FXML
    Label axisPositionLabel;
    
    @Parameter
    ImageDisplayService imageDisplayService;
    
    public AxisSlider(ImageDisplay display, int id) {
        
        super();
        FXUtilities.injectFXMLUnsafe(this);
        
        display.getContext().inject(this);
        
        this.axis = display.axis(id);
        this.display = display;
        axisId = id;
        slider.setMin(display.min(id));
        slider.setMax(display.max(id));
        slider.setValue(display.getLongPosition(id));
        slider.setMajorTickUnit(1.0);
        slider.setMinorTickCount(0);
        slider.setSnapToTicks(true);
        slider.setBlockIncrement(1.0f);
        slider.setShowTickMarks(true);
        //ImageDisplayPane.logService.info(String.format("Adding axis %s (%.1f - %.1f) with initial value : %.3f", axis.type(), getMin(), getMax(), getValue()));
        position = new ControlableProperty<ImageDisplay, Number>().setGetter(this::getPosition).setSetter(this::setPosition);
        slider.valueProperty().bindBidirectional(position);
        
    }

    private Double getPosition() {
        return new Long(display.getLongPosition(axisId)).doubleValue();
    }

    private Long getMinSlider() {
        return display.min(axisId);
    }

    private Long getMaxSlider() {
        return display.max(axisId);
    }

    private void setPosition(Number position) {
        if(slider.isValueChanging()) return;
        int p = position.intValue();
        DatasetView datasetView = imageDisplayService.getActiveDatasetView(display);
        
        if(position.longValue() == getPosition().longValue()) return;
        
        if(datasetView.getProjector().isComposite(p) == false) {
            datasetView.getProjector().setComposite(p, true);
        }
        
        datasetView.setPosition(position.longValue(), axisId);
        datasetView.getProjector().map();
        datasetView.update();
    }

    
    private String getAxisName() {
        return axis.type().getLabel();
    }
    
    private String getAxisPosition() {
        return String.format("%d / %d",getPosition()+1,display.max(axisId));
    }
    
    
    @EventHandler
    private void onDatasetViewChanged(DataViewUpdatedEvent event) {
        position.checkFromGetter();
    }
    
}
