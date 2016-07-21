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
import java.io.File;
import org.junit.Assert;
import org.junit.Test;
import org.scijava.plugin.Parameter;
import ijfx.core.imagedb.MetaDataExtractionService;
import ijfx.service.ImagePlaneService;
import ijfx.service.Timer;
import ijfx.service.TimerService;
import ijfx.service.thumb.ThumbService;
import io.scif.MetadataLevel;
import io.scif.config.SCIFIOConfig;
import io.scif.services.DatasetIOService;
import java.io.IOException;
import java.util.List;
import net.imagej.Dataset;
import net.imagej.DatasetService;

/**
 *
 * @author cyril
 */
public class MetadataExtractionTest extends BaseImageJTest {

    @Parameter
    MetaDataExtractionService extractorService;

    @Parameter
    DatasetIOService datasetIoService;

    @Parameter
    DatasetService datasetService;

    @Parameter
    TimerService timerService;

    @Parameter
    ImagePlaneService imagePlaneService;
    
    @Parameter
    ThumbService thumberService;
    
    private static File testFile = new File("./src/test/resources/multidim.tif");
    
    
    @Override
    protected Class[] getService() {
        return new Class[]{TimerService.class,DatasetIOService.class,MetaDataExtractionService.class,ImagePlaneService.class,ThumbService.class};
    }
    
   // @Test
    public void testTiffFile() {

    
        File f = testFile;
        if (f.exists() == false) {
            System.out.println("File doesn't exist... skipping test");
            return;
        } else {
            System.out.println("File exists, beginning test.");
        }
        MetaDataSet metadataset = extractorService.extractMetaData(f);
        System.out.println(metadataset);
        Assert.assertSame("Channel number", metadataset.get(MetaData.CHANNEL_COUNT).getIntegerValue(), 3);
        
      
    }
    
   
    
 
    public void testTransformation() throws IOException {
      
        File f = testFile;//OMG!!!!new File("/Users/cyril/test_img/jasmin/Sec63cherry GFPPho8truncHDEL/Sec63cherry GFPPho8truncHDEL 3-4x 1 stack.tif");//testFile;
        
//        f  = new File("/Users/cyril/test_img/jasmin/hello.png");
        MetaDataSet m = extractorService.extractMetaData(f);
        int channelCount = m.get(MetaData.CHANNEL_COUNT).getIntegerValue();
        int tCount = m.get(MetaData.TIME_COUNT).getIntegerValue();
        int zCount = m.get(MetaData.ZSTACK_NUMBER).getIntegerValue();
        System.out.println(m);
        List<MetaDataSet> mList = extractorService.extractPlaneMetaData(m);
        System.out.println(mList.get(0));
        
        Assert.assertEquals(tCount * zCount*channelCount, mList.size());
        Assert.assertEquals((long)mList.get(mList.size()-1).get(MetaData.PLANE_INDEX).getIntegerValue(),mList.size()-1);
        
        
        //thumberService.getThumb(f, 14, 100, 100);
    }

   
    public void testPlaneExtraction() throws IOException {
     

        Timer t = timerService.getTimer("Dataset open benchmarking");
        //String testFile = "/Users/cyril/test_img/psfj/gfp_nikon/gfp_nikon1_2048x2048.tif";
        //String testFile = "/Users/cyril/test_img/jasmin/Sec63cherry GFPPho8truncHDEL/Sec63cherry GFPPho8truncHDEL 1x 1b.tif";
        String testFile = "/Users/cyril/test_img/jasmin/Sec63cherry GFPPho8truncHDEL/Sec63cherry GFPPho8truncHDEL 3-4x 1 stack.tif";
        
        if(new File(testFile).exists() == false){
            return;
        }
        //String testFile = MetadataExtractionTest.testFile.getPath();//OMG!!!!"/Users/cyril/test_img/jasmin/Sec63cherry GFPPho8truncHDEL/Sec63cherry GFPPho8truncHDEL 1x 1b.tif";
        for (int i = 0; i != 100; i++) {
            t.start();
            Dataset dataset = imagePlaneService.extractPlane(new File(testFile), new long[]{0,1});
            t.elapsed("Dataset reading"); 
        }
        t.logAll();

    }
    
    private  Dataset openVirtualDataset(String file) throws IOException{
        final SCIFIOConfig config = new SCIFIOConfig();
	
		// skip min/max computation
		config.imgOpenerSetComputeMinMax(false);
                
		// prefer planar array structure, for ImageJ1 and ImgSaver compatibility
		config.imgOpenerSetImgModes(SCIFIOConfig.ImgMode.CELL);
                config.parserSetLevel(MetadataLevel.MINIMUM);
                
                return datasetIoService.open(file, config);
                
    }

}
