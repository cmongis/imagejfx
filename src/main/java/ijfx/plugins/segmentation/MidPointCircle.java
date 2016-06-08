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

import java.util.ArrayList;
import javafx.geometry.Point2D;
import net.imagej.Dataset;
import net.imglib2.RandomAccess;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;

/**
 * Implementation of the Midpoint circle algorithm
 * 
 * @author Pierre BONNEAU
 */
public class MidPointCircle {
    
    private ArrayList<Point2D> circle;
    
/**
 * Create a circle based on the Midpoint circle algorithm
 * @param cX X coordinate of the center
 * @param cY Y coordinate of the center
 * @param radius Radius of the circle
 */
    public MidPointCircle(double cX, double cY, int radius) {
        
        circle = new ArrayList<>();
        
        double currentX = cX + radius;
        double currentY = cY;
        
        double dX = radius;
        double dY = 0.0;
        
        while(dX >= dY){
            
            circle.add(new Point2D(cX+dX, cY+dY));
            circle.add(new Point2D(cX+dX, cY-dY));
            
            circle.add(new Point2D(cX-dX, cY+dY));
            circle.add(new Point2D(cX-dX, cY-dY));

            circle.add(new Point2D(cX-dY, cY+dX));
            circle.add(new Point2D(cX-dY, cY-dX));

            circle.add(new Point2D(cX+dY, cY+dX));
            circle.add(new Point2D(cX+dY, cY-dX));
            
            if(radiusError(currentX-1, currentY-1, radius) < radiusError(currentX, currentY-1, radius)){
                currentX = currentX-1;
                dX = dX-1;
            }
            currentY = currentY-1;
            dY = dY-1;
        }
        
    }
    
    public double radiusError(double x, double y, int radius){
        return Math.abs(x*x + y*y - radius*radius);
    }
    
    public void drawCircle(Dataset ds){
        for(int i = 0; i< circle.size(); i++){
            RandomAccess<RealType<?>> ra = ds.randomAccess();
            ra.setPosition((int)circle.get(0).getX(), (int)circle.get(i).getY());
            ra.get().setReal(123.0);
        }

    }    
}
