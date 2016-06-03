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

import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.scene.control.ToggleButton;

/**
 *
 * @author cyril
 */
 public class ListToggle<T> extends ToggleButton {
        
        private final ObservableList<T> list;
        private final T value;
        public ListToggle(ObservableList<T> list, T value) {
            super();
            
            this.list = list;
            this.value = value;
            setText(value.toString());
            selectedProperty().addListener(this::onSelectionChanged);
            maxWidthProperty().setValue(Double.POSITIVE_INFINITY);
        }
        
        public void onSelectionChanged(Observable obs, Boolean oldValue, Boolean newValue) {
            if(newValue && list.contains(value) == false)list.add(value);
            else list.remove(value);
        }

        public T getValue() {
            return value;
        }
        
        
        
    }
