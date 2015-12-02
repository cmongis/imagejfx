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
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.util.Callback;
import org.scijava.ui.DialogPrompt;
import org.scijava.ui.DialogPrompt.Result;
import mongis.utils.FXUtilities;

/**
 * JavaFx Implementation of ImageJ DialogPrompt
 *
 * @author Cyril MONGIS
 */
public class FxPromptDialog extends Dialog<Result> implements DialogPrompt {

    public FxPromptDialog(String message, String title, DialogPrompt.MessageType mt, DialogPrompt.OptionType ot) {

        super();

        setTitle(title);
        setContentText(message);

        switch (ot) {
            case YES_NO_OPTION:
                addButton(ButtonType.YES);
                addButton(ButtonType.NO);
                break;
            case OK_CANCEL_OPTION:
                addButton(ButtonType.OK);
                addButton(ButtonType.CANCEL);
                break;
            case YES_NO_CANCEL_OPTION:
                addButton(ButtonType.OK);
                addButton(ButtonType.NO);
                addButton(ButtonType.CANCEL);
                break;

            case DEFAULT_OPTION:
                addButton(ButtonType.OK);
                break;

        }

        setResultConverter(new Callback<ButtonType, Result>() {

            @Override
            public Result call(ButtonType param) {

                if (param == ButtonType.OK) {
                    return Result.OK_OPTION;
                }
                if (param == ButtonType.YES) {
                    return Result.YES_OPTION;
                }
                if (param == ButtonType.NO) {
                    return Result.NO_OPTION;
                }
                if (param == ButtonType.CANCEL) {
                    return Result.CANCEL_OPTION;
                }
                if (param == ButtonType.CLOSE) {
                    return Result.CLOSED_OPTION;
                }

                return Result.CLOSED_OPTION;

            }
        });

    }

    public void addButton(ButtonType type) {
        getDialogPane().getButtonTypes().add(type);
    }
    Result result;

    @Override
    public Result prompt() {

        try {
            FXUtilities.runAndWait(() -> {
                result = showAndWait().get();
            });
        } catch (InterruptedException ex) {
            ImageJFX.getLogger().log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
           ImageJFX.getLogger().log(Level.SEVERE, null, ex);
        }

        return result;
    }

}
