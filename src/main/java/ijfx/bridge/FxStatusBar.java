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
package ijfx.bridge;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.Event;
import org.scijava.event.EventHandler;
import org.scijava.module.Module;
import org.scijava.module.event.ModuleCanceledEvent;
import org.scijava.module.event.ModuleFinishedEvent;
import org.scijava.module.event.ModuleStartedEvent;
import org.scijava.ui.StatusBar;
import mongis.utils.CountProperty;

/**
 * JavaFx implementation of Image Status Bar
 * 
 * This is mainly a ViewModel which widgets can bind to.
 * 
 * @author Cyril MONGIS, 2015
 */
public class FxStatusBar implements StatusBar {

    
    protected final SimpleDoubleProperty progressProperty = new SimpleDoubleProperty(0);

    protected final SimpleStringProperty statusProperty = new SimpleStringProperty("");

    // static instance of the status bar
    protected static FxStatusBar instance;

    // tells if anything is progressing
    protected final SimpleBooleanProperty isProgressing = new SimpleBooleanProperty(false);

    // count the number of modules running
    protected final CountProperty moduleCount = new CountProperty();

    // List of running modules
    protected final ObservableList<Module> moduleRunning = FXCollections.observableArrayList();

   // current submited task
    Task currentTask;
    
    // tells if the currently submitted task can be canceled
    BooleanProperty canCancel = new SimpleBooleanProperty(false);
    
   
    
    private FxStatusBar() {
        
     
       moduleCount.addListener(this::onModuleCountChange);
       progressProperty.addListener(this::onProgressChange);
       //isProgressProperty().addListener(event->logger.info(event.toString()));
    }

    Logger logger = getLogger(FxStatusBar.class.getName());

    public static FxStatusBar getInstance() {
        if (instance == null) {
            instance = new FxStatusBar();
        }
        return instance;
    }

    protected void onProgressChange(Observable obs, Number oldValue, Number newValue) {
        //logger.info(newValue.toString());
       // isProgressing.setValue(newValue.doubleValue() > 0.0 && newValue.doubleValue() < 1.0);
    }
    protected void onModuleCountChange(Observable obs, Number oldValue, Number newValue) {
       // logger.info(newValue.toString());
        isProgressing.setValue(newValue.doubleValue() > 0.0);
    }
    
    @Override
    public void setStatus(String string) {

        Platform.runLater(() -> statusProperty.setValue(string));

    }

    @Override
    public void setProgress(int i, int i1) {
        Platform.runLater(() -> progressProperty.setValue(1.0 * i / i1));
    }

    public StringProperty statusProperty() {
        return statusProperty;
    }

    public Property<Number> progressProperty() {
        return progressProperty;
    }
    
    public Property<Boolean> canCancelProperty() {
        return canCancel;
    }

    public void setIsProgressing(final boolean value) {
        
        
        Platform.runLater(() -> isProgressing.setValue(value));
    }
    
    public BooleanProperty isProgressProperty() {
        return isProgressing;
    }

    @EventHandler
    public void onEvent(ModuleStartedEvent event) {

        if (moduleRunning.contains(event.getModule())) {
            return;
        }
        incrementRunningModule();

        moduleRunning.add(event.getModule());

        logger.info("Module started");
    }

    @EventHandler
    public void onEvent(ModuleCanceledEvent event) {
        if(moduleRunning.contains(event.getModule())==false) return;
        decrementRunningModule();
        moduleRunning.remove(event.getModule());
        logger.info("Module canceled");
    }

    @EventHandler
    public void onEvent(ModuleFinishedEvent event) {
        if(moduleRunning.contains(event.getModule())==false) return;
        decrementRunningModule();
        moduleRunning.remove(event.getModule());
        logger.info("Module finished");
    }

    private void incrementRunningModule() {
        Platform.runLater(() -> {
            moduleCount.increment();
            logModuleRunning();
        });

    }

    public void logModuleRunning() {
        logger.info("Module running : " + moduleCount.intValue());
    }

    private void decrementRunningModule() {
        Platform.runLater(() -> {
            moduleCount.decrement();
            logModuleRunning();
        });
    }

    public CountProperty moduleCountProperty() {
        return moduleCount;
    }
    
    
   
    
    
  public void submitTask(final Task task, final boolean canCancel) {
            
        progressProperty().bind(task.progressProperty());
       
        if (task.isRunning()) {
            //System.out.println("The task is already running");
             isProgressing.setValue(true);
             currentTask = task;
             this.canCancel.setValue(canCancel);
        } else {
            task.addEventHandler(WorkerStateEvent.WORKER_STATE_RUNNING, ch -> {
                //System.out.println("The task starts");
                 currentTask = task;
                 
                 this.canCancel.setValue(canCancel);
                statusProperty().bind(task.messageProperty());
                isProgressing.setValue(true);
               
            });
            task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED,this::onTaskOver);
            task.addEventHandler(WorkerStateEvent.WORKER_STATE_CANCELLED,this::onTaskOver);
            task.addEventHandler(WorkerStateEvent.WORKER_STATE_FAILED, this::onTaskOver);
        }
       // System.out.println("End of submission");
    }

  public void onCancel(ActionEvent event) {
        if(currentTask!=null) {
            currentTask.cancel();
            currentTask = null;
            isProgressing.setValue(false);
        }
  }
  
  public void onTaskOver(Event event) {
    
        progressProperty().unbind();
        statusProperty().unbind();
        
          if(event.getEventType().equals(WorkerStateEvent.WORKER_STATE_CANCELLED)) {
              statusProperty().set("Cancel !");
      }
        
        isProgressing.setValue(false);
        canCancel.setValue(false);
        currentTask = null;
       
  }
}
