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

import ijfx.plugins.DefaultAxisInterval;
import ijfx.plugins.DefaultInterval;
import ijfx.plugins.LongInterval;
import ijfx.service.display.DisplayRangeService;
import net.imagej.Dataset;
import net.imagej.axis.Axes;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.lut.LUTService;
import net.imglib2.display.ColorTable;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Cyril MONGIS, 2016
 */
@Plugin(menuPath = "Image > Color > Change LUT...",type = Command.class,initializer = "init")
public class ApplyLUT extends ContextCommand{
  
    
    @Parameter(type=ItemIO.BOTH)
    Dataset input;
    
    @Parameter(label = "Channel to apply the LUT")
    Integer channelId = 0;
    
    @Parameter(label = "Choose an LUT")
    ColorTable colorTable;
    
    @Parameter
    ImageDisplayService imageDisplayService;
    
    @Parameter
    LUTService lutService;
    
    @Parameter(label="LUT min/max",required=false)
    LongInterval displayRange;
    
    @Parameter
    DisplayRangeService displayRangeService;
    void init() {
        
        if(imageDisplayService.getActiveDataset() == input) {
            channelId = imageDisplayService.getActiveImageDisplay().getIntPosition(Axes.CHANNEL);
            if(channelId == -1) channelId = 0;
            else {
                ImageDisplay display = imageDisplayService.getActiveImageDisplay();
                long min = Math.round(displayRangeService.getCurrentDatasetMinimum());
                long max = Math.round(displayRangeService.getCurrentDatasetMaximum());
                long low = Math.round(displayRangeService.getCurrentViewMinimum());
                long high = Math.round(displayRangeService.getCurrentViewMaximum());
                displayRange = new DefaultInterval(low,high,min,max);
            }
        }
    }
    @Override
    public void run() {
        input.setColorTable(colorTable, channelId);
        ImageDisplay activeImageDisplay = imageDisplayService.getActiveImageDisplay();
        if(imageDisplayService.getActiveDataset() == input) {
            lutService.applyLUT(colorTable, imageDisplayService.getActiveImageDisplay());
        }
        if(displayRange != null) {
            
            input.setChannelMinimum(channelId, displayRange.getLowValue());
            input.setChannelMaximum(channelId,displayRange.getHighValue());
        }
       
       
    } 

    
}
