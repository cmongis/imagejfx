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
package ijfx.ui.service;

import ijfx.service.IjfxService;
import ijfx.service.display.DisplayRangeService;
import ijfx.ui.main.ImageJFX;
import java.util.Arrays;
import java.util.function.BiConsumer;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.util.Callback;
import net.imagej.axis.Axes;
import net.imagej.display.ColorMode;
import net.imagej.display.DatasetView;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.event.DataViewUpdatedEvent;
import net.imagej.display.event.LUTsChangedEvent;
import org.scijava.Priority;
import org.scijava.display.DisplayService;
import org.scijava.display.event.DisplayActivatedEvent;
import org.scijava.event.EventHandler;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 *
 * @author cyril
 */
@Plugin(type = Service.class, priority = Priority.VERY_LOW_PRIORITY)
public class ImageDisplayFXService extends AbstractService implements IjfxService {

    private ControlableProperty<ImageDisplay, DatasetView> currentDatasetView;

    private ControlableProperty<DatasetView, Boolean> isComposite;

    private ControlableProperty<ImageDisplayService, ImageDisplay> currentImageDisplay;

    private ControlableProperty<DatasetView, Number> minLUTValue;

    private ControlableProperty<DatasetView, Number> maxLUTValue;

    private ControlableProperty<DatasetView, long[]> currentPosition;
    
    
    
    @Parameter
    ImageDisplayService imageDisplayService;

    @Parameter
    DisplayService displayService;

    @Override
    public void initialize() {

        currentImageDisplay = new ControlableProperty<ImageDisplayService, ImageDisplay>()
                .setBean(imageDisplayService)
                .setSetter(displayService::setActiveDisplay)
                .setGetter(imageDisplayService::getActiveImageDisplay);

        currentDatasetView = new ControlableProperty<ImageDisplay, DatasetView>()
                .bindBeanTo(currentImageDisplay)
                .setGetter(imageDisplayService::getActiveDatasetView);

        isComposite = new ControlableProperty<DatasetView, Boolean>()
                .bindBeanTo(currentDatasetView)
                .setCaller(this::isComposite)
                .setBiSetter(this::setComposite);

        minLUTValue = new ControlableProperty<DatasetView,Number>()
                .setBiSetter(this::setCurrentChannelMin)
                .setCaller(this::getCurrentChannelMin)
                .setSilently(0.0)
                .bindBeanTo(currentDatasetView);
        
         maxLUTValue = new ControlableProperty<DatasetView,Number>()
                .setBiSetter(this::setCurrentChannelMax)
                .setCaller(this::getCurrentChannelMax)
                 .setSilently(255d)
                .bindBeanTo(currentDatasetView);
         
         
         currentPosition = new ControlableProperty<DatasetView,long[]>()
                 .setCaller(this::getCurrentPosition)
                 .bindBeanTo(currentDatasetView);
         
         
        
    }
    
    

    public Property<Boolean> currentColorModeProperty() {
        return isComposite;
    }

    public Property<Number> currentLUTMinProperty() {
        return minLUTValue;
    }
    
    public Property<Number> currentLUTMaxProperty() {
        return maxLUTValue;
    }
    
    public Boolean isComposite(DatasetView view) {
        return view.getColorMode() == ColorMode.COMPOSITE;
    }

    public void setComposite(DatasetView view, Boolean composite) {
        view.setColorMode(composite ? ColorMode.COMPOSITE : ColorMode.COLOR);
        ImageJFX.getThreadPool().submit(view::update);

    }

    @EventHandler
    public void onImageDisplayChanged(DisplayActivatedEvent event) {

        if (event.getDisplay() instanceof ImageDisplay) {
            currentImageDisplay.checkFromGetter();
        }

    }

    @EventHandler
    public void onDatasetViewUpdated(DataViewUpdatedEvent event) {
        if (currentDatasetView.getValue() == event.getView()) {
            isComposite.checkFromGetter();
            maxLUTValue.checkFromGetter();
            minLUTValue.checkFromGetter();
            currentPosition.checkFromGetter();
        }
    }

    @EventHandler
    public void onLutChangedEvent(LUTsChangedEvent event) {
        if (currentDatasetView.getValue() == event.getView()) {
            isComposite.checkFromGetter();
        }
    }

    public Integer getCurrentChannelPosition(DatasetView view) {
        Integer position = view.getIntPosition(Axes.CHANNEL);
        if (position == -1) {
            return 0;
        } else {
            return position;
        }
    }

    private Double getCurrentChannelMin(DatasetView view) {
        if(view == null) return 0d;
        return view.getChannelMin(getCurrentChannelPosition(view));
    }

    private Double getCurrentChannelMax(DatasetView view) {
        if(view == null) return 255d;
        return view.getChannelMax(getCurrentChannelPosition(view));
    }

    private void setCurrentChannelMin(DatasetView view, Number min) {
        if(view == null) return;
        view.setChannelRange(getCurrentChannelPosition(view), min.doubleValue(), getCurrentChannelMax(view));
        ImageJFX.getThreadPool().execute(new ViewUpdate(view));
    }

    private void setCurrentChannelMax(DatasetView view, Number max) {
        if(view == null) return;
        view.setChannelRange(getCurrentChannelPosition(view), getCurrentChannelMin(view), max.doubleValue());
       ImageJFX.getThreadPool().execute(new ViewUpdate(view));
    }
    
    public Property<long[]> currentPositionProperty() {
        return currentPosition;
    }
    
    
    private long[] lastPosition;
    private long[] getCurrentPosition(DatasetView view) {
        
        long[] position = new long[view.numDimensions()];
        view.localize(position);
        
        if(lastPosition == null || Arrays.equals(lastPosition, position) == false) {
            lastPosition = position;
        }
        
        return lastPosition;
    }
   
    
    private class ViewUpdate implements Runnable{
        final DatasetView view;

        public ViewUpdate(DatasetView view) {
            this.view = view;
        }
        
        public void run() {
            view.getProjector().map();
            view.update();
        }
        
    }
    
    
}
