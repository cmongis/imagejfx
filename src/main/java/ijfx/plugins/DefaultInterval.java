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
package ijfx.plugins;

import ij.IJ;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;

/**
 *
 * @author cyril
 */
public class DefaultInterval implements LongInterval{
  

   public final LongProperty min = new SimpleLongProperty();
   public final LongProperty max = new SimpleLongProperty();
   public final LongProperty highValue = new SimpleLongProperty();
   public final LongProperty lowValue = new SimpleLongProperty();
   
   
   public DefaultInterval(long lowValue, long highValue) {
       this.lowValue.set(lowValue);
       this.highValue.set(highValue);
   }
   
   public DefaultInterval(long lowValue, long highValue, long minValue, long maxValue) {
       this(lowValue,highValue);
       min.setValue(minValue);
       max.setValue(maxValue);
   }
   
   public long getMinValue() {
       return min.getValue();
   }
   public long getMaxValue() {
       return max.getValue();
   }
   public long getHighValue() {
    return highValue.getValue();
}
   public long getLowValue() {
       return lowValue.getValue();
   }
    
   public LongProperty minProperty() {
       return min;
   }
   public LongProperty maxProperty() {
       return max;
   }
   
   public LongProperty highValueProperty() {
       return highValue;
   }
   
   public LongProperty lowValueProperty() {
       return lowValue;
   }
   
    
}
