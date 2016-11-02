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

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class PixelStatisticsBase implements PixelStatistics{
    
    
        private final int MEDIAN_PERCENTILE = 50;    
    
    private  double mean;
    private  double max;
    private  double min;
    private  double standardDeviation;
    private  double variance;
    private  double median;
    private  long pixelCount;

    public PixelStatisticsBase() {
    }
    
    public PixelStatisticsBase(DescriptiveStatistics stats) {
        
        
        setMean(stats.getMean());
        setMax(stats.getMax());
        setStandardDeviation(stats.getStandardDeviation());
        setVariance(stats.getVariance());
        setMedian(stats.getPercentile(50));
        setPixelCount(stats.getN());
        setMin(stats.getMin());
        
    }

    public double getMean() {
        return mean;
    }

    public void setMean(double mean) {
        this.mean = mean;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getStandardDeviation() {
        return standardDeviation;
    }

    public void setStandardDeviation(double standardDeviation) {
        this.standardDeviation = standardDeviation;
    }

    public double getVariance() {
        return variance;
    }

    public void setVariance(double variance) {
        this.variance = variance;
    }

    public double getMedian() {
        return median;
    }

    public void setMedian(double median) {
        this.median = median;
    }

    public long getPixelCount() {
        return pixelCount;
    }

    public void setPixelCount(long pixelCount) {
        this.pixelCount = pixelCount;
    }
    
    
    
}
