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
package ijfx.service.ui;

import ijfx.service.uicontext.UiContextService;
import ijfx.service.log.LogService;
import ijfx.ui.activity.Activity;
import ijfx.ui.activity.ActivityService;
import ijfx.ui.main.ImageJFX;
import ijfx.ui.main.LoadingScreenRequestEvent;
import ijfx.ui.service.angular.AngularService;
import ijfx.ui.service.angular.WebAppActivityContainer;
import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import mercury.core.AngularMethod;

import mercury.core.LogEntry;
import mercury.helper.MercuryHelper;
import net.imagej.ImageJService;
import org.scijava.Context;
import org.scijava.InstantiableException;
import org.scijava.Priority;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = Service.class, priority = Priority.VERY_LOW_PRIORITY)
public class AppService extends AbstractService implements ImageJService {

    protected WebView currentWebView;

    protected MercuryHelper helper;

    @Parameter
    UiContextService contextService;

    public final Logger logger = ImageJFX.getLogger();
    
    @Parameter
    Context context;

    @Parameter
    EventService eventService;

    @Parameter
    LogService logErrorService;

    @Parameter
    ProjectManagerWebWrapper projectManager;

    @Parameter
    ActivityService activityService;
    
    //@Parameter
    //ImageJInfoService imageJInfoService;
    public AppService() {

    }

    public MercuryHelper getHelper() {
        
        logger.info("inspecting " + new File("./").getAbsolutePath());
        if (helper == null) {
            helper = new MercuryHelper();
            helper.getAppLoader()
                    .scanDirectory("./web")
                    .scanDirectory("./")
                    .scanDirectory("./src/main/resources/web")
                    .scanDirectory("./src/main/resources");
            
            

            helper.registerService("AppService", this);
            helper.registerService("ImageJService", context.getService(ImageJInfoService.class));
            helper.registerService("ActivityService",activityService);
            //helper.registerService("ProjectService", projectManager);
            helper.getBinder().getJsMessageList().addListener(new ListChangeListener<LogEntry>() {

                @Override
                public void onChanged(ListChangeListener.Change<? extends LogEntry> c) {
                    c.next();
                    c.getAddedSubList().forEach(msg -> logErrorService.notifyError(msg));
                }

            });
            
            context.getService(PluginService.class).getPluginsOfType(AngularService.class).forEach(info->{
                
                try {
                    
                    AngularService service = info.createInstance();
                    logger.info("Loading App Service : "+service.getAngularName());
                    context.inject(service);
                    helper.registerService(service.getAngularName(), service);
                } catch (InstantiableException ex) {
                    ImageJFX.getLogger().log(Level.SEVERE,null,ex);;
                }
            
            
            });
            helper.registerService("ContextService", contextService);
            
            
        }

        return helper;
    }

    public void bindWebView(WebView webView) {
        getHelper().bindWebView(webView);
        currentWebView = webView;
        currentWebView.getEngine().getLoadWorker().workDoneProperty().addListener((event, old, newValue) -> {
            
            eventService.publishLater(new LoadingScreenRequestEvent(newValue.intValue() < 100, newValue.toString()));
        });
    }

    @AngularMethod(sync = true, description = "launch and ImageFX App", inputExample = "index")
    public void showApp(String app) {
        
        activityService.open(new WebAppActivity(app));
        
    }
    
   

    public void quitApp() {
        contextService.leave("webapp");
        contextService.update();
    }

    public void reloadCurrentView() {
        if (currentWebView == null) {
            return;
        }
        currentWebView.getEngine().reload();
    }

    public void historyBack() {
        if (currentWebView == null) {
            return;
        }
        currentWebView.getEngine().getHistory().go(-1);
    }

    public void historyForward() {
        if (currentWebView == null) {
            return;
        }
        currentWebView.getEngine().getHistory().go(+1);
    }

    public WebEngine getCurrentWebEngine() {
        return currentWebView.getEngine();

    }

    private class WebAppActivity implements Activity {

        String name;
        final String  id;
        
        WebAppActivityContainer webAppContainer = (WebAppActivityContainer) activityService.getActivity(WebAppActivityContainer.class);
        
        public WebAppActivity(String webAppId) {
            this.id = webAppId;
        }
        
        @Override
        public String getActivityId() {
            return webAppContainer.getActivityId();
        }
        
        @Override
        public Node getContent() {
            
            //getHelper().loadAppOnView(id, currentWebView);
            System.out.println("Web App ID : "+id);
            webAppContainer.setCurrentApp(id);
            return webAppContainer.getContent();
        }

        @Override
        public Task updateOnShow() {
            return webAppContainer.updateOnShow();
        }
    }
    
}
