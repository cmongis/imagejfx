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

import java.util.ArrayList;
import net.imagej.ImageJService;
import net.imagej.axis.CalibratedAxis;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.measure.StatisticsService;
import org.scijava.display.DisplayService;
import org.scijava.display.event.DisplayUpdatedEvent;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 * Gives information on display range of an image display
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = Service.class)
public class DisplayRangeService extends AbstractService implements ImageJService{

    @Parameter
    DisplayService displayService;

    @Parameter
    ImageDisplayService imageDisplayService;

    @Parameter
    EventService eventService;

    @Parameter
    StatisticsService statService;
    
    
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

        return imageDisplayService
                .getActiveDataset(displayService
                        .getActiveDisplay(ImageDisplay.class))
                .getChannelMinimum(getCurrentChannelId());
    }

    public double getCurrentDatasetMaximum() {
        return imageDisplayService.getActiveDataset(displayService.getActiveDisplay(ImageDisplay.class)).getChannelMaximum(getCurrentChannelId());
    }

    public double getCurrentViewMinimum() {
        return imageDisplayService.getActiveDatasetView().getChannelMin(getCurrentChannelId());
    }

    public double getCurrentViewMaximum() {
        return imageDisplayService.getActiveDatasetView().getChannelMax(getCurrentChannelId());
    }
    
    public double getCurrentPixelMinimumValue() {
        return statService.minimum(imageDisplayService.getActiveDataset(displayService.getActiveDisplay(ImageDisplay.class)));
    }
    
    public double getCurrentPixelMaximumValue() {
        return statService.maximum(imageDisplayService.getActiveDataset(displayService.getActiveDisplay(ImageDisplay.class)));
    }

    public int getCurrentChannelId() {
        if (getChannelAxis() == null) {
            return 0;
        }
        return imageDisplayService.getActiveDatasetView().getIntPosition(getChannelAxis().type());

    }

    
   
    
    public void updateCurrentDisplayRange(double min, double max) {
        imageDisplayService.getActiveDatasetView().setChannelRange(getCurrentChannelId(), min, max);
        //imageDisplayService.getActiveDatasetView().setChannelRanges(minValue.doubleValue(), maxValue.doubleValue());
        imageDisplayService.getActiveDatasetView().getProjector().map();
       // imageDisplayService.getActiveDatasetView().update();

        eventService.publishLater(new DisplayUpdatedEvent(displayService.getActiveDisplay(), DisplayUpdatedEvent.DisplayUpdateLevel.UPDATE));
    }

    public void autoRange() {
        updateCurrentDisplayRange(getCurrentPixelMinimumValue(), getCurrentPixelMaximumValue());
        
    }
    
}
