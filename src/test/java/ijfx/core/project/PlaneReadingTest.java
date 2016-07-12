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

import ijfx.core.imagedb.MetaDataExtractionService;
import ijfx.service.ImagePlaneService;
import ijfx.service.Timer;
import ijfx.service.TimerService;
import ijfx.service.thumb.ThumbService;
import io.scif.MetadataLevel;
import io.scif.config.SCIFIOConfig;
import io.scif.services.DatasetIOService;
import java.io.File;
import java.io.IOException;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.junit.Test;
import org.scijava.plugin.Parameter;

/**
 *
 * @author cyril
 */
public class PlaneReadingTest extends BaseImageJTest {

    @Parameter
    DatasetIOService datasetIOService;

    @Parameter
    DatasetService datasetService;

    @Parameter
    TimerService timerService;

    @Parameter
    ImagePlaneService imagePlaneService;

       @Override
    protected Class[] getService() {
        return new Class[]{TimerService.class,DatasetIOService.class,MetaDataExtractionService.class,ImagePlaneService.class,ThumbService.class};
    }
    
    public void tiffPlaneReading() throws IOException {

        

        String file = "/Users/cyril/test_img/jasmin/Sec63cherry GFPPho8truncHDEL/Sec63cherry GFPPho8truncHDEL 3-4x 1 stack.tif";
        //String file = "./src/test/resources/multidim.tif";
        System.out.println("File found : " + new File(file).exists());
        Timer t = timerService.getTimer("plane reading");

        for (int i = 0; i != 100; i++) {
            
            resetMem();
            System.gc();
           
            usedMemory("after gc");
            
            //creatig the config
            SCIFIOConfig config = new SCIFIOConfig();
            config.imgOpenerSetComputeMinMax(false);
            config.imgOpenerSetOpenAllImages(false);
            config.imgOpenerSetImgModes(SCIFIOConfig.ImgMode.CELL,SCIFIOConfig.ImgMode.PLANAR);
            config.parserSetLevel(MetadataLevel.ALL);

            // starting timer
            t.start();
            
            
            // dataset representing the opened image
            Dataset virtual = datasetIOService.open(file, config);
            
            
            
            usedMemory("after opening the dataset");
            t.elapsed("virtual dataset opening");

            // dimension of the extracted plane
            long[] copyDims = new long[]{virtual.dimension(0), virtual.dimension(1)};
            
            
            // creating an empty dataset for the copied plane
            Dataset copy = imagePlaneService.createEmptyPlaneDataset(virtual);
            
          
            
            usedMemory("after empty dataset creation");
            
            copy(virtual, copy, new long[]{1, 2});
            
       
            
            usedMemory("after copy");
            t.elapsed("plane copy creation");

        }

        t.logAll();

    }

    static long last = 0;
    
    
    public void resetMem() {
        last = 0;
    }
    
    public void usedMemory(String text) {
        Runtime runtime = Runtime.getRuntime();
        long mem = (runtime.totalMemory() - runtime.freeMemory()) / 1000 / 1000;
        long diff = mem-last;
        
        System.out.println(String.format("INFO: [memory] Used memory %s : %dM (+%dM)",text,mem,diff));
        last = mem;
        
    }

    public <T extends RealType<T>> void copy(Dataset source, Dataset target, long[] position) {

        IntervalView<T> hyperSlice = (IntervalView<T>) Views.hyperSlice(source, 2, position[0]);

        for (int d = 1; d != position.length; d++) {
            hyperSlice = Views.hyperSlice(hyperSlice, 2, position[d]);
        }

        Cursor<T> cursor = hyperSlice.cursor();

        cursor.reset();

       
        RandomAccess<T> randomAccess = (RandomAccess<T>) target.randomAccess();
        while (cursor.hasNext()) {
            cursor.fwd();
            randomAccess.setPosition(cursor);
            randomAccess.get().set(cursor.get());
        }

    }

}
