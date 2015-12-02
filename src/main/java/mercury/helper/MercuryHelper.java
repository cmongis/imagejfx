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
package mercury.helper;

import ijfx.ui.main.ImageJFX;
import java.util.logging.Logger;
import javafx.scene.web.WebView;
import mercury.FileService;
import mercury.Mercury;
import mercury.app.MercuryAppLoader;
import mercury.app.MercuryAppService;
import mercury.core.AngularBinder;
import mercury.core.AngularDocumentationService;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class MercuryHelper {
    
    AngularBinder binder = new AngularBinder();
    MercuryAppLoader appLoader = new MercuryAppLoader();
    
    Logger logger = ImageJFX.getLogger();
    
    public MercuryHelper() {
        
        binder.registerService(Mercury.DOCUMENTATION_SERVICE, new AngularDocumentationService(binder));
        binder.registerService(Mercury.APP_SERVICE, new MercuryAppService(appLoader));
        binder.registerService(Mercury.FILE_SERVICE,new FileService());
    }
    
    public void bindWebView(WebView view) {
        getBinder().bindWebView(view);
    }
    
    public void registerService(String serviceName,Object service) {
        binder.registerService(serviceName,service);
    }
    
   public void loadAppOnView(String app, WebView view) {
       if(!appLoader.appExists(app)) {
           logger.info(String.format("The app \"%s\" doesn't exist.",app));
           return;
       }
       if(view != null)
        view.getEngine().load(appLoader.getApp(app).getAppURL());
   }

    public MercuryAppLoader getAppLoader() {
        return appLoader;
    }

    public AngularBinder getBinder() {
        return binder;
    }

    public void setBinder(AngularBinder binder) {
        this.binder = binder;
    }
    
    
}
