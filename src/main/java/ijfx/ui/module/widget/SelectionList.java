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
package ijfx.ui.module.widget;

import java.util.List;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

/**
 *
 * @author cyril
 */
public class SelectionList<T> extends VBox {
    
    ObservableList<T> selectedItems = FXCollections.observableArrayList();
    ObservableList<T> items = FXCollections.observableArrayList();
    Callback<T, ? extends ListToggle<T>> cellFactory;

    public SelectionList() {
        super();
        cellFactory = this::defaultCellFactory;
        getStyleClass().add("toggle-selection-list");
        items.addListener(this::onListItemChanged);
        setPrefWidth(200);
    }

    private void onListItemChanged(ListChangeListener.Change<? extends T> change) {
        while (change.next()) {
            List<? extends ListToggle<T>> toAdd = change.getAddedSubList().stream().map((value) -> cellFactory.call(value)).collect(Collectors.toList());
            List<Node> toRemove = change.getRemoved().stream().map( value -> getChildren().stream().filter((javafx.scene.Node node) -> ((ListToggle) node).getValue() == value).findFirst().orElse(null)).collect(Collectors.toList());
            getChildren().addAll(toAdd);
            getChildren().addAll(toRemove);
        }
    }

    private ListToggle<T> defaultCellFactory(T t) {
        return new ListToggle<>(selectedItems, t);
    }

    public ObservableList<T> getItems() {
        return items;
    }
    
    public ObservableList<T> getSelectedItems() {
        return selectedItems;
    }
    
}
