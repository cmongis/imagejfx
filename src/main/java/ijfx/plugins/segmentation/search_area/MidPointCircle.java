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
package ijfx.plugins.segmentation.search_area;

import ijfx.plugins.segmentation.search_area.AbstractSearchArea;
import javafx.geometry.Point2D;

/**
 * Implementation of the Midpoint circle algorithm
 * 
 * @author Pierre BONNEAU
 */
public class MidPointCircle extends AbstractSearchArea{
    
/**
 * Create a circle based on the Midpoint circle algorithm
 * @param cX X coordinate of the center
 * @param cY Y coordinate of the center
 * @param radius Radius of the circle
 */
    public MidPointCircle(double cX, double cY, int radius) {
        
        super(cX, cY, radius);
        
        double currentX = cX + radius;
        double currentY = cY;
        
        double dX = radius;
        double dY = 0.0;
        
        int err = 0;
        
        while(dX >= dY){
            
            points.add(new Point2D(cX+dX, cY+dY));
            points.add(new Point2D(cX+dX, cY-dY));
            
            points.add(new Point2D(cX-dX, cY+dY));
            points.add(new Point2D(cX-dX, cY-dY));

            points.add(new Point2D(cX-dY, cY+dX));
            points.add(new Point2D(cX-dY, cY-dX));

            points.add(new Point2D(cX+dY, cY+dX));
            points.add(new Point2D(cX+dY, cY-dX));
            
            err += 1 + 2*dY;
            if(2*(err-dX) + 1 > 0){
                currentX = currentX-1;
                dX = dX-1;
                err += 1 - 2*dX;                
            }

            currentY = currentY-1;
            dY = dY+1;
        }
    }
}
