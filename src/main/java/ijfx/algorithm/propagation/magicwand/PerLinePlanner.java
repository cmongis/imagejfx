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

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class PerLinePlanner implements PixelPlanner{

    private int x = 0;
  
    
    @Override
    public Pixel[] getNextWave(PixelGenerator pixelGenerator) {
        
        PixelList pixelList = new PixelList(2000);
        int y = 0;
        
        Pixel pixel = pixelGenerator.getPixel(x,y);
        
        while(pixelList.add(pixelGenerator.getPixel(x, y++)));
        x++;
        
        return null;
    }
    
}
