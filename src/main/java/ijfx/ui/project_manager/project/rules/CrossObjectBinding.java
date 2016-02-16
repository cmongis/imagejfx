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
package ijfx.ui.project_manager.project.rules;

import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;

/**
 *
 * @author cyril
 */
public abstract class CrossObjectBinding<A,B> {
    
    
    Property<A> firstProperty = new SimpleObjectProperty();
    
    Property<B> secondProperty = new SimpleObjectProperty();
    
    
    public CrossObjectBinding(Property<A> property1, Property<B> property2) {
        firstProperty.bindBidirectional(property1);
        secondProperty.bindBidirectional(property2);
        firstProperty.addListener(this::onFirstChanged);
        secondProperty.addListener(this::onSecondChanged);
    }
    
    private void onSecondChanged(Observable obs, B odlValue, B newValue) {
        firstProperty.setValue(convert(newValue));
    }
    private void onFirstChanged(Observable obs, A oldValue, A newValue) {
       secondProperty.setValue(convertBack(newValue));
    }
    abstract protected A convert(B b);
    abstract protected B convertBack(A a);
    
    private void unbind() {
        firstProperty.unbind();
        secondProperty.unbind();
    }
            
}
