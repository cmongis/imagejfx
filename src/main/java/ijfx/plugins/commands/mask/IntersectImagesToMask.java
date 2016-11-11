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
package ijfx.plugins.commands.mask;

import ijfx.service.ui.CommandRunner;
import java.util.Arrays;
import net.imagej.Dataset;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.OverlayService;
import net.imagej.overlay.BinaryMaskOverlay;
import net.imglib2.img.Img;
import net.imglib2.roi.BinaryMaskRegionOfInterest;
import net.imglib2.type.logic.BitType;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = Command.class,menuPath="Image > Mask > Create from overlap with...")
public class IntersectImagesToMask extends ContextCommand {

    @Parameter(type = ItemIO.BOTH)
    ImageDisplay imageDisplay;

    @Parameter
    Dataset reference;

    @Parameter
    OverlayService overlayService;

    @Parameter
    ImageDisplayService imageDisplayService;

    @Override
    public void run() {

        Img<BitType> mask = new CommandRunner(getContext())
                .set("input1", imageDisplayService.getActiveDataset(imageDisplay))
                .set("input2", reference)
                .runSync(IntersectToBinary.class)
                .getOutput("output");

        BinaryMaskOverlay overlay = new BinaryMaskOverlay(getContext(), new BinaryMaskRegionOfInterest<>(mask));
        overlayService.addOverlays(imageDisplay, Arrays.asList(overlay));
    }

}
