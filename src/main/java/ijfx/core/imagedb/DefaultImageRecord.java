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
package ijfx.core.imagedb;

import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import ijfx.core.metadata.MetaDataSet;
import java.io.File;
import java.util.Date;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class DefaultImageRecord implements ImageRecord {

    private File file;
    private MetaDataSet metadataset;
    private RecordStatus status;
    private Date lastStatusChange;

    public DefaultImageRecord() {
        
    }
    
    public DefaultImageRecord(File file, MetaDataSet metadataset) {
        setFile(file);
        setMetadataset(metadataset);
    }

   
    @JsonGetter("file")
    @Override
    public File getFile() {
        return file;
    }

    @JsonGetter("metadataset")
    @Override
    public MetaDataSet getMetaDataSet() {
        return metadataset;
    }

    @JsonGetter("status")
    @Override
    public RecordStatus getLastStatus() {
        return status;
    }

    @JsonGetter("lastStatusChange")
    @Override
    public Date getLastStatusChange() {
        return lastStatusChange;
    }

    @JsonSetter("status")
    public DefaultImageRecord setStatus(RecordStatus status) {
        if (status != null && this.status != status) {
            lastStatusChange = new Date(System.currentTimeMillis());
        }
        this.status = status;
        return this;
    }

    @JsonGetter("metadataset")
    public void setMetadataset(MetaDataSet metadataset) {
        this.metadataset = metadataset;
    }

    @JsonSetter("file")
    public void setFile(File file) {
        this.file = file;
    }

    @JsonSetter("lastStatusChange")
    
    public void setLastStatusChange(Date lastChange) {
        this.lastStatusChange = lastChange;
    }
    
    

}
