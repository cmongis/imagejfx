/*
 * /*
 *     This file is part of ImageJ FX.
 *
 *     ImageJ FX is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     ImageJ FX is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with ImageJ FX.  If not, see <http://www.gnu.org/licenses/>. 
 *
 * 	Copyright 2015,2016 Cyril MONGIS, Michael Knop
 *
 */
package ijfx.ui.module;

import ijfx.ui.module.input.Input;
import javafx.event.Event;
import javafx.event.EventType;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class InputEvent extends Event {
    public static final EventType<InputEvent> ALL = new EventType(ANY,"ALL");
    public static final EventType<InputEvent> FIELD_CHANGED = new EventType(ALL, "FIELD_CHANGED");
    public static final EventType<InputEvent> CALLBACK = new EventType(ALL,"CALLBACK");
    Input input;
    Object value;

    
    
    public InputEvent(EventType<? extends Event> eventType) {
        super(FIELD_CHANGED);
    }

    public InputEvent(Input input, Object value) {
        super(FIELD_CHANGED);
        setInput(input);
        setValue(value);
    }

    public Input getInput() {
        return input;
    }

    public void setInput(Input input) {
        this.input = input;
    }

    public Object getValue() {
        return value;
    }

    public InputEvent setValue(Object value) {
        this.value = value;
        return this;
    }

}
