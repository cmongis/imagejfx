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

import com.google.common.collect.Lists;
import ijfx.service.overlay.OverlayUtilsService;
import ijfx.service.uicontext.UiContextService;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.overlay.BinaryMaskOverlay;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.BinaryMaskRegionOfInterest;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = Command.class, menuPath = "Image > Mask > Apply")
public class ApplyMask extends ContextCommand {

    @Parameter(label = "Background value")
    double value = 0d;

    @Parameter(type = ItemIO.BOTH)
    ImageDisplay imageDisplay;

    @Parameter(label = "Apply to whole dataset")
    boolean wholeDataset = false;

    @Parameter
    OverlayUtilsService overlayUtilsSerivce;

    @Parameter
    ImageDisplayService imageDisplayService;
    
    @Parameter
    UiContextService uiContextService;
    
    @Override
    public void run() {

        BinaryMaskOverlay overlay = overlayUtilsSerivce.findOverlayOfType(imageDisplay, BinaryMaskOverlay.class);

        if (overlay == null) {
            cancel("This image has no binary mask");
            return;
        }

        RandomAccessibleInterval<BitType> mask = ((BinaryMaskRegionOfInterest<BitType, ?>) overlay.getRegionOfInterest()).getImg();

        applyBinary((RandomAccessibleInterval)imageDisplayService.getActiveDataset(imageDisplay),mask);
        
         uiContextService.enter("visualize");
         overlayUtilsSerivce.removeOverlay(imageDisplay, Lists.newArrayList(overlay));
         imageDisplay.update();
        
    }

    public <T extends RealType<T>> void applyBinary(RandomAccessibleInterval<T> interval, RandomAccessibleInterval<BitType> mask) {

        long[] imgPosition = new long[interval.numDimensions()];

        long[] maskPosition = new long[mask.numDimensions()];
        
        
        Cursor<T> imgCursor = Views.iterable(interval).cursor();
        
        RandomAccess<BitType> maskRa = mask.randomAccess();
        
        imgCursor.reset();
       
        while(imgCursor.hasNext()) {
            imgCursor.fwd();
            imgCursor.localize(imgPosition);
            
            System.arraycopy(imgPosition, 0, maskPosition, 0, maskPosition.length);
            
            mask.randomAccess().setPosition(maskPosition);
            
            if(!maskRa.get().get()) {
                imgCursor.get().setReal(value);
            }
            
        }
        
       
        
    }

}
