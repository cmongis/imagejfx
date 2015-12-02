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
package ijfx.core.project;
import ijfx.core.metadata.GenericMetaData;
import ijfx.core.metadata.MetaDataSet;
import java.io.File;
import loci.formats.ImageReader;
import ijfx.core.metadata.MetaDataSet;
import java.io.IOException;
import java.util.Hashtable;
import loci.formats.FormatException;
/**
 *
 * @author Cyril Quinton
 */
public class MetaDataExctractor {
    private ImageReader reader;
    
    public MetaDataExctractor(File file) throws FormatException, IOException {
        reader = new ImageReader();
        reader.setId(file.toString());
    }
    public MetaDataExctractor(String imagePath) throws FormatException, IOException {
        reader = new ImageReader();
        reader.setId(imagePath); 
    }
    public MetaDataSet getGlobalMetadata() {
        MetaDataSet metaDataSet = new MetaDataSet();
        Hashtable<String,Object> table = reader.getGlobalMetadata();
        for (String key : table.keySet()) {
            metaDataSet.put(new GenericMetaData(key, table.get(key)));
        }
        return metaDataSet;
    }
    
    
}
