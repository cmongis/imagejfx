/*
 * /*
 *     This file is part of ImageJ FX.
 *
 *     ImageJ FX is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     ImageJ FX is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with ImageJ FX.  If not, see <http://www.gnu.org/licenses/>. 
 *
 * 	Copyright 2015,2016 Cyril MONGIS, Michael Knop
 *
 */
package ijfx.service.display;

import ijfx.core.stats.IjfxStatisticService;
import ijfx.service.sampler.PositionIterator;
import ijfx.service.sampler.SamplingDefinition;
import ijfx.service.sampler.SparsePositionIterator;
import ijfx.ui.main.ImageJFX;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javafx.util.Pair;
import net.imagej.Dataset;
import net.imagej.ImageJService;
import net.imagej.axis.Axes;
import net.imagej.axis.CalibratedAxis;
import net.imagej.display.DatasetView;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.measure.StatisticsService;
import net.imagej.sampler.AxisSubrange;
import net.imglib2.RandomAccess;
import net.imglib2.type.numeric.RealType;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.scijava.display.DisplayService;
import org.scijava.display.event.DisplayUpdatedEvent;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 * Gives information on display range of an image display
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = Service.class)
public class DisplayRangeService extends AbstractService implements ImageJService {

    @Parameter
    DisplayService displayService;

    @Parameter
    ImageDisplayService imageDisplayService;

    @Parameter
    EventService eventService;

    @Parameter
    StatisticsService statService;

    @Parameter
    IjfxStatisticService ijfxStatsService;

    Map<String, SummaryStatistics> datasetMinMax = new HashMap<>();

    private CalibratedAxis getChannelAxis() {

        for (CalibratedAxis a : getAxisList()) {
            if (a.type().getLabel().toLowerCase().contains("channel")) {
                return a;
            }
        }
        return null;

    }

    private ArrayList<CalibratedAxis> getAxisList() {
        return getAxisList(displayService.getActiveDisplay(ImageDisplay.class));

    }

    private ArrayList<CalibratedAxis> getAxisList(ImageDisplay display) {
        ArrayList<CalibratedAxis> axises = new ArrayList<>();

        for (int i = 0; i != display.numDimensions(); i++) {
            axises.add(display.axis(i));
        }

        return axises;

    }

    public double getCurrentDatasetMinimum() {

        /*
        return imageDisplayService
                .getActiveDataset(displayService
                        .getActiveDisplay(ImageDisplay.class))
                .getChannelMinimum(getCurrentChannelId());*/
        return getDisplayStatistics(displayService.getActiveDisplay(ImageDisplay.class), getCurrentChannelId()).getMin();
    }

    public double getCurrentDatasetMaximum() {
        //return imageDisplayService.getActiveDataset(displayService.getActiveDisplay(ImageDisplay.class)).getChannelMaximum(getCurrentChannelId());
        return getDisplayStatistics(displayService.getActiveDisplay(ImageDisplay.class), getCurrentChannelId()).getMax();
    }

    public double getChannelMinimum(ImageDisplay display, int channel) {
        return imageDisplayService.getActiveDatasetView(display).getChannelMin(channel);
    }
    
    public double getChannelMaximum(ImageDisplay display, int channel) {
        return imageDisplayService.getActiveDatasetView(display).getChannelMax(channel);
    }
    
    public double getCurrentViewMinimum() {
        return imageDisplayService.getActiveDatasetView().getChannelMin(getCurrentChannelId());
    }

    public double getCurrentViewMaximum() {
        return imageDisplayService.getActiveDatasetView().getChannelMax(getCurrentChannelId());
    }

    public double getCurrentPixelMinimumValue() {
        return statService.minimum(imageDisplayService.getActiveDataset(displayService.getActiveDisplay(ImageDisplay.class)));
        // return getDisplayStatistics(displayService.getActiveDisplay(ImageDisplay.class), getCurrentChannelId()).getMin();
    }

    public double getCurrentPixelMaximumValue() {

        // getDisplayStatistics(displayService.getActiveDisplay(ImageDisplay.class), getCurrentChannelId()).getMax();
        return statService.maximum(imageDisplayService.getActiveDataset(displayService.getActiveDisplay(ImageDisplay.class)));
    }

    public int getCurrentChannelId() {
        int channelId = imageDisplayService.getActiveDatasetView().getIntPosition(Axes.CHANNEL);
        return channelId;

    }

    public void updateDisplayRange(ImageDisplay imageDisplay,int channel,double min, double max) {
        
        Dataset dataset = imageDisplayService.getActiveDataset(imageDisplay);
        DatasetView datasetView = imageDisplayService.getActiveDatasetView(imageDisplay);
        dataset.setChannelMinimum(channel, min);
        dataset.setChannelMaximum(channel, max);
        datasetView.setChannelRange(channel, min, max);
        
        
        imageDisplayService.getActiveDatasetView().getProjector().map();
        eventService.publishLater(new DisplayUpdatedEvent(imageDisplay, DisplayUpdatedEvent.DisplayUpdateLevel.UPDATE));
    }
    
    
    public void updateCurrentDisplayRange(double min, double max) {
        imageDisplayService.getActiveDataset().setChannelMaximum(getCurrentChannelId(), max);
        imageDisplayService.getActiveDataset().setChannelMinimum(getCurrentChannelId(), min);
        imageDisplayService.getActiveDatasetView().setChannelRange(getCurrentChannelId(), min, max);
        //imageDisplayService.getActiveDatasetView().setChannelRanges(minValue.doubleValue(), maxValue.doubleValue());

        ImageJFX.getThreadPool().execute(() -> {
            imageDisplayService.getActiveDatasetView().getProjector().map();
            // imageDisplayService.getActiveDatasetView().update();
            eventService.publishLater(new DisplayUpdatedEvent(displayService.getActiveDisplay(), DisplayUpdatedEvent.DisplayUpdateLevel.UPDATE));
        });

    }

    public void autoRange() {
        updateCurrentDisplayRange(getCurrentPixelMinimumValue(), getCurrentPixelMaximumValue());

    }

    private SummaryStatistics getDisplayStatistics(ImageDisplay display, int channel) {

        String uuid = getDisplayChannelId(display, channel);
        Dataset dataset = imageDisplayService.getActiveDataset(display);
        if (datasetMinMax.containsKey(uuid) == false) {

            SummaryStatistics stats = ijfxStatsService.getChannelStatistics(dataset, channel);
            datasetMinMax.put(uuid, stats);
        }
        return datasetMinMax.get(uuid);

    }

    private String getDisplayChannelId(ImageDisplay display, int channel) {
        return UUID.nameUUIDFromBytes(new String(display.hashCode() + "" + channel).getBytes()).toString();
    }

}
