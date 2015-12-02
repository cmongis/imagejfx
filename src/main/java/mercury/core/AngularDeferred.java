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
import com.fasterxml.jackson.databind.ObjectMapper;
import ijfx.ui.main.ImageJFX;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import netscape.javascript.JSObject;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class AngularDeferred {
    JSObject deferred;

    String originalJSON;
    
    ObjectMapper mapper;
    
    MercuryTimer timer = new MercuryTimer(this.getClass().getName());
    
    AngularDeferred(Object deffered) {

        this.deferred = (JSObject) deffered;

    }
    
    public static String RESOLVE = "resolve";
    public static String NOTIFY = "notify";
    public static String REJECT = "reject";
    public static String JSON_OBJECT_NAME = "JSON";
    public static String JSON_PARSE = "parse";
    public static String JSON_STRINGIFY = "stringify";
    
    
    public ObjectMapper getMapper() {
        if(mapper == null) {
            mapper = new ObjectMapper();
        }
        return mapper;
    }
    
    public void resolve(Object javaObject) {
        callDeferred(REJECT, javaObject);
    }

    public void notify(Object javaObject) {
        callDeferred(NOTIFY,javaObject);
    }
    
    public void reject(Object javaObject) {
        callDeferred(REJECT, javaObject);
    }
    
    public void notifyJSON(String... keyAndValues) {
        callDeferredWithJSON(NOTIFY,JSONUtils.generateJSON((Object[])keyAndValues));
    }
    
    public void resolveJSON(String... keysAndValues) {
        callDeferredWithJSON(RESOLVE,JSONUtils.generateJSON((Object[])keysAndValues));
    }
    
    
    public String mapObjectTOSJON(Object o ) {
       String json = "{}";
        try {
            json = getMapper().writeValueAsString(o);
        } catch (JsonProcessingException ex) {
           ImageJFX.getLogger().log(Level.SEVERE, null, ex);
            System.err.println("Couldn't map Object. Returning {}");
          
        }
        
        return json;
    }

    public void resolveAfterMapping(Object o) {
        resolveAsJSON(mapObjectTOSJON(o));
    }
    
    public void notifyAfterMapping(Object o) {
       notifyAsJSON(mapObjectTOSJON(o));
    }
    
    public void rejectAfterMapping(Object o) {
        notifyAsJSON(mapObjectTOSJON(o));
    }
    
    
    public void notifyAsJSON(String jsonString) {
        callDeferredWithJSON(NOTIFY, jsonString);
    }
    
    public void resolveAsJSON(String jsonString) {
        callDeferredWithJSON(RESOLVE, jsonString);
    }
    
    public void rejectAsJSON(String jsonString) {
        callDeferredWithJSON(REJECT, jsonString);
    }
    
    public void callDeferred(final String method, Object parameter) {
        runLater(()-> {
            deferred.call(method,parameter); 
        });
    }
    
    public void callDeferredWithJSON(final String method, String parameter) {
        runLater(() -> {
            timer.start();

            deferred.call(method, toJSON(parameter));
            timer.elapsed("deferred."+method+"("+parameter.toString()+")");
        });
    }
    
    private JSObject getJSONInstance() {
        return (JSObject) deferred.eval(JSON_OBJECT_NAME);
    }
    
    
    private JSObject toJSON(String jsonString) {
        return (JSObject) getJSONInstance().call(JSON_PARSE, jsonString);
    }
    private void runLater(Runnable r) {
        Platform.runLater(r);
    }
    
}
