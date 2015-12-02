/*
 * /*
 *     This file is part of ImageJ FX.
 *
 *     ImageJ FX is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     ImageJ FX is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with ImageJ FX.  If not, see <http://www.gnu.org/licenses/>. 
 *
 * 	Copyright 2015,2016 Cyril MONGIS, Michael Knop
 *
 */
package ijfx.core.metadata;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */





/**
 *
 * @author Cyril MONGIS, 2015
 */
public class GenericMetaData implements MetaData,Comparable<Object>{

    String key;
    Object value;
    
    int type = MetaData.TYPE_NOT_SET;
    
    int origin = ORIGIN_RAW;

   
    
    public GenericMetaData() {
        
    }
    
    public GenericMetaData(String key, Object value) {
        setName(key);
        setValue(value);
    }
    
    @Override
    public String getName() {
        return key;
    }
    
    public void set(String key, Object value) {
        setName(key);
        setValue(value);
    }
    
    @Override
    public void setName(String name) {
        key = MetaDataFactory.createKey(name);
    }
    
    @Override
    public Object getValue() {
        return value;
    }
    @Override
    public void setValue(Object value) {
        if(value == null) return;
        this.value = MetaDataFactory.createValue(value);
        updateType();
    }
    
    public void setValue(String value) {
        setValue(value);
    }
    
    public void setValue(Integer value) {
        setValue(value);
    }
    
    public void setValue(Double value) {
        setValue(value);
    }
    
    @Override
    public int getType() {
        return type;
    }
    
    @Override
    public Double getDoubleValue() {
        if(getType() == MetaData.TYPE_DOUBLE) {
            return (Double) value;
        }
        else if(getType() == MetaData.TYPE_INTEGER) {
            return new Double(getIntegerValue());
        }
        else {
            return 0.0;
        }
    }
    
    @Override
    public Integer getIntegerValue() {
    
        if(getType() == MetaData.TYPE_INTEGER) {
            return (Integer)value;
        }
        else if(getType() == MetaData.TYPE_DOUBLE) {
            
            return new Integer((int)Math.round(getDoubleValue()));
        }
        
        else if(getType() == MetaData.TYPE_STRING) {
            try {
                return Integer.parseInt(value.toString());
            }
            catch(Exception e) {
                return 0;
            }
        }
        else {
            return 0;
        }
    }
    
    @Override
    public String getStringValue() {
        return value.toString();
    }
    
    private int updateType() {
        if(value == null) {
            type = MetaData.TYPE_NOT_SET;
        }
        
        else  if(value instanceof Double) {
            type =  MetaData.TYPE_DOUBLE;
        }
        else if(value instanceof Integer) {
            type = MetaData.TYPE_INTEGER;
        }
        
       else if(value instanceof String) {
            type = MetaData.TYPE_STRING;
        }
        else {
            
            type = MetaData.TYPE_UNKNOWN;
        }
        
        return type;
    }

    @Override
    public int compareTo(Object o) {
        try {
            MetaData m = (MetaData) o;
            if(m.getType() == MetaData.TYPE_DOUBLE) 
                return m.getDoubleValue().compareTo(getDoubleValue());
            else if(m.getType() == MetaData.TYPE_INTEGER)
                return m.getIntegerValue().compareTo(getIntegerValue());
            else if(m.getType() == MetaData.TYPE_STRING) {
                return m.getStringValue().compareTo(getStringValue());
            }
            else return -1;
        }
        catch(Exception e) {
            System.err.println("Careful, you tried to compare an wierd object with a MetaData Object.");
            return -1;
        }
    }
    
    @Override
    public boolean equals(Object o) {
        try {
            MetaData m = (MetaData)o;
            return equals(m);
        }
        catch(Exception e) {
            return false;
        }
    }
    
  
    public boolean equals(MetaData m) {
        return m.getName().equals(getName()) && m.getValue().equals(getValue());
    }
    
   
    public boolean equals(GenericMetaData m) {
        return equals((MetaData)m);
    }
    
    @Override
    public String toString() {
        return String.format("%s : %s",getName(),getStringValue());
    }
    
    
     public int getOrigin() {
        return origin;
    }

    public void setOrigin(int origin) {
        this.origin = origin;
    }

    
}
