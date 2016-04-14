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
package ijfx.core.project;

import ijfx.core.metadata.MetaData;
import ijfx.core.metadata.MetaDataSet;
import io.scif.util.FormatTools;
import java.io.File;
import org.junit.Assert;
import org.junit.Test;
import org.scijava.plugin.Parameter;
import ijfx.core.imagedb.MetaDataExtractionService;

/**
 *
 * @author cyril
 */
public class MetadataExtractionTest extends BaseSciJavaTest{
    
    @Parameter
    MetaDataExtractionService extractorService;
    
    @Test
    public void testTiffFile() {
       
        init();
        File f = new File("./src/test/resources/multidim.tif");
        if(f.exists() == false) {
            System.out.println("File doesn't exist... skipping test");
            return;
        }
        else {
            System.out.println("File exists, beginning test.");
        }
        MetaDataSet metadataset = extractorService.extractMetaData(f);
        
        Assert.assertSame("Channel number",metadataset.get(MetaData.CHANNEL_COUNT).getIntegerValue(),3);
        
    }
    
}
