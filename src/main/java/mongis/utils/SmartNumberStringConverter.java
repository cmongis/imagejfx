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
package mongis.utils;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.util.converter.NumberStringConverter;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class SmartNumberStringConverter extends NumberStringConverter{

    
   private final BooleanProperty floatingPoint = new SimpleBooleanProperty();
   
   private final IntegerProperty floatingPointNumber = new SimpleIntegerProperty(2);
   
   @Override
   public Number fromString(String str) {
       if(str.trim().equals("")) return Double.NaN;
       if(floatingPoint.getValue()) {
           return new Double(str);
       }
       else {
           return new Integer(str);
       }
   }
   
   @Override
   public String toString(Number number) {
       if(floatingPoint.getValue() && floatingPointNumber.getValue() != 0) {
           return StringUtils.numberToString(number.doubleValue(), floatingPointNumber.getValue());
       }
       else {
           return Integer.toString(number.intValue());
       }
   }

    public boolean isFloatingPoint() {
        return floatingPoint.getValue();
    }

    public void setFloatingPoint(boolean floatingPoint) {
        this.floatingPoint.setValue(floatingPoint);
    }
   
    public BooleanProperty floatingPointProperty() {
        return floatingPoint;
    }
   
    public IntegerProperty floatingPointNumberProperty() {
        return floatingPointNumber;
    }
    
}
