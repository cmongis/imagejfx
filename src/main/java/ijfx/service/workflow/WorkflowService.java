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

import ijfx.ui.main.ImageJFX;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.imagej.ImageJService;
import org.scijava.Priority;
import org.scijava.command.CommandService;
import org.scijava.event.EventService;
import org.scijava.module.Module;
import org.scijava.module.ModuleService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import mongis.utils.FXUtilities;
import mongis.utils.ProgressHandler;

/**
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = Service.class, priority = Priority.LOW_PRIORITY)
public class WorkflowService extends AbstractService implements ImageJService {

    @Parameter
    ModuleService moduleService;

    @Parameter
    CommandService commandService;

    @Parameter
    EventService eventService;

    protected final static String MODULE_NAME_FORMAT = "%s-%d";
    protected final static String NO_MODULE_NAME = "NoName";

    Logger logger = ImageJFX.getLogger();

    @Parameter
    ImageJService imageJService;

    
    boolean isExecutingWorkflow = false;
    
    public boolean executeWorkflow(Workflow workflow) {

        int count = 1;
        int total = workflow.getStepList().size();
        isExecutingWorkflow = true;
        //for each step, execute the module
        for (WorkflowStep step : workflow.getStepList()) {

            // stop the workflow if asked
            if (workflow.mustBeStopped()) {
                eventService.publish(new WorkflowEndedEvent(WorkflowEnd.CANCELED));

                // we just cancel the stop signal for the future starts
                workflow.setMustBeStopped(false);
                isExecutingWorkflow = false;
                return false;

            }

            logger.info("The show must go on.");

            eventService.publish(new WorkflowStartEvent(workflow));

            logger.info("Executing step  " + step.getId());
            final Module module = moduleService.createModule(step.getModule().getInfo());

            step.getParameters().forEach((key, value) -> {
                module.setInput(key, value);
                module.setResolved(key, true);
            });
            
            
            
            String moduleName = module.getInfo().getDelegateClassName();
            logger.info(String.format("[%s] starting module", moduleName));
            Future<Module> run = moduleService.run(module, true);
            logger.info(String.format("[%s] module started", moduleName));

            try {

                run.get();
                logger.info(String.format("[%s] module finished", moduleName));
            } catch (InterruptedException ex) {
                eventService.publish(new WorkflowEndedEvent(WorkflowEnd.ERROR));
                ImageJFX.getLogger().log(Level.SEVERE,"Error when running workflow",ex);;
               

            } catch (ExecutionException ex) {
                eventService.publish(new WorkflowEndedEvent(WorkflowEnd.ERROR));
                ImageJFX.getLogger().log(Level.SEVERE,null,ex);;
            }
            
            if (run == null) {
                isExecutingWorkflow = false;
                return false;
            }
        }
        isExecutingWorkflow = false;
        return true;
    }

    public void injectModule(WorkflowStep step) {
        context().inject(step);
    }

    public boolean stepNameExists(List<? extends WorkflowStep> steps, String name) {
        if (name == null) {
            return false;
        }
        return steps.stream().anyMatch(step -> name.equals(step.getId()));
    }

    public String generateStepName(Workflow workflow, WorkflowStep step) {
        return generateStepName(workflow.getStepList(), step, 1);
    }

    public String generateStepName(List<? extends WorkflowStep> stepList, WorkflowStep step) {
        return generateStepName(stepList, step, 1);
    }

    public String generateStepName(List<? extends WorkflowStep> stepList, WorkflowStep step, int i) {

        // class name of the module
        String moduleClass = null;

        // in case the step has no module yet
        if (step.getModule() == null) {
            moduleClass = NO_MODULE_NAME;
        } else {
            moduleClass = step.getModule().getDelegateObject().getClass().getSimpleName();
        }

        // final name
        String name = String.format(MODULE_NAME_FORMAT, moduleClass, i);

        // we use a bit of recursion
        if (stepNameExists(stepList, name)) {
            return generateStepName(stepList, step, i + 1);
        } else {
            return name;
        }

    }

    public WorkflowStep duplicate(WorkflowStep step) {

        DefaultWorkflowStep duplicatedStep = new DefaultWorkflowStep(step.getModule().getInfo().getDelegateClassName());

        duplicatedStep.createModule(commandService, moduleService);

        step.getParameters().forEach((key, value) -> {
            duplicatedStep.setParameter(key, value);
        });

        return duplicatedStep;

    }

   
    
    public Boolean executeStep(ProgressHandler handler,WorkflowStep step) throws InterruptedException, ExecutionException {
       
        handler = ProgressHandler.check(handler);

        handler.setStatus("Re-executing...");
        handler.setProgress(1, 10);
        //moduleService.run(step.getModule().getInfo(), false, step.getParameters());
        Module module = moduleService.createModule(step.getModule().getInfo());
        handler.setProgress(3,10);
        step.getParameters().forEach((key, value) -> {
            module.setInput(key, value);
            module.setResolved(key, true);
        });
        
        Future<Module> run = moduleService.run(module, true);
        run.get();
        handler.setProgress(10);
        return Boolean.TRUE;
    }

    public void executeModule(WorkflowStep step) {

        moduleService.run(step.getModule().getInfo(), true);
    }

    public static String getModuleLabel(Module module) {
        String[] possibleNames = new String[]{
            module.getInfo().getTitle(),
            module.getInfo().getName(),
            module.getInfo().getLabel(),
            FXUtilities.javaClassToName(module.getInfo().getDelegateClassName())
        };

        for (String name : possibleNames) {
            if (name != null && "".equals(name) == false) {
                return name;
            }
        }
        return "No name ?";
    }

    boolean isRunning() {
        return isExecutingWorkflow;
    }
}
