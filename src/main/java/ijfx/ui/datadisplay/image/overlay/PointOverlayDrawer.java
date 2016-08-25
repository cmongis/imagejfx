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
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import net.imagej.overlay.Overlay;
import net.imagej.overlay.PointOverlay;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type=OverlayDrawer.class)
public class PointOverlayDrawer implements OverlayDrawer<PointOverlay>{

    Group pointNode;
    
    double radius = 5.0;
    
    Property<Point2D> position = new SimpleObjectProperty<Point2D>();
    
    DoubleProperty strokeWidth = new SimpleDoubleProperty(1);
    Property<Paint> strokePaint = new SimpleObjectProperty<>(Color.YELLOW);
    
    Rectangle r;
    
    ViewPort viewport;
    
    public void createNode() {
        
        Polyline polyLine = new Polyline();
        
        Group group = new Group();
        Line vertical = new Line(radius/2,0,radius/2,radius);
        Line horizontal = new Line(0,radius/2,radius,radius/2);
        
        vertical.strokeWidthProperty().bind(strokeWidth);
        horizontal.strokeWidthProperty().bind(strokeWidth);
        
        
        
        vertical.strokeProperty().bind(strokePaint);
        horizontal.strokeProperty().bind(strokePaint);
        
        group.getChildren().addAll(vertical,horizontal);
        
        r = new Rectangle(3, 3);
    }
    
    @Override
    public Node update(PointOverlay overlay, ViewPort viewport) {
        
        
        if(r == null) {
            createNode();
        }
        
        
        
        
        
        Point2D onScreen = viewport.getPositionOnCamera(PointOverlayHelper.getOverlayPosition(overlay));
    
        double x = onScreen.getX();
        double y = onScreen.getY();
        r.setVisible(true);
        r.setX(x-1);
        r.setY(y-1);
        
        r.setWidth(3);
        r.setHeight(3);
        r.setFill(Color.RED);
        r.setStrokeWidth(2.0);
        
       
        
        OverlayDrawer.color(overlay, r);
         r.setVisible(viewport.getSeenRectangle().contains(PointOverlayHelper.getOverlayPosition(overlay)));
        return r;
        
        
    }

    @Override
    public boolean canHandle(Overlay t) {
        return t instanceof PointOverlay;
    }
    
    @Override
    public boolean isOnOverlay(Overlay overlay, ViewPort viewport, double xOnImage, double yOnImage) {
        
        if(overlay instanceof PointOverlay) {
            
            PointOverlay pointOverlay = (PointOverlay)overlay;
            
            double[] point = pointOverlay.getPoint(0);
            
            double dx = Math.abs(xOnImage-point[0]);
            double dy = Math.abs(yOnImage-point[1]);
            
            return dx * dy < 10*10;
        }
        else return false;
    }
    
   
   
}
