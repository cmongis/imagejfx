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
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class AngularDocumentationService {
    
    
    AngularBinder manager;
    
    public AngularDocumentationService(AngularBinder manager) {
        this.manager = manager;
    } 
    
    @AngularMethod(
            description = "Returns a list of available services"
            ,inputExample = "{}"
            ,outputExample = "['DocumentationService','TestService']"
            ,inputDescription = "Nothing"
    )
    public void getServiceList(Deferred deferred, JSONParameters param) {
        ArrayList<String> serviceNames = new ArrayList<>();
        manager.services().forEach(service->serviceNames.add(service.getName()));

       deferred.parseAndResolve(JSONUtils.formatObjectForJSON(serviceNames));
        
       // deferred.parseAndResolve("['DocumentationService','TestService']");
        
    }
    
    @AngularMethod(
            description="Returns the list of the methods proposed by the service with their descriptions.",
            inputDescription = "Service name"
            ,inputExample = "{'name':'DocumentationService'}"
            ,outputExample = "[{'name':'doSomething','inputExample':'...','outputExample':''}]"
    )
    public void getMethodList(Deferred deferred, JSONParameters params) {
        String serviceName = params.getFirstString();
        MercuryTimer t = new MercuryTimer(this.getClass().getSimpleName());
        t.start();
        ObjectMapper mapper = new ObjectMapper();
        t.elapsed("initialising mapper");
        try {
            
            String json  = mapper.writeValueAsString(manager.getService(serviceName).getMethodDescriptionList());
            t.elapsed("JSON transformation");
            deferred.parseAndResolve(json);
            t.elapsed("JSON resolve");
        } catch (Exception ex) {
           ImageJFX.getLogger().log(Level.SEVERE, null, ex);
            deferred.parseAndReject("[]");
        }
    }
    
    
    
    
    
}
