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
import ijfx.ui.datadisplay.image.OverlayDrawer;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import net.imagej.overlay.RectangleOverlay;

/**
 *
 * @author cyril
 */
public class RectangleDrawer implements OverlayDrawer<RectangleOverlay>{

    
    Rectangle rectangle;
    
    @Override
    public Node update(RectangleOverlay overlay, ViewPort viewport) {
        
        if(rectangle == null) {
            rectangle = new Rectangle();
        }
        
          System.out.println("updating");
        double minX = overlay.getOrigin(0);
        double minY = overlay.getOrigin(1);
        double maxX = minX + overlay.getExtent(0);
        double maxY = minY + overlay.getExtent(1);
        RectangleOverlayHelper helper = new RectangleOverlayHelper(overlay);
        Point2D a = helper.getMinEdge();
        Point2D b = helper.getMaxEdge();
        a = viewport.getPositionOnCamera(a);
        b = viewport.getPositionOnCamera(b);
        rectangle.setFill(Color.RED);
        rectangle.setVisible(true);
        rectangle.setX(a.getX());
        rectangle.setY(a.getY());
        rectangle.setWidth(Math.abs(a.getX() - b.getX()));
        rectangle.setHeight(Math.abs(a.getY() - b.getY()));
        return rectangle;
        
        
    }

    @Override
    public boolean canDraw(RectangleOverlay t) {
        return t instanceof RectangleOverlay;
    }
    
}
