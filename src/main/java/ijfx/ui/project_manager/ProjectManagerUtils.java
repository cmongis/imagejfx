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
package ijfx.ui.project_manager;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import ijfx.ui.main.ImageJFX;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Labeled;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 *
 * @author Cyril Quinton
 */
public interface ProjectManagerUtils {

    public static Stage createDialogWindow(Window ownerWindow, Pane content, String title) {
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(ownerWindow);
        stage.setTitle(title);
        Scene scene = new Scene(content);
        scene.getStylesheets().add(ImageJFX.STYLESHEET_ADDR);
        stage.setScene(scene);
        return stage;
    }

    public static String capitalize(String text) {
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    public static FontAwesomeIconView getSpinnerIcon() {
        FontAwesomeIconView spinner = new FontAwesomeIconView();
        spinner.setGlyphName("SPINNER");
        return spinner;
    }
    public static FontAwesomeIconView getRemoveIcon() {
         FontAwesomeIconView removeIcon = new  FontAwesomeIconView();
        removeIcon.setStyleClass("remove-icon");
        return removeIcon;
    }
    public static void tooltipLabeled(Labeled label) {
        Tooltip tooltip = new Tooltip();
        tooltip.textProperty().bind(label.textProperty());
        label.setTooltip(tooltip);
    }
}
