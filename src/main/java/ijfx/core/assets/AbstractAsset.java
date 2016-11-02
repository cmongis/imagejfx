/*
    This file is part of ImageJ FX.

    ImageJ FX is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    ImageJ FX is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with ImageJ FX.  If not, see <http://www.gnu.org/licenses/>. 
    
     Copyright 2015,2016 Cyril MONGIS, Michael Knop
	
 */
package ijfx.core.assets;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import java.io.File;
import java.util.UUID;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public abstract class AbstractAsset<T> implements Asset<T> {

    @JsonIgnore
    private File file;
    
    @JsonIgnore
    private final Class<T> type;

    @JsonIgnore
    private UUID id;
    
    
    protected AbstractAsset(Class<T> type) {
        this.type = type;
    }
    
    @JsonIgnore
    public UUID getId() {
        if(id == null) {
            id = UUID.nameUUIDFromBytes(getIdString().getBytes());
        }
        
        return id;
        
    }
    
    
    @JsonSetter("file")
    public void setFile(File f) {
        this.file = f;
        
        
    }
    
    @Override
    public File getFile() {
        return file;
    }

    @JsonIgnore
    @Override
    public Class<T> getAssetType() {
        return type;
    }
    
    protected abstract String getIdString();
    
}
