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
package ijfx.ui.segmentation;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.Property;

/**
 *
 * @author cyril
 */
public class ConfigurationMapper<T> {
        
    
    
    
    
    final HashMap<T,State> map = new HashMap<>();
    
    final Property<T> observable;
    
    final Property[] properties;
    
    final List<Consumer<T>> listener = new ArrayList<>();
    
    public ConfigurationMapper(Property<T> observable, List<Property> properties) {
        this(observable,properties.stream().toArray(s->new Property[s]));
    }
    
    public ConfigurationMapper(Property<T> observable, Property... properties) {
        
        
        this.observable = observable;
      
        observable.addListener(this::onObservableChanged);
        
        for(Property p : properties) {
            p.addListener(this::onPropertyChanged);
        }
                
        
        this.properties = properties;
        
    }
    
    public ConfigurationMapper<T> addPropertyListener(Consumer<T> propertyChangeListener) {
        
        listener.add(propertyChangeListener);
        return this;
        
        
    }
    
    
    private void onPropertyChanged(Observable obs, Object oldValue, Object newValue) {
        Platform.runLater(this::updateCurrentState);
        
        
        
    }
    
    private void onObservableChanged(Observable v, T oldValue, T newValue) {
        if(newValue == null) {
            return;
        }
        else {
            // the state is restored only if it existed before
            if(map.containsKey(newValue)) map.get(newValue).restore();
        }
    }
    
    
    
    
    
    private void updateCurrentState() {
        
        listener.forEach(l->l.accept(observable.getValue()));
        
        if(map.containsKey(observable.getValue()) == false) {
            map.put(observable.getValue(),new State());
        }
        
        map.get(observable.getValue()).updateState();
        
    }
        
    
    private class State {
        
        
        
        List<Object> values = new ArrayList<>();
        
        public State() {
            
        }
        
        
        public void updateState() {
            
           values  = Stream.of(properties)
                    .map(p->p.getValue())
                    .collect(Collectors.toList());
            
          
            
        }
     
        public void restore() {
            for(int i = 0;i!=properties.length; i++) {
                properties[i].setValue(values.get(i));
            }
        }
        
    }
    
}
