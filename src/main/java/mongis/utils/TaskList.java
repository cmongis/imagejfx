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

import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.Event;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class TaskList  {

    private final ObservableList<Task> runningList = FXCollections.observableArrayList();
    private final ObservableList<Task> waitingList = FXCollections.observableArrayList();
    private final ListProperty<Task> listProperty = new SimpleListProperty<>(runningList);

    private final ObjectProperty<Task> foregroundTask = new SimpleObjectProperty<>();

    public TaskList() {
        runningList.addListener(this::onOngoingListChange);
        waitingList.addListener(this::onWaitingListChange);
    }

    public ObservableList<Task> runningListProperty() {
        return runningList;
    }

    public ListProperty<Task> listProperty() {
        return listProperty;
    }

    public ReadOnlyIntegerProperty runningListCountProperty() {
        return listProperty.sizeProperty();
    }
    
    public ReadOnlyObjectProperty foregroundTaskProperty() {
        return foregroundTask;
    }

    public void submitTask(Task task) {
       
        if (task.isRunning()) {
            runningList.add(task);

        } else {
            waitingList.add(task);
        }

    }

    private void onWaitingListChange(ListChangeListener.Change<? extends Task> change) {
        while (change.next()) {
            change.getAddedSubList().forEach(this::listenForStart);
            change.getRemoved().forEach(this::stopListening);
        }
    }

    private void onOngoingListChange(ListChangeListener.Change<? extends Task> change) {
        while (change.next()) {
            
            
            change.getAddedSubList().forEach(this::listenForStop);
            change.getRemoved().forEach(this::stopListening);
            if (runningList.size() > 1) {
                foregroundTask.setValue(runningList.get(0));
            } else {
                foregroundTask.setValue(null);
            }
        }
    }

    private void listenForStart(Task task) {
       
        task.addEventHandler(WorkerStateEvent.WORKER_STATE_RUNNING, this::onTaskStart);
    }

    private void onTaskStart(Event event) {
        Task task = (Task) event.getSource();
        
        runningList.add(task);
    }

    private void listenForStop(Task task) {
        task.addEventHandler(WorkerStateEvent.ANY, this::onStop);
    }

    private void onStop(Event event) {

        Task task = (Task) event.getSource();
        
        runningList.remove(task);
    }

    private void stopListening(Task task) {
        
        task.removeEventHandler(WorkerStateEvent.ANY, this::onStop);
        task.removeEventHandler(WorkerStateEvent.WORKER_STATE_RUNNING, this::onTaskStart);
    }

   

}
