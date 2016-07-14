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
import io.scif.MetadataLevel;
import io.scif.config.SCIFIOConfig;
import io.scif.services.DatasetIOService;
import java.io.File;
import java.io.IOException;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.axis.AxisType;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.realtransform.RealTransformRandomAccessible;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.apache.commons.lang.ArrayUtils;
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

    public Dataset extractPlane(MetaDataSet set) {
        return null;
    }

    public Dataset openVirtualDataset(File file) throws IOException {
         final SCIFIOConfig config = new SCIFIOConfig();

        // skip min/max computation
        config.imgOpenerSetComputeMinMax(false);

        // prefer planar array structure, for ImageJ1 and ImgSaver compatibility
        config.imgOpenerSetImgModes(SCIFIOConfig.ImgMode.PLANAR);
        config.parserSetLevel(MetadataLevel.ALL);
        
        return datasetIoService.open(file.getAbsolutePath(), config);
    }
    
    @Override
    public <T extends RealType<T>> Dataset extractPlane(File file, long[] nonPlanarPosition) throws IOException {

        Timer t = timerService.getTimer(this.getClass());
        
      
        
        
         SCIFIOConfig config = new SCIFIOConfig();
            config.imgOpenerSetComputeMinMax(false);
            config.imgOpenerSetOpenAllImages(false);
            config.imgOpenerSetImgModes(SCIFIOConfig.ImgMode.CELL,SCIFIOConfig.ImgMode.PLANAR);
            config.parserSetLevel(MetadataLevel.ALL);

            // starting timer
            t.start();
            
            
            // dataset representing the opened image
            Dataset virtual = datasetIoService.open(file.getAbsolutePath(), config);
            
            t.elapsed("virtual dataset opening");

            // creating an empty dataset for the copied plane
            Dataset copy = createEmptyPlaneDataset(virtual);
            copy(virtual, copy, nonPlanarPosition);
            
            return copy;
    
    }
    
    
    public Dataset openVirtual(File file, long[] nonPlanarPosition) throws IOException {
          SCIFIOConfig config = new SCIFIOConfig();
            config.imgOpenerSetComputeMinMax(false);
            config.imgOpenerSetOpenAllImages(false);
            config.imgOpenerSetImgModes(SCIFIOConfig.ImgMode.CELL,SCIFIOConfig.ImgMode.PLANAR);
            config.parserSetLevel(MetadataLevel.ALL);

        // dataset representing the opened image
        Dataset virtual = datasetIoService.open(file.getAbsolutePath(), config);

        return virtual;
    }
    
    
    public <T extends RealType<T>> Dataset extractThumb(File file, long[] nonPlanarCoordinates) throws IOException {
        Dataset virtual = openVirtual(file, nonPlanarCoordinates);
        
        return null;
        
    }

    public Dataset extractPlane(File file, int planeIndex) throws IOException{
         final SCIFIOConfig config = new SCIFIOConfig();

        // skip min/max computation
        config.imgOpenerSetComputeMinMax(false);

        // prefer planar array structure, for ImageJ1 and ImgSaver compatibility
        config.imgOpenerSetImgModes(SCIFIOConfig.ImgMode.CELL);
        config.imgOpenerSetRange(String.format("%d-%d",planeIndex,planeIndex));
        config.parserSetLevel(MetadataLevel.MINIMUM);
        
        config.forEach((key,value)->String.format("%s = %s",key,value.toString()));
        
        Dataset virtualDataset = datasetIoService.open(file.getAbsolutePath(), config);
       
        
        
        if(true) return virtualDataset;
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

       Dataset output = datasetService.create(dims, input.getName(), axisTypeList, input.getValidBits(), input.isSigned(), false);
       
       return output;
    }

    @Override
    public <T extends RealType<T>> Dataset isolatePlane(Dataset dataset, long[] position) {
        Dataset emptyDataset = createEmptyPlaneDataset(dataset);
        
        RandomAccess<T> randomAccessOrigin = (RandomAccess<T>) dataset.randomAccess();
        RandomAccess<T> randomAccessOutput = (RandomAccess<T>) emptyDataset.randomAccess();
        randomAccessOrigin.setPosition(position);

        randomAccessOrigin.setPosition(position);
        
        
        long width = dataset.dimension(0);
        long height = dataset.dimension(1);
        
        
        for (int i = 0; i < width; i++) {
            randomAccessOrigin.setPosition(i, 0);
            randomAccessOutput.setPosition(i, 0);

            for (int j = 0; j < height; j++) {
                randomAccessOrigin.setPosition(j, 1);
                randomAccessOutput.setPosition(j, 1);
                randomAccessOutput.get().set(randomAccessOrigin.get());
            }
        }
        
        return emptyDataset;
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
