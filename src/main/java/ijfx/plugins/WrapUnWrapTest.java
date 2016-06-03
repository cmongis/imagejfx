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
package ijfx.plugins;

import ij.ImagePlus;
import java.awt.Color;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.ImgPlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.img.display.imagej.ImageJVirtualStack;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type=Command.class,menuPath = "Plugins > Test > Wrapping",label="Wrapping")
public class WrapUnWrapTest implements Command{

    @Parameter(type=ItemIO.INPUT)
    Dataset input;
    
    @Parameter(type= ItemIO.OUTPUT)
    Dataset output;
    
    @Parameter
    DatasetService service;
    
    @Override
    public void run() {
        
       ImagePlus imp = datasetToImagePlus(input);
        
       imp.setSlice(0);
       imp.getProcessor().drawString("Hello",0, 0, Color.WHITE);
        
       output = wrapDataset(imp);

        datasetToImagePlus(output).show();
       
        System.out.println(output);
        
    }
    
    public <T extends NumericType<T>> ImagePlus datasetToImagePlus(Dataset dataset) {
        RandomAccessibleInterval<T> imgPlus =  (RandomAccessibleInterval <T>)dataset.getImgPlus();
        
        ImagePlus wrapImage = ImageJFunctions.wrap(imgPlus, "");
        
        wrapImage.show();
        return wrapImage;
    }
    
     public <T extends RealType<T> & NativeType<T>>  Dataset wrapDataset(ImagePlus imp) {
        
         imp.updatePosition(1, 1, 1);
          imp.updateImage();
         imp.updateAndDraw();
         //imp.resetStack();
         imp.setActivated();
         //imp.setSlice(0);
         ImagePlus duplicate = imp.duplicate();
         ImageJVirtualStack stack = (ImageJVirtualStack) imp.getImageStack();
         
         Img<T> img = ImageJFunctions.wrap(imp);
        
        return service.create(new ImgPlus<T>(img));
    }
    
}
