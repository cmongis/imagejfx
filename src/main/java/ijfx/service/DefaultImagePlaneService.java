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
import io.scif.img.ImageRegion;
import io.scif.services.DatasetIOService;
import java.io.File;
import java.io.IOException;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.axis.AxisType;
import net.imglib2.RandomAccess;
import net.imglib2.type.numeric.RealType;
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

        Timer timer = timerService.getTimer(this.getClass());
        
        final SCIFIOConfig config = new SCIFIOConfig();
        config.imgOpenerSetComputeMinMax(false);
        config.imgOpenerSetOpenAllImages(false);
       
        // skip min/max computation
        config.imgOpenerSetComputeMinMax(false);

        // prefer planar array structure, for ImageJ1 and ImgSaver compatibility
        config.imgOpenerSetImgModes(SCIFIOConfig.ImgMode.CELL);
        config.parserSetLevel(MetadataLevel.MINIMUM);
        
        
        
         Dataset virtualDataset = datasetIoService.open(file.getAbsolutePath(), config);
         
         timer.elapsed("virtual plane opening");
         
        Dataset finalPlane =  isolatePlane(virtualDataset, DimensionUtils.nonPlanarToPlanar(nonPlanarPosition));
        timer.elapsed("pixel reading");
        
        
        timer.logAll();
        return finalPlane;
        /*
        long[] position = new long[nonPlanarPosition.length+2];
        //long[] dimemsions = new long[nonPlanarPosition.length+2];
         
        System.arraycopy(nonPlanarPosition, 0, position, 2, nonPlanarPosition.length);
        //System.arraycopy(dimLengths,0,dimemsions,2,dims.length);
        
        
        //config.imgOpenerSetRange("0");
        
        Dataset virtualDataset = datasetIoService.open(file.getAbsolutePath(), config);
        Dataset outputDataset = createEmptyPlaneDataset(virtualDataset);

        RandomAccess<T> inputCursor = (RandomAccess<T>) virtualDataset.randomAccess();
        RandomAccess<T> outputCursor = (RandomAccess<T>) outputDataset.randomAccess();
        System.out.println("dims before");
        System.out.println(ArrayUtils.toString(nonPlanarPosition));

        
        
        System.out.println(ArrayUtils.toString(position));
        System.arraycopy(nonPlanarPosition, 0, position, 2, nonPlanarPosition.length);
        System.out.println(ArrayUtils.toString(position));

        long width = outputDataset.dimension(0);
        long height = outputDataset.dimension(1);

        System.out.printf("width = %d, height = %d\n", width, height);
        Timer lineTimer = timerService.getTimer("LineTimer");
        Timer pixelTimer = timerService.getTimer("PixelTimer");
        long[] outputPosition = new long[2];
        
        
        
        for (long x = 0; x != width; x++) {
            for (long y = 0; y != height; y++) {
                pixelTimer.start();
                position[0] = x;
                position[1] = y;
                
                outputPosition[0] = x;
                outputPosition[1] = y;
                inputCursor.setPosition(position);

                outputCursor.setPosition(outputPosition);
                outputCursor.get().set(inputCursor.get());
                pixelTimer.measure("pixel reading");
            }
            lineTimer.measure("reading one line");
            
            System.out.println("line " + x);
        }
        pixelTimer.logAll();
        lineTimer.logAll();

        System.out.println("Dataset on the track !");
        return outputDataset;*/
    }

    public Dataset extractPlane(File file, int planeIndex) throws IOException{
         final SCIFIOConfig config = new SCIFIOConfig();

        // skip min/max computation
        config.imgOpenerSetComputeMinMax(false);

        // prefer planar array structure, for ImageJ1 and ImgSaver compatibility
        config.imgOpenerSetImgModes(SCIFIOConfig.ImgMode.AUTO);
        config.imgOpenerSetRange(String.format("%d-%d",planeIndex,planeIndex));
        config.parserSetLevel(MetadataLevel.ALL);
        
        config.forEach((key,value)->String.format("%s = %s",key,value.toString()));
        
        Dataset virtualDataset = datasetIoService.open(file.getAbsolutePath(), config);
        if(true) return virtualDataset;
        Dataset outputDataset = createEmptyPlaneDataset(virtualDataset);
        
        outputDataset.setPlane(0, virtualDataset.getPlane(planeIndex));
        return outputDataset;
        
    }
    
    public Dataset createEmptyPlaneDataset(Dataset input) {
        AxisType[] axisTypeList = new AxisType[2];

        long width = input.max(0);
        long height = input.max(1);
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
        
        
        long width = dataset.max(0);
        long height = dataset.max(1);
        
        
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

}
