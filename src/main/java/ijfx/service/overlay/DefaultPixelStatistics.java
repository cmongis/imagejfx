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
public class DefaultPixelStatistics implements PixelStatistics{
    
    @Parameter
    public OverlayStatService overlayStatService;    
    
    private final int MEDIAN_PERCENTILE = 50;    
    
    private final double mean;
    private final double max;
    private final double min;
    private final double standardDeviation;
    private final double variance;
    private final double median;
    private final long pixelCount;
    
    public DefaultPixelStatistics(ImageDisplay display, Overlay overlay, Context context){

        context.inject(this);
        
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
    
    
    public String toString(){
        return "\n Pixel Statistics : "
                +"\n\t Mean : "+this.mean
                +"\n\t Max : "+this.max
                +"\n\t Min : "+this.min
                +"\n\t SD : "+this.standardDeviation
                +"\n\t Variance : "+this.variance
                +"\n\t Median : "+this.median
                +"\n\t PixelCount : "+this.pixelCount;      
    }
}
