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
package ijfx.service.batch;

import ijfx.ui.main.ImageJFX;
import ijfx.service.workflow.Workflow;
import ijfx.service.workflow.WorkflowStep;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import net.imagej.Dataset;
import net.imagej.ImageJService;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import org.scijava.display.DisplayService;
import org.scijava.module.Module;
import org.scijava.module.ModuleItem;
import org.scijava.module.ModuleService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = Service.class)
public class BatchService extends AbstractService implements ImageJService {

    @Parameter
    private ModuleService moduleService;

    private Logger logger = ImageJFX.getLogger();

    @Parameter
    private ImageDisplayService imageDisplayService;

    @Parameter
    private DisplayService displayService;

   

    boolean running = false;
    
    
    // applies a single modules to multiple inputs and save them
    public Task<Boolean> applyModule(List<BatchSingleInput> inputs, final Module module, HashMap<String, Object> parameters) {

        return new Task<Boolean>() {

            @Override
            protected Boolean call() throws Exception {

                int totalOps = inputs.size();
                int count = 0;

                for (BatchSingleInput input : inputs) {
                    input.load();
                    count++;
                    final Module createdModule = moduleService.createModule(module.getInfo());
                    if (!executeModule(input, createdModule, parameters)) {
                        return false;
                    };
                    input.save();
                    updateProgress(count, totalOps);
                }

                return true;
            }

        };
    }

    // applies a workflow to a list of inputs
    public Task<Boolean> applyWorkflow(List<BatchSingleInput> inputs, Workflow workflow) {

        Task<Boolean> task = new Task<Boolean>() {

            Boolean lock = new Boolean(true);

            @Override
            protected Boolean call() throws Exception {
                int totalOps = inputs.size() * workflow.getStepList().size();
                int count = 0;
                
               
                
                updateMessage("Starting batch processing...");

                boolean success = true;
                BooleanProperty successProperty = new SimpleBooleanProperty();
                Exception error = null;
                setRunning(true);

               for (BatchSingleInput input : inputs) {
               //inputs.parallelStream().forEach(input->{
                    logger.info("Running...");

                    if (isCancelled()) {
                        updateMessage("Batch Processing cancelled");
                        success = false;
                        //return;
                        break;

                    }

                    synchronized (lock) {
                        logger.info("Loading input...");
                        try {
                            getContext().inject(input);
                        }
                        catch(IllegalStateException ise) {
                            logger.warning("Context already injected");
                        }
                        try {
                            input.load();
                        } catch (Exception e) {
                            logger.log(Level.SEVERE, "Couldn't load input", e);
                            error = e;
                            continue;

                        }
                        logger.info("Input loaded");
                    }

                    for (WorkflowStep step : workflow.getStepList()) {
                        logger.info("Executing step : " + step.getId());
                        updateMessage(String.format("Processing %s with %s", input.getName(), step.getModule().getInfo().getTitle()));

                        updateProgress(count++, totalOps);

                        final Module module = moduleService.createModule(step.getModule().getInfo());
                        logger.info("Module created : " + module.getDelegateObject().getClass().getSimpleName());
                        if (!executeModule(input, module, step.getParameters())) {

                            updateMessage("Error :-(");
                            updateProgress(0, 1);
                            success = false;
                            break;
                        };

                    }

                    if (success == false) {
                        break;
                    }

                    synchronized (lock) {
                        input.save();
                    }
                    input.dispose();
                }

                if (success) {

                    updateMessage("Batch processing completed.");
                    updateProgress(1, 1);

                } else if (isCancelled()) {
                    updateMessage("Batch processing cancelled");
                } else {

                    updateMessage("An error happend during the process.");
                    updateProgress(1, 1);
                }
                
                setRunning(false);
                return success;
            }
        };

        return task;
    }

    // execute a module (with all the side parameters injected)
    public boolean executeModule(BatchSingleInput input, Module module, Map<String, Object> parameters) {

        logger.info("Executing module " + module.getDelegateObject().getClass().getSimpleName());
        logger.info("Injecting input");
        boolean inputInjectionSuccess = injectInput(input, module);

        if (!inputInjectionSuccess) {
            logger.warning("Error when injecting input.");
            return false;
        }

        logger.info("Injecting parameters");
        parameters.forEach((key, value) -> {
            if (value == null) {
                return;
            }
            logger.info(String.format("Parameter : %s = %s", key, value.toString()));
            module.setInput(key, value);
            module.setResolved(key, true);
        });

        module.getInputs().forEach((key, value) -> {
            module.setResolved(key, true);
        });

        String moduleName = module.getInfo().getDelegateClassName();
        logger.info(String.format("[%s] starting module", moduleName));

        logger.info("Running module");

        Future<Module> run = moduleService.run(module, true);

        logger.info(String.format("[%s] module started", moduleName));

        try {
            run.get();
            logger.info(String.format("[%s] module finished", moduleName));
            extractOutput(input, module);
        } catch (Exception ex) {
            ImageJFX.getLogger().log(Level.SEVERE,"Error when running module "+moduleName,ex);;
            return false;

        }

        return true;

    }

    // inject the dataset or display depending on the requirements of the module
    private boolean injectInput(BatchSingleInput input, Module module) {

        ModuleItem item;
        logger.info("Injecting inputs into module : " + module.getDelegateObject().getClass().getSimpleName());
        item = moduleService.getSingleInput(module, Dataset.class);

        if (item != null) {
            logger.info("Dataset input found !");
            Dataset dataset = input.getDataset();

            if (dataset == null) {
                logger.info("The Dataset for was null for " + input.getName());
            } else {
                module.setInput(item.getName(), dataset);
                logger.info("Injection done for " + item.getName() + " with " + dataset.toString());
                return true;
            }
        }

        // testing if it takes a Display as input
        item = moduleService.getSingleInput(module, ImageDisplay.class);
        if (item != null) {
            logger.info("ImageDisplay found !");
            // if yes, injecting the display
            module.setInput(item.getName(), input.getDisplay());
            return true;
        } else {
            logger.info("Error when injecting input !");
            return false;
        }

    }

    // extract the input from an executed module
    public boolean extractOutput(BatchSingleInput input, Module module) {

        // testing if it takes a Display as input
        ModuleItem item = moduleService.getSingleOutput(module, Dataset.class);
        if (item != null) {
            // if yes, injecting the display
            input.setDataset((Dataset) module.getOutput(item.getName()));
            return false;
        }

        item = moduleService.getSingleInput(module, Dataset.class);

        if (item != null) {
            input.setDisplay((ImageDisplay) module.getOutput(item.getName()));
            return true;
        }

        return false;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
    
    
    

}
