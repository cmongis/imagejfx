package knop.ij2plugins.bfs;


import ij.ImagePlus;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Cyril MONGIS, 2016
 */
public abstract class ImageJ2PluginAdapter implements Command{
    
    
    @Parameter (type = ItemIO.BOTH)
    protected Dataset dataset;
    
    @Parameter
    DatasetService service;

    
    public ImagePlus getInput() {
        return unwrapDataset(dataset);
    }
    
    public void setOutput(ImagePlus imp) {
        dataset = wrapDataset(imp);
    }
    
    public ImagePlus unwrapDataset(Dataset dataset) {
        RandomAccessibleInterval<UnsignedShortType> r = (RandomAccessibleInterval<UnsignedShortType>) dataset.getImgPlus();
        ImagePlus wrapImage = ImageJFunctions.wrap(r, "");
        return wrapImage;
    }
    
    public Dataset wrapDataset(ImagePlus imp) {
         Img img = ImageJFunctions.wrap(imp);
        return service.create(img);
    }
    
    
    public abstract ImagePlus run(ImagePlus input);
    
    @Override
    public void run() {
        ImagePlus result = run(getInput());
        setOutput(result);
        //setOutput(result);
    }
    
    
}
