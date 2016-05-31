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
package ijfx.ui.module;

import ijfx.ui.main.ImageJFX;
import java.util.logging.Logger;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.util.Callback;
import org.scijava.Context;
import org.scijava.module.Module;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class InputDialog extends Dialog<Module> {

    ModuleConfigPane moduleConfigPane = new ModuleConfigPane();
    Module module;

    Logger logger = ImageJFX.getLogger();

    public InputDialog() {
        super();

        // adding the stylesheet
        getDialogPane().getStylesheets().add(ImageJFX.getStylesheet());

        // adding the buttons
        getDialogPane().getButtonTypes().add(ButtonType.OK);
        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        // linking the form validity to the dialog
        moduleConfigPane.validProperty().addListener((evt, oldValue, newValue) -> {
            getDialogPane().lookupButton(ButtonType.OK).setDisable(!newValue);

        });

        moduleConfigPane.getCloseButton().setVisible(false);

        // setting the content
        getDialogPane().setContent(moduleConfigPane);

    }

    public InputDialog(Module module, Context context) {
        this();
        this.module = module;
        context.inject(moduleConfigPane);
        moduleConfigPane.configure(module);

        moduleConfigPane.addEventHandler(InputEvent.CALLBACK, this::onCallbackRequested);

        setResultConverter(this::convertResult);

    }

    public Module convertResult(ButtonType param) {
        String moduleName = module.getDelegateObject().getClass().getSimpleName();

        if (param == ButtonType.OK) {
            logger.info("Validating parameters for : " + moduleName);
            moduleConfigPane.getHashMap().forEach((key, value) -> {

                module.setInput(key, value);
                module.setResolved(key, true);
            });
            return module;

        } else {
            logger.info("Cancelling " + moduleName);
            module.cancel();
            
        }
        return module;
    }

    public boolean canShow() {
        return moduleConfigPane.inputCount() > 0;
    }

    public void onCallbackRequested(InputEvent event) {

    }

}
