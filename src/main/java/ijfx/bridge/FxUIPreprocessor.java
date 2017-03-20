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
package ijfx.bridge;

import ijfx.ui.main.ImageJFX;
import ijfx.ui.module.InputDialog;
import ijfx.ui.module.InputSkinPluginService;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.concurrent.Task;
import mongis.utils.CallbackTask;
import net.imagej.display.ImageDisplayService;

import org.scijava.Context;
import org.scijava.Priority;
import org.scijava.command.InteractiveCommand;
import org.scijava.display.DisplayService;
import mongis.utils.FXUtilities;
import org.scijava.module.Module;
import org.scijava.module.ModuleService;
import org.scijava.module.process.AbstractPreprocessorPlugin;
import org.scijava.module.process.PreprocessorPlugin;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;

/**
 * The FXUIProcessor generates dialogs for inputing plugins parameters.
 *
 * @author Cyril MONGIS, 2015
 */
//@Plugin(type = PreprocessorPlugin.class, priority = Priority.VERY_LOW_PRIORITY)
public class FxUIPreprocessor extends AbstractPreprocessorPlugin {

    @Parameter
    PluginService pluginService;

    @Parameter
    DisplayService displayService;

    @Parameter
    ImageDisplayService imageDisplayService;

    @Parameter
    ModuleService moduleService;

    @Parameter
    Context context;

    Logger logger = ImageJFX.getLogger();

    @Parameter
    InputSkinPluginService inputSkinPluginService;

    @Override
    public void process(Module module) {

        logger.info("Preprocessing : " + module.getDelegateObject().getClass().getSimpleName());
        logger.info(module.getDelegateObject().getClass().getName());

        try {

            // checking how many inputs should be handled by the UI
            int handledInputs = inputSkinPluginService.getHandledInputs(module);

            // checking that the input has been injected
            logger.info("Input handled by the UI : " + handledInputs);

            // A dialog is generated only if there are skin for at least one of the parameters
            if (handledInputs == 0) {
                logger.info("Coulnd't produce interface for " + module.getDelegateObject().getClass().getName());
                return;
            }
            
            
            Task<InputDialog> task = new CallbackTask<Object, InputDialog>()
                    .run(objectNull-> new InputDialog(module,context));
            Platform.runLater(task);
                    
            
            InputDialog dialog = task.get();
            
            if(module.getDelegateObject() instanceof InteractiveCommand) {
                logger.info("Showing dialog and moving on");
                Platform.runLater(dialog::show);
            }
            else {
                logger.info("Showing dialog and wait");
                FXUtilities.runAndWait(dialog::showAndWait);
            }
            /*
            FXUtilities.runAndWait(() -> {
                InputDialog dialog = new InputDialog(module, context);

                if (InteractiveCommand.class.isAssignableFrom(module.getDelegateObject().getClass())) {
                    dialog.show();
                } else {
                    dialog.showAndWait();
                }

            });*/
        } catch (Exception ex) {
            ImageJFX.getLogger().log(Level.SEVERE, null, ex);
        }
    }

}
