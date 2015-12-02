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
package mercury.core;

import mercury.core.Deferred;
import com.fasterxml.jackson.databind.ObjectMapper;
import ijfx.ui.main.ImageJFX;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mercury.test.AngularTestService;
import netscape.javascript.JSObject;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class JSONParameters {

    JSObject originalParameter;

    int firstInt;
    String firstString;
    double firstDouble;
    String jsonString;

    boolean isObject;
    boolean isArray;

    HashMap<String, Object> hash;
    List<Object> valueArray;

   

    
    
    
    
    public JSONParameters(Object parameters) {

       
        originalParameter = (JSObject) parameters;

        JSObject json = (JSObject) originalParameter.eval("JSON");
        
        //
        jsonString = json.call(Deferred.JSON_STRINGIFY, parameters).toString();


    }

    public List getValueArray() {
        if (valueArray == null) {
            if (isArray) {
                ObjectMapper om = new ObjectMapper();
                try {
                    valueArray = om.readValue(getJSONString(), List.class);
                } catch (IOException ex) {
                   ImageJFX.getLogger().log(Level.SEVERE, null, ex);
                }
            } else {
                valueArray = Arrays.asList(getHash().values().toArray());
            }
        }
        return valueArray;
    }

    public HashMap<String, Object> getHash() {
        if (hash == null) {
            ObjectMapper objectMapper = new ObjectMapper();

            try {
                hash = objectMapper.readValue(getJSONString(), HashMap.class);
            } catch (IOException ex) {
                hash = new HashMap<>();
                ImageJFX.getLogger();
            }
        }
        return hash;
    }

    public String getJSONString() {
        return jsonString;
    }

    public Integer getFirstInt() {
        return getFirst(Integer.class);
    }

    public Double getFirstDouble() {
        return getFirst(Double.class);
    }

    public String getFirstString() {
        return (String) getFirst(String.class);
    }

    public <Type> Type getFirst(Class<Type> cl) {
        List values = getValueArray();
        
        if (values == null) {
            System.err.println("getValueArray() failed");
            return null;
        }

        for (Object o : values) {
            
            if(cl.isAssignableFrom(o.getClass())) {
                return (Type) o;
            }
        }

        return null;
    }

    public <T> T get(String key, T def) {
        if (getHash().containsKey(key)) {
            return (T) getHash().get(key);
        } else {
            return def;
        }
    }

    public String getString(String aString, String def) {
        return get(aString, def);
    }

    public Integer getInt(String aInt, Integer def) {
        return get(aInt, def);
    }

    public Double getDouble(String aDouble, Double def) {
        return get(aDouble, def);
    }

    public String getString(String aString) {
        return getString(aString, "");
    }

    public Integer getInt(String aInt) {
        return getInt(aInt, -1);
    }

    public Double getDouble(String aDouble) {
        return getDouble(aDouble, Double.NaN);
    }

    public <T> T mapObject(Class<T> aClass) {

        try {
            
            ObjectMapper mapper = new ObjectMapper();
            
            return mapper.readValue(getJSONString(), aClass);
        } catch (IOException ex) {
            System.err.println("Coudn't match the class.");
            ImageJFX.getLogger();
            return null;
        }
    }

    public <T> List<T> mapObjectList(Class<T> aClass) {
        return (List<T>) mapObject(List.class);
    }

}
