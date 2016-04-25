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
package ijfx.service.overlay;

import javafx.geometry.Point2D;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

import net.imagej.display.ImageDisplay;
import net.imagej.overlay.Overlay;

import net.imglib2.roi.RectangleRegionOfInterest;
import org.scijava.Context;

/**
 *
 * @author Pierre BONNEAU
 */
public class RectangleOverlayStatistics extends AbstractOverlayStatistics{
    
    private Rectangle shape;
    
    
    public RectangleOverlayStatistics(ImageDisplay display, Overlay overlay, Context context){
        
        super(display, overlay, context);
        
        this.shape = (Rectangle) getShape(getOverlay());
        
        super.area = setArea();
        super.minimumBoundingRectangle = setMinimumBoundingRectangle();
        super.centerOfGravity = setCenterOfGravity();
        super.feretDiameter = setFeretDiameter();
        super.minFeretDiameter = setMinFeretDiameter();
        super.longSideMBR = setLongSideMBR();
        super.shortSideMBR = setShortSideMBR();
        super.aspectRatio = super.setAspectRatio();
        super.convexity = setConvexity();
        super.solidity = setSolidity();
        super.circularity = setCircularity();
        super.thinnesRatio = setThinnesRatio();

        
    }
    
    
    public Shape getShape(Overlay overlay){
        
        RectangleRegionOfInterest roi = (RectangleRegionOfInterest) overlay.getRegionOfInterest();
        
        return new Rectangle(roi.getOrigin(0), roi.getOrigin(1), roi.getExtent(0), roi.getExtent(1));        
    }

    
    public double setArea(){
        return this.shape.getHeight()*this.shape.getWidth();
    }
    
    
    public Polygon setMinimumBoundingRectangle(){
        
        Double[] p = new Double[8];
        
        p[0] = shape.getX();
        p[1] = shape.getY();
        p[2] = shape.getX() + shape.getWidth();
        p[3] = shape.getY();
        p[4] = shape.getX() + shape.getWidth();
        p[5] = shape.getY() + shape.getHeight();
        p[6] = shape.getX();
        p[7] = shape.getY() + shape.getHeight();

        Polygon polygon = new Polygon();
        polygon.getPoints().addAll(p);
        
        return polygon;
    }
    
    
    public Point2D setCenterOfGravity(){
        
        double xc, yc;
        
        xc = this.shape.getX() + Math.abs((this.shape.getX() + this.shape.getWidth()) - this.shape.getX());
        yc = this.shape.getY() + Math.abs((this.shape.getY() + this.shape.getHeight()) - this.shape.getY());
        return new Point2D(xc, yc);
    }
    
    
    public double setFeretDiameter(){
        return shape.getWidth() > shape.getHeight() ? shape.getWidth():shape.getHeight();
    }
    
    
    public double setMinFeretDiameter(){
        return shape.getWidth() < shape.getHeight() ? shape.getWidth():shape.getHeight();
    }
    
    
    public double setLongSideMBR(){
        return shape.getWidth() > shape.getHeight() ? shape.getWidth():shape.getHeight();
    }
    
    
    public double setShortSideMBR(){
        return shape.getWidth() < shape.getHeight() ? shape.getWidth():shape.getHeight();
    }
    
    
    public double setConvexity(){
        return 1.0;
    }
    
    
    public double setSolidity(){
        return 1.0;
    }
    
    
    public double getPerimeter(){
        return 2*this.shape.getHeight() + 2*this.shape.getWidth();
    }
    
    
    public double setCircularity(){
        return Math.pow(getPerimeter(), 2)/getArea();
    }
    
    
        public double setThinnesRatio(){
        
        thinnesRatio = (4*Math.PI)/getCircularity();
        thinnesRatio = (thinnesRatio>1)?1:thinnesRatio;
        
        return thinnesRatio;        
    }
}
