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
package ijfx.plugins.bunwarpJ;

import bunwarpj.Transformation;
import ij.ImagePlus;
import ijfx.ui.correction.WorkflowModel;
import io.scif.services.DatasetIOService;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
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
@Plugin(type = Command.class)
public class ChromaticCorrection implements Command {

    
    @Parameter
    ModuleService moduleService;

    @Parameter
    DatasetService datasetService;

    @Parameter
    DatasetIOService ioService;

    @Parameter(type = ItemIO.INPUT)
    Dataset sourceDataset;

    @Parameter(type = ItemIO.INPUT)
    long[] sourcePosition;

    @Parameter(type = ItemIO.INPUT)
    long[] targetPosition;

    @Parameter(type = ItemIO.INPUT)
    File landmarksFile;

    @Parameter(type = ItemIO.OUTPUT)
    Dataset outputDataset;

    @Parameter(type = ItemIO.INPUT, required = false)
    Transformation transformation;

    @Parameter
    bunwarpj.Param parameter;

    @Parameter
    CommandService commandService;

    @Override
    public void run() {

            Module promise;

            //Extract Slice
            Map<String, Object> map = new HashMap<>();
            map.put("inputDataset", this.sourceDataset);
            map.put("position", sourcePosition);
            promise = executeCommand(ExtractSliceCommand.class, map).orElseThrow(NullPointerException::new);
            Dataset smallSourceDataset = (Dataset) promise.getOutput("outputDataset");

            Map<String, Object> map2 = new HashMap<>();
            map2.put("inputDataset", this.sourceDataset);
            map2.put("position", targetPosition);
            promise = executeCommand(ExtractSliceCommand.class, map2).orElseThrow(NullPointerException::new);
            Dataset targetDataset = (Dataset) promise.getOutput("outputDataset");

            // Perform correction with bUnwarpJ
            Map<String, Object> map3 = new HashMap<>();
            map3.put("sourceDataset", smallSourceDataset);
            map3.put("targetDataset", targetDataset);
            map3.put("landmarksFile", landmarksFile);
            map3.put("parameter", parameter);
            map3.put("transformation", transformation);
            promise = executeCommand(BunwarpJFX.class, map3).orElseThrow(NullPointerException::new);
            Dataset correctedSourceDataset = (Dataset) promise.getOutput("outputDataset");
            
            // Replace the corrected slice in the sourceDataset
              Map<String, Object> map4 = new HashMap<>();
            map4.put("stack", this.sourceDataset);
            map4.put("sliceDataset", correctedSourceDataset);
            map4.put("position", sourcePosition);
            promise = executeCommand(ReplaceSlice.class, map4).orElseThrow(NullPointerException::new);
            outputDataset = (Dataset) promise.getOutput("outputDataset");

            
            
       
    }

    public <C extends Command> Optional<Module> executeCommand(Class<C> type, Map<String, Object> parameters) {
        Module module = moduleService.createModule(commandService.getCommand(type));
        try {
            module.initialize();
            parameters.forEach((k, v) -> {
                module.setInput(k, v);
                module.setResolved(k, true);
            });

            Future run = moduleService.run(module, false, parameters);

            run.get();
        } catch (MethodCallException | InterruptedException | ExecutionException ex) {
            Logger.getLogger(WorkflowModel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Optional.of(module);
    }

}
