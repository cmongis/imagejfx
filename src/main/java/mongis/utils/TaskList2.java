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

import java.util.HashMap;
import java.util.Map;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class TaskList2 {
    
    ObservableList<Task> running = FXCollections.observableArrayList();
    ObservableList<Task> waiting = FXCollections.observableArrayList();
    
    ObjectProperty<Task> foreground = new SimpleObjectProperty();
    
    Map<ObservableValue,Task> propertyToTask = new HashMap<>(); 
    
    public TaskList2() {
          running.addListener(this::onOngoingListChange);
    }
    
    public void submitTask(Task task) {
        if(task.isDone()) return;
        
        startListening(task);
        if(task.isRunning()) running.add(task);
        else waiting.add(task);
      
        
        
    }
    
    private void startListening(Task task) {
        task.runningProperty().addListener(this::onTaskRunningPropertyChanged);
        propertyToTask.put(task.runningProperty(), task);
    }
    
    private void stopListening(Task task) {
        task.runningProperty().removeListener(this::onTaskRunningPropertyChanged);
        propertyToTask.remove(task.runningProperty());
    }
    
    private void onTaskRunningPropertyChanged(ObservableValue<? extends Boolean> value, Boolean oldValue, Boolean isRunning) {
        
        Task task = propertyToTask.get(value);
        
       if(isRunning) {
           running.add(task);
       }
       else {
           running.remove(task);
           stopListening(task);
       }
    }
    
   private void onOngoingListChange(ListChangeListener.Change<? extends Task> change) {
        while (change.next()) {            
           
        }
        
         if (running.size() > 0) {
                foreground.setValue(running.get(0));
            } else {
                foreground.setValue(null);
            }
    }

    public Property<Task> foregroundTaskProperty(){
        return foreground;
    }
    
   
   
    
}
