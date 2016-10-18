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
package ijfx.service.dataset;

import ijfx.plugins.flatfield.FlatFieldCorrectionOld;
import ijfx.service.sampler.DatasetSamplerService;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.axis.AxisType;
import net.imagej.axis.CalibratedAxis;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.plugins.commands.assign.DivideDataValuesBy;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.apache.commons.io.FilenameUtils;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.display.DisplayService;
import org.scijava.module.MethodCallException;
import org.scijava.module.Module;
import org.scijava.module.ModuleService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 *
 * @author Tuan anh TRINH
 */
@Plugin(type = Service.class)
public class DefaultDatasetUtillsService extends AbstractService implements DatasetUtillsService {

    @Parameter
    DatasetSamplerService datasetSamplerService;

    @Parameter
    ImageDisplayService imageDisplayService;

    @Parameter
    CommandService commandService;

    @Parameter
    ModuleService moduleService;

    @Parameter
    DisplayService displayService;

    @Parameter
    DatasetService datasetService;

    public final static String DEFAULT_SEPARATOR = " - ";
    
    @Override
    public Dataset extractPlane(ImageDisplay imageDisplay) throws NullPointerException {
        CalibratedAxis[] calibratedAxises = new CalibratedAxis[imageDisplay.numDimensions()];
        int[] position = new int[imageDisplay.numDimensions()];
        imageDisplay.localize(position);
        Dataset dataset = (Dataset) imageDisplay.getActiveView().getData();
        imageDisplay.axes(calibratedAxises);
        for (int i = 2; i < position.length; i++) {
            dataset = datasetSamplerService.isolateDimension(dataset, calibratedAxises[i].type(), position[i]);
        }
        return dataset;
    }

    @Override
    public ImageDisplay getImageDisplay(Dataset dataset) {
        return imageDisplayService.getImageDisplays()
                .parallelStream()
                .filter((d) -> imageDisplayService.getActiveDataset(d) == dataset)
                .findFirst().orElse(null);
    }

    /**
     * Divide 2 different Dataset with different dimensions
     *
     * @param <T>
     * @param numerator
     * @param denominator
     * @return
     */
    @Override
    public < T extends RealType< T>> Dataset divideDatasetByDataset(Dataset numerator, Dataset denominator) {

        Dataset resultDataset = numerator.duplicateBlank();

        RandomAccess<T> resultRandomAccess = (RandomAccess<T>) resultDataset.randomAccess();
        Cursor<T> numeratorCursor = (Cursor<T>) numerator.cursor();
        RandomAccess<T> denominatorRandomAccess = (RandomAccess<T>) denominator.randomAccess();

        int[] positionDenominator = new int[denominator.numDimensions()];
        denominatorRandomAccess.localize(positionDenominator);
        while (numeratorCursor.hasNext()) {
            numeratorCursor.next();
            //Set position
            positionDenominator[0] = numeratorCursor.getIntPosition(0);
            positionDenominator[1] = numeratorCursor.getIntPosition(1);
            denominatorRandomAccess.setPosition(positionDenominator);
            resultRandomAccess.setPosition(numeratorCursor);

            //Calculate value
            try {

                resultRandomAccess.get().set(numeratorCursor.get());
            } catch (Exception e) {
                e.printStackTrace();
            }
            Float f = numeratorCursor.get().getRealFloat() / denominatorRandomAccess.get().getRealFloat();
            resultRandomAccess.get().setReal(f);

        }
        return resultDataset;
    }

    @Override
    public Dataset divideDatasetByValue(Dataset dataset, double value) {
        ImageDisplay display = this.getImageDisplay(dataset);
        if (display == null) {
            display = (ImageDisplay) displayService.createDisplay(dataset.getName(), dataset);
        }
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("value", value);
        parameters.put("preview", false);
        parameters.put("allPlanes", true);
        parameters.put("display", display);
        Module module = executeCommand(DivideDataValuesBy.class, parameters);
        ImageDisplay imageDisplay = (ImageDisplay) module.getOutput("display");
        Dataset datasetResult = (Dataset) imageDisplay.getActiveView().getData();
        return datasetResult;

    }

    //Not here... But where?
    public <C extends Command> Module executeCommand(Class<C> type, Map<String, Object> parameters) {
        Module module = moduleService.createModule(commandService.getCommand(type));
        try {
            module.initialize();
        } catch (MethodCallException ex) {
            Logger.getLogger(FlatFieldCorrectionOld.class.getName()).log(Level.SEVERE, null, ex);
        }
        parameters.forEach((k, v) -> {
            module.setInput(k, v);
            module.setResolved(k, true);
        });

        Future run = moduleService.run(module, false, parameters);

        try {
            run.get();
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(FlatFieldCorrectionOld.class.getName()).log(Level.SEVERE, null, ex);
        }
        return module;
    }

    @Override
    public Dataset divideActivePlaneByValue(Dataset dataset, long[] position, double value) {
        double width = dataset.max(0) + 1;
        double height = dataset.max(1) + 1;

        RandomAccess<RealType<?>> randomAccess = dataset.randomAccess();
        randomAccess.setPosition(position);
        for (int x = 0; x < width; x++) {
            randomAccess.setPosition(x, 0);
            for (int y = 0; y < height; y++) {
                randomAccess.setPosition(y, 1);
                Double d = (Double) randomAccess.get().getRealDouble() / value;
                randomAccess.get().setReal(d);
            }
        }
        return dataset;
    }

    @Override
    public Dataset divideActivePlaneByActivePlane(Dataset dataset, long[] position, Dataset datasetValue, long[] positionValue) {
//        long[] position = new long[dataset.numDimensions()];
//        this.getImageDisplay(dataset).localize(position);
        double width = dataset.max(0) + 1;
        double height = dataset.max(1) + 1;

//        long[] positionValue = new long[datasetValue.numDimensions()];
//        this.getImageDisplay(dataset).localize(positionValue);
        double widthValue = datasetValue.max(0) + 1;
        double heightValue = datasetValue.max(1) + 1;

        if (width != widthValue || height != heightValue) {
            throw new IllegalArgumentException("The sizes have to be the same");
        }
        RandomAccess<RealType<?>> randomAccess = dataset.randomAccess();
        RandomAccess<RealType<?>> randomAccessValue = datasetValue.randomAccess();

        randomAccess.setPosition(position);
        randomAccessValue.setPosition(positionValue);
        for (int x = 0; x < width; x++) {
            randomAccess.setPosition(x, 0);
            randomAccessValue.setPosition(x, 0);
            for (int y = 0; y < height; y++) {
                randomAccess.setPosition(y, 1);
                randomAccessValue.setPosition(y, 1);
//                System.out.println("ijfx.service.dataset.DefaultDatasetUtillsService.divideActivePlaneByActivePlane()");
//                System.out.println(randomAccess.get().getRealDouble());
                Double d = randomAccess.get().getRealDouble() / randomAccessValue.get().getRealDouble();
                randomAccess.get().setReal(d);
//                System.out.println(randomAccess.get().getRealDouble());
            }
        }
        return dataset;
    }

    @Override
    public <T extends RealType<T> & NativeType<T>> Dataset emptyConversion(Dataset dataset, T t) {

        long[] dimensions = new long[dataset.numDimensions()];
        AxisType[] axisTypes = new AxisType[dataset.numDimensions()];
        CalibratedAxis[] axes = new CalibratedAxis[dataset.numDimensions()];
        dataset.axes(axes);

        Dataset output = datasetService.create(t, dimensions, "", axisTypes);
        output.setAxes(axes);
        output.setName(dataset.getName());
        output.setSource(dataset.getSource());

        return output;

    }

    private void copyMetaData(Dataset source, Dataset target) {
        CalibratedAxis[] axes = new CalibratedAxis[source.numDimensions()];
        source.axes(axes);

        target.setAxes(axes);
        target.setName(target.getName());
        target.setSource(target.getSource());

    }

    @Override
    public <T extends RealType<T> & NativeType<T>> Dataset convert(Dataset dataset, T t) {
        long[] dimensions = new long[dataset.numDimensions()];
        AxisType[] axisTypes = new AxisType[dataset.numDimensions()];

        dataset.dimensions(dimensions);

        for (int i = 0; i != dimensions.length; i++) {
            axisTypes[i] = dataset.axis(i).type();
        }

        Dataset output = datasetService.create(t, dimensions, "", axisTypes);

        Cursor<? extends RealType<?>> cursor = dataset.cursor();
        cursor.reset();
        RandomAccess<RealType<?>> randomAccess = output.randomAccess();
        while (cursor.hasNext()) {
            cursor.fwd();

            randomAccess.setPosition(cursor);
            randomAccess.get().setReal(cursor.get().getRealDouble());

        }

        copyMetaData(dataset, output);
        return output;
    }

    public void addSuffix(Dataset dataset, String suffix, String separator) {
        if(separator == null) separator = DEFAULT_SEPARATOR;

        String datasetName = dataset.getName();
        File datasetFolder;
        if (dataset.getSource() != null) {
            datasetFolder = new File(dataset.getSource()).getParentFile();
        }
        else {
            datasetFolder = new File("./");
        }
        
        String baseName = FilenameUtils.getBaseName(datasetName);
       String extension = FilenameUtils.getExtension(datasetName);
       
       dataset.setName(baseName+separator+extension);
       dataset.setSource(new File(datasetFolder,dataset.getName()).getAbsolutePath());
       

    }

}
