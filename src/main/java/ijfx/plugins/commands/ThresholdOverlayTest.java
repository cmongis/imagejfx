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

import com.google.common.collect.Lists;
import net.imagej.Dataset;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.OverlayService;
import net.imagej.overlay.ThresholdOverlay;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Cyril MONGIS, 2016
 */
@Plugin(type = Command.class, menuPath = "Plugins > Test > Threshold Overlay")
public class ThresholdOverlayTest extends ContextCommand{

    @Parameter
            ImageDisplay imageDisplay;
    
    @Parameter
            ImageDisplayService imageDisplayService;
    
    @Parameter
            OverlayService overlayService;
    
    
    Dataset dataset;
    
    @Parameter(label = "min")
    double min = 1000;
    
    @Parameter(label = "max")
    double max = 3000;
    
    
    
    @Override
    public void run() {
        
        
        dataset = imageDisplayService.getActiveDataset(imageDisplay);
        ThresholdOverlay overlay = new ThresholdOverlay(getContext(), dataset, min, max);
        overlayService.addOverlays(imageDisplay, Lists.newArrayList(overlay));
        
        
    }
    
    
}
