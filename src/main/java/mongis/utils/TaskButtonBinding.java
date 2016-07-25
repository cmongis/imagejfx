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

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventType;
import javafx.scene.control.Button;
import javafx.util.Callback;
import javafx.util.Duration;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class TaskButtonBinding {

    private Button button;

    private Callback<TaskButtonBinding, Task> taskFactory;

    private Runnable whenSucceed;

    private FontAwesomeIcon baseIcon = FontAwesomeIcon.ARROW_CIRCLE_RIGHT;

    private FontAwesomeIcon successIcon = FontAwesomeIcon.CHECK;

    private FontAwesomeIcon errorIcon = FontAwesomeIcon.REPEAT;

    private Task currentTask;

    private StringProperty textBeforeTaskProperty = new SimpleStringProperty("Run");

    private StringProperty textWhenSucceedProperty = new SimpleStringProperty("Done !");

    private StringProperty textWhenErrorProperty = new SimpleStringProperty("Retry");

    private StringProperty textWhenRunningProperty = new SimpleStringProperty("Cancel");

    private FontAwesomeIconView iconView = new FontAwesomeIconView(baseIcon);

    private ObjectProperty<EventType<WorkerStateEvent>> lastTaskStatus = new SimpleObjectProperty<>();

    private final Property<Worker.State> taskStateProperty = new SimpleObjectProperty(Worker.State.READY);

    private final Property<FontAwesomeIcon> iconProperty = new SimpleObjectProperty();

    public static final String BUTTON_PRIMARY_CLASS = "primary";
    public static final String BUTTON_SUCCESS_CLASS = "success";
    public static final String BUTTON_DANGER_CLASS = "danger";

    private final RotateTransition runningAnimation = new RotateTransition(Duration.seconds(1), iconView);
    
    public TaskButtonBinding(Button button) {
        this.button = button;

        textBeforeTaskProperty.setValue(button.getText());
        button.setGraphic(iconView);
        button.setOnAction(this::onClick);

        iconView.glyphNameProperty().bind(Bindings.createStringBinding(this::getCurrentIcon, taskStateProperty));
        button.textProperty().bind(Bindings.createStringBinding(this::getButtonString, taskStateProperty,textBeforeTaskProperty,textWhenErrorProperty,textWhenRunningProperty,textWhenRunningProperty));
        
        taskStateProperty.addListener(this::onTaskStateChanged);
        runningAnimation.setByAngle(360);
        runningAnimation.setCycleCount(-1);
        runningAnimation.setInterpolator(Interpolator.LINEAR);
    }

    
    public void onTaskStateChanged(Observable obs) {
        if(getWorkerState() == Worker.State.RUNNING) {
            runningAnimation.play();
        }
        else {
            runningAnimation.stop();
           iconView.setRotate(0.0);
        }
    }
    
    public Worker.State getWorkerState() {
        return taskStateProperty.getValue();
    }

    protected String getButtonString() {
        if (getWorkerState() == Worker.State.RUNNING) {
            return getTextWhenRunning();
        }
        if (getWorkerState() == Worker.State.FAILED) {
            return getTextWhenError();
        }
       else {
            return getTextBeforeTask();
        }
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
        if (currentTask == null || currentTask.isRunning() == false) {
            currentTask = taskFactory.call(this);
            
            if(currentTask == null) {
                
                throw new NullPointerException("The TaskFactory returned a null pointer");
                
            }
            
            taskStateProperty.bind(currentTask.stateProperty());
            if (!currentTask.isRunning()) {
                new Thread(currentTask).start();
            }
        }
        else {
            if(currentTask != null) currentTask.cancel();
        }
    }

    protected String getCurrentIcon() {
        if (getWorkerState() == Worker.State.RUNNING) {
            return FontAwesomeIcon.SPINNER.name();
        }
        if (getWorkerState() == Worker.State.SUCCEEDED) {
            return successIcon.name();
        } else {
            return baseIcon.name();
        }
    }

    public String getTextBeforeTask() {
        return textBeforeTaskProperty.getValue();
    }

    public TaskButtonBinding setTextBeforeTask(String initialText) {
        this.textBeforeTaskProperty.setValue(initialText);
        //button.setText(initialText);
        return this;
    }

    public String getTextWhenRunning() {
        return textWhenRunningProperty.getValue();
    }

    public TaskButtonBinding setTextWhenRunning(String whenRunningText) {
        this.textWhenRunningProperty.setValue(whenRunningText);;
        return this;
    }

    public String getTextWhenSucceed() {
        return textWhenSucceedProperty.getValue();
    }

    public TaskButtonBinding setTextWhenSucceed(String textWhenSucceed) {
        this.textWhenSucceedProperty.setValue(textWhenSucceed);
        return this;

    }

    public String getTextWhenError() {
        return textWhenErrorProperty.getValue();
    }

    public TaskButtonBinding setTextWhenError(String textWhenError) {
        this.textWhenErrorProperty.setValue(textWhenError);
        return this;
    }

    public Runnable getWhenSucceed() {
        return whenSucceed;
    }

    public TaskButtonBinding setWhenSucceed(Runnable whenSucceed) {
        this.whenSucceed = whenSucceed;
        return this;
    }

    public StringProperty textBeforeTaskProperty() {
        return textBeforeTaskProperty;
    }

    public StringProperty textWhenSucceedProperty() {
        return textWhenSucceedProperty;
    }

    public StringProperty textWhenErrorProperty() {
        return textWhenErrorProperty;
    }

    public StringProperty textWhenRunningProperty() {
        return textWhenRunningProperty;
    }

    
    
   

}
