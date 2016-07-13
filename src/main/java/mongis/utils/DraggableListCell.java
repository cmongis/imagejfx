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
package mongis.utils;

import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public abstract class DraggableListCell<T extends Object> extends ListCell<T>{

    
    Node node;
    
    public DraggableListCell() {
        ListCell<T> thisCell = this;
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        makeNodeDraggable(this);
        
        itemProperty().addListener(this::onItemChanged);
        
        
        selectedProperty().addListener(this::onSelectionChanged);
        
    }
    
    
    protected abstract void onItemChanged(Observable obs, T oldValue, T newValue);
    
    public DraggableListCell(Node node) {
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        makeNodeDraggable(node);
        this.node = node;
    }

    public void onSelectionChanged(Observable obs) {
       
        if(getGraphic() != null)
            FXUtilities.toggleCssStyle(getGraphic(),"selected",isSelected());
    }
    
    
    public void makeNodeDraggable(Node node) {

        this.node = node;
        setAlignment(Pos.BOTTOM_LEFT);
        node.setOnDragDetected((event) -> {
        });
        
        node.setOnDragDetected(this::whenDragDetected);
        node.setOnDragOver(this::whenDragOver);
        node.setOnDragEntered(this::whenDragEntered);
        node.setOnDragExited(this::whenDragExited);
        node.setOnDragDropped(this::whenDragDropped);
        node.setOnDragDone(this::whenDragDone);
    }
    
    public void whenDragDetected(MouseEvent event) {
       
        if (getItem() == null) {
            return;
        }
        
       
        
        ObservableList<T> items = getListView().getItems();
        Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
       
        dragboard.setDragView(node.snapshot(null, null));
        ClipboardContent content = new ClipboardContent();
        content.putString("" + getListView().getItems().indexOf(getItem()));

        //dragboard.setDragView();
        dragboard.setContent(content);
        event.consume();
    }

    public void whenDragEntered(DragEvent event) {
        if (event.getGestureSource() != this && event.getDragboard().hasString()) {
            setOpacity(0.3);
        }
    }

    public void whenDragOver(DragEvent event) {
        if (event.getGestureSource() != this && event.getDragboard().hasString()) {
            event.acceptTransferModes(TransferMode.MOVE);
        }
        event.consume();
    }

    public void whenDragExited(DragEvent event) {
        if (event.getGestureSource() != this && event.getDragboard().hasString()) {
            setOpacity(1);
        }
    }

    public void whenDragDropped(DragEvent event) {
        if (getItem() == null) {
            return;
        }
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasString()) {

            ObservableList<T> items = getListView().getItems();

            int draggedId = Integer.parseInt(db.getString());
            T dragged = items.get(draggedId);
            items.remove(draggedId);
            int shiftedId = items.indexOf(getItem());

            if (shiftedId == -1) {
                items.add(dragged);
            } else {
                items.add(shiftedId, dragged);
            }

            success = true;
        }
        event.setDropCompleted(success);
        event.consume();
    }

    public void whenDragDone(DragEvent event) {
        event.consume();
    }

}
