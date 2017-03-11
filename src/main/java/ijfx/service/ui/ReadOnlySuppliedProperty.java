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

import static ijfx.service.usage.Usage.listener;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyObjectPropertyBase;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ObservableValue;

/**
 *
 * @author cyril
 */
public class ReadOnlySuppliedProperty<T> extends ReadOnlyObjectPropertyBase<T> {

    private ReadOnlyProperty bean;

    private T currentValue;

    private final Getter<T> getter;

    public ReadOnlySuppliedProperty(Getter<T> getter) {
        this.getter = getter;
    }

    public ReadOnlySuppliedProperty bindTo(Property property) {
        property.addListener(this::onBeanChanged);
        return this;
    }

    @Override
    public T get() {
        currentValue = getter.get();
        return currentValue;
    }

    @Override
    public Object getBean() {
        return bean != null ? bean.getValue() : null;
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void updateFromGetter() {
        T newValue = getter.get();
        if (newValue != currentValue) {
            currentValue = newValue;
        }
        fireValueChangedEvent();
    }

    public void onBeanChanged(ObservableValue value, Object oldValue, Object newValue) {
        Platform.runLater(this::updateFromGetter);
    }

}
