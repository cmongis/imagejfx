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
package ijfx.algorithm.propagation;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ijfx.algorithm.propagation.magicwand.NeightbourPlanner;
import ijfx.algorithm.propagation.magicwand.ThresholdVisitor;
import mercury.core.MercuryTimer;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class PixelTest {
    public static void main(String... args) {
        
        ImagePlus imp = IJ.openImage("/Users/cyril/test_img/256-bit-image.tif");
        
        ImagePlusPixelGenerator generator = new ImagePlusPixelGenerator(imp);
        
        Pixel<Double> origin = generator.getPixel(162, 336);
        
        PixelVisitor visitor= new ThresholdVisitor(origin.getValue(),0.1);
        
        ImagePlus mask = new ImagePlus("mask", new ByteProcessor(imp.getWidth(),imp.getHeight()));
        
        PixelPlanner planner = new NeightbourPlanner(origin);
        
        MercuryTimer t = new MercuryTimer("process");
        
        PropagationProcessor propagationProcessor = new PropagationProcessor()
                .setGenerator(generator)
                .setPlanner(planner)
                .setVisitor((pixel,neighbour)-> {
                    MercuryTimer timer2 = new MercuryTimer("Visitor");
                    visitor.visit(pixel, neighbour);
                    /*
                    if(pixel.isValid()) {
                          mask.getProcessor().set(pixel.getX(), pixel.getY(), 255);
                    }*/
                    
                });
        t.elapsed("Setting the generator");
        propagationProcessor
                .execute();
         t.elapsed("Propagation process");
        //propagationProcessor.setPlanner(new PerLinePlanner());
        
        //propagationProcessor.execute();
       // t.elapsed("PerLine processor");
        t.start();
        byte[] pixels = (byte[])imp.getProcessor().getPixels();
        
        for(int i = 0; i != pixels.length; i++ ) {
            pixels[i] = pixels[i];
        }
        t.elapsed("Classic processing");
       
       ImageProcessor ip = imp.getProcessor();
       
       
        
        
        
        mask.show();
        
    }
    
    
    private static class ImagePlusPixelGenerator implements PixelGenerator {

        ImagePlus imp;
      
        public ImagePlusPixelGenerator(ImagePlus imp) {
            this.imp = imp;
        }
        
        
        
        @Override
        public Pixel getPixel(int x, int y) {
            
            if(x < 0 || x >= imp.getWidth()) return null;
            if(y < 0 || y >= imp.getHeight()) return null;
            return new GenericPixel<Double>(x,y,10.0);
            //return new GenericPixel<Double>(x, y, new Double(imp.getProcessor().get(x, y)));
        }

       
    
        
        
    
    }
    
    
    
    

}
    
    
