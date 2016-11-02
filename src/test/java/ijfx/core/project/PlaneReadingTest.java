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

import ijfx.core.stats.IjfxStatisticService;
import ijfx.service.ImagePlaneService;
import ijfx.service.Timer;
import ijfx.service.TimerService;
import io.scif.services.DatasetIOService;
import java.io.File;
import java.io.IOException;
import junit.framework.Assert;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.junit.Test;
import org.scijava.plugin.Parameter;

/**
 *
 * @author Cyril MONGIS, 2016
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

    @Parameter
    IjfxStatisticService statsService;
    
    @Override
    protected Class[] getService() {
        return null;
    }

    //String file = "/Users/cyril/test_img/metadata/WellA06_Point0002_Seq0003.tif";
    //String file = "/Users/cyril/test_img/metadata/YDK60-1-1_R3D.dv";
    String file = "./src/test/resources/multidim.tif";
    long[] planePosition = new long[] {2,2,1};
    
    @Test
    public void SinglePlaneReading() throws IOException {
        
        //String file = "/Users/cyril/test_img/jasmin/Sec63cherry GFPPho8truncHDEL/Sec63cherry GFPPho8truncHDEL 3-4x 1 stack.tif";
        
        //String file = "/Users/cyril/test_img/metadata/WellA06_Point0002_Seq0003.tif";
        usedMemory("before dataset reading");
        Dataset dataset = imagePlaneService.extractPlane(new File(file), planePosition);
        usedMemory("after dataset reading");
        Assert.assertEquals(2,dataset.numDimensions());
        
        SummaryStatistics stats = statsService.getSummaryStatistics(dataset);
        System.out.println(stats);
        Assert.assertTrue(stats.getMean() > 0);
        
        
    }
    
    
    public void planeReadingBenchMark() throws IOException {

        //String file = "/Users/cyril/test_img/jasmin/Sec63cherry GFPPho8truncHDEL/Sec63cherry GFPPho8truncHDEL 3-4x 1 stack.tif";
        //String file = "./src/test/resources/multidim.tif";
        System.out.println("File found : " + new File(file).exists());
        Timer t = timerService.getTimer("plane reading");

        for (int i = 0; i != 20; i++) {

            resetMem();
            System.gc();

            usedMemory("after gc");

            //creatig the config
            // starting timer
            t.start();

            // dataset representing the opened image
            Dataset virtual = imagePlaneService.openVirtualDataset(new File(file));

            usedMemory("after opening the dataset");
            t.elapsed("virtual dataset opening");

            // dimension of the extracted plane
            //long[] copyDims = new long[]{virtual.dimension(0), virtual.dimension(1)};

            // creating an empty dataset for the copied plane
            Dataset copy = imagePlaneService.createEmptyPlaneDataset(virtual);

            usedMemory("after empty dataset creation");

            copy(virtual, copy, planePosition);

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
        long diff = mem - last;

        System.out.println(String.format("INFO: [memory] Used memory %s : %dM (+%dM)", text, mem, diff));
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
