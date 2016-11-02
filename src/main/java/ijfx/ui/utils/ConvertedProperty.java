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
package ijfx.ui.utils;

import ijfx.core.utils.Converter;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.util.Callback;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class ConvertedProperty<A,B> implements Converter<A,B>{

    
    Callback<B,A> backward = b-> null;
    Callback<A,B> forward = a->null;
    
    ObjectProperty<A> frontProperty = new SimpleObjectProperty<>();
    ObjectProperty<B> backProperty = new SimpleObjectProperty<>();
    
    public ConvertedProperty() {
        
        frontProperty.addListener(this::onFrontChanged);
        backProperty.addListener(this::onBackChanged);
    }
    
    public ConvertedProperty<A,B> forward(Callback<A,B> callback) {
       this.forward = callback;
        return this;
    }
    public ConvertedProperty<A,B> backward(Callback<B,A> callback) {
        this.backward = callback;
        return this;
    }
    
    
    public void onFrontChanged(Observable value, A oldValue, A newValue) {
        Platform.runLater(()->backProperty.setValue(forward(newValue)));
    }
    
    public void onBackChanged(Observable value, B oldValue, B newValue) {
        Platform.runLater(()->frontProperty.setValue(backward(newValue)));
    }
    
    public void updateForward() {
        
        
        
    }
    
    @Override
    public A backward(B b) {
        return this.backward.call(b);
    }

    @Override
    public B forward(A a) {
        return this.forward.call(a);
    }

    public ObjectProperty<B> backProperty() {
        return backProperty;
    }
    public ObjectProperty<A> frontProperty() {
        return frontProperty;
    }
    
    
    
    
}
