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
package ijfx.ui.datadisplay.image.overlay;

import ijfx.ui.canvas.utils.ViewPort;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.shape.Rectangle;
import net.imagej.overlay.Overlay;
import net.imagej.overlay.RectangleOverlay;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = OverlayDrawer.class)
public class RectangleDrawer implements OverlayDrawer<RectangleOverlay>{
    

    
    RectangleOverlayHelper helper;
    public  void update(RectangleOverlay overlay, ViewPort viewport,Canvas canvas) {
        
        
        /**
        if(rectangle == null) {
            rectangle = new Rectangle();
        }
        
         
      
      * if
        
        Point2D a = helper.getMinEdge();
        Point2D b = helper.getMaxEdge();
        a = viewport.getPositionOnCamera(a);
        b = viewport.getPositionOnCamera(b);
      
        rectangle.setVisible(true);
        rectangle.setX(a.getX());
        rectangle.setY(a.getY());
        
        OverlayDrawer.color(overlay, rectangle);
        rectangle.setWidth(Math.abs(a.getX() - b.getX()));
        rectangle.setHeight(Math.abs(a.getY() - b.getY()));
        return rectangle;
        
        **/
        
        
       helper  = new RectangleOverlayHelper(overlay);
       Point2D a = helper.getMinEdge();
        Point2D b = helper.getMaxEdge();
        a = viewport.getPositionOnCamera(a);
        b = viewport.getPositionOnCamera(b);
        
        
        
       canvas.getGraphicsContext2D().strokeRect(a.getX(), a.getX(), b.getX()-a.getX(), b.getY()-a.getY())
               ;
        
        
    }

    public boolean canHandle(Class<?> t) {
        return t ==  RectangleOverlay.class;
    }

   
    
}
