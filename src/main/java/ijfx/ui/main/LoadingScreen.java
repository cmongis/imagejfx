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
package ijfx.ui.main;

import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import ijfx.ui.context.animated.TransitionQueueNG;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class LoadingScreen extends StackPane {

    Text iconNode;
    Rectangle rectangle;
    Transition iconTransition;
    RotateTransition rotateTransition;
    Label statusText = new Label("Charging...");
    Circle circle = new Circle(4 * 16);
    public static LoadingScreen singleton = new LoadingScreen();
    
    Pane defaultPane;
    Pane currentPane;
    
    Button cancelButton = new Button("Cancel");
    ProgressBar progressBar = new ProgressBar();
    
    TransitionQueueNG trQueue = new TransitionQueueNG();
    
    BooleanProperty canCancel = new SimpleBooleanProperty(false);
    
    public static LoadingScreen getInstance() {
        return singleton;
    }
    
    private class TaskRequest {
        Task task;
        boolean cancelable;
        Pane pane;

        public TaskRequest(Task task, boolean cancelable, Pane pane) {
            this.task = task;
            this.cancelable = cancelable;
            this.pane = pane;
        }
    }
    
    TaskRequest currentTaskRequest;
    
    LinkedList<TaskRequest> queue = new LinkedList<>();
    LinkedList<Transition> transitionQueue = new LinkedList<>();
    
    public final static String CSS_CLASS = "loading-screen";

    public LoadingScreen() {
        super();
        iconNode = GlyphsDude.createIcon(FontAwesomeIcon.CIRCLE_ALT_NOTCH);
        iconNode.setFill(Color.WHITE);
        iconNode.setScaleX(4);
        iconNode.setScaleY(4);
        rectangle = new Rectangle(400, 400);
        circle.setStrokeWidth(10);
        circle.setStroke(Color.WHITE);
        circle.setFill(null);
        rectangle.getStyleClass().add(ImageJFX.CSS_DARK_FILL);
        rectangle.setFill(Color.BLACK);
        statusText.setTranslateY(100);
        progressBar.setTranslateY(150);
        cancelButton.setTranslateY(200);
        
        cancelButton.visibleProperty().bind(canCancel);
        cancelButton.setOnAction(this::onCancel);
        getChildren().addAll(rectangle, iconNode, circle, statusText,cancelButton,progressBar);
        
        progressBar.setVisible(false);
        progressBar.progressProperty().addListener((obs,oldValue,newValue)->{
            if(newValue.doubleValue() == 0) {
                progressBar.setVisible(false);
            }
            else {
                progressBar.setVisible(true);
            }
        });
        
        getStyleClass().add(CSS_CLASS);
        
        animate();
    }

    
    
    public void animate() {
        // animating the loading thing
        rotateTransition = new RotateTransition(Duration.millis(1000), iconNode);
        rotateTransition.setByAngle(360);
        //rotateTransition.setCycleCount(300);
        rotateTransition.setInterpolator(Interpolator.LINEAR);
        double scale = 1.1;
        // circle animatino : hearbeat
        SequentialTransition st = new SequentialTransition();
        Duration halfTime = Duration.millis(MainWindowController.ANIMATION_DURATION / 3);
        ScaleTransition scaleUp = new ScaleTransition(halfTime, circle);
        scaleUp.setToX(scale);
        scaleUp.setToY(scale);
        ScaleTransition scaleDown = new ScaleTransition(halfTime, circle);
        scaleDown.setToX(1);
        scaleDown.setToY(1);
        st.getChildren().addAll(scaleUp, scaleDown);
        st.setDelay(Duration.millis(1000 - 300));
        //st.setCycleCount(20);
        ParallelTransition pt = new ParallelTransition(rotateTransition, st);
        pt.setCycleCount(300);
        iconTransition = pt;
    }

    public void onCancel(ActionEvent event) {
        currentTaskRequest.task.cancel();
    }
    
    public void setText(String text) {
        Platform.runLater(() -> statusText.textProperty().setValue(text));
    }

    public String getText() {
        return statusText.textProperty().getValue();
    }

    public void showOn(Pane node) {
        // rectangle.widthProperty().setValue(node.widthProperty().getValue());
        //rectangle.heightProperty().setValue(node.heightProperty().getValue());
      //  System.out.println("Is the loading screen already displayed ?");

        //System.out.println(node.getChildren().contains(this));
        if (node.getChildren().contains(this)) {
            
            return;
        }
        
        Bounds boundsInParent = node.getBoundsInParent();
        double with = boundsInParent.getWidth();
        double height = boundsInParent.getHeight();
        rectangle.setWidth(with);
        rectangle.setHeight(height);
        //MainWindowController.logger.info("" + node.getWidth());
        
        if(node.getChildren().contains(this) == false)   {
            
            node.getChildren().add(this);
            if(this.isVisible() == false) this.setVisible(true);
      
        }
        
        Platform.runLater(()->fadeIn());
        
    }

    public void show() {
        Platform.runLater(()->showOn(getDefaultPane()));
    }

    public void hide() {
        Platform.runLater(()->hideFrom(getDefaultPane()));
    }

    public void hideFrom(Pane node) {
        final LoadingScreen thisLoadingScreen = this;
        
        if(currentTaskRequest != null) return;
        
        
        
        
       fadeOut().setOnFinished(event->{
       
           node.getChildren().remove(thisLoadingScreen);
   
       });
       
    
       
        
    }

    public void fadeIn() {
        //this.setOpacity(0.0);
    
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), this);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        
       // iconTransition.play();
       fadeIn.play();
        iconTransition.play();
        //queueTransition(fadeIn);
        //trQueue.queue(fadeIn);
    }

    public Transition fadeOut() {
  
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), this);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.play();
        iconTransition.stop();
        //trQueue.queue(fadeOut);
        
        return fadeOut;
    }
    
    
    public static void submit(Task task, boolean canCancel) {
        submit(task, getInstance().getDefaultPane());
    }
    
    public static void submit(Task task, Pane pane) {
        getInstance().submitTask(task, pane);
    }
    
    public void submitTask(Task task) {
        submitTask(task, defaultPane);
    }

    public void submitTask(Task task, boolean canCancel) {
        submitTask(task, canCancel,getDefaultPane());
    }
    
    public void submitTask(Task task, Pane pane) {
        submitTask(task, true, pane);
    }
    
    
    public void submitTask(Task task, boolean canCancel, Pane pane) {
         
        TaskRequest request = new TaskRequest(task, canCancel, pane);
        
        if(currentTaskRequest == null) {
     
            loadTask(request);
        }
        else
        queue.add(request);
  
    }
    
    
    private void loadTask(TaskRequest request) {
       
    
        currentTaskRequest = request;
        
        final boolean canCancel = request.cancelable;
        final Task task = request.task;
        final Pane pane = request.pane;
       
        progressBar.progressProperty().bind(task.progressProperty());
       
        if (task.isRunning()) {
            showOn(pane);
             
             this.canCancel.setValue(canCancel);
        } else {

            task.addEventHandler(WorkerStateEvent.WORKER_STATE_RUNNING, ch -> {
                 
                 this.canCancel.setValue(canCancel);
                statusText.textProperty().bind(task.messageProperty());
                showOn(pane);
               
            });
            
            EventHandler<WorkerStateEvent> handler = event->{
                    onTaskOver();
                    Executors.newScheduledThreadPool(1).schedule(()->Platform.runLater(()->hideFrom(pane)), 200, TimeUnit.MILLISECONDS);
                    
            };
            
            task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, handler);
            task.addEventHandler(WorkerStateEvent.WORKER_STATE_CANCELLED, handler);
            task.addEventHandler(WorkerStateEvent.WORKER_STATE_FAILED, handler);
        }
        
    }
    
    public void onTaskOver() {
          statusText.textProperty().unbind();
          progressBar.setVisible(false);
          currentTaskRequest = null;
          nextTask();
    }
    
    public void nextTask() {
        
       
        
        if(queue.size() > 0)
        loadTask(queue.poll());
    }

    public void queueTransition(Transition tr) {
   
        if(transitionQueue.size() ==0) tr.play();
         transitionQueue.add(tr);
        tr.onFinishedProperty().addListener(this::onQueueEnded);
       
    }
    
    public void onQueueEnded(Observable event) {
    
        transitionQueue.poll();
        if(transitionQueue.size() > 0);
        transitionQueue.get(0).play();
    }
    
    public Pane getDefaultPane() {
        return defaultPane;
    }

    public void setDefaultPane(Pane defaultPane) {
        this.defaultPane = defaultPane;
    }

    public void onFadeOutFinished(ActionEvent event) {
    }

}
