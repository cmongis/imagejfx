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
package ijfx.ui.project_manager.other;

import ijfx.ui.main.ImageJFX;
import java.io.IOException;
import java.util.logging.Level;
import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import mongis.utils.FXUtilities;

/**
 *
 * @author Cyril Quinton
 */
public class SelectablePane extends AnchorPane implements SelectableController {

    @FXML
    private CheckBox checkBox;
    @FXML
    private Pane contentPane;

    public SelectablePane() {
        try {
            FXUtilities.injectFXML(this)
                    ;
        } catch (IOException ex) {
            ImageJFX.getLogger().log(Level.SEVERE, null, ex);
        }
       FXUtilities.loadView(getClass().getResource("SelectablePane.fxml"), this, true);
    }

    @Override
    public BooleanProperty selectedProperty() {
        return checkBox.selectedProperty();
    }

    @Override
    public Object getContent() {
        return contentPane.getChildren().isEmpty() ? null : contentPane.getChildren().get(0);
    }

    @Override
    public void setContent(AnchorPane content) {
        FXUtilities.emptyPane(contentPane);
        contentPane.getChildren().add(content);
    }
}
