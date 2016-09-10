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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 *
 * @author Pierre BONNEAU
 */
@JsonDeserialize(as=PixelStatisticsBase.class)
public interface PixelStatistics {
    
    double getMean();
    double getMax();
    double getMin();
    double getStandardDeviation();
    double getVariance();
    double getMedian();
    long getPixelCount();
    
    
    PixelStatistics EMPTY = new PixelStatistics() {
        @Override
        public double getMean() {
             return Double.NaN;
        }

        @Override
        public double getMax() {
             return Double.NaN;
        }

        @Override
        public double getMin() {
             return Double.NaN;
        }

        @Override
        public double getStandardDeviation() {
             return Double.NaN;
        }

        @Override
        public double getVariance() {
             return Double.NaN;
        }

        @Override
        public double getMedian() {
             return Double.NaN;
        }

        @Override
        public long getPixelCount() {
             return 0;
        }
    };
    
}
