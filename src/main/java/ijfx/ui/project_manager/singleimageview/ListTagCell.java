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
package ijfx.ui.project_manager.singleimageview;


import ijfx.ui.project_manager.other.EditHandler;
import ijfx.ui.project_manager.other.EditableTextField;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;

/**
 *
 * @author Cyril Quinton
 */
public class ListTagCell extends ListCell<String> {
    private final EditHandler editHandler;

    public ListTagCell(EditHandler editHandler) {
        this.editHandler = editHandler;
       
    }

    @Override
    public void updateItem(String string, boolean empty) {
        super.updateItem(string, empty);
        if (string == null || empty) {
            setGraphic(null);
            setText(null);
        }
        else {
            setGraphic( new SimpleListTagCell(editHandler, string));
        } 
    }
}
