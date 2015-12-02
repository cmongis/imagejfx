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
package ijfx.ui.plugin.panel;

import static com.sun.javafx.image.impl.IntArgbPre.accessor;
import javafx.scene.control.Button;
import static javafx.scene.input.KeyCode.T;
import net.imagej.Dataset;
import net.imagej.axis.Axes;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.OverlayService;
import net.imagej.event.DatasetRestructuredEvent;
import net.imagej.event.DatasetUpdatedEvent;
import net.imagej.widget.HistogramBundle;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.histogram.DiscreteFrequencyDistribution;
import net.imglib2.histogram.Histogram1d;
import net.imglib2.histogram.Real1dBinMapper;
import net.imglib2.ops.pointset.HyperVolumePointSet;
import net.imglib2.ops.pointset.PointSetIterator;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.util.Intervals;
import org.scijava.ItemVisibility;
import org.scijava.event.EventHandler;
import org.scijava.plugin.Parameter;
import org.scijava.ui.UIService;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class HistogramModel {
    // -- fields that are Parameters --

    @Parameter
    private UIService uiService;

    @Parameter
    private ImageDisplayService imageDisplayService;

    @Parameter
    private OverlayService overlayService;

    @Parameter
    private ImageDisplay display;

    @Parameter(label = "Histogram", initializer = "initBundle")
    private HistogramBundle bundle;

    @Parameter(visibility = ItemVisibility.MESSAGE, persist = false)
    private String pixelsStr;

    @Parameter(visibility = ItemVisibility.MESSAGE, persist = false)
    private String minStr;

    @Parameter(visibility = ItemVisibility.MESSAGE, persist = false)
    private String maxStr;

    @Parameter(visibility = ItemVisibility.MESSAGE, persist = false)
    private String meanStr;

    @Parameter(visibility = ItemVisibility.MESSAGE, persist = false)
    private String stdDevStr;

    @Parameter(visibility = ItemVisibility.MESSAGE, persist = false)
    private String binsStr;

    @Parameter(visibility = ItemVisibility.MESSAGE, persist = false)
    private String binWidthStr;

    @Parameter(label = "Static", callback = "liveClicked")
    private Button liveButton;

    @Parameter(label = "List", callback = "listClicked")
    private Button listButton;

    @Parameter(label = "Copy", callback = "copyClicked")
    private Button copyButton;

    @Parameter(label = "Log", callback = "logClicked")
    private Button logButton;

    @Parameter(label = "Composite", callback = "chanClicked")
    private Button chanButton;

	// -- other fields --
    private Dataset dataset;
    private long channels;
    private Histogram1d[] histograms;
    private double[] means;
    private double[] stdDevs;
    private double[] mins;
    private double[] maxes;
    private double[] sum1s;
    private double[] sum2s;
    private long sampleCount;
    private double binWidth;
    private double dataMin;
    private double dataMax;
    private long binCount;
    private int currHistNum;
    private boolean liveUpdates = false;

	// -- public interface --
    public void setDisplay(ImageDisplay disp) {
        display = disp;
        dataset = imageDisplayService.getActiveDataset(display);
    }

    public ImageDisplay getDisplay() {
        return display;
    }

	// -- initializers --
    protected void initBundle() {
        build();
        bundle = new HistogramBundle(histograms[histograms.length - 1]);
        setValues(histograms.length - 1);
    }

	// -- callbacks --
    protected void copyClicked() {
		// TODO
        // In IJ1 this command copies the histogram into a two column text table
        // on the clipboard. The 1st column contains calibrated data values and
        // the second column contains counts.
        uiService.showDialog("To be implemented");
    }

    protected void listClicked() {
		// TODO
        // In IJ1 this command copies the histogram into a two column text table
        // as a results table. The 1st column contains calibrated data values and
        // the second column contains counts.
        uiService.showDialog("To be implemented");
    }

    protected void liveClicked() {
        liveUpdates = !liveUpdates;
		//final MutableModuleItem<Button> item =
        //	getInfo().getMutableInput("liveButton", Button.class);
        //item.setLabel(liveUpdates ? "Live" : "Static");
        if (liveUpdates) {
            liveUpdate(dataset);
        }
    }

    protected <T extends RealType<T>> void logClicked() {
        long maxCount = max(bundle.getHistogram(0).dfd());
		// TODO - replace prev line with
        // long maxCount = bundle.getHistogram(0).dfd().modeCount();
        double max = Math.log(maxCount);
        if (bundle.getHistogramCount() == 1) {
            Real1dBinMapper<T> mapper
                    = new Real1dBinMapper<T>(dataMin, dataMax, binCount, false);
            Histogram1d<T> hist = new Histogram1d<T>(mapper);
            long[] binPos = new long[1];
            for (int i = 0; i < binCount; i++) {
                binPos[0] = i;
                long count = bundle.getHistogram(0).dfd().frequency(binPos);
                long value = (long) (maxCount * Math.log(count) / max);
                setBinValue(hist.dfd(), binPos, value);
				// TODO - replace prev line with
                // hist.dfd().setFrequency(binPos, value);
            }
            bundle.setHistogram(1, hist);
        } else { // count == 2
            bundle.setHistogram(1, null);
        }
    }

	// TODO - this should be a capability of DFDs.
    private long max(DiscreteFrequencyDistribution dfd) {
        long max = -1;
        Cursor<LongType> cursor = dfd.cursor();
        while (cursor.hasNext()) {
            LongType val = cursor.next();
            if (val.get() > max) {
                max = val.get();
            }
        }
        return max;
    }

	// TODO - this should be a capability of DFDs. Right now horribly slow.
    private void setBinValue(DiscreteFrequencyDistribution dfd, long[] binPos,
            long value) {
        // reset to zero
        while (dfd.frequency(binPos) > 0) {
            dfd.decrement(binPos);
        }
        // set to new value
        for (long i = 0; i < value; i++) {
            dfd.increment(binPos);
        }
    }

	// -- EventHandlers --
        /*
     @EventHandler
     protected void onEvent(DatasetRestructuredEvent evt) {
     liveUpdate(evt.getObject());
     }

     @EventHandler
     protected void onEvent(DatasetUpdatedEvent evt) {
     liveUpdate(evt.getObject());
     }*/
	// -- private helpers --
    private void display(int histNumber) {
        int h = histNumber;
        if (h >= histograms.length) {
            h = histograms.length - 1;
        }
        currHistNum = h;
        bundle.setHistogram(0, histograms[h]);
        setTitle(h);
        setValues(h);
		// TODO - refresh the ui panel? I think Live will not work unless we do.
        // Also maybe just if bundle.hasChanges() is true.
    }

    private void setValues(int histNumber) {
        pixelsStr = formatStr("Pixels", sampleCount / channels);
        minStr = formatStr("Min", mins[histNumber]);
        maxStr = formatStr("Max", maxes[histNumber]);
        meanStr = formatStr("Mean", means[histNumber]);
        stdDevStr = formatStr("Std Dev", stdDevs[histNumber]);
        binsStr = formatStr("Bins", binCount);
        binWidthStr = formatStr("Bin Width", binWidth);
    }

    private String formatStr(final String label, final long num) {
        return String.format("%12s:%10d", label, num);
    }

    private String formatStr(final String label, final double num) {
        return String.format("%12s:%10.2f", label, num);
    }

    /*
     * 7-18-2013 BDZ
     * This code left over from when this plugin was a Swing command. In Swing the
     * title would update on every click of the channel button. When converted to
     * generic command this ability was lost. SwingInputHarvester does not make a
     * link between the Module and the SwingDialog such that the Module could
     * reset the title of the dialog when things change. This is a thing to think
     * about implementing. Right now the title gets set once and never updates.
     * For now I am disabling this code so that it is not misleading.
     */
    private void setTitle(int histNum) {
        String title;
        if (histNum == histograms.length - 1) {
            title = "Composite histogram of ";
        } else {
            title = "Channel " + histNum + " histogram of ";
        }
        title += display.getName();
        /* Disabled
         getInfo().setLabel(title);
         */
    }

    private void calcBinInfo() {
        // calc the data ranges - 1st pass thru data
        dataMin = Double.POSITIVE_INFINITY;
        dataMax = Double.NEGATIVE_INFINITY;
        Cursor<? extends RealType<?>> cursor = dataset.getImgPlus().cursor();
        while (cursor.hasNext()) {
            double val = cursor.next().getRealDouble();
            if (val < dataMin) {
                dataMin = val;
            }
            if (val > dataMax) {
                dataMax = val;
            }
        }
        if (dataMin > dataMax) {
            dataMin = 0;
            dataMax = 0;
        }
        double dataRange = dataMax - dataMin;
        if (dataset.isInteger()) {
            dataRange += 1;
            if (dataRange <= 65536) {
                binCount = (long) dataRange;
                binWidth = 1;
            } else {
                binCount = 65536;
                binWidth = dataRange / binCount;
            }
        } else { // float dataset
            binCount = 1000;
            binWidth = dataRange / binCount;
        }
    }

	// NB : this plugin uses low level access. Histograms are
    // designed to be fed an iterable data source. But in the case of this
    // plugin we do direct computations on the histograms' bins for efficency
    // reasons (so we can calc stats from the same data). Histograms thus have
    // both a high level generic API and a low level nongeneric API.
    @SuppressWarnings("unchecked")
    private <T extends RealType<T>> void allocateDataStructures() {
        // initialize data structures
        int chIndex = dataset.dimensionIndex(Axes.CHANNEL);
        channels = (chIndex < 0) ? 1 : dataset.dimension(chIndex);
        histograms = new Histogram1d[(int) channels + 1]; // +1 for chan compos
        Real1dBinMapper<T> mapper
                = new Real1dBinMapper<T>(dataMin, dataMax, binCount, false);
        for (int i = 0; i < histograms.length; i++) {
            histograms[i] = new Histogram1d<T>(mapper);
        }
        means = new double[histograms.length];
        stdDevs = new double[histograms.length];
        sum1s = new double[histograms.length];
        sum2s = new double[histograms.length];
        mins = new double[histograms.length];
        maxes = new double[histograms.length];
        for (int i = 0; i < histograms.length; i++) {
            mins[i] = Double.POSITIVE_INFINITY;
            maxes[i] = Double.NEGATIVE_INFINITY;
        }
    }

    public void computeStats() {
        // calc stats - 2nd pass thru data
        int chIndex = dataset.dimensionIndex(Axes.CHANNEL);
        int composH = histograms.length - 1;
        RandomAccess<? extends RealType<?>> accessor = dataset.getImgPlus().randomAccess();
        long[] span = Intervals.dimensionsAsLongArray(dataset);
        if (chIndex >= 0) {
            span[chIndex] = 1; // iterate channels elsewhere
        }
        HyperVolumePointSet pixelSpace = new HyperVolumePointSet(span);
        PointSetIterator pixelSpaceIter = pixelSpace.iterator();
        sampleCount = 0;
        while (pixelSpaceIter.hasNext()) {
            long[] pos = pixelSpaceIter.next();
            accessor.setPosition(pos);
			// count values by channel. also determine composite pixel value (by
            // channel averaging)
            double composVal = 0;
            for (long chan = 0; chan < channels; chan++) {
                if (chIndex >= 0) {
                    accessor.setPosition(chan, chIndex);
                }
                double val = accessor.get().getRealDouble();
                composVal += val;
                long index = (long) ((val - dataMin) / binWidth);
                // NB in float case the max data point overflows the index range
                if (index >= binCount) {
                    index = binCount - 1;
                }
                int c = (int) chan;
                histograms[c].increment(index);
                sum1s[c] += val;
                sum2s[c] += val * val;
                if (val < mins[c]) {
                    mins[c] = val;
                }
                if (val > maxes[c]) {
                    maxes[c] = val;
                }
                sampleCount++;
            }
            composVal /= channels;
            long index = (long) ((composVal - dataMin) / binWidth);
            // NB in float case the max data point overflows the index range
            if (index >= binCount) {
                index = binCount - 1;
            }
            histograms[composH].increment(index);
            sum1s[composH] += composVal;
            sum2s[composH] += composVal * composVal;
            if (composVal < mins[composH]) {
                mins[composH] = composVal;
            }
            if (composVal > maxes[composH]) {
                maxes[composH] = composVal;
            }
        }
        // calc means etc.
        long pixels = sampleCount / channels;
        for (int i = 0; i < histograms.length; i++) {
            means[i] = sum1s[i] / pixels;
            stdDevs[i]
                    = Math.sqrt((sum2s[i] - ((sum1s[i] * sum1s[i]) / pixels)) / (pixels - 1));
        }
    }

    public void build() {
        dataset = imageDisplayService.getActiveDataset(display);
        calcBinInfo();
        allocateDataStructures();
        computeStats();
		// Maybe?
        // setValues(currHistNum);
    }

    private void liveUpdate(Dataset ds) {
        if (!liveUpdates) {
            return;
        }
        if (ds != dataset) {
            return;
        }
        build();
        bundle.setHasChanges(true);
        display(currHistNum);
    }

    public Histogram1d[] getHistogram() {
        return histograms;
    }

    public String getPixelsStr() {
        return pixelsStr;
    }

    public void setPixelsStr(String pixelsStr) {
        this.pixelsStr = pixelsStr;
    }

    public String getMinStr() {
        return minStr;
    }

    public void setMinStr(String minStr) {
        this.minStr = minStr;
    }

    public String getMaxStr() {
        return maxStr;
    }

    public void setMaxStr(String maxStr) {
        this.maxStr = maxStr;
    }

    public String getMeanStr() {
        return meanStr;
    }

    public void setMeanStr(String meanStr) {
        this.meanStr = meanStr;
    }

    public String getStdDevStr() {
        return stdDevStr;
    }

    public void setStdDevStr(String stdDevStr) {
        this.stdDevStr = stdDevStr;
    }

    public String getBinsStr() {
        return binsStr;
    }

    public void setBinsStr(String binsStr) {
        this.binsStr = binsStr;
    }

    public String getBinWidthStr() {
        return binWidthStr;
    }

    public void setBinWidthStr(String binWidthStr) {
        this.binWidthStr = binWidthStr;
    }

}
