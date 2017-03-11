package ijfx.ui.utils;

import ijfx.service.ui.ControlableProperty;
import ijfx.service.ui.ImageDisplayFXService;
import ijfx.service.ui.ReadOnlySuppliedProperty;
import ijfx.ui.main.ImageJFX;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import net.imagej.Dataset;
import net.imagej.axis.Axes;
import net.imagej.display.ColorMode;
import net.imagej.display.DatasetView;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.event.AxisPositionEvent;
import net.imagej.display.event.DataViewUpdatedEvent;
import net.imagej.display.event.LUTsChangedEvent;
import net.imagej.event.DatasetUpdatedEvent;
import net.imagej.lut.LUTService;
import net.imglib2.display.ColorTable;
import org.reactfx.EventStream;
import org.reactfx.EventStreams;
import org.scijava.Context;
import org.scijava.display.DisplayService;
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

    private ControlableProperty<ImageDisplay, DatasetView> currentDatasetView;

    private ControlableProperty<DatasetView, Boolean> isComposite;

    private Property<ImageDisplay> currentImageDisplay = new SimpleObjectProperty<>();

    private ControlableProperty<DatasetView, Number> minLUTValue;

    private ControlableProperty<DatasetView, Number> maxLUTValue;

    private ControlableProperty<DatasetView, long[]> currentPosition;

    private ControlableProperty<ImageDisplay, Dataset> currentDataset;

    private ControlableProperty<Dataset, Number> minDatasetValue;

    private ControlableProperty<Dataset, Number> maxDatasetValue;

    private ControlableProperty<DatasetView, Number> currentChannel;

    private ControlableProperty<DatasetView, Double> currentChannelAsDouble;
    
    private ControlableProperty<DatasetView, ColorTable> currentLUT;

    private Property<Double> possibleMinLUTValue = new SimpleObjectProperty<Double>();

    private Property<Double> possibleMaxLUTValue = new SimpleObjectProperty<Double>();

    private ReadOnlyProperty<Number> channelCountProperty = new ReadOnlySuppliedProperty<Number>(this::getChannelCount);
            
    
    
    private static ObservableList<ColorTable> colorTableList;

    @Parameter
    ImageDisplayService imageDisplayService;

    @Parameter
    DisplayService displayService;

    @Parameter
    ImageDisplayFXService imageDisplayFXService;

    @Parameter
    LUTService lutService;

    Property lastChanges = new SimpleObjectProperty();

    EventStream<Object> modificationStream = EventStreams.valuesOf(lastChanges);

    
    
    public ImageDisplayObserver(Context context) {

        context.inject(this);

        currentDatasetView = new ControlableProperty<ImageDisplay, DatasetView>()
                .bindBeanTo(currentImageDisplay)
                .setGetter(imageDisplayService::getActiveDatasetView);

        isComposite = new ControlableProperty<DatasetView, Boolean>()
                .bindBeanTo(currentDatasetView)
                .setCaller(this::isComposite)
                .setBiSetter(this::setComposite);

        minLUTValue = new ControlableProperty<DatasetView, Number>()
                .setBiSetter(this::setCurrentChannelMin)
                .setCaller(this::getCurrentChannelMin)
                .setSilently(0.0)
                .bindBeanTo(currentDatasetView);

        maxLUTValue = new ControlableProperty<DatasetView, Number>()
                .setBiSetter(this::setCurrentChannelMax)
                .setCaller(this::getCurrentChannelMax)
                .setSilently(255d)
                .bindBeanTo(currentDatasetView);

        currentPosition = new ControlableProperty<DatasetView, long[]>()
                .setCaller(this::getCurrentPosition)
                .bindBeanTo(currentDatasetView);

        currentDataset = new ControlableProperty<ImageDisplay, Dataset>()
                .setGetter(imageDisplayService::getActiveDataset)
                .bindBeanTo(currentImageDisplay);

        currentChannel = new ControlableProperty<DatasetView, Number>()
                .setCaller(this::getCurrentChannel)
                .setBiSetter(this::setCurrentChannel)
                .bindBeanTo(currentDatasetView);

        
        
        minDatasetValue = new ControlableProperty<Dataset, Number>()
                .setGetter(this::getCurrentDatasetMin)
                .bindBeanTo(currentDataset);

        maxDatasetValue = new ControlableProperty<Dataset, Number>()
                .setGetter(this::getCurrentDatasetMax)
                .bindBeanTo(currentDataset);

        currentLUT = new ControlableProperty<DatasetView, ColorTable>()
                .setBiSetter(this::setCurrentLUT)
                .setCaller(this::getCurrentLUT)
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

    public Property<Double> possibeMinLUTValueProperty() {
        return possibleMinLUTValue;
    }

    public Property<Double> possibleMaxLUTValueProperty() {
        return possibleMaxLUTValue;
    }

    public Property<Number> currentDatasetMaximumValue() {
        return maxDatasetValue;
    }

    public Property<Number> currentDatasetMinimumValue() {
        return minDatasetValue;
    }

    public Property<Dataset> currentDatasetProperty() {
        return currentDataset;
    }

    public Property<ColorTable> currentLUTProperty() {
        return currentLUT;
    }
    
    public Property<Number> currentChannelProperty() {
        return currentChannel;
    }
    
    public Property<? extends Number> currentChannelPropertyAsDouble() {
        return currentChannel;
    }

    public Boolean isComposite(DatasetView view) {
        return view.getColorMode() == ColorMode.COMPOSITE;
    }

    public void setComposite(DatasetView view, Boolean composite) {
        view.setColorMode(composite ? ColorMode.COMPOSITE : ColorMode.COLOR);
        ImageJFX.getThreadPool().submit(view::update);

    }

    public ColorTable getCurrentLUT() {
        return ImageDisplayObserver.this.getCurrentLUT(currentDatasetView.getValue());
    }

    public ColorTable getCurrentLUT(DatasetView view) {
        ColorTable table = view.getColorTables().get(getCurrentChannel());
        if (table == null) {
            table = currentDataset.getValue().getColorTable(getCurrentChannel());
        }

        if (availableColorTableProperty().contains(table) == false) {
            final ColorTable initial = table;
            ColorTable match = availableColorTableProperty()
                    .stream()
                    .filter(t -> compare(t, initial))
                    .findFirst()
                    .orElse(null);

            if (match == null) {
                availableColorTableProperty().add(initial);

                return initial;
            } else {
                return match;
            }
        }

        return table;
    }

    public void setCurrentLUT(DatasetView view, ColorTable colorTable) {

        view.setColorTable(colorTable, getCurrentChannel());

        //currentDataset.getValue().setColorTable(colorTable, getCurrentChannel());
        ImageJFX.getThreadPool().execute(new ViewUpdate(view));
        //view.getProjector().map();
        //view.update();
    }

    @EventHandler
    public void onDatasetViewUpdated(DataViewUpdatedEvent event) {
        if (currentDatasetView.getValue() == event.getView()) {
            isComposite.checkFromGetter();
            currentChannel.checkFromGetter();
            currentPosition.checkFromGetter();

            maxLUTValue.checkFromGetter();
            minLUTValue.checkFromGetter();
            minDatasetValue.checkFromGetter();
            maxDatasetValue.checkFromGetter();
            currentLUT.checkFromGetter();
            currentChannel.checkFromGetter();
        }
    }

    @EventHandler
    public void onLutChangedEvent(LUTsChangedEvent event) {
        if (currentDatasetView.getValue() == event.getView()) {
            isComposite.checkFromGetter();
            currentLUT.checkFromGetter();
        }
    }

    @EventHandler
    public void onDatasetUpdatedEvent(DatasetUpdatedEvent event) {
        minDatasetValue.checkFromGetter();
        maxDatasetValue.checkFromGetter();
    }

    @EventHandler
    public void onAxisEvent(AxisPositionEvent event) {
        if (event.getDisplay() == currentImageDisplay.getValue()) {

            minLUTValue.checkFromGetter();
            maxLUTValue.checkFromGetter();
            currentLUT.checkFromGetter();

        }
    }

    private Integer getCurrentChannelPosition(DatasetView view) {
        Integer position = view.getIntPosition(Axes.CHANNEL);
        if (position == -1) {
            return 0;
        } else {
            return position;
        }
    }

    private Double getCurrentChannelMin(DatasetView view) {
        if (view == null) {
            return 0d;
        }

        double value = view.getChannelMin(getCurrentChannelPosition(view));

        return value;
    }

    private Double getCurrentChannelMax(DatasetView view) {
        if (view == null) {
            return 255d;
        }
        double value = view.getChannelMax(getCurrentChannelPosition(view));
        return value;
    }

    private Number getCurrentDatasetMax() {
        return imageDisplayFXService.getDatasetMaximum(currentDataset.getValue(), getCurrentChannel());
    }

    private Number getCurrentDatasetMin() {
        return imageDisplayFXService.getDatasetMinimum(currentDataset.getValue(), getCurrentChannel());
    }

    private void setCurrentChannelMin(DatasetView view, Number min) {
        if (view == null) {
            return;
        }
        view.setChannelRange(getCurrentChannelPosition(view), min.doubleValue(), getCurrentChannelMax(view));
        ImageJFX.getThreadPool().execute(new ViewUpdate(view));
    }

    private void setCurrentChannelMax(DatasetView view, Number max) {
        if (view == null) {
            return;
        }
        view.setChannelRange(getCurrentChannelPosition(view), getCurrentChannelMin(view), max.doubleValue());

        ImageJFX.getThreadPool().execute(new ViewUpdate(view));
    }

    public Number getCurrentChannel(DatasetView datasetView) {
        return datasetView.getChannelCount() == 1 ? 0 : datasetView.getIntPosition(Axes.CHANNEL);
    }
    
    public void setCurrentChannel(DatasetView view, Number channel) {
        view.setPosition(channel.intValue(), Axes.CHANNEL);
        ImageJFX.getThreadQueue().submit(new ViewUpdate(view));
    }

    public Property<long[]> currentPositionProperty() {
        return currentPosition;
    }

    private long[] lastPosition;

    private long[] getCurrentPosition(DatasetView view) {

        long[] position = new long[view.numDimensions()];
        view.localize(position);

        if (lastPosition == null || Arrays.equals(lastPosition, position) == false) {
            lastPosition = position;
        }

        return lastPosition;
    }

    public Integer getCurrentChannel() {
        return currentChannel.getValue().intValue();
    }

    private static final Boolean lock = Boolean.TRUE;

    private class ViewUpdate implements Runnable {

        final DatasetView view;

        public ViewUpdate(DatasetView view) {
            this.view = view;
        }

        public void run() {
            synchronized (lock) {
                view.getProjector().map();
                view.update();
            }
        }
    }

    public ObservableList<ColorTable> availableColorTableProperty() {
        if (colorTableList == null) {
            List<ColorTable> tables = lutService
                    .findLUTs()
                    .values()
                    .parallelStream()
                    .map(this::loadLUT)
                    .filter(lut -> lut != null)
                    .collect(Collectors.toList());

            colorTableList = FXCollections.observableArrayList();
            colorTableList.addAll(tables);
        }
        return colorTableList;

    }

    private boolean compare(ColorTable table1, ColorTable table2) {

        if (table1.getLength() != table2.getLength()) {
            return false;
        }

        for (int i = 0; i != table1.getLength(); i++) {
            for (int c = 0; c != 3; c++) {

                if (table1.get(c, i) != table2.get(c, i)) {
                    return false;
                }

            }
        }
        return true;

    }

    private ColorTable loadLUT(URL url) {
        try {
            return lutService.loadLUT(url);
        } catch (Exception e) {
            return null;
        }
    }
    
    public Number getChannelCount() {
       return currentDatasetView.getValue().getChannelCount();
    }
    
    public ReadOnlyProperty<Number> channelCountProperty() {
        return channelCountProperty;
    }

}
