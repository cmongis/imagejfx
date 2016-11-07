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
package ijfx.plugins.commands;

import ijfx.core.stats.IjfxStatisticService;
import net.imagej.Dataset;
import net.imagej.axis.Axes;
import net.imagej.display.DatasetView;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Cyril MONGIS, 2016
 */
@Plugin(type = Command.class, menuPath = "Image > Adjust > Auto-Contrast+")
public class AutoContrast extends ContextCommand {

    @Parameter
    ImageDisplayService imageDisplayService;

    @Parameter(required = false)
    ImageDisplay imageDisplay;

    @Parameter
    IjfxStatisticService statsService;

    @Parameter(label = "Channel dependant", description = "If yes, the algorithm use contrast settings different for each channel.")
    boolean channelDependant;

    @Parameter
    EventService eventService;

    @Parameter(type = ItemIO.BOTH)
    Dataset dataset;

    @Override
    public void run() {

        
        
        if(imageDisplay == null && imageDisplayService.getActiveDataset() == dataset) {
            imageDisplay = imageDisplayService.getActiveImageDisplay();
        }
        
        if(dataset != null && dataset.isRGBMerged()) {
            return;
        }
        boolean multiChannel = dataset.dimensionIndex(Axes.CHANNEL) != -1;

        if (multiChannel && channelDependant == true) {
            for (int i = 0; i <= dataset.max(dataset.dimensionIndex(Axes.CHANNEL)); i++) {
                
                autoChannelMinMax(imageDisplay, dataset, i);
                //SummaryStatistics stats = statsService.getChannelStatistics(dataset, i);
                //setMinMax(imageDisplay, dataset, stats, i);
            }
        } else {
           // SummaryStatistics stats = statsService.getSummaryStatistics(dataset);
            //setMinMax(imageDisplay, dataset, stats, 0);
            autoChannelMinMax(imageDisplay, dataset, 0);
        }
       
        
        if(dataset != null) {
            dataset.update();
        }
        
        if(imageDisplay != null) {
             DatasetView view = (DatasetView) imageDisplay.getActiveView();
              view.getProjector().map();
        }
    }

    
    private void autoChannelMinMax(ImageDisplay imageDisplay, Dataset dataset, int channel) {
        
        double[] minMax = statsService.getChannelMinMax(dataset, channel);
        
        if(dataset!= null) {
            dataset.setChannelMinimum(channel, minMax[0]);
            dataset.setChannelMaximum(channel, minMax[1]);
        }
        
        if(imageDisplay != null) {
            DatasetView view = (DatasetView) imageDisplay.getActiveView();
            view.setChannelRange(channel,minMax[0],minMax[1]);
        }
        
        
        
    }
    
    
    private static void setMinMax(ImageDisplay imageDisplay, Dataset dataset, SummaryStatistics stats, int channel) {
       
        double[] minMax;
        
        
        if (dataset != null) {
            dataset.setChannelMinimum(channel, stats.getMin());
             dataset.setChannelMaximum(channel, stats.getMax());
             dataset.update();
        }
       
           
 if (imageDisplay != null) {
            DatasetView view = (DatasetView) imageDisplay.getActiveView();
            view.setChannelRange(channel, stats.getMin(), stats.getMax());
            
            view.update();
            view.getProjector().map();
        }  
    }

}
