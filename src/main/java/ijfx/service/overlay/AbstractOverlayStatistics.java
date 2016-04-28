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
import net.imagej.display.ImageDisplay;
import net.imagej.overlay.Overlay;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.scijava.Context;

import org.scijava.plugin.Parameter;

/**
 *
 * @author Pierre BONNEAU
 */
abstract class AbstractOverlayStatistics implements OverlayStatistics{
        
    private final Overlay overlay;
    
    PixelStatistics pixelStatistics;
    
    protected double area;
    protected Polygon minimumBoundingRectangle;
    protected Point2D centerOfGravity;
    protected double feretDiameter;
    protected double minFeretDiameter;
    protected double longSideMBR;
    protected double shortSideMBR;
    protected double aspectRatio;
    protected double convexity;
    protected double solidity;
    protected double circularity;
    protected double thinnesRatio;
    
    
    public AbstractOverlayStatistics(ImageDisplay display, Overlay overlay, Context context){
        
        context.inject(this);
        
        this.overlay = overlay;
        this.pixelStatistics = new DefaultPixelStatistics(display, overlay, context);

    }
    
    @Override
    public Overlay getOverlay(){
        return this.overlay;
    }
    
    
    @Override
    public PixelStatistics getPixelStatistics(){
        return this.pixelStatistics;
    }
    
    @Override
    public double getArea(){
        return this.area;
    }
    
    @Override
    public Polygon getMinimumBoundingRectangle(){
        return this.minimumBoundingRectangle;
    }
    
    @Override
    public Point2D getCenterOfGravity(){
        return this.centerOfGravity;
    }
    
    @Override
    public double getFeretDiameter(){
        return this.feretDiameter;
    }
    
    
    @Override
    public double getMinFeretDiameter(){
        return minFeretDiameter;
    }
    
    
//    public double getOrientationMajorAxis(){
//        
//    }
//    
//    
//    public double getOrientationMinorAxis(){
//        
//    }
//    
//    
    @Override
    public double getLongSideMBR(){
        return this.longSideMBR;
    }
    
    
    @Override
    public double getShortSideMBR(){
        return this.shortSideMBR;
    }
    
    
    @Override
    public double getAspectRatio(){
        return this.aspectRatio;
    }
    
    
    public double setAspectRatio(){
        return getLongSideMBR()/getShortSideMBR();
    }
    
    
    @Override
    public double getConvexity(){
        return this.convexity;
    }
    
    
    @Override
    public double getSolidity(){
        return this.solidity;
    }
    
    
    @Override
    public double getCircularity(){
        return this.circularity;
    }
    
    
    @Override
    public double getThinnesRatio(){
        return this.thinnesRatio;
    }

    
    public String toString(){
        return "\nSTATISTICS"
                +"\n\t Pixels Statistics : "+this.pixelStatistics.toString()
                +"\n\t Area : "+this.area                
                +"\n\t Minimum Bounding Rectangle : "+ this.minimumBoundingRectangle.toString()
                +"\n\t Center of gravity : "+ this.centerOfGravity.toString()
                +"\n\t Maximum Feret's diameter : "+ this.feretDiameter
                +"\n\t Minimum Feret's diameter : "+ this.minFeretDiameter
                +"\n\t Long Side MBR : "+ this.longSideMBR
                +"\n\t Short Side MBR : "+this.shortSideMBR
                +"\n\t Aspect Ratio : "+this.aspectRatio
                +"\n\t Convexity : "+this.convexity
                +"\n\t Solidity : "+this.solidity
                +"\n\t Circularity : "+this.circularity
                +"\n\t Thinnes Ratio : "+this.thinnesRatio;

    }
}
