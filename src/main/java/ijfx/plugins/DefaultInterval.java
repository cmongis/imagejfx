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

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;

/**
 *
 * @author cyril
 */
@JsonTypeInfo(use =JsonTypeInfo.Id.CLASS,defaultImpl = LongInterval.class)
public class DefaultInterval implements LongInterval{
  

    @JsonIgnore
   protected final LongProperty min = new SimpleLongProperty();
    @JsonIgnore
   protected final LongProperty max = new SimpleLongProperty();
    @JsonIgnore
   protected final LongProperty highValue = new SimpleLongProperty();
    @JsonIgnore
   protected final LongProperty lowValue = new SimpleLongProperty();
   
   public DefaultInterval() {
       
   }
   
   public DefaultInterval(long lowValue, long highValue) {
       this.lowValue.set(lowValue);
       this.highValue.set(highValue);
   }
   
   public DefaultInterval(long lowValue, long highValue, long minValue, long maxValue) {
       this(lowValue,highValue);
       min.setValue(minValue);
       max.setValue(maxValue);  
   }
   
   
   @JsonSetter("min")
   public void setMinValue(long minValue) {
       min.setValue(minValue);
   }
   @JsonSetter("max")
   public void setMaxValue(long maxValue) {
       max.setValue(maxValue);
   }
   @JsonSetter("low")
   public void setLowValue(long low) {
       lowValue.setValue(low);
   }
   
   @JsonSetter("high")
   public void setHighValue(long high) {
       highValue.setValue(high);
   }
   
   @JsonGetter("min")
   public long getMinValue() {
       return min.getValue();
   }
   @JsonGetter("max")
   public long getMaxValue() {
       return max.getValue();
   }
   @JsonGetter("high")
   public long getHighValue() {
    return highValue.getValue();
}
   @JsonGetter("low")
   public long getLowValue() {
       return lowValue.getValue();
   }
    
   @JsonIgnore
   public LongProperty minProperty() {
       return min;
   }
   @JsonIgnore
   public LongProperty maxProperty() {
       return max;
   }
   
   @JsonIgnore
   public LongProperty highValueProperty() {
       return highValue;
   }
   
   public LongProperty lowValueProperty() {
       return lowValue;
   }
   
    
}
