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
package mongis.utils;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.util.Duration;

/**
 *
 * @author cyril
 */
public class TransitionBinding<T> {

    ReadOnlyProperty<Boolean> booleanProperty;

    Property<T> onFalseValue = new SimpleObjectProperty<>();

    Property<T> onTrueValue = new SimpleObjectProperty<>();

    Property<T> transitioned;

    Timeline currentTimeline;

    Duration duration = new Duration(300);

    BooleanProperty stateProperty = new SimpleBooleanProperty(false);

    public TransitionBinding(T onFalse, T onTrue) {

        onFalseValue.setValue(onFalse);
        onTrueValue.setValue(onTrue);

    }

    public TransitionBinding<T> bind(ReadOnlyProperty<Boolean> property, Property<T> toModify) {
        transitioned = toModify;

        property.addListener(this::onValueChanged);

        return this;
    }

    public TransitionBinding<T> setDuration(Duration t) {
        duration = t;
        return this;
    }

    public TransitionBinding<T> setOnTrue(T value) {
        onTrueValue.setValue(value);
        return this;
    }

    public TransitionBinding<T> setOnFalse(T value) {
        onFalseValue.setValue(value);
        return this;
    }

    public void onValueChanged(Observable o, Boolean oldValue, Boolean newValue) {
        
        if (currentTimeline != null) {
            currentTimeline.stop();
        }

        currentTimeline = new Timeline();

        T value = newValue ? onTrueValue.getValue() : onFalseValue.getValue();

        currentTimeline.getKeyFrames().add(new KeyFrame(duration, new KeyValue(transitioned, value)));
        currentTimeline.play();
        // the state True is always set at the end of the transition while the state fall
        // is always turn on before
        if (newValue) {
            currentTimeline.setOnFinished(event -> stateProperty.setValue(newValue));
        } else {
            stateProperty.setValue(newValue);
        }
    }

    public ReadOnlyBooleanProperty stateProperty() {
        return stateProperty;
    }

}
