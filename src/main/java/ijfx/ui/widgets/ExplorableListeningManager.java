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

import ijfx.ui.explorer.Explorable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author cyril
 */
public class ExplorableListeningManager {
 
    
    Map<Explorable,SelectionListener> listeners = new HashMap<>();
    
    private BiConsumer<Explorable,Boolean> consumer;

    
    private ObservableList<Explorable> itemList = FXCollections.observableArrayList();
    
    public ExplorableListeningManager(BiConsumer<Explorable, Boolean> consumer) {
        this.consumer = consumer;
    }
    
    public  
    
    
    
    public void listen(Explorable explorable) {
        
        SelectionListener listener = new SelectionListener(explorable);
        listeners.put(explorable, listener);
        
        explorable.selectedProperty().addListener(listener);
        if(explorable.selectedProperty().getValue() && consumer != null) {
            consumer.accept(explorable,true);
        }
        
    }
    
    public void stopListening(Explorable explorable) {
        SelectionListener listener = listeners.get(explorable);
        explorable.selectedProperty().removeListener(listener);
        listeners.remove(explorable);
    }
    
    
    private class SelectionListener implements ChangeListener<Boolean>{
        private final Explorable exp;

        public SelectionListener(Explorable exp) {
            this.exp = exp;
        }
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            consumer.accept(exp, newValue);
        }
    }
}
