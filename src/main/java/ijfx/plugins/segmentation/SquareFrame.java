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
package ijfx.plugins.segmentation;

import javafx.geometry.Point2D;

/**
 *
 * @author Pierre BONNEAU
 */
public class SquareFrame extends AbstractSearchArea{
    
    public SquareFrame(double cX, double cY, int radius){
        
        super(cX, cY, radius);
        
        double currentX = cX + radius;
        double currentY = cY;
        
        int dX = radius;
        int dY = 0;
        
        for(int i = 0; i <= radius; i++){
            points.add(new Point2D(cX+dX, cY+dY));
            points.add(new Point2D(cX+dX, cY-dY));
            points.add(new Point2D(cX-dX, cY+dY));
            points.add(new Point2D(cX-dX, cY-dY));
            points.add(new Point2D(cX+dY, cY+dX));
            points.add(new Point2D(cX+dY, cY-dX));
            points.add(new Point2D(cX-dY, cY+dX));
            points.add(new Point2D(cX-dY, cY-dX));
            dY++;
        }
    }
}
