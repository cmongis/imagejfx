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

import ijfx.ui.main.ImageJFX;
import ijfx.ui.service.angular.TaskWatcher;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import mercury.app.MercuryAppLoader;
import mercury.generator.JavascriptGenerator;
import netscape.javascript.JSException;
import netscape.javascript.JSObject;

/**
 * The AngularBinder binds AngularJS to Java objects. 
 * @author Cyril MONGIS, 2015
 */
public class AngularBinder {

    private static final Logger logger
            = ImageJFX.getLogger();

    protected HashMap<String, AngularService> services = new HashMap<>();

    protected ExecutorService executor = Executors.newFixedThreadPool(4);

    protected JavascriptGenerator javascriptGenerator = new JavascriptGenerator(this);

    protected String angularModuleCode;

    public static String DOC_WEB_APP = "/public_html/index.html";

    protected MercuryTimer timer = new MercuryTimer("JSMercury");

    protected MercuryAppLoader appManager = new MercuryAppLoader();

    protected ObservableList<LogEntry> jsMessageList = FXCollections.observableArrayList();

    public static String JS_LOG = "Javascript log";
    public static String JS_ERROR = "Javascript error";
    public static String JS_WARNING = "Javascript warning";
    public static String ACTION_EXECUTION_ERROR = "Action execution error : %s";
    
    
    public AngularBinder() {

        jsMessageList.addListener((ListChangeListener.Change<? extends LogEntry> change) -> {
            change.next();

            change.getAddedSubList().forEach(msg -> logger.info(msg.toString()));

        });

    }

    

    /**
     * 
     * @return the code to inject into page. The code will
     * call the different registered services
     */
    public String getAngularCode(){
        if (angularModuleCode == null) {
            angularModuleCode = javascriptGenerator.generate();
        }
        return angularModuleCode;
    }
    /**
     * Register object as a service in AngularJS. The service
     * 
     * @param serviceName name representing the object in AngularJS
     * @param service java object representing the service
     */
    public void registerService(String serviceName, Object service) {
        services.put(serviceName, new AngularService(serviceName, service));
        angularModuleCode = null;
    }

    /**
     * Load  a page web from a jar file
     * @param object reference object (used to get the ressource)
     * @param webView webview to load the page into
     * @param addr address of the resource inside the jar
     */
    public void load(Object o, WebView webView, String addr) {
        // Starting the view
        logger.info(o.getClass().getResource(addr).toExternalForm());
        webView.getEngine().load(o.getClass().getResource(addr).toExternalForm());
    }

    public AngularService getService(String serviceName) {
        if (serviceExists(serviceName)) {
            return services.get(serviceName);
        } else {
            logger.warning("No service " + serviceName + " exists");
            return null;
        }
    }
    /**
     * Returns true if a service exists
     * @param serviceName the name of the service
     * @return true if the service has already been registered
     */
    
    public boolean serviceExists(String serviceName) {
        return services.containsKey(serviceName);
    }

    /**
     * Register an action (method) available from a service
     * @param serviceName the name of the service that proposes the action (method)
     * @param actionName the name of the action (method)
     */
    protected void registerAction(String serviceName, String actionName) {
        // make service
        try {
            logger.info(String.format("Registering %s from service %s", actionName, serviceName));
            getService(serviceName).addAction(actionName);
        } catch (NullPointerException e) {
            logger.log(Level.WARNING, "Couldn't add " + actionName + " for " + serviceName, e);
        }
    }

    /**
     * Features yet to implements
     * @param serviceName
     * @param variableName 
     */
    public void registerVariable(String serviceName, String variableName) {

    }

    /**
     * Returns a list of registered services
     * @return a list of registerd services
     */
    public Collection<AngularService> services() {
        return services.values();
    }

    /**
     * Replaces console.log (used in javascript)
     * @param text 
     */
    public void log(String text) {
        getJsMessageList().add(new LogEntry(LogEntryType.LOG).setTitle(JS_LOG).setText(text));
    }
    /**
     * Replaces console.warn (to use in javascript)
     * @param text 
     */
    public void warn(String text) {
        getJsMessageList().add(new LogEntry(LogEntryType.WARNING).setTitle(JS_WARNING).setText(text));
    }

    /**
     * Replaces console.error (used in javascript)
     * @param text 
     */
    public void error(String text) {
        getJsMessageList().add(new LogEntry(LogEntryType.ERROR).setTitle(JS_ERROR).setText(text));

    }

    /**
     * 
     * @param type
     * @param annotation
     * @return 
     */
    public static List<Method> getMethodsAnnotatedWith(final Class<?> type, final Class<? extends Annotation> annotation) {
        final List<Method> methods = new ArrayList<Method>();
        Class<?> klass = type;
        //System.out.println(klass.getName());
        final List<Method> allMethods = new ArrayList<Method>(Arrays.asList(klass.getDeclaredMethods()));
        for (final Method method : allMethods) {
            if (method.isAnnotationPresent(annotation)) {
                methods.add(method);
            }
        }

        return methods;
    }

    
    /**
     * Function used to save the javascript code generated injected to the pages
     * @param url 
     */
    public void saveJavascriptCode(String url) {

        String msg = getAngularCode();

        try {
            Files.write(Paths.get(url), msg.getBytes());
        } catch (IOException ex) {
            ImageJFX.getLogger();
        }
    }

    /**
     * Binds a webview. Each time a page is loaded, the AngularBinder will inject
     * the necessary angular code to the webview.
     * @param webView 
     */
    public void bindWebView(WebView webView) {

        webView.getEngine().getLoadWorker().valueProperty().addListener(new ChangeListener<Void>() {
            @Override
            public void changed(ObservableValue<? extends Void> observable, Void oldValue, Void newValue) {
                logger.info("State changed, new value = "+newValue);
                injectCode(webView);
            }
        });
        webView.getEngine().getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {

            @Override
            public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) {

                logger.info("State changed : " + newValue);
                injectCode(webView);
            }
        });
        webView.getEngine().getLoadWorker().workDoneProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                //System.out.println("WordDone changed : " + newValue);

                if (newValue.doubleValue() >= 0) {
                    injectCode(webView);
                }
            }
        });

      

    }

    /**
     * Inject the code in a @WebView
     * @param view the WebView
     */
    private void injectCode(WebView view) {

        //getting the window object
        WebEngine webEngine = view.getEngine();
        JSObject document = (JSObject) webEngine.executeScript("window");
        
        try {
            
            // stops angular bootstrap sequence
            document.eval("window.name = 'NG_DEFER_BOOTSTRAP!';");
            
            // putting channeling error messages
            document.eval("console.error = function(message) { mercury.error(message); } ;");
            document.eval("console.log = function(message) {mercury.log(message); };");
            document.eval("console.warn = function(message) {mercury.warn(message); };");
            
            // checking if the binder has already been injected
            if (document.getMember("mercury").equals("undefined")) {
                //loading the binder
                document.setMember("mercury", this);
                logger.info("Mecury loaded");
            }
            else {
                logger.info("Mercury already loaded");
            }
            //checking is the angular module has been injected
            if (document.getMember("angularLoaded").toString().contains("true") == false) {
                // checking if angular has already been initialized
                if (document.getMember("angular").toString().equals("undefined")) {
                    return;
                }
                
                logger.info("Loading angular code...");
                document.eval("window.angularLoaded = true");
                document.eval(getAngularCode());
                
                // resume the bootstrap sequence once done
                logger.info("Angular code loaded.");
                document.eval("if(angular != undefined && angular.resumeBootstrap != undefined) angular.resumeBootstrap();");
                // document.eval("window.onerror = function (msg, url, line) {mercury.onerror(msg, url, line);};");

            } else {
                logger.info("Angular already loaded");
            }

        }
        catch (JSException jse) {
            logger.log(Level.SEVERE,"Error when executing Javascript injection !",jse); 
            getJsMessageList().add(new LogEntry(jse).setTitle(JS_ERROR));
        }

    }

    /**
     * Get the list of all messages (log,warn,error)
     * delivered by the WebViews
     * @return a list of JSMessages
     */
    public ObservableList<LogEntry> getJsMessageList() {
        return jsMessageList;
    }

    @Deprecated
    public void onerror(Object msg, Object url, Object line) {

        String message = "[JS Error] ***************\nLine %s (%s)\nError : %s\n***********";
        String[] urlComponents = url.toString().split("/");
        String fileName = urlComponents[urlComponents.length - 1];
        msg = msg.toString().replaceAll(Matcher.quoteReplacement("%2F"), Matcher.quoteReplacement("/"));
        System.err.println(String.format(message, line.toString(), fileName, msg.toString()));
    }

    /**
     * Execute an action (used from the javascript side)
     * @param serviceName
     * @param actionName
     * @param deferred deferred object created in javascript
     * @param params json object representing the parameters
     * @throws SecurityException 
     */
    public void executeAction(String serviceName, String actionName, final Object deferred, final Object params) throws SecurityException {
        try {

            // JSObject jsObject = (JSObject)params;
            String jsonString = new String(params.toString());

            logger.info("(Java) Executing action : " + actionName);
            final Object service = getService(serviceName).getObject();

            final MercuryTimer t = new MercuryTimer("ACTION : " + actionName);
            t.setLogger(logger);
            
            
            // create parameter
            final Method m = service.getClass().getMethod(actionName, Deferred.class, JSONParameters.class);
           
            
            
            // wrap promise
            //      - allow notifiying and request success or error by calling the promise object methods : "defered", "notify" and "resolve"
            final Deferred request = new Deferred(deferred);
            
            final JSONParameters parser = new JSONParameters(params);
            

            t.elapsed("Wrapping");
            
            
            // if the method returns a task, we wrap the task watch it
            if(Task.class.isAssignableFrom(m.getReturnType())) {
                try {
                     logger.info("Invoking the method : "+actionName);
                    Task task = (Task) m.invoke(service, request,parser);

                    
                    new TaskWatcher(task, request)
                            .setMapToJSON(m.isAnnotationPresent(MapResultToJSON.class))
                            .startInParallel();
                    
                }
                catch(Exception ex) {
                    getJsMessageList().add(new LogEntry(ex).setTitle(ACTION_EXECUTION_ERROR.format(actionName)));
                    ImageJFX.getLogger().log(Level.SEVERE,null,ex);;
                }
                finally {
                    return;
                }
            }
            
            
            // wrap params
            executor.submit(() -> {
                try {
                    //t.elapsed("Before submission");
                    logger.info("Invoking the method : "+actionName);

                    m.invoke(service, request, parser);
                    t.elapsed("Action time");
                    logger.info("Invocation over : "+actionName);
                    // submit to thread
               
                } catch (Exception ex) {
                    getJsMessageList().add(new LogEntry(ex).setTitle(ACTION_EXECUTION_ERROR.format(actionName)));
                    ImageJFX.getLogger();
                }
            });
        } catch (NoSuchMethodException ex) {
            ImageJFX.getLogger().log(Level.SEVERE, null, ex);
           
        }

    }

}
