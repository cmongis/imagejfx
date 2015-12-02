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

import ijfx.algorithm.propagation.magicwand.NeightbourPlanner;
import java.util.Arrays;
import java.util.List;
import mercury.core.MercuryTimer;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class PropagationProcessor {
    
    
    PixelGenerator generator;
    PixelPlanner planner;
    PixelVisitor visitor;

    
    
    boolean visitNeighbour = false;
    
    public PixelGenerator getGenerator() {
        return generator;
    }

    public PropagationProcessor setGenerator(PixelGenerator generator) {
        this.generator = generator;
        return this;
    }

    public PixelPlanner getPlanner() {
        return planner;
    }

    public PropagationProcessor setPlanner(PixelPlanner planner) {
        this.planner = planner;
        return this;
    }

    public PixelVisitor getVisitor() {
        return visitor;
    }

    public PropagationProcessor setVisitor(PixelVisitor visitor) {
        this.visitor = visitor;
        return this;
    }
    
    public PropagationProcessor execute() {
        
         MercuryTimer timer = new MercuryTimer("Processor");
        Pixel[] nextWave = planner.getNextWave(generator);
        timer.elapsed("getting first wave");
       
        int visitedPixel = 0;
        
        // unless all the pixels of the wave are invalid or empty
        while(nextWave.length > 0) {
            
            visitedPixel += nextWave.length;
            
            if(visitNeighbour == false)
                Arrays.stream(nextWave, 0, nextWave.length)
                    
                    .forEach(pixel->visitor.visit(pixel, null));
            else
               
                      Arrays.stream(nextWave, 0, nextWave.length)
                              .filter(pixel->pixel!=null)
                              
                        .forEach(pixel->{
                            if(pixel != null)
                                visitor.visit(pixel, new NeightbourPlanner(pixel).getNextWave(generator));
                                    });
            
           // timer.elapsed("treating a wave");
            if(Arrays.stream(nextWave, 0, nextWave.length).filter(pixel->pixel != null && pixel.isValid()).count() == 0) break;
           // timer.elapsed("testing the last wave");
            
            nextWave = planner.getNextWave(generator);
            //timer.elapsed("Getting next wave");
        }
        
        timer.elapsed("All " + visitedPixel + " pixels visited in ");
        return this;
    }
    
    
    
}
