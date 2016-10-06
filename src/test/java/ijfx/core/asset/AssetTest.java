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
package ijfx.core.asset;

import ijfx.core.assets.AssetService;
import ijfx.core.assets.DatasetAsset;
import ijfx.core.assets.FlatfieldAsset;
import ijfx.core.project.BaseImageJTest;
import io.scif.img.ImgUtilityService;
import io.scif.services.DatasetIOService;
import java.io.File;
import net.imagej.Dataset;
import org.junit.Assert;
import org.junit.Test;
import org.scijava.plugin.Parameter;

/**
 *
 * @author cyril
 */
public class AssetTest extends BaseImageJTest{

    @Parameter
    AssetService assertService;
    
    
    public void serviceLoaded() {
        Assert.assertNotNull(assertService);
    }
    /**
     * The Assert Service should return the same object for two different asset object
     */
    
    public void buffering() {
        DatasetAsset asset1 = new DatasetAsset(new File("./src/test/resources/multidim.tif"));
        DatasetAsset asset2 = new DatasetAsset(new File("./src/test/resources/multidim.tif"));
        
        Dataset first = assertService.load(asset1);
        Dataset second = assertService.load(asset2);
        
        Assert.assertEquals(first,second);    
    }
    
    
    /**
     *  Test that the the FlatfieldAssetLoader which handles flatfield assets
     *  has priority over the DatasetAssetLoader which handles all Dataset containing asssets
     */
    
    public void loaderPriority() {
        
        DatasetAsset datasetAsset = new DatasetAsset(new File("./src/test/resources/multidim.tif"));
        
        Dataset dataset = assertService.load(datasetAsset);
        
        FlatfieldAsset flatfieldAsset = new FlatfieldAsset(new File("./src/test/resources/multidim.tif"));
        
        Dataset flatfield = assertService.load(flatfieldAsset);
        
        Assert.assertNotSame(dataset, flatfield);
        
        Assert.assertTrue(dataset.getTypeLabelLong().contains("16"));
        Assert.assertTrue(flatfield.getTypeLabelLong().contains("32"));
        
        
    }
    
    
    @Override
    protected Class[] getService() {
        
        return new Class[] {AssetService.class};
    }
    
}
