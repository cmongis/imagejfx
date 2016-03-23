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
package ijfx.service.overlay;

import ijfx.ui.main.ImageJFX;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.ImageJService;
import net.imagej.display.ImageDisplay;
import net.imagej.display.OverlayService;
import net.imagej.measure.StatisticsService;

import net.imagej.ops.OpService;
import net.imagej.overlay.LineOverlay;
import net.imagej.overlay.Overlay;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.ops.pointset.HyperVolumePointSet;
import net.imglib2.ops.pointset.PointSet;
import net.imglib2.ops.pointset.PointSetIterator;
import net.imglib2.ops.pointset.RoiPointSet;
import net.imglib2.type.numeric.RealType;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = Service.class)
public class OverlayStatService extends AbstractService implements ImageJService {

    @Parameter
    private OverlayService overlayService;

    @Parameter
    private DatasetService datasetService;

    @Parameter
    private OpService opService;

    @Parameter
    private StatisticsService statService;

    
    
    final private Logger logger = ImageJFX.getLogger();

    public final static String LBL_MEAN = "Mean";
    public final static String LBL_MEDIAN = "Median";
    public final static String LBL_AREA = "Area";
    public final static String LBL_MAX = "Max";
    public final static String LBL_MIN = "Min";
    public final static String LBL_ = "";

    public HashMap<String, Double> getStat(ImageDisplay imageDisplay, Overlay overlay) {

       
         HashMap<String, ParallelMeasurement> measures = new HashMap<>();
          HashMap<String, Double> stats = new HashMap<>();
         DescriptiveStatistics statistics = new DescriptiveStatistics(ArrayUtils.toPrimitive(getValueList(imageDisplay, overlay)));
        
        measures.put(LBL_MIN, () -> statistics.getMin());
        measures.put(LBL_MAX, () -> statistics.getMax());
        measures.put(LBL_MEAN, () -> statistics.getMean());
        measures.put("Std. Dev.", () -> statistics.getStandardDeviation());
        measures.put("Variance", () -> statistics.getVariance());
        measures.put(LBL_MEDIAN, () -> statistics.getPercentile(50));
        
        //measures.put(LBL_AREA, () -> statService.geometricMean(ds, rps));
        

        
        for (String key : measures.keySet()) {

            logger.fine("Calculating " + key);
            ParallelMeasurement runnable = measures.get(key);
            Double value = runnable.measure();

            logger.fine(String.format("Calculated %s = %.0f", key, value));
            stats.put(key, value);
        };
        logger.fine("finished");
        return stats;
    }
    
    
    
    
    public Double[] getValueList(ImageDisplay imageDisplay, Overlay overlay) {
        
        
        System.out.println("Getting value list");
         if(overlay instanceof LineOverlay) return getValueList(imageDisplay,(LineOverlay)overlay);
        final Dataset ds = datasetService.getDatasets(imageDisplay).get(0);
        ArrayList<Double> values = new ArrayList<>(10000);
        RoiPointSet rps = new RoiPointSet(overlay.getRegionOfInterest());
       
        HashMap<String, ParallelMeasurement> measures = new HashMap<>();
        HashMap<String, Double> stats = new HashMap<>();
                 
    

        PointSetIterator psc = rps.cursor();
        RandomAccess<RealType<?>> randomAccess = ds.randomAccess();
        
        
        long[] position = new long[imageDisplay.numDimensions()];
        imageDisplay.localize(position);
       
        psc.reset();
        int c = 0;
        while(psc.hasNext()) {
            psc.fwd();
            long[] roiPosition = psc.get();
            
            for(int i =0;i!=roiPosition.length;i++) {
                position[i] = roiPosition[i];
            }
            randomAccess.setPosition(position);
            values.add(randomAccess.get().getRealDouble());
            c++;
        }
        System.out.printf("%d values retrieved\n",c);
        return values.toArray(new Double[values.size()]);
        
    }
    
    public Double[] getValueList(ImageDisplay imageDisplay, LineOverlay overlay) {
        
        
        
        final Dataset ds = datasetService.getDatasets(imageDisplay).get(0);
        
        System.out.printf("Num dimensions %d\n",overlay.numDimensions());
        
         RandomAccess<RealType<?>> randomAccess = ds.randomAccess();
         
         
         
         int x0 = new Double(overlay.getLineStart(0)).intValue();
         int y0 = new Double(overlay.getLineStart(1)).intValue();
         int x1 = new Double(overlay.getLineEnd(0)).intValue();
         int y1 = new Double(overlay.getLineEnd(1)).intValue();
         
         List<int[]> pixels = Bresenham.findLine(x0, y0, x1, y1);
         
         
         long[] position = new long[imageDisplay.numDimensions()];
         Double[] values = new Double[pixels.size()];
         
         int i = 0;
         for(int[] coordinate : pixels) {
             position[0] = coordinate[0];
             position[1] = coordinate[1];
             randomAccess.setPosition(position);
             
             values[i] = randomAccess.get().getRealDouble();
             i++;
         }
         
        
        
        return values;
    }
    
    /*
    private int[][] traceLine(int x0, int y0, int x1, int y1) {
        
    
        
    
    }*/

    private interface ParallelMeasurement {

        public Double measure();
    }

    public class OverlayStats extends HashMap<String, Double> {

    }

    private PointSet getRegion(ImageDisplay display, Overlay overlay) {
        if (overlay != null) {
            return new RoiPointSet(overlay.getRegionOfInterest());
        }
        long[] pt1 = new long[display.numDimensions()];
        long[] pt2 = new long[display.numDimensions()];
        // current plane only
        pt1[0] = 0;
        pt1[1] = 0;
        pt2[0] = display.dimension(0) - 1;
        pt2[1] = display.dimension(1) - 1;
        for (int i = 2; i < display.numDimensions(); i++) {
            pt1[i] = pt2[i] = display.getLongPosition(i);
        }
        return new HyperVolumePointSet(pt1, pt2);
    }

}
