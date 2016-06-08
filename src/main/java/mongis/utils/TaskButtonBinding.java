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
package mongis.utils;

import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.control.Button;
import javafx.util.Callback;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class TaskButtonBinding {
    
    private Button button;

   
    
    private Callback<TaskButtonBinding,Task> taskFactory;
    
    private Runnable whenSucceed;
    
    
    private FontAwesomeIcon baseIcon = null;
    
    private FontAwesomeIcon successIcon = FontAwesomeIcon.CHECK;

    private FontAwesomeIcon errorIcon = FontAwesomeIcon.REPEAT;
    
    private Task currentTask;
    
    private String textBeforeTask;
    
    private String textWhenSucceed;
    
    private String textWhenError;
    
    private String textWhenRunning = "Cancel";
   
    private ObjectProperty<EventType<WorkerStateEvent>> lastTaskStatus = new SimpleObjectProperty<>();
    
    
    private static final EventType<WorkerStateEvent> TASK_READY = WorkerStateEvent.WORKER_STATE_READY;
    private static final EventType<WorkerStateEvent> TASK_SUCCESS = WorkerStateEvent.WORKER_STATE_SUCCEEDED;
    private static final EventType<WorkerStateEvent> TASK_FAILED = WorkerStateEvent.WORKER_STATE_FAILED;
    private static final EventType<WorkerStateEvent> TASK_CANCELED = WorkerStateEvent.WORKER_STATE_CANCELLED;
    private static final EventType<WorkerStateEvent> TASK_RUNNING = WorkerStateEvent.WORKER_STATE_RUNNING;
    
    
    public static final String BUTTON_PRIMARY_CLASS = "primary";
    public static final String BUTTON_SUCCESS_CLASS = "success";
    public static final String BUTTON_DANGER_CLASS = "danger";
    
     public TaskButtonBinding(Button button) {
        this.button = button;
        
        textBeforeTask = button.getText();
         
         button.setOnAction(this::onClick);
         
         lastTaskStatus.addListener(this::onLastTaskStatusChange);
         
     }
    
    
    public Callback<TaskButtonBinding, Task> getTaskFactory() {
        return taskFactory;
    }

    public TaskButtonBinding setTaskFactory(Callback<TaskButtonBinding, Task> onClick) {
        this.taskFactory = onClick;
        return this;
    }

    

    public FontAwesomeIcon getBaseIcon() {
        return baseIcon;
    }

    public TaskButtonBinding setBaseIcon(FontAwesomeIcon baseIcon) {
        this.baseIcon = baseIcon;
        button.setGraphic(new FontAwesomeIconView(baseIcon));
        return this;
    }

    public FontAwesomeIcon getSuccessIcon() {
        return successIcon;
    }

    public TaskButtonBinding setSuccessIcon(FontAwesomeIcon successIcon) {
        this.successIcon = successIcon;
        return this;
    }
    
    protected void onClick(ActionEvent event) {
       
        
        
        if(currentTask == null || currentTask.isDone()) {
            currentTask = taskFactory.call(this); 
        }
        
        currentTask.addEventHandler(WorkerStateEvent.ANY, this::onTaskStatusChange);
            
        
        if(currentTask.isRunning()) {
            lastTaskStatus.set(TASK_RUNNING);
        }
        else {
            new Thread(currentTask).start();
        }
        
        
    }

    public String getTextBeforeTask() {
        return textBeforeTask;
    }

    public TaskButtonBinding setTextBeforeTask(String initialText) {
        this.textBeforeTask = initialText;
        button.setText(initialText);
        return this;
    }

    public String getTextWhenRunning() {
        return textWhenRunning;
    }

    public TaskButtonBinding setTextWhenRunning(String whenRunningText) {
        this.textWhenRunning = whenRunningText;
        return this;
    }

    public String getTextWhenSucceed() {
        return textWhenSucceed;
    }

    public TaskButtonBinding setTextWhenSucceed(String textWhenSucceed) {
        this.textWhenSucceed = textWhenSucceed;
        return this;
               
    }

    public String getTextWhenError() {
        return textWhenError;
    }

    public TaskButtonBinding setTextWhenError(String textWhenError) {
        this.textWhenError = textWhenError;
        return this;
    }

    
    
    
    public Runnable getWhenSucceed() {
        return whenSucceed;
    }

    public TaskButtonBinding setWhenSucceed(Runnable whenSucceed) {
        this.whenSucceed = whenSucceed;
        return this;
    }
    
    
    
    public void onTaskStatusChange(Event event) {
        
      
             lastTaskStatus.setValue((EventType<WorkerStateEvent>)event.getEventType());
        
    }
    
    public void onLastTaskStatusChange(Observable obs, EventType<WorkerStateEvent> oldValue,  EventType<WorkerStateEvent> newValue) {

        if(newValue == TASK_RUNNING) {
            button.getStyleClass().removeAll(BUTTON_DANGER_CLASS,BUTTON_SUCCESS_CLASS);
            button.setText(textWhenRunning);
            button.setGraphic(GlyphsDude.createIcon(FontAwesomeIcon.SPINNER));
            
            
            
        }
        else if(newValue == TASK_SUCCESS) {
            
            if(textWhenSucceed != null) {
                button.getStyleClass().removeAll(BUTTON_DANGER_CLASS,BUTTON_PRIMARY_CLASS);
                button.getStyleClass().add(BUTTON_SUCCESS_CLASS);
                button.setGraphic(GlyphsDude.createIcon(successIcon));
                button.setText(textWhenSucceed);
            
            }
            else {
                button.setText(textBeforeTask);
                
            }
            
            if(whenSucceed != null) whenSucceed.run();
            
        }
        
        else if(newValue == TASK_FAILED && textWhenError != null){
            button.setText(textWhenError);
            button.getStyleClass().removeAll(BUTTON_PRIMARY_CLASS,BUTTON_SUCCESS_CLASS);
            button.getStyleClass().add(BUTTON_DANGER_CLASS);
            button.setGraphic(GlyphsDude.createIcon(errorIcon));
            
            
        }
        else {
            button.setText(textBeforeTask);
            button.setGraphic(null);
        }
        
        
    }
    
    
    
    
    
    
}
