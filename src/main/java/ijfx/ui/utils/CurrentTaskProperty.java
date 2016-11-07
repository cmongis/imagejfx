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

import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventType;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class CurrentTaskProperty<T> extends SimpleObjectProperty<Task<T>>{
    
    
    private static final EventType[] endEvents = new EventType[] {
        WorkerStateEvent.WORKER_STATE_CANCELLED
            ,WorkerStateEvent.WORKER_STATE_FAILED
            ,WorkerStateEvent.WORKER_STATE_SUCCEEDED
    };
    
    
    public void submit(Task<T> task) {
        
        setValue(task);
        for(EventType eventType : endEvents) {
            task.addEventHandler(eventType, this::onFinished);
        }
        task.addEventHandler(WorkerStateEvent.WORKER_STATE_CANCELLED, this::onFinished);
        task.addEventHandler(WorkerStateEvent.WORKER_STATE_FAILED,this::onFinished);
        task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED,this::onFinished);
        
    }
    
    
    public void onFinished(WorkerStateEvent newEvent) {
         for(EventType eventType : endEvents) {
           getValue().removeEventHandler(eventType, this::onFinished);
        }
        setValue(null);
    }
    
    
    
    
}
