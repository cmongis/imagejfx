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

import ijfx.bridge.FxUIPreprocessor;
import ijfx.ui.main.ImageJFX;
import ijfx.service.workflow.Workflow;
import ijfx.service.workflow.WorkflowRecorderPreprocessor;
import ijfx.service.workflow.WorkflowStep;
import ijfx.ui.batch.BatchPrepreprocessorPlugin;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import mongis.utils.AsyncCallback;
import mongis.utils.ProgressHandler;
import mongis.utils.SilentProgressHandler;
import net.imagej.Dataset;
import net.imagej.ImageJService;
import net.imagej.display.DatasetView;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import org.scijava.display.DisplayPostprocessor;
import org.scijava.display.DisplayService;
import org.scijava.module.Module;
import org.scijava.module.ModuleItem;
import org.scijava.module.ModuleService;
import org.scijava.module.process.InitPreprocessor;
import org.scijava.module.process.PostprocessorPlugin;
import org.scijava.module.process.PreprocessorPlugin;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;
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

    @Parameter
    private PluginService pluginService;

    private boolean running = false;

    private List<Class<?>> processorBlackList = new ArrayList<>();

    public BatchService() {
        super();
        processorBlackList.add(FxUIPreprocessor.class);
        processorBlackList.add(DisplayPostprocessor.class);
        processorBlackList.add(InitPreprocessor.class);
        processorBlackList.add(WorkflowRecorderPreprocessor.class);

    }

    // applies a single modules to multiple inputs and save them
    public Boolean applyModule(ProgressHandler progress, List<BatchSingleInput> inputs, final Module module, boolean process, HashMap<String, Object> parameters) {

        int totalOps = inputs.size();
        int count = 0;

        for (BatchSingleInput input : inputs) {
            input.load();
            count++;
            final Module createdModule = moduleService.createModule(module.getInfo());
            if (!executeModule(input, createdModule, process, parameters)) {
                return false;
            }
            input.save();
            progress.setProgress(count, totalOps);
        }

        return true;

    }

    public Task<Boolean> applyWorkflow(List<BatchSingleInput> inputs, Workflow workflow) {
        return new AsyncCallback<List<BatchSingleInput>, Boolean>()
                .setInput(inputs)
                .run((progress, input) -> applyWorkflow(progress, inputs, workflow));
    }

    public Boolean applyWorkflow(ProgressHandler handler, BatchSingleInput input, Workflow workflow) {
        List<BatchSingleInput> inputList = new ArrayList<>();
        inputList.add(input);
        return applyWorkflow(handler, inputList, workflow);
    }

    // applies a workflow to a list of inputs
    public Boolean applyWorkflow(ProgressHandler progress, List<BatchSingleInput> inputs, Workflow workflow) {

        if (progress == null) {
            progress = new SilentProgressHandler();
        }

        Boolean lock = new Boolean(true);
        int totalOps = inputs.size() * workflow.getStepList().size();
        int count = 0;

        progress.setStatus("Starting batch processing...");

        boolean success = true;
        BooleanProperty successProperty = new SimpleBooleanProperty();
        Exception error = null;
        setRunning(true);

        for (BatchSingleInput input : inputs) {
            //inputs.parallelStream().forEach(input->{
            logger.info("Running...");

            if (progress.isCancelled()) {
                progress.setStatus("Batch Processing cancelled");
                success = false;
                //return;
                break;

            }

            synchronized (lock) {
                logger.info("Loading input...");
                try {
                    getContext().inject(input);
                } catch (IllegalStateException ise) {
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
                progress.setStatus(String.format("Processing %s with %s", input.getName(), step.getModule().getInfo().getTitle()));

                progress.setProgress(count++, totalOps);

                final Module module = moduleService.createModule(step.getModule().getInfo());
                getContext().inject(module.getDelegateObject());
                logger.info("Module created : " + module.getDelegateObject().getClass().getSimpleName());
                if (!executeModule(input, module, true, step.getParameters())) {

                    progress.setStatus("Error :-(");
                    progress.setProgress(0, 1);
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

            progress.setStatus("Batch processing completed.");
            progress.setProgress(1.0);

        } else if (progress.isCancelled()) {
            progress.setStatus("Batch processing cancelled");
        } else {

            progress.setStatus("An error happend during the process.");
            progress.setProgress(1, 1);
        }
        setRunning(false);
        return success;

    }

    // execute a module (with all the side parameters injected)
    public boolean executeModule(BatchSingleInput input, Module module, boolean process, Map<String, Object> parameters) {
       
        
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

        pluginService
                .createInstancesOfType(BatchPrepreprocessorPlugin.class)
                .forEach(processor->processor.process(input, module,parameters));
        
        
        String moduleName = module.getInfo().getDelegateClassName();
        logger.info(String.format("[%s] starting module", moduleName));

        logger.info("Running module");
        Future<Module> run;
  
        try {
            //getContext().inject(run);
            getContext().inject(module);
            module.initialize();
        }
        catch(Exception e) {
            logger.info("Context already injected.");
            //   e.printStackTrace();
        }
            run = moduleService.run(module, getPreProcessors(), getPostprocessors(), parameters);
       // } else {
       //     run = moduleService.run(module, process, parameters);

        //}

        logger.info(String.format("[%s] module started", moduleName));

        try {
            run.get();
            logger.info(String.format("[%s] module finished", moduleName));
            extractOutput(input, module);
        } catch (Exception ex) {
            ImageJFX.getLogger().log(Level.SEVERE, "Error when running module " + moduleName, ex);;
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
            logger.info("Dataset input field found : " + item.getName());
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
            logger.info("ImageDisplay input field found : " + item.getName());
            // if yes, injecting the display
            module.setInput(item.getName(), input.getDisplay());
            return true;
        }
        item = moduleService.getSingleInput(module, DatasetView.class);
        if (item != null) {
            logger.info("DatasetView field found : " + item.getName());
            // if yes, injecting the display
            DatasetView datasetView = input.getDatasetView();

            module.setInput(item.getName(), datasetView);
            return true;
        } else {
            logger.info("Error when injecting input !");
            return false;
        }

    }

    // extract the outpu from an executed module
    public void extractOutput(BatchSingleInput input, Module module) {
        Map<String, Object> outputs = module.getOutputs();
        outputs.forEach((s, o) -> {
            logger.info(String.format("Trying to find output from %s = %s",s,o));
            if (Dataset.class.isAssignableFrom(o.getClass())) {
                logger.info("Extracting Dataset !");
                input.setDataset((Dataset) module.getOutput(s));
            } else if (ImageDisplay.class.isAssignableFrom(o.getClass())) {
                logger.info("Extracting ImageDisplay !");
                input.setDisplay((ImageDisplay) module.getOutput(s));
            } else if (o instanceof DatasetView) {
                logger.info("Extracting DatasetView !");
                input.setDatasetView((DatasetView) module.getOutput(s));
            }
        });

    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    Class<? extends PreprocessorPlugin>[] preProcessorBlackList;

    private <T> T injectPlugin(T p) {
        try {
            getContext().inject(p);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        finally {
            return p;
        }
    }

    private List<PreprocessorPlugin> getPreProcessors() {
        return pluginService
                .createInstancesOfType(PreprocessorPlugin.class)
                .stream()
                .sequential()
                .filter(p -> !processorBlackList.contains(p.getClass()))
                .sequential()
                .map(p->{
                    System.out.println(p.getClass());
                    return p;
                })
                //.map(this::injectPlugin)
                
                .collect(Collectors.toList());
    }

    private List<PostprocessorPlugin> getPostprocessors() {
        return pluginService
                .createInstancesOfType(PostprocessorPlugin.class)
                .stream()
                .sequential()
                .filter(p -> !processorBlackList.contains(p.getClass()))
                //.map(this::injectPlugin)
                .collect(Collectors.toList());
    }

}
