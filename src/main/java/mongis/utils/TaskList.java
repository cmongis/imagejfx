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

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import javafx.application.Application;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.Event;
import javafx.stage.Stage;

/**
 *
 * @author cyril
 */
public class TaskList extends Application{

    ObservableList<Task> runningList = FXCollections.observableArrayList();
    ObservableList<Task> waitingList = FXCollections.observableArrayList();
    ListProperty<Task> listProperty = new SimpleListProperty<>(runningList);
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
    
    
    public void submitTask(Task task) {
        System.out.println("Is task running ? "+task.isRunning());
        if (task.isRunning()) {
            runningList.add(task);

        } else {
            waitingList.add(task);
        }

    }

    public void onWaitingListChange(ListChangeListener.Change<? extends Task> change) {
        while (change.next()) {
            change.getAddedSubList().forEach(this::listenForStart);
            change.getRemoved().forEach(this::stopListening);
        }
    }

    public void onOngoingListChange(ListChangeListener.Change<? extends Task> change) {
        while (change.next()) {
            change.getAddedSubList().forEach(this::listenForStop);
            change.getRemoved().forEach(this::stopListening);
        }
    }

    public void listenForStart(Task task) {
        System.out.println("Listening for start : "+task.toString());
        task.addEventHandler(WorkerStateEvent.WORKER_STATE_RUNNING, this::onTaskStart);
    }

    public void onTaskStart(Event event) {
        Task task = (Task) event.getSource();
        System.out.println("Task has started : "+task);
        runningList.add(task);
    }

    public void listenForStop(Task task) {
        task.addEventHandler(WorkerStateEvent.ANY, this::onStop);
    }

    public void onStop(Event event) {
        
        Task task = (Task) event.getSource();
        System.out.println("Task has stop : "+task.toString());
        runningList.remove(task);
    }

    public void stopListening(Task task) {
        System.out.println("We stop listeneing for :"+task.toString());
        task.removeEventHandler(WorkerStateEvent.ANY, this::onStop);
        task.removeEventHandler(WorkerStateEvent.WORKER_STATE_RUNNING, this::onTaskStart);
    }
    
    
    public static void main(String... args) {
        
        launch(args);
        
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        TaskList taskList = new TaskList();
        
        
        System.out.println("Starting ...");
        taskList.submitTask(new FakeTask(5000).start());
        taskList.submitTask(new FakeTask(7000).start());
        taskList.submitTask(new FakeTask(6000).start());
        
        taskList.runningListCountProperty().addListener((obs,oldValue,newValue)->{
            System.out.println("Task running "+ newValue);
            if(newValue.intValue() < oldValue.intValue() && newValue.intValue() == 1) {
                Random r = ThreadLocalRandom.current();
                taskList.submitTask(new FakeTask(r.nextInt(10)*1000));
            }
        });
    }
    
}
