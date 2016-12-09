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

import ijfx.service.display.DisplayRangeService;
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
@Plugin(type = Command.class,menuPath = "Image > Color > Spread current channel settings")
public class SpreadCurrentChannelSettings extends ContextCommand{

    @Parameter(type=ItemIO.BOTH)
    ImageDisplay imageDisplay;
    
    @Parameter
    ImageDisplayService imageDisplayService;
    
    @Parameter
    DisplayRangeService displayRangeService;
    
    @Parameter(label = "Spread also LUT")
    boolean spreadLUT = false;
    
    @Parameter
    LUTService lutService;
    
    @Override
    public void run() {
        
        int currentChannel = getCurrentChannel(imageDisplay);
        
        final double min = displayRangeService.getChannelMinimum(imageDisplay, currentChannel);
        final double max = displayRangeService.getChannelMaximum(imageDisplay, currentChannel);
        
        imageDisplayService
                   .getImageDisplays()
                   .stream()
                   .parallel()
                   .forEach(display->{
                       
                       if(display == imageDisplay) return;
                       int channel = getCurrentChannel(display);

                       if(spreadLUT) {
                           ColorTable table = imageDisplayService.getActiveDatasetView(imageDisplay).getColorTables().get(channel);
                            //imageDisplayService.getActiveDatasetView(display).setColorTable(table, channel);
                            //imageDisplayService.getActiveDataset(display).setColorTable(table, channel);
                             lutService.applyLUT(table, display);
                       }
                      
                      imageDisplayService.getActiveDatasetView(display).setChannelRange(channel, min, max);
                      imageDisplayService.getActiveDatasetView(display).update();
           });
        
    
    }
    
    private int getCurrentChannel(ImageDisplay display) {
        // getting the current channel
        int currentChannel = imageDisplay.getIntPosition(Axes.CHANNEL);
        
        // making the sure the current channel is always greater than 0
        currentChannel = currentChannel == -1 ? 0 : currentChannel;
        return currentChannel;
    }
}
