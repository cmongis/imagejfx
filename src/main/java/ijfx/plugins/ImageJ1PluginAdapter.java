/*
 * /*
 *     This file is part of ImageJ FX.
 *
 *     ImageJ FX is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     ImageJ FX is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with ImageJ FX.  If not, see <http://www.gnu.org/licenses/>. 
 *
 * 	Copyright 2015,2016 Cyril MONGIS, Michael Knop
 *
 */
package ijfx.plugins;


import ij.ImagePlus;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.axis.Axes;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;

import net.imglib2.type.numeric.integer.UnsignedShortType;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;


/**
 *
 * @author Cyril MONGIS, 2015
 */
public abstract class ImageJ1PluginAdapter implements Command{
    
    
    @Parameter (type = ItemIO.BOTH)
    protected Dataset dataset;
    
    @Parameter
    DatasetService service;
    
    @Parameter
    ImageDisplayService imageDisplayService;
    
    
    public ImagePlus getInput() {
        return unwrapDataset(dataset);
    }
    
    public void setOutput(ImagePlus imp) {
        dataset = wrapDataset(imp);
    }
    
    public static ImagePlus unwrapDataset(Dataset dataset) {
        RandomAccessibleInterval<UnsignedShortType> r = (RandomAccessibleInterval<UnsignedShortType>) dataset.getImgPlus();
        
        ImagePlus wrapImage = ImageJFunctions.wrap(r, "");
        return wrapImage;
    }
    
    public Dataset wrapDataset(ImagePlus imp) {
         Img img = ImageJFunctions.wrap(imp);
        return service.create(img);
    }
    
    public static void configureImagePlus(ImagePlus imp, ImageDisplay imageDisplay) {
        
         imp.setC(imageDisplay.getIntPosition(Axes.CHANNEL));
         imp.setZ(imageDisplay.getIntPosition(Axes.Z));
         imp.setT(imageDisplay.getIntPosition(Axes.TIME));
        
    }
    
    
    public abstract ImagePlus run(ImagePlus input);
    
    @Override
    public void run() {
        ImagePlus result = run(getInput());
        setOutput(result);
        //setOutput(result);
    }
    
   
    
    
}
