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
package ijfx.service.ui.hint;

import javafx.event.Event;
import javafx.event.EventType;

/**
 *
 * @author cyril
 */
public class HintEvent  extends Event{
     
    public static final EventType<HintEvent> HINT_REQESTED = new EventType(ANY,"HINT_REQUESTED");
    
    public HintEvent(EventType<? extends Event> eventType) {
        super(eventType);
    }
    
    public HintEvent() {
        super(HINT_REQESTED);
    }
     
}
