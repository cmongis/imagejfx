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
import java.util.logging.Logger;
import javafx.application.Platform;
import netscape.javascript.JSException;
import netscape.javascript.JSObject;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class Deferred {

    JSObject deferred;

    String originalJSON;

    ObjectMapper mapper;
    
    Logger logger = ImageJFX.getLogger();
    MercuryTimer timer = new MercuryTimer(this.getClass().getName());

    
    
    
    Deferred(Object deffered) {
       
        timer.setLogger(ImageJFX.getLogger());
        this.deferred = (JSObject) deffered;

       
    }

    public static String RESOLVE = "resolve";
    public static String NOTIFY = "notify";
    public static String REJECT = "reject";
    public static String JSON_OBJECT_NAME = "JSON";
    public static String JSON_PARSE = "parse";
    public static String JSON_STRINGIFY = "stringify";

    public ObjectMapper getMapper() {
        if (mapper == null) {
            mapper = new ObjectMapper();
        }
        return mapper;
    }

    /**
     *
     * @param javaObject any java object that will be passed directly to the javascript (no JSON nor string transformation)
     * 
     */
    public void resolve(Object javaObject) {
        callDeferred(RESOLVE, javaObject);
    }

    public void notify(Object javaObject) {
        callDeferred(NOTIFY, javaObject);
    }

    public void reject(Object javaObject) {
        callDeferred(REJECT, javaObject);
    }

    /**
     *
     * @param keyAndValues a list of keys and values followed one after the other.\n Example : notifySimpleJSON("count",10) -> {"count":10}
     * 
     */
    public void notifySimpleJSON(Object... keyAndValues) {
        callDeferredWithJSON(NOTIFY, JSONUtils.generateJSON((Object[]) keyAndValues));
    }

    /**
     *
     * @param keysAndValues a list of keys and values followed one after the other.\n Example : resolveSimpleJSON("count",10) -> {"count":10}
     * 
     */
    public void resolveSimpleJSON(Object... keysAndValues) {
        callDeferredWithJSON(RESOLVE, JSONUtils.generateJSON((Object[]) keysAndValues));
    }

    public String mapObjectTOSJON(Object o) {
        String json = "{}";
        try {
            json = getMapper().writeValueAsString(o);
        } catch (JsonProcessingException ex) {
            logger.warning("Couldn't map Object. Returning {}");
            ImageJFX.getLogger();
            ex.printStackTrace();
        }
        return json;
    }

    /**
     * Maps a Java Object into JSON Object and resolves it to the pro.
     * @param any Java object
     */
    public void mapAndResolve(Object o) {
        parseAndResolve(mapObjectTOSJON(o));
    }

    /**
     * Maps a Java Object into JSON Object and notifies it to the promise
     * @param o
     */
    public void mapAndNotify(Object o) {
        parseAndNotify(mapObjectTOSJON(o));
    }

    
    public void mapAndReject(Object o) {
        parseAndNotify(mapObjectTOSJON(o));
    }

    public void parseAndNotify(String jsonString) {
        callDeferredWithJSON(NOTIFY, jsonString);
    }

    public void parseAndResolve(String jsonString) {
        callDeferredWithJSON(RESOLVE, jsonString);
    }

    public void parseAndReject(String jsonString) {
        callDeferredWithJSON(REJECT, jsonString);
    }

    public void callDeferred(final String method, Object parameter) {
        runLater(() -> {
            deferred.call(method, parameter);
        });
    }

    public void callDeferredWithJSON(final String method, String parameter) {
        runLater(() -> {
            timer.start();
            deferred.call(method, toJSON(parameter));
            timer.elapsed("deferred." + method + "(" + parameter.toString() + ")");
        });
    }

    private JSObject getJSONInstance() {
        return (JSObject) deferred.eval(JSON_OBJECT_NAME);
    }

    private JSObject toJSON(String jsonString) {
        try {
            return (JSObject) getJSONInstance().call(JSON_PARSE, jsonString);
        } catch (JSException exception) {
            ImageJFX.getLogger();
            throw exception;
        }
        catch(Exception any) {
            any.printStackTrace();
            ImageJFX.getLogger();
        }
        return null;
    }

    private void runLater(Runnable r) {
        Platform.runLater(r);
    }

}
