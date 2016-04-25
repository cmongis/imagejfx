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
    
    @Parameter
    public OverlayStatService overlayStatService;
    
    
    private final int MEDIAN_PERCENTILE = 50;
    
    private final Overlay overlay;
    
    private final double mean;
    private final double max;
    private final double min;
    private final double standardDeviation;
    private final double variance;
    private final double median;
    private final long pixelCount;
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

        Double[] valueList = overlayStatService.getValueList(display, overlay);
        
        DescriptiveStatistics statistics = new DescriptiveStatistics(ArrayUtils.toPrimitive(valueList));
        
        this.mean = statistics.getMean();
        this.max = statistics.getMax();
        this.min = statistics.getMin();
        this.standardDeviation = statistics.getStandardDeviation();
        this.variance = statistics.getVariance();
        this.median = statistics.getPercentile(MEDIAN_PERCENTILE);
        this.pixelCount = valueList.length;
        
        
    }
    
    @Override
    public Overlay getOverlay(){
        return this.overlay;
    }
    
    
    @Override
    public double getMean(){
        return this.mean;
    }
    
    @Override
    public double getMax(){
        return this.max;
    }
    
    
    @Override
    public double getMin(){
        return this.min;
    }
    
    
    @Override
    public double getStandardDeviation(){
        return this.standardDeviation;
    }
    
    
    @Override
    public double getVariance(){
        return this.variance;
    }
    
    
    @Override
    public double getMedian(){
        return this.median;
    }
    
    
    @Override
    public long getPixelCount(){
        return this.pixelCount;
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
                +"\n\t Mean : "+this.mean
                +"\n\t Max : "+this.max
                +"\n\t Min : "+this.min
                +"\n\t SD : "+this.standardDeviation
                +"\n\t Variance : "+this.variance
                +"\n\t Median : "+this.median
                +"\n\t Area : "+this.area
                +"\n\t PixelCount : "+this.pixelCount
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
