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
package ijfx.service;

import com.sun.org.apache.xalan.internal.xsltc.util.IntegerArray;
import ijfx.core.utils.DimensionUtils;
import ijfx.ui.main.ImageJFX;
import io.scif.FormatException;
import io.scif.ImageMetadata;
import io.scif.Metadata;
import io.scif.SCIFIO;
import io.scif.bf.BioFormatsFormat;
import io.scif.config.SCIFIOConfig;
import io.scif.img.ImgIOException;
import io.scif.img.ImgOpener;
import io.scif.img.Range;
import io.scif.img.SCIFIOImgPlus;
import io.scif.services.DatasetIOService;
import io.scif.services.DefaultDatasetIOService;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.axis.CalibratedAxis;
import net.imagej.axis.DefaultLinearAxis;
import net.imagej.ops.OpService;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.apache.commons.lang.ArrayUtils;
import org.scijava.Priority;
import org.scijava.app.StatusService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.Service;
import org.scijava.util.IntArray;

/**
 *
 * @author Cyril MONGIS, 2016
 */
@Plugin(type = Service.class, priority = Priority.HIGH_PRIORITY)
public class IjfxDatasetIOService extends DefaultDatasetIOService implements IjfxService, DatasetIOService {

    @Parameter
    DatasetService datasetService;

    @Parameter
    OpService opService;

    Logger logger = ImageJFX.getLogger();

    @Parameter
    StatusService statusService;

    @Parameter
    TimerService timerService;

    SCIFIO scifio;

    public void initialize() {
        super.initialize();
        scifio = new SCIFIO(getContext());
    }

    @Override
    public Dataset open(String source) throws IOException {

        SCIFIOConfig config = new SCIFIOConfig();
        config.groupableSetGroupFiles(false);

        statusService.showStatus("Checking image metadata...");
        Metadata parse =  null;

        try {
            parse = scifio.format().getFormat(source).createParser().parse(source);
        } catch (Exception e) {
            try {
                parse = scifio.format().getFormatFromClass(BioFormatsFormat.class).createParser().parse(source);
            } catch (FormatException fe) {
                ImageJFX.getLogger().log(Level.SEVERE, source, fe);
            }
        }

        if (parse == null) {
            throw new IOException(source);
        }

        if (parse.getImageCount() == 1) {
            return super.open(source);
        } else {

            config.imgOpenerSetOpenAllImages(true);
            config.imgOpenerSetRange(new Range((long)0, new Long(parse.getImageCount()-1)));
            return open(source, config);

        }

    }

    @Override
    public Dataset open(String source, SCIFIOConfig config) throws IOException {

        if (config.imgOpenerIsOpenAllImages()) {
            try {
                
                Range range = config.imgOpenerGetRange();
                
                if(range.size() == 0) {
                    throw new IllegalArgumentException("The range is empty. No image can be opened");
                }
                
                
                int[] toOpen = range.stream()
                        .mapToInt(l->l.intValue())
                        .toArray();
               
                
                return new ParallelOpener(source, toOpen).getDataset();
            } catch (ImgIOException ex) {
                Logger.getLogger(IjfxDatasetIOService.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        } else {
            return super.open(source, config);
        }

    }

    private <T extends NativeType<T> & RealType<T>> Dataset concatenate(SCIFIOImgPlus<T>... openImgs) {
        // RandomAccessibleInterval<T> stack = Views.stack(openImgs);

        SCIFIOImgPlus<T> ref = openImgs[0];

        // creating a dataset containing all the images in a new axis
        //  
        long[] srcDim = DimensionUtils.getDimension(openImgs[0]);
        int newDimLength = srcDim.length + 1;
        int newDimPosition = newDimLength - 1;
        long[] targetDim = new long[newDimLength];

        // imported all the axes from the reference
        CalibratedAxis[] axes = new CalibratedAxis[newDimLength];
        CalibratedAxis[] srcAxes = new CalibratedAxis[newDimLength - 1];
        ref.axes(srcAxes);
        System.arraycopy(srcAxes, 0, axes, 0, newDimLength - 1);
        System.arraycopy(srcDim, 0, targetDim, 0, newDimLength - 1);
        // creating a new axis for series 
        axes[newDimPosition] = new DefaultLinearAxis(Axes.get("Series"));
        targetDim[newDimPosition] = targetDim.length;

        // retrieving the AxisTypes
        AxisType[] types = Stream
                .of(axes)
                .map(axe -> axe.type())
                .toArray(s -> new AxisType[s]);

        // creating the dataset
        Dataset dataset = datasetService.create(ref.firstElement(), targetDim, ref.getName(), types);

        // copying the data
        for (int i = 0; i != openImgs.length; i++) {

            IntervalView target = Views.hyperSlice(dataset, newDimPosition, i);
            opService.copy().rai(target, openImgs[i]);

        }

        //dataset.setName(openImgs[0].getName());
        dataset.setSource(openImgs[0].getSource());
        dataset.setAxes(axes);

        return dataset;

    }

    private void updateDataset(final Dataset dataset,
            final ImageMetadata imageMeta) {
        // If the original image had some level of merged channels, we should set
        // RGBmerged to true for the sake of backwards compatibility.
        // See https://github.com/imagej/imagej-legacy/issues/104

        // Look for Axes.CHANNEL in the planar axis list. If found, set RGBMerged to
        // true.
        boolean rgbMerged = false;

        for (final CalibratedAxis axis : imageMeta.getAxesPlanar()) {
            if (axis.type().equals(Axes.CHANNEL)) {
                rgbMerged = true;
            }
        }

        dataset.setRGBMerged(rgbMerged);

    }

    private class ParallelOpener {

        final String path;

        final int[] toOpen;

        ImgOpener imageOpener = new ImgOpener(getContext());

        Dataset dataset;

        SCIFIOConfig config;

        long[] targetDim;

        int newDimPosition;

        Consumer<Integer> updateStatus;

        public ParallelOpener(String path, int[] toOpen) throws ImgIOException {

            Timer t = timerService.getTimer(this.getClass());
            this.path = path;
            if (toOpen == null) {
                this.toOpen = new int[]{0};
            } else {
                this.toOpen = toOpen;
            }

            config = new SCIFIOConfig();
            config.imgOpenerSetOpenAllImages(false);
            config.imgOpenerSetComputeMinMax(false);
            config.checkerSetOpen(true);
            config.groupableSetGroupFiles(false);

            updateStatus = i -> statusService.showStatus(i, this.toOpen.length, String.format("Opening serie %d/%d", i, this.toOpen.length));

            createDataset();

            IntStream.range(1, this.toOpen.length)
                    .parallel()
                    .forEach(this::loadAndCopy);

            t.elapsed("Total");
        }

        public Dataset getDataset() {
            return dataset;
        }

        private <T extends NativeType<T> & RealType<T>> void createDataset() throws ImgIOException {
            Timer t = timerService.getTimer(this.getClass());
            SCIFIOImgPlus<T> ref = (SCIFIOImgPlus<T>) openImg(path, toOpen[0]);

            long[] srcDim = DimensionUtils.getDimension(ref);
            int newDimLength = srcDim.length + 1;
            newDimPosition = newDimLength - 1;
            targetDim = new long[newDimLength];

            // imported all the axes from the reference
            CalibratedAxis[] axes = new CalibratedAxis[newDimLength];
            CalibratedAxis[] srcAxes = new CalibratedAxis[newDimLength - 1];
            ref.axes(srcAxes);
            System.arraycopy(srcAxes, 0, axes, 0, newDimLength - 1);
            System.arraycopy(srcDim, 0, targetDim, 0, newDimLength - 1);
            // creating a new axis for series 
            axes[newDimPosition] = new DefaultLinearAxis(Axes.get("Series"));
            targetDim[newDimPosition] = toOpen.length;

            // retrieving the AxisTypes
            AxisType[] types = Stream
                    .of(axes)
                    .map(axe -> axe.type())
                    .toArray(s -> new AxisType[s]);

            // creating the dataset
            dataset = datasetService.create(ref.firstElement(), targetDim, ref.getName(), types);
            updateStatus.accept(1);
            loadInto(ref, 0);
            t.elapsed("dataset creation");
        }

        private <T extends NativeType<T> & RealType<T>> void loadAndCopy(int i) {
            try {

                updateStatus.accept(i);
                loadInto((SCIFIOImgPlus<T>) openImg(path, toOpen[i]), i);
            } catch (ImgIOException ioe) {
                logger.log(Level.SEVERE, "Error when reading image", ioe);
            }
        }

        private synchronized <T extends NativeType<T> & RealType<T>> SCIFIOImgPlus<T> openImg(String path, int i) throws ImgIOException {
            Timer t = timerService.getTimer(this.getClass());
            config.imgOpenerSetIndex(i);
            SCIFIOImgPlus<T> openImg = (SCIFIOImgPlus<T>) new ImgOpener(getContext()).openImg(path, config);
            t.elapsed("image opening");
            return openImg;
        }

        private <T extends NativeType<T> & RealType<T>> void loadInto(SCIFIOImgPlus<T> img, int position) {

            Timer t = timerService.getTimer(this.getClass());
            IntervalView target = Views.hyperSlice(dataset, newDimPosition, position);
            opService.copy().rai(target, img);
            t.elapsed("pixel copy");
        }

    }

}
