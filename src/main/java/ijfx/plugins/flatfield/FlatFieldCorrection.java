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
import ijfx.plugins.convertype.ConvertTo8bits;
import ijfx.plugins.convertype.TypeChangerIJFX;
import ijfx.service.batch.BatchSingleInput;
import ijfx.service.batch.DisplayBatchInput;
import ijfx.service.sampler.DatasetSamplerService;
import ijfx.ui.main.ImageJFX;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imagej.axis.CalibratedAxis;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.plugins.commands.assign.DivideDataValuesBy;
import net.imagej.plugins.commands.typechange.TypeChanger;
import net.imagej.sampler.SamplingDefinition;
import net.imagej.types.DataTypeService;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.CommandModule;
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
@Plugin(type = Command.class, menuPath = "Plugins>FlatField")
public class FlatFieldCorrection implements Command {

    @Parameter(type = ItemIO.INPUT)
    ImageDisplay flatFieldImageDisplay;

    @Parameter(type = ItemIO.INPUT)
    Dataset inputDataset;

    @Parameter(type = ItemIO.OUTPUT)
    Dataset outputDataset;

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
    boolean createNewImage = true;

    private double median;

    @Override
    public void run() {
        Dataset flatFieldDataset = extractFlatfield(flatFieldImageDisplay);//convertTo32(inputDataset);
        median = ijfxStatisticService.getDatasetDescriptiveStatistics(outputDataset).getPercentile(50);
        inputDataset = convertTo32(inputDataset);
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
        module.setInput("typeName", type);
        module.setResolved("typeName", true);
        module.setInput("combineChannels", false);
        module.setResolved("combineChannels", true);

        Future run = moduleService.run(module, true);

        try {
            run.get();
        } catch (InterruptedException ex) {
            Logger.getLogger(FlatFieldCorrection.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(FlatFieldCorrection.class.getName()).log(Level.SEVERE, null, ex);
        }
        return (Dataset) module.getOutput("data");
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

    private Dataset divideDatasetBy(Dataset dataset, double value) {
        Map<String,Object> parameters = new HashMap<>();
        parameters.put("value", value);
        parameters.put("preview", false);
        parameters.put("allPlanes",true);
        Module module = executeCommand(DivideDataValuesBy.class, parameters);
        ImageDisplay imageDisplay = (ImageDisplay)module.getOutput("display");
        return (Dataset)imageDisplay.getActiveView().getData();
        
    }

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

        Future run = moduleService.run(module, true);

        try {
            run.get();
        } catch (InterruptedException ex) {
            Logger.getLogger(FlatFieldCorrection.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(FlatFieldCorrection.class.getName()).log(Level.SEVERE, null, ex);
        }
        return module;
    }
}
