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

import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;

import net.imagej.overlay.RectangleOverlay;

/**
 *
 * @author cyril
 */
public class RectangleOverlayHelper {
    
    final RectangleOverlay overlay;
    Property<Point2D> minEdgeProperty = new SimpleObjectProperty();
    Property<Point2D> maxEdgeProperty = new SimpleObjectProperty();

    
    public RectangleOverlayHelper(RectangleOverlay overlay) {
        this.overlay = overlay;
        
        
        minEdgeProperty.addListener(this::onMinEdgeChanged);
        maxEdgeProperty.addListener(this::onMaxEdgeChanged);
    }

    private void onMinEdgeChanged(Observable obs, Point2D oldValue, Point2D newValue) {
        setMinEdge(newValue);
    }
    
     private void onMaxEdgeChanged(Observable obs, Point2D oldValue, Point2D newValue) {
        setMaxEdge(newValue);
    }
    
    public void setMinEdge(Point2D p) {
        
        // when modifying the min edge, the width and height must be recalculated;
        Point2D vector = getMinEdge().subtract(p);
        
        
        
        
 
        this.overlay.setOrigin(p.getX(), 0);
        this.overlay.setOrigin(p.getY(), 1);
        
        
        
        double newWidth = getWidth()+vector.getX();
        double newHeight = getHeight()+vector.getY();
        
        System.out.println(newWidth);
        System.out.println(getWidth());
        
        this.overlay.setExtent(newWidth, 0);
        this.overlay.setExtent(newHeight, 1);
        
        
        
    }

    public void setMaxEdge(Point2D p) {
        this.overlay.setExtent(p.getX() - overlay.getOrigin(0), 0);
        this.overlay.setExtent(p.getY() - overlay.getOrigin(1), 1);
    }

    public Point2D getMinEdge() {
        return new Point2D(overlay.getOrigin(0), overlay.getOrigin(1));
    }

    public Point2D getMaxEdge() {
        return new Point2D(overlay.getOrigin(0) + overlay.getExtent(0), overlay.getOrigin(1) + overlay.getExtent(1));
    }

    public double getWidth() {
        return overlay.getExtent(0);
    }

    public double getHeight() {
        return overlay.getExtent(1);
    }
    
}
