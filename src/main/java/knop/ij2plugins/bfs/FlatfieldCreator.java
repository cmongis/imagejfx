/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package knop.ij2plugins.bfs;


import ij.IJ;
import ij.ImagePlus;
import ij.process.FloatProcessor;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = Command.class, headless = true, menuPath = "Image > Correction > Average flatfield from stack")
public class FlatfieldCreator extends ImageJ2PluginAdapter{
     
    public static final String USE_MEAN = "Use mean";
    public static final  String USE_MEDIAN = "Use median";
    @Parameter(label = "Method",
    choices = { USE_MEAN,USE_MEDIAN })
    String method = USE_MEAN;
    
    
    public ImagePlus run(ImagePlus imp) {
        FloatProcessor fp = imp.getProcessor().duplicate().convertToFloatProcessor();
        double divider;
        if(method.equals(USE_MEDIAN)) {
            divider = 1.0/fp.getStatistics().median;
        }
        else {
            divider = 1.0/fp.getStatistics().mean;
        }
        //divider = 1.0/fp.getStatistics().mean;
        
        
        fp.multiply(divider);
        return new ImagePlus("",fp);
    }
    
 
}
