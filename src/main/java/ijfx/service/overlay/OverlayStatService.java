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
import java.util.HashMap;
import java.util.logging.Logger;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.ImageJService;
import net.imagej.display.ImageDisplay;
import net.imagej.display.OverlayService;
import net.imagej.measure.StatisticsService;

import net.imagej.ops.OpService;
import net.imagej.overlay.Overlay;
import net.imglib2.Cursor;
import net.imglib2.ops.pointset.HyperVolumePointSet;
import net.imglib2.ops.pointset.PointSet;
import net.imglib2.ops.pointset.PointSetIterator;
import net.imglib2.ops.pointset.RoiPointSet;
import net.imglib2.type.numeric.RealType;
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


        //final PointSet ps = getRegion(imageDisplay, overlay);
        final Dataset ds = datasetService.getDatasets(imageDisplay).get(0);

        for (int i = 0; i != imageDisplay.numDimensions(); i++) {
            // overlay.setAxis(imageDisplay.axis(i), imageDisplay.getIntPosition(i));
        }

        RoiPointSet rps = new RoiPointSet(overlay.getRegionOfInterest());

        HashMap<String, ParallelMeasurement> measures = new HashMap<>();
        HashMap<String, Double> stats = new HashMap<>();


        DescriptiveStatistics statistics = new DescriptiveStatistics();

        PointSetIterator c = rps.cursor();

        Cursor<? extends RealType<?>> dsc = ds.getImgPlus().cursor();

        measures.put(LBL_MIN, () -> statService.minimum(ds, rps));
        measures.put(LBL_MAX, () -> statService.maximum(ds, rps));
        measures.put(LBL_MEAN, () -> {
            return statService.arithmeticMean(ds, rps);
        });
        measures.put("Std. Dev.", () -> statService.populationStdDev(ds, rps));
        measures.put("Variance", () -> statService.populationVariance(ds, rps));
        measures.put(LBL_MEDIAN, () -> statService.median(ds, rps));

        measures.put(LBL_AREA, () -> statService.geometricMean(ds, rps));
        measures.put("Mid point", () -> statService.midpoint(ds, rps));
        measures.put("Product", () -> statService.product(ds, rps));
        measures.put("Pop. Kurtosis", () -> statService.populationKurtosis(ds, rps));
        measures.put("Pop. Skew", () -> statService.populationSkew(ds, rps));

        
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
