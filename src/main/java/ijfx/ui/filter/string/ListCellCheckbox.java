/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ijfx.ui.filter.string;


import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;

/**
 *
 * @author tuananh
 */
class ListCellCheckbox extends ListCell<Item> {

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
