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
import javafx.beans.property.SimpleBooleanProperty;
import javafx.util.converter.NumberStringConverter;

/**
 *
 * @author cyril
 */
public class SmartNumberStringConverter extends NumberStringConverter{

    
   private final BooleanProperty floatingPoint = new SimpleBooleanProperty();
   
   @Override
   public Number fromString(String str) {
       if(floatingPoint.getValue()) {
           return new Float(str);
       }
       else {
           return new Integer(str);
       }
   }
   
   @Override
   public String toString(Number number) {
       if(floatingPoint.getValue()) {
           return Float.toString(number.floatValue());
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
   
    
}
