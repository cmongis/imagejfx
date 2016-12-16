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
package ijfx.service.ui;

import java.util.function.BiConsumer;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.util.Callback;
import org.scijava.Context;
import org.scijava.event.EventService;
import org.scijava.event.SciJavaEvent;
import org.scijava.plugin.Parameter;

/**
 *
 * @author cyril
 */
public class ControlableProperty<R, T> extends ObjectPropertyBase<T> {
    
    private Getter<T> getter;
    private Setter<T> setter;
    private BiConsumer<R, T> doubleSetter;
    private Callback<R, T> doubleGetter;
    private final Property<R> beanProperty = new SimpleObjectProperty<R>();
    
    
    @Parameter
    private EventService eventService;
    
    public ControlableProperty() {
        beanProperty().addListener(this::onBeanChanged);
    }

    public ControlableProperty<R, T> setBean(R bean) {
        beanProperty.setValue(bean);
        return this;
    }

    public Property<R> beanProperty() {
        return beanProperty;
    }

    public ControlableProperty<R, T> bindBeanTo(ObservableValue<R> property) {
        beanProperty.bind(property);
        return this;
    }
    
    
    public ControlableProperty<R, T> inject(Context context) {
        context.inject(this);
        return this;
    }
    
   

    @Override
    public void setValue(T t) {
        T oldValue = super.getValue();
        // avoid loop
        if (oldValue != t) {
            if (doubleSetter != null) {
                
                doubleSetter.accept(beanProperty.getValue(), t);
            } else if (setter != null) {
                setter.set(t);
            }
           
        }
        super.setValue(t);
        
    }

    public ControlableProperty<R,T> setSilently(T t) {
        super.setValue(t);
        return this;
    }

    public ControlableProperty<R, T> setGetter(Getter<T> g) {
        this.getter = g;
        getValue();
        return this;
    }

    public ControlableProperty<R, T> setCaller(Callback<R, T> callback) {
        doubleGetter = callback;
        getValue();
        return this;
    }

    public ControlableProperty<R, T> setBiSetter(BiConsumer<R, T> biConsumer) {
        doubleSetter = biConsumer;
        return this;
    }

    public ControlableProperty<R, T> setSetter(Setter<T> s) {
        this.setter = s;
        return this;
    }

    public void onBeanChanged(Observable obs, R oldBean, R newBean) {
        Platform.runLater(this::checkFromGetter);
    }

    @Override
    public Object getBean() {
        return beanProperty.getValue();
    }

    public T getValue() {
        T t;
        if (doubleGetter != null && beanProperty.getValue() != null) {
            t = doubleGetter.call(beanProperty.getValue());
        } else if (getter != null) {
            t = getter.get();
        } else {
            t = super.getValue();
        }
        return t;
    }

    @Override
    public String getName() {  
       return "";
    }

    
    
    public void checkFromGetter() {
        if(getValue() != super.getValue()) {
            Platform.runLater(()->super.setValue(getValue()));
        }
    }
    
}
