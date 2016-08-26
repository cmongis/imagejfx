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
 * @author cyril
 */
@Plugin(type = Command.class,menuPath="Image > Color > Spread channel setting")
public class SpreadChannelSettings extends ContextCommand{

    @Parameter(type=ItemIO.BOTH)
    ImageDisplay imageDisplay;
    
    @Parameter
    ImageDisplayService imageDisplayService;
    
    @Parameter
    DisplayRangeService displayRangeService;
    
    @Parameter(label="Spread also the LUT")
    boolean spreadLUT = false;
    
    @Parameter
    LUTService lutService;
    
    @Override
    public void run() {
        
        int numChannel = (int) AxisUtils.getChannelNumber(imageDisplayService.getActiveDataset(imageDisplay));
        if(displayRangeService == null) displayRangeService = getContext().getService(DisplayRangeService.class);
        for(int i = 0; i!= numChannel;i++) {
            
            final double min = displayRangeService.getChannelMinimum(imageDisplay, i);
            final double max = displayRangeService.getChannelMaximum(imageDisplay, i);
            final int channel = i;
           imageDisplayService
                   .getImageDisplays()
                   .stream()
                   .parallel()
                   .forEach(display->{
                       
                       
                       
                       if(channel+1 > AxisUtils.getChannelNumber(imageDisplayService.getActiveDataset(display))) return;
                       
                       if(spreadLUT) {
                           ColorTable table = imageDisplayService.getActiveDatasetView(imageDisplay).getColorTables().get(channel);
                            //imageDisplayService.getActiveDatasetView(display).setColorTable(table, channel);
                            //imageDisplayService.getActiveDataset(display).setColorTable(table, channel);
                             lutService.applyLUT(table, display);
                       }
                       
                       displayRangeService.updateDisplayRange(display, channel, min, max);
           });
        }
    }
}
