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
package ijfx.service.ui.angular;

import ijfx.ui.main.ImageJFX;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import mercury.core.Deferred;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class TaskWatcher {
    Deferred deferred;
    Task task;
    boolean mapToJSON = false;
    
    
    
    public TaskWatcher(Task task, Deferred derred) {
        this.deferred = derred;
        this.task = task;
        task.messageProperty().addListener((javafx.beans.value.ObservableValue<? extends java.lang.String> event, java.lang.String oldMessage, java.lang.String newMessage) -> {


            deferred.notifySimpleJSON("message", newMessage, "progress", task.getProgress());
        });
        task.progressProperty().addListener((event, oldProgress, newProgress) -> {
            deferred.notifySimpleJSON("message", task.getMessage(), "progress", newProgress);
        });
        
        
        task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED,event -> {            

            if(mapToJSON) {
                deferred.mapAndResolve(task.getValue());
            }
            else {
                deferred.resolveSimpleJSON("success", true);
            }
        });
        task.addEventHandler(WorkerStateEvent.WORKER_STATE_CANCELLED,event -> {   

            deferred.reject("canceled");
        });
        task.addEventHandler(WorkerStateEvent.WORKER_STATE_FAILED,event -> {   

            deferred.reject("failed");
        });
    }

    public TaskWatcher setMapToJSON(boolean mapToJSON) {
        this.mapToJSON = mapToJSON;
        return this;
    }

    
    
    
    public void startInParallel() {
        ImageJFX.getThreadPool().submit(task);
    }

    public void startInQueue() {
        ImageJFX.getThreadQueue().submit(task);
    }
    
}
