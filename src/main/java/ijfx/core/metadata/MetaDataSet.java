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



import java.util.HashMap;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class MetaDataSet extends HashMap<String, MetaData> {
    public MetaData put(MetaData data) {
        String key = MetaDataFactory.createKey(data.getName());
        if(containsKey(data.getName())) {
            
        }
        data.setName(key);
        return put(key,data);
    }
    
    
    
    
    public MetaDataSet merge(MetaDataSet set) {
        for(MetaData data : set.values()) {
            put(data);
        }
        return this;
    }
    
    
    public MetaData get(String key) {
        return super.getOrDefault(key, new GenericMetaData(key,null));
    }
    
    public String toString() {
        StringBuffer buffer = new StringBuffer(10000);
        buffer.append("## Metadata Set ##");
        for(String key : keySet()) {
            buffer.append(String.format("\n%s = %s",key,get(key).getStringValue()));
        }
        
        return buffer.toString();
    }
    
    public void putGeneric(String key, Object value) {
        put(new GenericMetaData(key,value));
    }
    public boolean containMetaData(MetaData metaData) {
        if (this.containsKey(metaData.getName())) {
            if (this.get(metaData.getName()).equals(metaData)) {
                return true;
            }
        }
        return false;
    }
   
    
}
