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
package ijfx.ui.filter.string;


import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;

/**
 *
 * @author Tuan anh TRINH
 */
public class ListCellCheckbox extends ListCell<Item> {

    CheckBox checkbox = new CheckBox();

    HBox box = new HBox(checkbox);
    
    // method called every time a checkbox is ticked or unticked
    Runnable onSelectedChanged;
    
    public ListCellCheckbox(Runnable onSelectedChanged) {
        box.getStyleClass().add("list-cell");
        itemProperty().addListener(this::onItemChanged);
        this.onSelectedChanged = onSelectedChanged;
        checkbox.setOnAction(this::onActionEvent);
    }

    
    
    public void onItemChanged(Observable obs, Item oldValue, Item newValue) {

        if (newValue == null) {
            setGraphic(null);
        } else {
            setGraphic(box);
            checkbox.textProperty().setValue(newValue.getName() + " (" + newValue.getNumber() + ")");
            checkbox.setSelected(newValue.getState());
        }
    }
    
    public void onActionEvent(ActionEvent event) {
       getItem().setState(checkbox.isSelected());
       onSelectedChanged.run();
    }

}
