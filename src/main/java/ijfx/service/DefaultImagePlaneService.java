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

import ijfx.core.metadata.MetaDataSet;
import ijfx.core.utils.DimensionUtils;
import ijfx.ui.main.ImageJFX;
import io.scif.Metadata;
import io.scif.MetadataLevel;
import io.scif.config.SCIFIOConfig;
import io.scif.img.ImgFactoryHeuristic;
import io.scif.img.cell.SCIFIOCellImgFactory;
import io.scif.services.DatasetIOService;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.axis.AxisType;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.ImgFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.scijava.Prioritized;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 *
 * @author cyril
 */
@Plugin(type = Service.class)
public class DefaultImagePlaneService extends AbstractService implements ImagePlaneService {

    @Parameter
    DatasetIOService datasetIoService;

    @Parameter
    DatasetService datasetService;

    @Parameter
    TimerService timerService;

    Logger logger = ImageJFX.getLogger();

    public Dataset extractPlane(MetaDataSet set) {
        return null;
    }

    @Override
    public Dataset openVirtualDataset(File file) throws IOException {

        Timer timer = timerService.getTimer(this.getClass());
        SCIFIOConfig config = new SCIFIOConfig();
        config.imgOpenerSetComputeMinMax(false);
        config.imgOpenerSetOpenAllImages(false);
        //config.imgOpenerSetImgModes(SCIFIOConfig.ImgMode.CELL,SCIFIOConfig.ImgMode.PLANAR);
        config.imgOpenerSetImgFactoryHeuristic(new CellImgFactoryHeuristic());
        config.parserSetLevel(MetadataLevel.MINIMUM);
        timer.start();
        Dataset open = datasetIoService.open(file.getAbsolutePath(), config);
        timer.elapsed("Virtual dataset opening");
        return open;
    }

    @Override
    public <T extends RealType<T>> Dataset extractPlane(File file, long[] nonPlanarPosition) throws IOException {

        Dataset virtual = openVirtualDataset(file);
        // creating an empty dataset for the copied plane

        return isolatePlane(virtual, DimensionUtils.planarToAbsolute(nonPlanarPosition));

    }

    public Dataset openVirtual(File file, long[] nonPlanarPosition) throws IOException {
        Dataset virtual = openVirtualDataset(file);

        Dataset plane = createEmptyPlaneDataset(virtual);
        copy(virtual, plane, nonPlanarPosition);

        return plane;
    }

    public <T extends RealType<T>> Dataset extractThumb(File file, long[] nonPlanarCoordinates) throws IOException {
        Dataset virtual = openVirtual(file, nonPlanarCoordinates);
        return null;
    }

    public Dataset extractPlane(File file, int planeIndex) throws IOException {
        final SCIFIOConfig config = new SCIFIOConfig();

        // skip min/max computation
        config.imgOpenerSetComputeMinMax(false);

        // prefer planar array structure, for ImageJ1 and ImgSaver compatibility
        config.imgOpenerSetImgModes(SCIFIOConfig.ImgMode.CELL);
        config.imgOpenerSetRange(String.format("%d-%d", planeIndex, planeIndex));
        config.parserSetLevel(MetadataLevel.MINIMUM);

        config.forEach((key, value) -> String.format("%s = %s", key, value.toString()));

        Dataset virtualDataset = datasetIoService.open(file.getAbsolutePath(), config);

        if (true) {
            return virtualDataset;
        }
        Dataset outputDataset = createEmptyPlaneDataset(virtualDataset);

        outputDataset.setPlane(0, virtualDataset.getPlane(planeIndex));
        return outputDataset;

    }

    public Dataset createEmptyPlaneDataset(Dataset input) {
        AxisType[] axisTypeList = new AxisType[2];


        long width = input.dimension(0);
        long height = input.dimension(1);
        long[] dims = new long[]{width, height};

        axisTypeList[0] = input.getImgPlus().axis(0).type();
        axisTypeList[1] = input.getImgPlus().axis(1).type();

        Dataset output = datasetService.create(dims, input.getName(), axisTypeList, input.getValidBits(), input.isSigned(), !input.isInteger());

        return output;
    }

    public Dataset createEmptyPlaneDataset(Dataset input, long width, long height) {
        AxisType[] axisTypeList = new AxisType[2];

        long[] dims = new long[]{width, height};

        axisTypeList[0] = input.getImgPlus().axis(0).type();
        axisTypeList[1] = input.getImgPlus().axis(1).type();

        Dataset output = datasetService.create(dims, input.getName(), axisTypeList, input.getValidBits(), input.isSigned(), false);

        return output;
    }

    @Override
    public <T extends RealType<T>> Dataset isolatePlane(Dataset dataset, long[] position) {

        Timer t = timerService.getTimer(this.getClass());
        t.start();
        
        if(position.length < dataset.numDimensions()) {
            position = DimensionUtils.planarToAbsolute(position);
        }
        
        Dataset emptyDataset = createEmptyPlaneDataset(dataset);

        RandomAccess<T> randomAccessOrigin = (RandomAccess<T>) dataset.randomAccess();
        RandomAccess<T> randomAccessOutput = (RandomAccess<T>) emptyDataset.randomAccess();
        randomAccessOrigin.setPosition(position);

        randomAccessOrigin.setPosition(position);

        long width = dataset.dimension(0);
        long height = dataset.dimension(1);

        for (int i = 0; i < height; i++) {
            randomAccessOrigin.setPosition(i, 1);
            randomAccessOutput.setPosition(i, 1);

            for (int j = 0; j < width; j++) {
                randomAccessOrigin.setPosition(j, 0);
                randomAccessOutput.setPosition(j, 0);
                randomAccessOutput.get().set(randomAccessOrigin.get());
            }
        }
        t.elapsed("plane isolation");
        return emptyDataset;
    }

    public <T extends RealType<T>> void copy(Dataset source, Dataset target, long[] nonSpacialPosition) {

        Cursor<T> cursor;

        if (nonSpacialPosition.length == 0) {
            cursor = (Cursor<T>) source.cursor();
        } else {

            IntervalView<T> hyperSlice = (IntervalView<T>) Views.hyperSlice(source, 2, nonSpacialPosition[0]);

            for (int d = 1; d != nonSpacialPosition.length; d++) {
                hyperSlice = Views.hyperSlice(hyperSlice, 2, nonSpacialPosition[d]);
            }

            cursor = hyperSlice.cursor();
        }
        cursor.reset();

        RandomAccess<T> randomAccess = (RandomAccess<T>) target.randomAccess();
        while (cursor.hasNext()) {
            cursor.fwd();

            randomAccess.setPosition(cursor);
            randomAccess.get().set(cursor.get());
        }

    }

    @Override
    public <T extends RealType<T>> IntervalView<T> planeView(Dataset source, long[] position) {

        int srcNumDimension = source.numDimensions();

        if (position.length + 2 < srcNumDimension) {
            if (logger != null) {
                logger.warning("position incompatible with source. Correcting.");
            }

            long[] correctedPosition = new long[srcNumDimension - 2];
            System.arraycopy(position, 0, correctedPosition, 0, position.length);
            position = correctedPosition;

        }

        if (position.length <= 0) {
            return (IntervalView<T>) Views.translate(source, 0, 0);
        }
        IntervalView<T> hyperSlice = (IntervalView<T>) Views.hyperSlice(source, 2, position[0]);
        if (position.length == 1) {
            return hyperSlice;
        }
        for (int d = 1; d != position.length; d++) {
            hyperSlice = Views.hyperSlice(hyperSlice, 2, position[d]);
        }
        return hyperSlice;
    }

    @Override
    public <T extends RealType<T>> RandomAccessibleInterval<T> openVirtualPlane(File file, long[] nonSpacialPosition) throws IOException {

        Dataset dataset = openVirtualDataset(file);
        return planeView(dataset, nonSpacialPosition);

    }


    @Override
    public <T extends RealType<T>> IntervalView<T> plane(RandomAccessibleInterval<T> source, long[] position) {
        
        int srcNumDimension = source.numDimensions();
        int positionLength = position.length;
        
        if(srcNumDimension == positionLength) {
            logger.warning(String.format("An absolute dimension (%d-d) was given instead of a planar dimension (%d-d)",srcNumDimension,srcNumDimension-2));
            position = DimensionUtils.absoluteToPlanar(position);
        }

        if (position.length <= 0) {
            return (IntervalView<T>) Views.translate(source, 0, 0);
        }
        IntervalView<T> hyperSlice = (IntervalView<T>) Views.hyperSlice(source, 2, position[0]);
        if (position.length == 1) {
            return hyperSlice;
        }
        for (int d = 1; d != position.length; d++) {
            hyperSlice = Views.hyperSlice(hyperSlice, 2, position[d]);
        }
        return hyperSlice;
        
        
    }

  

}
