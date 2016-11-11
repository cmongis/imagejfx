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

import ijfx.core.utils.DimensionUtils;
import ijfx.service.overlay.OverlayUtilsService;
import java.util.Arrays;
import net.imagej.Dataset;
import net.imagej.ImgPlus;
import net.imagej.ImgPlusService;
import net.imagej.display.ImageDisplay;
import net.imagej.overlay.BinaryMaskOverlay;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import org.scijava.ItemIO;
import org.scijava.command.ContextCommand;
import org.scijava.plugin.Parameter;

/**
 *
 * @author cyril
 */
public class IntersectToBinary extends ContextCommand{
     
    @Parameter(type = ItemIO.INPUT)
    Dataset input1;
    
    @Parameter(label="Compare to")
    Dataset input2;
    
    @Parameter(type = ItemIO.OUTPUT)
    Img<BitType> output;
    
    @Parameter
    OverlayUtilsService overlayUtilsService;
    
    @Parameter
    ImgPlusService imgPlusService;
    
    

    
    @Override
    public void run() {
        
       
        
        long[] dim1 = DimensionUtils.getDimension(input1);
        long[] dim2 = DimensionUtils.getDimension(input2);
        
        if(Arrays.equals(dim1, dim2) == false) {
            cancel("Both images must have the size dimensions");
            return;
        }
        
        output = getBinaryOverlap((RandomAccessibleInterval)input1, (RandomAccessibleInterval)input2);
        
        
        
        
        
        
    }
    
    
    public static <T extends RealType<T>, R extends RealType<R>> Img<BitType> getBinaryOverlap(RandomAccessibleInterval<T> source, RandomAccessibleInterval<R> reference) {
        
        long[] dimension = DimensionUtils.getDimension(source);
        
        Img<BitType> mask = new ArrayImgFactory<BitType>().create(dimension, new BitType());
        
        Cursor<T> cursor1 = Views.iterable(source).cursor();
        Cursor<R> cursor2 = Views.iterable(reference).cursor();
        Cursor<BitType> maskCursor = mask.cursor();
        
        cursor1.reset();
        cursor2.reset();
        maskCursor.reset();
        
        while(cursor1.hasNext() && cursor2.hasNext()) {
            
            cursor1.fwd();
            cursor2.fwd();
            maskCursor.fwd();
            boolean value = cursor1.get().getRealDouble() > 0 && cursor2.get().getRealDouble() > 0;
            
            maskCursor.get().set(value);
            
            
        }
        
        return mask;
        
        
    }
    
    
    
}
