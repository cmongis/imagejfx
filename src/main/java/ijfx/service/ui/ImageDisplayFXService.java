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
package ijfx.service.ui;

import ijfx.service.IjfxService;
import ijfx.ui.main.ImageJFX;
import java.util.Arrays;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import mongis.utils.UUIDMap;
import net.imagej.Dataset;
import net.imagej.axis.Axes;
import net.imagej.display.ColorMode;
import net.imagej.display.DatasetView;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.event.DataViewUpdatedEvent;
import net.imagej.display.event.LUTsChangedEvent;
import net.imagej.event.DatasetUpdatedEvent;
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

    private ControlableProperty<ImageDisplay, Dataset> currentDataset;

    private ControlableProperty<Dataset, Number> minDatasetValue;

    private ControlableProperty<Dataset, Number> maxDatasetValue;

    private ControlableProperty<DatasetView, Integer> currentChannel;

    private static final String MINIMUM = "minimum";

    private static final String MAXIMUM = "maximum";

    //private final Map<String, SummaryStatistics> datasetMinMax = new HashMap<>();
    private final UUIDMap<Double> datasetChannelMin = new UUIDMap();

    private Property<Double> possibleMinLUTValue = new SimpleObjectProperty<Double>();

    private Property<Double> possibleMaxLUTValue = new SimpleObjectProperty<Double>();

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

        currentChannel = new ControlableProperty<DatasetView, Integer>()
                .setCaller(this::getCurrentChannel)
                .bindBeanTo(currentDatasetView);

        minDatasetValue = new ControlableProperty<Dataset, Number>()
                .setCaller(this::getDatasetMinimumValue)
                .bindBeanTo(currentDataset);

        maxDatasetValue = new ControlableProperty<Dataset, Number>()
                .setCaller(this::getDatasetMaximumValue)
                .bindBeanTo(currentDataset);

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

            currentPosition.checkFromGetter();
            minDatasetValue.checkFromGetter();
            maxDatasetValue.checkFromGetter();
            maxLUTValue.checkFromGetter();
            minLUTValue.checkFromGetter();

        }
    }

    @EventHandler
    public void onLutChangedEvent(LUTsChangedEvent event) {
        if (currentDatasetView.getValue() == event.getView()) {
            isComposite.checkFromGetter();
        }
    }

    @EventHandler
    public void onDatasetUpdatedEvent(DatasetUpdatedEvent event) {
        minDatasetValue.checkFromGetter();
        maxDatasetValue.checkFromGetter();
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

    public void saveDatasetMinimum(Dataset dataset, int channel, double value) {
        datasetChannelMin.get(dataset, channel, MINIMUM).put(value);
        minDatasetValue.checkFromGetter();
    }

    public void saveDatasetMaximum(Dataset dataset, int channel, double value) {
        datasetChannelMin.get(dataset, channel, MAXIMUM).put(value);
        maxDatasetValue.checkFromGetter();
    }

    public Number getDatasetMinimum(Dataset dataset, int channel) {
        if (dataset == null) {
            return 0;
        }
        if (dataset.getType().getBitsPerPixel() <= 8) {
            return 0;
        }
        System.out.printf("DatasetMinMax : Minimum : %s,%d,%s\n", dataset.toString(), channel, datasetChannelMin.get(dataset, channel, MINIMUM).id().toString());
        return datasetChannelMin.get(dataset, channel, MINIMUM).orPut(dataset.getChannelMinimum(channel));
    }

    public Number getDatasetMaximum(Dataset dataset, int channel) {

        if (dataset.getType().getBitsPerPixel() == 1) {
            return 1;
        }
        if (dataset.getType().getBitsPerPixel() == 8) {
            return 255;
        }

        System.out.printf("DatasetMinMax : Maximum : %s,%d,%s\n", dataset.toString(), channel, datasetChannelMin.get(dataset, channel, MAXIMUM).id().toString());
        return datasetChannelMin.get(dataset, channel, MAXIMUM).orPut(dataset.getChannelMaximum(channel));
    }

    public Integer getCurrentChannel(DatasetView datasetView) {
        return datasetView.getChannelCount() == 1 ? 0 : datasetView.getIntPosition(Axes.CHANNEL);
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

    public Number getDatasetMinimumValue(Dataset dataset) {
        return getDatasetMinimum(dataset, getCurrentChannel());
    }

    public int getCurrentChannel() {
        return currentChannel.getValue();
    }

    public Number getDatasetMaximumValue(Dataset dataset) {
        return getDatasetMaximum(dataset, getCurrentChannel());
    }

    private class ViewUpdate implements Runnable {

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
