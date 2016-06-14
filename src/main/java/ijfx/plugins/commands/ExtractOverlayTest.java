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

import ijfx.service.ImagePlaneService;
import ijfx.service.overlay.OverlayDrawingService;
import ijfx.service.overlay.OverlaySelectionService;
import net.imagej.Dataset;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.overlay.Overlay;
import net.imglib2.type.numeric.RealType;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = Command.class,menuPath = "Plugins > Text Overlay Drawing")
public class ExtractOverlayTest extends ContextCommand {

    @Parameter
    OverlayDrawingService overlayService;
    
    @Parameter
    OverlaySelectionService overlaySelectionService;
    
    @Parameter(type = ItemIO.INPUT)
    ImageDisplay imageDisplay;
    
    @Parameter(type = ItemIO.OUTPUT)
    Dataset output;
    
    @Parameter
    ImageDisplayService imageDisplayService;
    
    @Parameter
    ImagePlaneService imagePlaneService;
    
    @Override
    public void run() {
        
        Dataset dataset = imageDisplayService.getActiveDataset(imageDisplay);
        
        Overlay selected = overlaySelectionService.getSelectedOverlays(imageDisplay).get(0);
        selected.setLineWidth(3.0);
        long[] position = new long[dataset.numDimensions()];
        imageDisplay.localize(position);
        
        output = imagePlaneService.createEmptyPlaneDataset(dataset);
        
        overlayService.drawOverlay(selected, OverlayDrawingService.OUTLINER, output, 65000);
        
    }
    
}
