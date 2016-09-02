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
package ijfx.plugins.commands;

import java.util.List;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.autoscale.AutoscaleService;
import net.imagej.autoscale.DataRange;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.axis.CalibratedAxis;
import static net.imagej.plugins.commands.binary.Binarize.WHITE;
import net.imagej.threshold.ThresholdMethod;
import net.imagej.threshold.ThresholdService;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.display.ColorTable8;
import net.imglib2.histogram.Histogram1d;
import net.imglib2.histogram.Real1dBinMapper;
import net.imglib2.img.Img;
import net.imglib2.img.cell.AbstractCellImg;
import net.imglib2.ops.pointset.HyperVolumePointSet;
import net.imglib2.ops.pointset.PointSet;
import net.imglib2.ops.pointset.PointSetIterator;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.command.DynamicCommand;
import org.scijava.menu.MenuConstants;
import org.scijava.module.MutableModuleItem;
import org.scijava.plugin.Attr;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = Command.class, initializer = "init", menu = {
	@Menu(label = MenuConstants.PROCESS_LABEL,
		weight = MenuConstants.PROCESS_WEIGHT,
		mnemonic = MenuConstants.PROCESS_MNEMONIC),
	@Menu(label = "Binary", mnemonic = 'b'), @Menu(label = "Binarize (FX)...") },
	headless = true, attrs = { @Attr(name = "no-legacy") })
public class Binarize<T extends RealType<T>> extends DynamicCommand {

    public static final String INSIDE = "Inside threshold";
    public static final String OUTSIDE = "Outside threshold";
    public static final String WHITE = "White";
    public static final String BLACK = "Black";
    public static final String DEFAULT_METHOD = "Default";

    @Parameter(label = "Method")
    String thresholdMethod = "Default";

    @Parameter
    ThresholdService thresholdService;

    @Parameter(type = ItemIO.INPUT)
    Dataset dataset;

    @Parameter(type = ItemIO.OUTPUT)
    Dataset output;

    @Parameter(label = "Threshold each plane")
    private boolean thresholdEachPlane = false;

    @Parameter
    AutoscaleService autoscaleSrv;

    @Parameter
    DatasetService datasetSrv;

    @Parameter
    CommandService commandService;

    ThresholdMethod method;

    String maskPixels = INSIDE;

    public void init() {
        MutableModuleItem<String> input
                = getInfo().getMutableInput("thresholdMethod", String.class);
        
        List<String> choices = thresholdService.getThresholdMethodNames();
        
        input.setChoices(choices);
    }

    @Override
    public void run() {

        Dataset inputData = dataset;

        Dataset inputMask = null;

        method = thresholdService.getThresholdMethod(thresholdMethod);

        long[] dims = Intervals.dimensionsAsLongArray(inputData);

        CalibratedAxis[] axes = new CalibratedAxis[dims.length];
        inputData.axes(axes);
        AxisType[] types = new AxisType[dims.length];
        for (int i = 0; i < dims.length; i++) {
            types[i] = axes[i].type();
        }
        Dataset mask
                = datasetSrv.create(new BitType(), dims,
                        "Mask", types, isVirtual(inputData));
        mask.setAxes(axes);
        RandomAccess<BitType> maskAccessor
                = (RandomAccess<BitType>) mask.getImgPlus().randomAccess();
        RandomAccess<? extends RealType<?>> dataAccessor
                = inputData.getImgPlus().randomAccess();
        DataRange minMax = calcDataRange(inputData);
        Histogram1d<T> histogram = null;
        boolean testLess = maskPixels.equals(INSIDE);
        DoubleType val = new DoubleType();
        // TODO - use Views and Cursors instead of PointSets and RandomAccess?
        // Better performance? Especially for CellImgs?
        if (thresholdEachPlane && planeCount(inputData) > 1) {
            // threshold each plane separately
            long[] planeSpace = planeSpace(inputData);
            PointSetIterator pIter = new HyperVolumePointSet(planeSpace).iterator();
            while (pIter.hasNext()) {
                long[] planePos = pIter.next();
                histogram = buildHistogram(inputData, planePos, minMax, histogram);
                double cutoffVal = cutoff(histogram, method, testLess, val);
                PointSet planeData = planeData(inputData, planePos);
                PointSetIterator iter = planeData.iterator();
                while (iter.hasNext()) {
                    updateMask(iter.next(), testLess, cutoffVal, dataAccessor,
                            maskAccessor);
                }
            }
        } else { // threshold entire dataset once
            histogram = buildHistogram(inputData, null, minMax, null);
            double cutoffVal = cutoff(histogram, method, testLess, val);
            PointSet fullData = fullData(dims);
            PointSetIterator iter = fullData.iterator();
            while (iter.hasNext()) {
                updateMask(iter.next(), testLess, cutoffVal, dataAccessor, maskAccessor);
            }
        }
        dataset.setImgPlus(mask.getImgPlus());
        
        output = dataset;
        output.update();
        assignColorTables(output);

    }

    private DataRange calcDataRange(Dataset ds) {
        return autoscaleSrv.getDefaultIntervalRange(ds.getImgPlus());
    }

    private boolean isVirtual(Dataset ds) {
        final Img<?> img = ds.getImgPlus().getImg();
        return AbstractCellImg.class.isAssignableFrom(img.getClass());
    }

    // returns the number of planes in a dataset
    private long planeCount(Dataset ds) {
        long count = 1;
        for (int d = 0; d < ds.numDimensions(); d++) {
            AxisType type = ds.axis(d).type();
            if (type == Axes.X || type == Axes.Y) {
                continue;
            }
            count *= ds.dimension(d);
        }
        return count;
    }

    // returns the dimensions of the space that contains the planes of a dataset.
    // this is the dataset dims minus the X and Y axes.
    long[] planeSpace(Dataset ds) {
        long[] planeSpace = new long[ds.numDimensions() - 2];
        int i = 0;
        for (int d = 0; d < ds.numDimensions(); d++) {
            AxisType type = ds.axis(d).type();
            if (type == Axes.X || type == Axes.Y) {
                continue;
            }
            planeSpace[i++] = ds.dimension(d);
        }
        return planeSpace;
    }

    // calculates the histogram of a portion of the dataset. if planePos is null
    // the region is the entire dataset. Otherwise it is the single plane.
    private Histogram1d<T> buildHistogram(Dataset ds, long[] planePos,
            DataRange minMax, Histogram1d<T> existingHist) {
        long[] min = new long[ds.numDimensions()];
        long[] max = min.clone();
        int xIndex = ds.dimensionIndex(Axes.X);
        int yIndex = ds.dimensionIndex(Axes.Y);
        // TODO - figure out what to do when no X axis or no Y axis present
        int i = 0;
        for (int d = 0; d < ds.numDimensions(); d++) {
            if (planePos == null || d == xIndex || d == yIndex) {
                min[d] = 0;
                max[d] = ds.dimension(d) - 1;
            } else {
                // it's a plane dimension from within planePos
                min[d] = planePos[i];
                max[d] = planePos[i];
                i++;
            }
        }
        @SuppressWarnings("unchecked")
        Img<T> img = (Img<T>) ds.getImgPlus();
        IntervalView<T> view = Views.interval(img, min, max);
        IterableInterval<T> data = Views.iterable(view);
        final Histogram1d<T> histogram;
        if (existingHist == null) {
            histogram = allocateHistogram(ds.isInteger(), minMax);
        } else {
            existingHist.resetCounters();
            histogram = existingHist;
        }
        histogram.countData(data);
        return histogram;
    }

    // allocates a histogram after determining a good size
    private Histogram1d<T> allocateHistogram(boolean dataIsIntegral,
            DataRange dataRange) {
        double range = dataRange.getExtent();
        if (dataIsIntegral) {
            range++;
        }
        Real1dBinMapper<T> binMapper = null;
        // TODO - size of histogram affects speed of all autothresh methods
        // What is the best way to determine size?
        // Do we want some power of two as size? For now yes.
        final int maxBinCount = 16384;
        for (int binCount = 256; binCount <= maxBinCount; binCount *= 2) {
            if (range <= binCount) {
                binMapper
                        = new Real1dBinMapper<T>(dataRange.getMin(), dataRange.getMax(),
                                binCount, false);
                break;
            }
        }
        if (binMapper == null) {
            binMapper
                    = new Real1dBinMapper<T>(dataRange.getMin(), dataRange.getMax(),
                            maxBinCount, false);
        }
        return new Histogram1d<T>(binMapper);
    }

    // determines the data value that delineates the threshold point
    @SuppressWarnings({"unchecked", "rawtypes"})
    private double cutoff(Histogram1d<T> hist, ThresholdMethod thresholdMethod,
            boolean testLess, DoubleType val) {
        long threshIndex = thresholdMethod.getThreshold(hist);
        if (testLess) {
            hist.getUpperBound(threshIndex, (T) (RealType) val);
        } else {
            hist.getLowerBound(threshIndex, (T) (RealType) val);
        }
        return val.getRealDouble();
    }

    // updates the mask pixel values for which the data values are on the correct
    // side of the cutoff value.
    private void updateMask(long[] pos, boolean testLess, double cutoffVal,
            RandomAccess<? extends RealType<?>> dataAccessor,
            RandomAccess<BitType> maskAccessor) {
        dataAccessor.setPosition(pos);
        boolean partOfMask;
        final boolean fillFg = true;
        final boolean fillBg = true;
        if (testLess) {
            partOfMask = dataAccessor.get().getRealDouble() <= cutoffVal;
        } else { // test greater
            partOfMask = dataAccessor.get().getRealDouble() >= cutoffVal;
        }
        
		if (partOfMask) {
			if (fillFg) {
				maskAccessor.setPosition(pos);
				maskAccessor.get().set(true);
			}
		}
		else { // not part of mask
			if (fillBg) {
				maskAccessor.setPosition(pos);
				maskAccessor.get().set(false);
			}
		}
    }

    // returns a PointSet that represents the points in a plane of a dataset
    private PointSet planeData(Dataset ds, long[] planePos) {
        long[] pt1 = new long[ds.numDimensions()];
        long[] pt2 = new long[ds.numDimensions()];
        int i = 0;
        for (int d = 0; d < ds.numDimensions(); d++) {
            AxisType type = ds.axis(d).type();
            if (type == Axes.X || type == Axes.Y) {
                pt1[d] = 0;
                pt2[d] = ds.dimension(d) - 1;
            } else {
                pt1[d] = planePos[i];
                pt2[d] = planePos[i];
                i++;
            }
        }
        return new HyperVolumePointSet(pt1, pt2);
    }

    // returns a PointSet that represents all the points in a dataset
    private PointSet fullData(long[] dims) {
        return new HyperVolumePointSet(dims);
    }
    /*
    DynamicCommandInfo info;

    public DynamicCommandInfo getInfo() {
        if (info == null) {
            // NB: Create dynamic metadata lazily.
            final CommandInfo commandInfo = commandService.getCommand(getClass());
            info = new DynamicCommandInfo(commandInfo, getClass());
        }
        return info;
    }*/
    
    private void assignColorTables(Dataset ds) {
		ColorTable8 table = new ColorTable8();
		long planeCount = planeCount(ds);
		if (planeCount > Integer.MAX_VALUE) {
			// TODO: for now just set all color tables. Later: throw exception?
			planeCount = Integer.MAX_VALUE;
		}
		ds.initializeColorTables((int) planeCount);
		for (int i = 0; i < planeCount; i++) {
			ds.setColorTable(table, i);
		}
	}
}
