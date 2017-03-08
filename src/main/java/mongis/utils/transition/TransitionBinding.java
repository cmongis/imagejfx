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
package mongis.utils.transition;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Binding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.util.Duration;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class TransitionBinding<T> {

    protected ReadOnlyProperty<Boolean> booleanProperty;

    protected Property<T> onFalseValue = new SimpleObjectProperty<>();

    protected final Property<T> onTrueValue = new SimpleObjectProperty<>();

    protected Property<T> transitioned;
    protected ObservableValue<Boolean> listened;
    Timeline currentTimeline;

    protected Duration duration = new Duration(300);

    protected final BooleanProperty stateProperty = new SimpleBooleanProperty(false);

    public TransitionBinding() {

    }

    public TransitionBinding(T onFalse, T onTrue) {

        onFalseValue.setValue(onFalse);
        onTrueValue.setValue(onTrue);

    }

    public TransitionBinding<T> bindOnTrue(ReadOnlyProperty<T> onTrueProperty) {
        onTrueValue.bind(onTrueProperty);
        return this;
    }

    public TransitionBinding<T> bindOnFalse(Binding<T> binding) {
        onFalseValue.bind(binding);
        return this;
    }

    public TransitionBinding<T> bindOnFalse(ReadOnlyProperty<T> onFalseProperty) {
        onFalseValue.bind(onFalseProperty);
        return this;
    }

    public TransitionBinding<T> bind(Binding<Boolean> property, Property<T> toModify) {
        transitioned = toModify;
        listened = property;
        property.addListener(this::onValueChanged);
        Platform.runLater(this::update);
        return this;
    }

    public TransitionBinding<T> bind(ReadOnlyProperty<Boolean> property, Property<T> toModify) {
        transitioned = toModify;
        listened = property;
        property.addListener(this::onValueChanged);
        Platform.runLater(this::update);

        return this;
    }
    
    

    protected void update() {
        transitioned.setValue(getNewValue(listened.getValue()));
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

    protected T getNewValue(Boolean value) {
        return value ? onTrueValue.getValue() : onFalseValue.getValue();
    }

    public void onValueChanged(Observable o, Boolean oldValue, Boolean newValue) {

        if (currentTimeline != null) {
            currentTimeline.stop();
        }

        currentTimeline = new Timeline();

        T value = getNewValue(newValue);

        currentTimeline.getKeyFrames().add(new KeyFrame(duration, new KeyValue(transitioned, value)));

        // the state True is always set at the end of the transition while the state false
        // is always turn on before
        if (newValue) {
            currentTimeline.setOnFinished(event -> {
                stateProperty.setValue(newValue);
                currentTimeline = null;
            });
        } else {
            stateProperty.setValue(newValue);
        }
        currentTimeline.play();
    }

    public ReadOnlyBooleanProperty stateProperty() {
        return stateProperty;
    }

}
