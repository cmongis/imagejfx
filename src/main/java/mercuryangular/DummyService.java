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
package mercuryangular;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ijfx.ui.main.ImageJFX;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import mercury.core.Deferred;
import mercury.core.AngularMethod;
import mercury.core.JSONParameters;
import mercury.core.JSONUtils;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class DummyService {

    // synchronized angular method : the method can be
    // executed from the Angular without promises. 
    @AngularMethod(sync = true,description = "do ")
    public String getServiceName() {
        return "DummyService";
    }

    // a more complicated method that map a file object to json
    @AngularMethod(sync = true)
    public File getFileInfoSync(String path) {
        return new File(path);
    }
    
    // now, let do something that takes long.
    // We use the deferred object to communicate with the angular
    // part of the software
    @AngularMethod(description = "some description")
    public void doAsyncThings(Deferred deferred, JSONParameters parameters) {
        
        // inform the promise of the progress of the action
        for(int i = 0;i!= 10;i++) {
            deferred.notifySimpleJSON("count",i); // sends {'count':'10'} It's a quick way to generate simple JSON messages
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                ImageJFX.getLogger().log(Level.SEVERE, null, ex);
            }
        }
        
        // now we map a file object to a JSON String using JSON Jackson.
        // the object will be automatically transformed back to JSON in the web part
        deferred.parseAndResolve(JSONUtils.mapToJSON(new DummyObject()));
    }
    
    
    
    
    public class DummyObject {
    
        
        public String getName() {
            return "Margin";
        }
        
        public String getAddress() {
            return "Heidelberg";
        }
        
        public int getCount() {
            return 10;
        }
    
    }
    
}
