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
package ijfx.ui.widgets;

import javafx.beans.Observable;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class SimpleListCell<T> extends ListCell<T> {
    
    Callback<T, String> callback;

    public SimpleListCell(Callback<T, String> callback) {
        this.callback = callback;
        itemProperty().addListener(this::onItemChanged);
    }
    Label label = new Label();

    private void onItemChanged(Observable obs, T oldValue, T newValue) {
        if (newValue == null) {
            setGraphic(null);
        } else {
            setGraphic(label);
            label.setText(callback.call(newValue));
        }
    }

    public static <R> Callback<ListView<R>,ListCell<R>> createFactory(Callback<R,String> callback) {
        return (listview)->new SimpleListCell(callback);
    }
    
}
