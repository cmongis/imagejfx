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




import java.util.ArrayList;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class MetaDataFactory {
    
    private final static ArrayList<String> createdKeys = new ArrayList<String>();
    public synchronized static String createKey(String string) {
        for(String key : createdKeys) {
            
            if(key.equals(string)) {
                
                //System.out.println("[MetaDataFactory] Existing key : "+key);
                return key;
            }
        }
        //System.out.println("[MetaDataFacotyr] Creating key "+string);
       createdKeys.add(string);
       return string;
    }
    public static final ArrayList<Object> createdValues = new ArrayList<Object>();
    public static Object createValue(Object o) {
        for(Object value : createdValues) {
           if(value == null) continue;
            if(value.equals(o)) {
                
                
                return value;
            }
        }
        createdValues.add(o);
       return o;
    }
    
}
