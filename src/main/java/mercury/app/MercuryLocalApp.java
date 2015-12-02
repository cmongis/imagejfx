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

import com.fasterxml.jackson.annotation.JsonCreator;
import java.io.File;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class MercuryLocalApp implements MercuryApp {
    
    String id;
    
    String name;
    
    String version;
    
    String description;
    
    String appDirectory;
    
    String url;
    
    @JsonCreator
    public MercuryLocalApp() {
        
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    
    
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAppDirectory() {
        return appDirectory;
    }
    
    public String getAppURL() {
        return new File(appDirectory).toURI().toString()+getUrl();
    }

    public void setAppPath(String AppDirectory) {
        this.appDirectory = AppDirectory;
    }

    public String getUrl() {
        if(url == null) return getId()+".html";
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    
    
    
    
    
}
