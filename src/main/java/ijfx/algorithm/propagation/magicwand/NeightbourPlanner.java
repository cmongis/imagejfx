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
package ijfx.algorithm.propagation.magicwand;

import ijfx.algorithm.propagation.Pixel;
import ijfx.algorithm.propagation.PixelGenerator;
import ijfx.algorithm.propagation.PixelList;
import ijfx.algorithm.propagation.PixelPlanner;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class NeightbourPlanner implements PixelPlanner{

    Pixel pixel;

   
    int radius = 1;
    
    
    
    public NeightbourPlanner(Pixel pixel) {
        this.pixel = pixel;
    }
    
    
    @Override
    public Pixel[] getNextWave(PixelGenerator generator) {
        
        Pixel[] pixelList = new Pixel[8+(radius-1)*8];
        Pixel p;
        //PixelList pixelList = new PixelList(8 + ( radius * 4 ));
        int i = 0;
        for( int x = pixel.getX() - radius; x <= pixel.getX() + radius; x++) {
            
           
            
            pixelList[i++] = (generator.getPixel(x, pixel.getY()-radius));
            
            pixelList[i++] = (generator.getPixel(x, pixel.getY()+radius));
        }
        
        
        for (int y = pixel.getY() - radius + 1; y <= pixel.getY() + radius -1; y++) {
            pixelList[i++] = (generator.getPixel(pixel.getX()-radius,y));
            pixelList[i++] = generator.getPixel(pixel.getY()+radius,y);
        }
        
        
        radius++;
        
        return pixelList;
        
    }

   
    
    
    
    
}
