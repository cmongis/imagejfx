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

import ijfx.core.imagedb.ImageRecordService;
import ijfx.core.imagedb.MetaDataExtractionService;
import org.junit.Assert;
import org.junit.Test;
import org.scijava.plugin.Parameter;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class ExampleTest extends BaseImageJTest{

    
    @Parameter
    MetaDataExtractionService service;
    
    @Parameter
    ImageRecordService imageRecordService;
    
    @Override
    protected Class[] getService() {
        return new Class[]{
            ImageRecordService.class
                };
    }
    
    @Test
    public void testInjection() {
        Assert.assertNotNull(imageRecordService);
        Assert.assertNotNull(service);
    }
    
    
}
