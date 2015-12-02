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
package mercury.app;

import javafx.scene.web.WebView;
import mercury.core.Deferred;
import mercury.core.AngularMethod;
import mercury.core.JSONParameters;
import mercury.core.AngularBinder;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class MercuryAppService {
    
    
   
    MercuryAppLoader appManager;
    
    public MercuryAppService (MercuryAppLoader loader) {
        setAppManager(loader);
    }
    
    
    
    public MercuryAppLoader getAppManager() {
        return appManager;
    }

    public MercuryAppService setAppManager(MercuryAppLoader appManager) {
        this.appManager = appManager;
        return this;
    }
    
    

    
    
    @AngularMethod(description = "returns the list of available apps",outputExample = "['Main','Browser']")
    public void getAppList(Deferred deferred, JSONParameters params) {
        
        deferred.mapAndResolve(appManager.getAppList());
        
    }
    
    @AngularMethod(description = "launchApp an app on the current view",outputExample = "{}")
    public void launchApp(Deferred deferred, JSONParameters params) {
        
    }
    
   
    
    
    
    
}
