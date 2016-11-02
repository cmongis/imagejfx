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

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class StringUtils {
    
    
    public static String numberToString(Double number, int maxDecimal) {
        
        String str = number.toString();
        
        if(str.indexOf(".") == -1) return number.toString();
        
        if(maxDecimal == 0) return Integer.toString(number.intValue());
        
        if(str.endsWith(".0")) {
            return str.substring(0, str.length()-2);
        }
        else {
            int nDecimal = str.length() - str.indexOf(".") - 1;
            if(nDecimal > maxDecimal) nDecimal = maxDecimal;
            
            return str.substring(0, str.indexOf(".")+nDecimal+1);
        }
        
    }
}
