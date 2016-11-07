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
package ijfx.core.metadata;

import static ijfx.core.metadata.MetaData.TYPE_DOUBLE;
import static ijfx.core.metadata.MetaData.TYPE_INTEGER;
import static ijfx.core.metadata.MetaData.TYPE_NUMBER;
import java.text.DecimalFormat;
import java.util.Objects;
import javax.jws.HandlerChain;
import mongis.utils.StringUtils;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class GenericMetaData implements MetaData, Comparable<Object> {

 
    Double number;
    String name;
    String string;

    
    public GenericMetaData() {
        
    }
    
    public GenericMetaData(String key, Object value) {
        setName(key);
        setValue(value);
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = MetaDataFactory.createKey(name);
    }

    @Override
    public void setValue(Object value) {
        
        if(value == null) {
            number = null;
            string = null;
            return;
        }
        
        try {   
            number = new Double(value.toString());
        } catch (Exception e) {
            string = value.toString();
        }
    }

    @Override
    public String getStringValue() {
        if(number == null && string == null) return "null";
        return number == null ? string : numberToString(number);
    }

    @Override
    public boolean isNull() {
        return number == null && string == null;
    }
    
    
    @Override
    public Integer getIntegerValue() {
        return number == null ? 0 : number.intValue();
    }

    @Override
    public Object getValue() {
        return number == null ? string : number;
    }

    @Override
    public Double getDoubleValue() {
        return number == null ? 0.0 : number.doubleValue();

    }

    @Override
    public int getType() {

        return number == null ? MetaData.TYPE_STRING : TYPE_NUMBER;

    }

    @Override
    public int getOrigin() {
        return ORIGIN_BASIC;
    }

    @Override
    public int compareTo(Object o) {
        try {
            
            if(o instanceof MetaData) {
                MetaData m = (MetaData)o;
                if(getType() == TYPE_NUMBER) {
                    return Double.compare(number, m.getDoubleValue());
                }
                return str().compareTo(m.str());
            }
            return number == null ? string.compareTo((String) o) : number.compareTo((Double) o);
        } catch (Exception e) {
            return 0;
        }
    }
    
    
    public static String numberToString(Double d) {
        return new DecimalFormat("#.####").format(d);
    }
    
    
    
    @Override
    public boolean equals(Object o) {
        
        if(MetaData.class.isAssignableFrom(o.getClass())) {
            
            MetaData m = (MetaData)o;
            
            return m.getStringValue().equals(getStringValue());
            
            
        }
        else return false;
        
        
        
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + Objects.hashCode(this.number);
        hash = 53 * hash + Objects.hashCode(this.string);
        return hash;
    }
    
     public  String toString() {
        
        final int type = getType();
        
        if(type == TYPE_DOUBLE || type == TYPE_NUMBER ||type == TYPE_INTEGER) {
            return StringUtils.numberToString(getDoubleValue(), 3);
        }
        if(getValue() == null) return null;
        return getValue().toString();
        
        
    }

}
