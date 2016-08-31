/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package knop.ij2plugins.bfs;

import knop.ij2plugins.bfs.BrightFieldSegmentation;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.imagej.Dataset;
import net.imagej.ImageJ;
import org.scijava.Context;
import org.scijava.command.CommandModule;


public class BrightfieldSegmentationImageJ2 {

    /**
     * @param args the command line arguments
     */
    
    
    
    public static void main(String[] args) throws ExecutionException, InterruptedException {
       
            Context c = new Context();
            //ImageJ ij = new ImageJ(c);
            ImageJ ij = new ImageJ(c);
            String inputPath = "/Users/cyril/test_img/illia/3_processed/ID=19__Channel=2__Time=4__flatfield=true.tif";//"/Users/cyril/test_img/flo/renamed/Well=A01__Plane Index=0__ZStack=0002__Pos=0003__Seq=0011.tif";
            
            
            
            Dataset dataset = null;
        try {
            dataset = ij.dataset().open(inputPath);
        } catch (IOException ex) {
            Logger.getLogger(BrightfieldSegmentationImageJ2.class.getName()).log(Level.SEVERE, null, ex);
        }
        Future<CommandModule> run = ij.command().run(BrightFieldSegmentation.class, true, "dataset",dataset,"maxSizeMSER",8000,"lambda",7,"tileDim",50);
        
           ij.module().waitFor(run);
           dataset  = (Dataset)run.get().getOutput("dataset");
            
        try {
            ij.io().save(dataset, "./test.tif");
        } catch (IOException ex) {
            Logger.getLogger(BrightfieldSegmentationImageJ2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    
    
}
