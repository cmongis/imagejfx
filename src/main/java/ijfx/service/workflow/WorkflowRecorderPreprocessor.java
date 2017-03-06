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
package ijfx.service.workflow;

import ijfx.service.batch.BatchService;
import ijfx.service.history.HistoryService;
import ijfx.service.usage.Usage;
import ijfx.ui.main.ImageJFX;
import java.io.File;
import java.util.logging.Logger;
import javafx.application.Platform;
import net.mongis.usage.UsageLocation;
import net.mongis.usage.UsageType;
import org.scijava.Context;
import org.scijava.Priority;
import org.scijava.command.CommandModule;
import org.scijava.module.Module;
import org.scijava.module.ModuleItem;
import org.scijava.module.process.AbstractPreprocessorPlugin;
import org.scijava.module.process.PreprocessorPlugin;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = PreprocessorPlugin.class, priority = Priority.VERY_LOW_PRIORITY - 10)
public class WorkflowRecorderPreprocessor extends AbstractPreprocessorPlugin {

    @Parameter
    Context context;

    @Parameter
    HistoryService editService;

    @Parameter
    WorkflowService workflowService;

    @Parameter
    BatchService batchService;

    private final Logger logger = ImageJFX.getLogger();

    @Override
    public void process(Module module) {

        if (workflowService.isRunning() || batchService.isRunning()) {
            return;
        }

        if (CommandModule.class.isAssignableFrom(module.getClass())) {
            if (((CommandModule) module).isCanceled()) {
                logger.info("Command canceled. No recording");
                return;
            }
        }
        
       

        DefaultWorkflowStep step = new DefaultWorkflowStep(module);
        context.inject(step);
        step.setParameters(module.getInputs());
        // creating a builder for logging purpose
        final StringBuilder builder = new StringBuilder();
        builder.append("Recorded parameters  : ");
        module.getInputs().forEach((key, value) -> {
            builder.append("|").append(key).append("=").append(value);
        });

        
        StringBuilder safeInfos = new StringBuilder();
        
        for(ModuleItem item : module.getInfo().inputs()) {
            
            if(item.getType() != File.class) {
                safeInfos
                        .append(item.getName())
                        .append("=")
                        .append(module.getInput(item.getName()))
                        .append("; ");
            }
            
        }
        
        logger.info(builder.toString());

        if(module.getClass().getName().contains("OpenFile") == false)
        Usage.factory()
               .createUsageLog(UsageType.SET, module.getInfo().getTitle(), UsageLocation.get("Executed Plugin"))
               .setValue(safeInfos.toString())
               .send();
        
        step.setId(workflowService.generateStepName(editService.getStepList(), step));

        Platform.runLater(() -> editService.getStepList().add(step));

    }

}
