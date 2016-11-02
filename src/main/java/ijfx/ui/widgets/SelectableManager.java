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

import ijfx.ui.explorer.Selectable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class SelectableManager<T extends Selectable> {
 
    
    Map<T,SelectionListener> listeners = new HashMap<>();
    
    private BiConsumer<T,Boolean> consumer;

    
    private ObservableList<T> itemList = FXCollections.observableArrayList();
    
    
    public SelectableManager() {
        this.itemList.addListener(this::onItemListChanged);
    }
    
    public SelectableManager(BiConsumer<T, Boolean> consumer) {
        
        this();
        this.consumer = consumer;
        
        
        
    }
    
    
    
    
    public void onItemListChanged(ListChangeListener.Change<? extends T> change) {
        
        while(change.next()) {
            change.getAddedSubList().forEach(this::listen);
            change.getRemoved().forEach(this::stopListening);
        }
        
        
    }
    
    
    
    public void listen(T explorable) {
        
        SelectionListener listener = new SelectionListener(explorable);
        listeners.put(explorable, listener);
        
        explorable.selectedProperty().addListener(listener);
        if(explorable.selectedProperty().getValue() && listener != null) {
            consumer.accept(explorable,true);
        }
        
    }
    
    public void stopListening(T explorable) {
        SelectionListener listener = listeners.get(explorable);
        explorable.selectedProperty().removeListener(listener);
        listeners.remove(explorable);
    }
    
    public void setItem(Collection<? extends T> collection) {
        
        itemList.clear();
        itemList.addAll(collection);
        
    }
    
   
    
    private class SelectionListener implements ChangeListener<Boolean>{
        private final T exp;

        public SelectionListener(T exp) {
            this.exp = exp;
        }
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            if(consumer != null) consumer.accept(exp, newValue);
        }
    }
}
