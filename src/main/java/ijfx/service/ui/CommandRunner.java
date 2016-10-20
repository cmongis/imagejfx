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
package ijfx.service.ui;

import ijfx.bridge.FxUIPreprocessor;
import ijfx.service.batch.BatchService;
import ijfx.service.log.DefaultLoggingService;
import ijfx.service.workflow.WorkflowRecorderPreprocessor;
import ijfx.ui.main.ImageJFX;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import mongis.utils.CallbackTask;
import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.command.CommandInfo;
import org.scijava.command.CommandModule;
import org.scijava.command.CommandService;
import org.scijava.module.Module;
import org.scijava.module.ModuleService;
import org.scijava.module.process.InitPreprocessor;
import org.scijava.plugin.Parameter;
import static weka.gui.beans.BeanConnection.inputs;

/**
 *
 * @author cyril
 */
public class CommandRunner {

    @Parameter
    LoadingScreenService loadScreenService;

    @Parameter
    CommandService commandService;

    @Parameter
    ModuleService moduleService;

    @Parameter
    DefaultLoggingService loggerService;

    @Parameter
    BatchService batchService;

    HashMap<String, Object> params = new HashMap<>();

    Logger logger = ImageJFX.getLogger();

    private Module lastRun;

    String title = "Please wait...";
    
    public CommandRunner(Context context) {
        context.inject(this);
    }

    public CommandRunner set(String paramName, Object value) {
        params.put(paramName, value);
        return this;
    }

    public CommandRunner runSync(Class<? extends Command> command) {

        Module module = batchService.createModule(command);

        batchService.preProcessExceptFor(module, InitPreprocessor.class, FxUIPreprocessor.class, WorkflowRecorderPreprocessor.class);

        Future<Module> run = moduleService.run(module, false, params);
        try {
            lastRun = run.get();

        } catch (InterruptedException ex) {
            Logger.getLogger(CommandRunner.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(CommandRunner.class.getName()).log(Level.SEVERE, null, ex);
        }

        return this;
    }

    public <T> CallbackTask<Module, T> runAsync(Class<? extends Command> clazz, String outputName, boolean show) {

        Module module = batchService.createModule(clazz);

        batchService.preProcessExceptFor(module, InitPreprocessor.class, FxUIPreprocessor.class, WorkflowRecorderPreprocessor.class);

        Future<Module> run = moduleService.run(module, false, params);

        CallbackTask<Module, T> task = new CallbackTask<Module, T>()
                .setInput(module)
                .setName(title)
                .run(m->runModule(clazz,outputName));

        if (show) {
            task.submit(loadScreenService);
        }
        return task.start();
    }

    private CommandRunner setTitle(String title) {
        this.title = title;
        return this;
    }
    
    private <T> T runModule(Class<? extends Command> command, String outputName) throws Exception {

        Module module = batchService.createModule(command);

        batchService.preProcessExceptFor(module, InitPreprocessor.class, FxUIPreprocessor.class, WorkflowRecorderPreprocessor.class);
        Module finishedModule = moduleService.run(module, false, params).get();
        if (outputName != null) {
            return (T) finishedModule.getOutput(outputName);
        } else {
            return null;
        }
    }

    public <T> T getOutput(String name) {
        return (T) lastRun.getOutput(name);
    }

    public CommandRunner run(String title, Class<? extends Command> clazz, Object... params) {

        loadScreenService.frontEndTask(new CallbackTask<Object, Object>()
                .setName(title)
                .run(() -> {
                    try {
                        Future<CommandModule> run = commandService.run(clazz, true, params);
                        run.get();
                    } catch (Exception e) {
                        loggerService.severe(e);
                    }
                }).start());

        return this;
    }
}
