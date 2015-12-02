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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ijfx.ui.main.ImageJFX;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.web.WebEngine;
import netscape.javascript.JSObject;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class JSONUtils {
    
    
    public static void logError(String error) {
        System.err.println(error);
    }
    
    // Transform object in Json
    public static  String formatObjectForJSON(Object o) {
        if (o == null) {
            return "{}";
        }
        if (o instanceof String) {

            String s = (String) o;
            if (s.startsWith("{") && s.endsWith("}")) {
                return s;
            } else if (s.startsWith("[") && s.endsWith("]")) {

                return s;
            } else {
                return String.format("\"%s\"", o.toString().replace("\"", "\\\""));
            }

        } else if (o instanceof ArrayList) {
            StringBuffer buffer = new StringBuffer(2000);
            buffer.append("[");
            int count = 0;
            for (Object son : (ArrayList<Object>) o) {
                if (count > 0) {
                    buffer.append(",");
                }
                buffer.append(formatObjectForJSON(son));
                count++;
            }
            buffer.append("]");
            return buffer.toString();
        } else {
            return o.toString();
        }
    }
    // generation a JSON object from a list of keys and values
    public static String generateJSON(Object... keysAndValues) {
        if (keysAndValues == null) {
            return "{}";
        }
        if (keysAndValues.length % 2 != 0) {
            logError("Error when generating JSON");
            return "";
        }

        StringBuffer result = new StringBuffer(3000);
        result.append("{");

        for (int i = 0; i < keysAndValues.length; i += 2) {

            String key = formatObjectForJSON(keysAndValues[i]);
            String value = formatObjectForJSON(keysAndValues[i + 1]);
            if (i == 0) {
                result.append(String.format("%s : %s", key, value));
            } else {
                result.append(String.format(",%s : %s", key, value));
            }

        }

        result.append("}");

        return result.toString();
    }
    
    /**
     *
     * @param engine WebEngine used for parsing
     * @param json json string
     * @return JSObject returns a JS Object, useful for Async function
     */
    public static JSObject parseToJSON(WebEngine engine, String json) {
        
        JSObject document = (JSObject) engine.executeScript("window");
        JSObject jsonObject = (JSObject) document.getMember("JSON");
        return (JSObject) jsonObject.call("parse", json);
    }
    
    
    public static String mapToJSON(Object object) {
        ObjectMapper mapper = new ObjectMapper();
        
        try {

            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException ex) {
            ImageJFX.getLogger();
            return "{}";
        }
    }
    
    
    public static <T> List<T> mapFromJSONList(Object object){
        
        List<T> list = new ArrayList<T>();
        
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(object.toString(),new TypeReference<List<T>>(){});
        } catch (IOException ex) {
            ImageJFX.getLogger().log(Level.SEVERE, null, ex);
            return list;
        }
        
        
    }   
    
}
