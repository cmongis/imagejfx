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
package ijfx.ui.input.widgets;

import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.scijava.widget.InputWidget;

/**
 *
 * @author cyril
 */
public class ModelBinder<T> implements ChangeListener<T>{
    
    final InputWidget<T, ?> widget;

    Property<T> property;
    
    
    
    public ModelBinder(InputWidget<T, ?> widget) {
        this.widget = widget;
    }
    
    public ModelBinder(Property<T> property, InputWidget<T, ?> widget) {
        this(widget);
        
        
    }
    public ModelBinder<T> bind(Property<T> property) {
        
        
        property.setValue((T)widget.get().getValue());
        
        property.addListener(this);
        this.property = property;
        return this;
        
    }
    
    
    @Override
    public void changed(ObservableValue<? extends T> observable, T oldValue, T newValue) {
        widget.updateModel();
    }
    
    public  void refreshWidget(T t) {
        if(property.getValue() == null 
                || (property.getValue() != null && property.getValue().equals(t) == false))
        property.setValue(t);
    }
    
    public T getValue() {
        return property.getValue();
    }
    
    
    
    
}
