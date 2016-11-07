
package ijfx.ui.utils;

import ijfx.service.ImagePlaneService;
import ijfx.service.display.DisplayRangeService;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import net.imagej.display.ColorMode;
import net.imagej.display.DatasetView;
import net.imagej.display.ImageDisplay;
import net.imagej.display.event.AxisPositionEvent;
import net.imagej.display.event.DataViewUpdatedEvent;
import net.imagej.ops.OpService;
import org.apache.commons.lang.ArrayUtils;
import org.scijava.Context;
import org.scijava.display.event.DisplayUpdatedEvent;
import org.scijava.event.EventHandler;
import org.scijava.plugin.Parameter;


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
/**
 *
 * @author Cyril MONGIS, 2016
 */
public class ImageDisplayObserver {
    
    
    
    public Property<ImageDisplay> imageDisplayProperty = new SimpleObjectProperty(this,"imageDisplay");
    
    public final Property<long[]> positionProperty = new SimpleObjectProperty(this,"position");
    
    public final Property<long[]> dimensionsProperty = new SimpleObjectProperty<>(this,"dimension");
    
    public final Property<ColorMode> colorModeProperty = new SimpleObjectProperty<>(this,"colorMode");
    
    public final Property<DatasetView> datasetViewProperty = new SimpleObjectProperty<>(this,"datasetView");
    
    public final Property<Integer> currentChannelProperty = new SimpleObjectProperty<>(this,"currentChanenl");
    
    public final DoubleProperty currentChannelMin = new SimpleDoubleProperty(this,"currentChannelMin");
    public final DoubleProperty currentChannelMax = new SimpleDoubleProperty(this,"currentChannelMax");
    
    @Parameter
    Context context;
    
    @Parameter
    OpService opService;
    
    @Parameter
    ImagePlaneService imagePlaneService;
    
    @Parameter
    DisplayRangeService displayRangeService;
    
    public ImageDisplayObserver() {
        
        
        
        imageDisplayProperty.addListener(this::onDisplayChanged);    
        
        datasetViewProperty.addListener(this::onDatasetViewChanged);
        
        positionProperty.addListener(this::onPositionChanged);
        
        currentChannelProperty.setValue(0);
        
        
    }
    
    
    public ImageDisplay getDisplay() {
        return imageDisplayProperty.getValue();
    }

    
    /*
        JavaFX handlers
    */
    
    
    
    private void onDisplayChanged(Observable observable, ImageDisplay oldValue, ImageDisplay display) {
        
        if(display != null) {
            
            if(context == null) {
                display.getContext().inject(this);
            }
            
            DatasetView datasetView = display.stream()
                    .filter(view->view instanceof DatasetView)
                    .findFirst()
                    .map(view->(DatasetView)view)
                    .orElse(null); 
            
            if(datasetView != null) updateLater(datasetViewProperty,datasetView);
        }
        runLaterInParallel(this::updatePosition,this::updateDimension);
    }
    
    
    private void onDatasetViewChanged(Observable obs, DatasetView oldVlaue, DatasetView newValue) {
        
        runLaterInParallel(this::updateColorMode);
        
    }
    
    
    private void onPositionChanged(Observable obs, long[] old, long[] position) {
        runLaterInParallel(this::updateCurrentMinMax);
    }
   
    private void updatePosition() {
        final ImageDisplay display = getDisplay();
        if(display == null) return;
        final long[] localization = getDisplayArray(display,display::localize);
        if(!arrayEquals(localization, positionProperty)) {
           updateLater(positionProperty,localization);
        }
       
    }
    
    
    
    
    private void updateDimension() {
        ImageDisplay display = getDisplay();
        if(display == null) return;
        long[] dimensions = getDisplayArray(display,display::dimensions);
        if(!arrayEquals(dimensions, dimensionsProperty)) {
            updateLater(dimensionsProperty,dimensions);
        }
    }
    
    private void updateColorMode() {
        
        final ImageDisplay display = getDisplay();
    }
    
    private DatasetView datasetView() {
        return datasetViewProperty.getValue();
    }
    
    private int currentChannel() {
        if(currentChannelProperty.getValue() == null) return 0;
        return currentChannelProperty.getValue();
    }
    
    private void updateCurrentMinMax() {
        
        final ImageDisplay display = getDisplay();
        if(display == null)  {
            updateLater(currentChannelMin,0);
            updateLater(currentChannelMax,10);
        }
        else {
            
            double min = datasetView().getChannelMin(currentChannel());
            double max = datasetView().getChannelMax(currentChannel());
            
            updateLater(currentChannelMin,min);
            updateLater(currentChannelMax,max);     
        }
    }
    
    
    
    
   /*
        SciJavaHandlers
    */
    
    
    @EventHandler
    public void onDisplayUpdated(DisplayUpdatedEvent event) {
        if(event.getDisplay() == getDisplay()) {
            runInParallel(this::updateDimension,this::updatePosition);
        }
    }
    
    @EventHandler
    public void onDataviewUpdatedEvent(DataViewUpdatedEvent event) {
        if(event.getView() == datasetView()) {
            runLaterInParallel(this::updatePosition,this::updateCurrentMinMax);
        }
    }
    
    @EventHandler
    public void onAxisEvent(AxisPositionEvent position) {
        
        runLaterInParallel(this::updatePosition);
        
    }
    
    /*
        Utils functions
    */
   
    
     private boolean arrayEquals(long[] arrays, Property<long[]> property) {
        return arrayEquals(arrays,property.getValue());
    }
    
    private boolean arrayEquals(long[] arrays, long[] Arrays) {
        if(arrays == null || Arrays == null) return false;
        if(arrays.length != Arrays.length) return false;
        return ArrayUtils.isEquals(arrays, Arrays);
    }
    
    private long[] getDisplayArray(ImageDisplay display, Consumer<long[]> getter) {
        long[] array = new long[display.numDimensions()];
        getter.accept(array);
        return array;
    }
    
    private <T> void updateLater(Property<T> property, T value) {
        Platform.runLater(()->property.setValue(value));
    }
    
    
    private void runInParallel(Runnable... runnables) {
        Stream.of(runnables)
                .parallel()
                .forEach(Runnable::run);
    }
    
    private void runLaterInParallel(Runnable... runnables) {
        updateLater(()->runInParallel(runnables));
    }
    
    private void updateLater(Runnable... runnable) {
        Platform.runLater(()->{
            Stream.of(runnable).forEach(Runnable::run);
        });
    }
    
    
    /*
        Property
    */
    
    public Property<ImageDisplay> imageDisplayProperty() {
        return imageDisplayProperty;
    }
    
    public Property<long[]> positionProperty() {
        return positionProperty;
    }
    
}
