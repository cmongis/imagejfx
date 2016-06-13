/**
 * This file is part of ImageJ FX.
 *
 * ImageJ FX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ImageJ FX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ImageJ FX.  If not, see <http://www.gnu.org/licenses/>. * Copyright
 * 2015,2016 Cyril MONGIS, Michael Knop
 *
 */
package ijfx.plugins.flatfield;

import ijfx.core.stats.IjfxStatisticService;
import ijfx.plugins.convertype.TypeChangerIJFX;
import ijfx.service.sampler.DatasetSamplerService;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.imagej.Dataset;
import net.imagej.axis.CalibratedAxis;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.plugins.commands.assign.DivideDataValuesBy;
import net.imagej.types.DataTypeService;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.type.numeric.RealType;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.module.MethodCallException;
import org.scijava.module.Module;
import org.scijava.module.ModuleService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Tuan anh TRINH
 */
@Plugin(type = Command.class, menuPath = "Plugins>FlatField Corrrection")
public class FlatFieldCorrection implements Command {

    @Parameter
    CommandService commandService;

    @Parameter
    ModuleService moduleService;

    @Parameter
    DataTypeService dataTypeService;

    @Parameter
    ImageDisplayService imageDisplayService;

    @Parameter
    IjfxStatisticService ijfxStatisticService;

    @Parameter
    DatasetSamplerService datasetSamplerService;

    @Parameter
    Dataset flatFieldDataset;

    @Parameter
    Dataset inputDataset;

    @Parameter(type = ItemIO.OUTPUT)
    Dataset outputDataset;

    @Parameter
    boolean createNewImage = true;

    private double median = 0.0;

    @Override
    public void run() {
        flatFieldDataset = extractFlatfield(getImageDisplay(flatFieldDataset));//convertTo32(inputDataset);
        inputDataset = convertTo32(inputDataset);
        flatFieldDataset = convertTo32(flatFieldDataset);
        median = ijfxStatisticService.getDatasetDescriptiveStatistics(inputDataset).getPercentile(50);
        inputDataset = divideDatasetByValue(inputDataset, median);
        outputDataset = divideDatasetByDataset(inputDataset, flatFieldDataset);
    }

    public boolean isCreateNewImage() {
        return createNewImage;
    }

    public void setCreateNewImage(boolean createNewImage) {
        this.createNewImage = createNewImage;
    }

    public <T> Dataset convertTo32(Dataset dataset) {
        String type = dataTypeService.getTypeByAttributes(32, true, false, true, true).longName();
        Module module = moduleService.createModule(commandService.getCommand(TypeChangerIJFX.class));
        try {
            module.initialize();
        } catch (MethodCallException ex) {
            Logger.getLogger(FlatFieldCorrection.class.getName()).log(Level.SEVERE, null, ex);
        }
        module.setInput("data", dataset);
        module.setResolved("data", true);
        module.setInput("typeName", type);
        module.setResolved("typeName", true);
        module.setInput("combineChannels", false);
        module.setResolved("combineChannels", true);

        Future run = moduleService.run(module, false);

        try {
            run.get();
        } catch (InterruptedException ex) {
            Logger.getLogger(FlatFieldCorrection.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(FlatFieldCorrection.class.getName()).log(Level.SEVERE, null, ex);
        }
        Dataset data = (Dataset) module.getOutput("data");
        return data;
    }

    private Dataset extractFlatfield(ImageDisplay imageDisplay) {
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

    private Dataset divideDatasetByValue(Dataset dataset, double value) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("value", value);
        parameters.put("preview", false);
        parameters.put("allPlanes", true);
        parameters.put("display", getImageDisplay(dataset));
        Module module = executeCommand(DivideDataValuesBy.class, parameters);
        ImageDisplay imageDisplay = (ImageDisplay) module.getOutput("display");
        return (Dataset) imageDisplay.getActiveView().getData();

    }

//    private Dataset divideDatasetByDataset(Dataset numerator, Dataset denominator) {
//        Map<String, Object> parameters = new HashMap<>();
//        parameters.put("input1", numerator);
//        parameters.put("input2", denominator);
//        parameters.put("wantDoubles", true);
//        try {
//            parameters.put("op", OpDivide.class.newInstance());
//        } catch (InstantiationException ex) {
//            Logger.getLogger(FlatFieldCorrection.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IllegalAccessException ex) {
//            Logger.getLogger(FlatFieldCorrection.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        Module module = executeCommand(ImageCalculator.class, parameters);
//        Dataset result = (Dataset) module.getOutput("output");
//        return result;
//    }
    private <C extends Command> Module executeCommand(Class<C> type, Map<String, Object> parameters) {
        Module module = moduleService.createModule(commandService.getCommand(type));
        try {
            module.initialize();
        } catch (MethodCallException ex) {
            Logger.getLogger(FlatFieldCorrection.class.getName()).log(Level.SEVERE, null, ex);
        }
        parameters.forEach((k, v) -> {
            module.setInput(k, v);
            module.setResolved(k, true);
        });

        Future run = moduleService.run(module, false, parameters);

        try {
            run.get();
        } catch (InterruptedException ex) {
            Logger.getLogger(FlatFieldCorrection.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(FlatFieldCorrection.class.getName()).log(Level.SEVERE, null, ex);
        }
        return module;
    }

    public ImageDisplay getImageDisplay(Dataset dataset) {
        Optional<ImageDisplay> optional = imageDisplayService.getImageDisplays()
                .parallelStream()
                .filter((d) -> imageDisplayService.getActiveDataset(d) == dataset)
                .findFirst();
        return optional.get();
    }

    /**
     * Divide 2 different Dataset with different dimensions
     * @param <T>
     * @param numerator
     * @param denominator
     * @return 
     */
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
           // System.out.println(resultRandomAccess.get().toString());
            Float f = numeratorCursor.get().getRealFloat()+denominatorRandomAccess.get().getRealFloat();
            resultRandomAccess.get().setReal(f);

        }
        return resultDataset;
    }

}
