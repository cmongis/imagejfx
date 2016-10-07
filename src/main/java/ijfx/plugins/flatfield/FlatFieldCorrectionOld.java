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
import ijfx.service.dataset.DatasetUtillsService;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.imagej.Dataset;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.plugins.commands.typechange.TypeChanger;
import net.imagej.types.DataTypeService;
import net.imglib2.type.numeric.RealType;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.command.ContextCommand;
import org.scijava.module.MethodCallException;
import org.scijava.module.Module;
import org.scijava.module.ModuleService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Tuan anh TRINH
 */
@Deprecated
public class FlatFieldCorrectionOld extends ContextCommand {

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
    DatasetUtillsService datasetUtillsService;

    @Parameter
    ImageDisplay flatFieldImageDisplay;
//    Dataset flatFieldDataset;

    @Parameter
    ImageDisplay inputImageDisplay;

    @Parameter(type = ItemIO.OUTPUT)
    Dataset outputDataset;

    @Parameter
    boolean createNewImage = false;

    private double median = 1.0;

    @Override
    public void run() {
        long[] position = new long[inputImageDisplay.numDimensions()];
        inputImageDisplay.localize(position);
        long[] positionFlatField = new long[flatFieldImageDisplay.numDimensions()];
        flatFieldImageDisplay.localize(positionFlatField);
        Dataset dataset = imageDisplayService.getActiveDataset(inputImageDisplay);
        Dataset dataset32 = convertTo32(dataset);
        Dataset flatFieldDataset = imageDisplayService.getActiveDataset(flatFieldImageDisplay);
        Dataset flatFieldDataset32 = convertTo32(flatFieldDataset);
        median = ijfxStatisticService.getPlaneDescriptiveStatistics(dataset32, position).getPercentile(50);
        Dataset datasetDividedByValue = datasetUtillsService.divideActivePlaneByValue(dataset32, position, median);;
        outputDataset = datasetUtillsService.divideActivePlaneByActivePlane(datasetDividedByValue, position, flatFieldDataset32, positionFlatField);
    }

    public boolean isCreateNewImage() {
        return createNewImage;
    }

    public void setCreateNewImage(boolean createNewImage) {
        this.createNewImage = createNewImage;
    }

    public Dataset convertTo32(Dataset dataset) {
        RealType<?> firstElement = dataset.firstElement();
        String typeFirstElement = dataTypeService.getTypeByAttributes(firstElement.getBitsPerPixel(), true, false, !dataset.isInteger(), dataset.isSigned()).longName();

        String type = dataTypeService.getTypeByAttributes(32, true, false, true, true).longName();

        if (typeFirstElement.equals(type)) return dataset;
        Map<String, Object> map = new HashMap<>();

        map.put("data", dataset);
        map.put("typeName", type);
        map.put("combineChannels", false);

        Module executeCommand = executeCommand(TypeChangerIJFX.class, map);
        Dataset data = (Dataset) executeCommand.getOutput("data");
        return data;
    }


    private <C extends Command> Module executeCommand(Class<C> type, Map<String, Object> parameters) {
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
        } catch (InterruptedException ex) {
            Logger.getLogger(FlatFieldCorrectionOld.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(FlatFieldCorrectionOld.class.getName()).log(Level.SEVERE, null, ex);
        }
        return module;
    }

}
