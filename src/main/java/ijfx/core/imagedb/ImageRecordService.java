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

import ijfx.core.metadata.MetaDataSet;
import ijfx.service.IjfxService;
import java.io.File;
import java.util.Collection;
import java.util.function.Predicate;

/**
 *
 * @author cyril
 */
public interface ImageRecordService extends IjfxService{
    
    
    public boolean isPresent(File file);
    
    // add a single record to the database
    public void addRecord(ImageRecord imageRecord);
    
    // the database never returns record because every analysis
    // is done asynchronisily in a separate thread
    public void addRecord(File file);
    
    // adds a record manually
    public void addRecord(File file, MetaDataSet metaDataSet);
   
    // get all the record
    public Collection<? extends ImageRecord> getRecords();
    
    // query a list of record using the following condition
    public Collection<? extends ImageRecord> queryRecords(Predicate<ImageRecord> query);
    
    public Collection<? extends ImageRecord> getRecordsFromDirectory(File file);
    
}
