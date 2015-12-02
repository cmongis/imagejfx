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
package ijfx.service.remote;

import ijfx.ui.main.ImageJFX;
import javafx.concurrent.Task;
import mercury.core.JSONUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.scijava.service.AbstractService;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class DefaultLoginService extends AbstractService implements LoginService{

    
    String server = "http://localhost:4000/";
    
    boolean isLoggedIn = false;
    
    private String email;
    private String password;
    
    public final static String EMAIL = "email";
    public final static String PASSWORD = "password";
    
    public final static String API_USER = "user/";
    
    
    @Override
    public Task<Boolean> login(final String email, final String password) {
        
        Task<Boolean> task = new Task() {

            @Override
            protected Boolean call() throws Exception {
                
                int response = Request.Get(server+API_USER)
                        .bodyString(JSONUtils.generateJSON(EMAIL,email,PASSWORD,password), ContentType.APPLICATION_JSON)
                        .execute()
                        .returnResponse().getStatusLine().getStatusCode();
                
                return response == 200;
            }
            
        };
        
        ImageJFX.getThreadPool().submit(task);
        return task;
        
    }

    @Override
    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    @Override
    public String getCurrentUser() {
        return email;
    }

    @Override
    public LoginService setServer(String server) {
        this.server = server;
        return this;
    }

    @Override
    public String getServer() {
        return server;
    }
    
    
    
}
