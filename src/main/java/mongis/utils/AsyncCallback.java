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

import ijfx.ui.main.ImageJFX;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.concurrent.Task;
import javafx.util.Callback;

/**
 *
 * @author cyril
 */
public class AsyncCallback<INPUT, OUTPUT> extends Task<OUTPUT> implements ProgressHandler, Consumer<INPUT> {

    INPUT input;

    Callable<INPUT> inputGetter;
    
    Callback<INPUT, OUTPUT> callback;
    LongCallback<ProgressHandler, INPUT, OUTPUT> longCallback;
    Callback<ProgressHandler, OUTPUT> longCallable;

    Consumer<Throwable> errorHandler;
    Throwable error;

    Runnable runnable;

    ExecutorService executor = ImageJFX.getThreadPool();

    Consumer<OUTPUT> successHandler;
    
    private final static Logger logger = Logger.getLogger(AsyncCallback.class.getName());
    
    public AsyncCallback() {
        super();
    }

    public AsyncCallback(INPUT input) {
        this();
        setInput(input);
    }

    public AsyncCallback(Callback<INPUT, OUTPUT> callback) {
        this();
        this.callback = callback;
    }

    public AsyncCallback<INPUT,OUTPUT> setInput(Callable<INPUT> inputGetter) {
        this.inputGetter = inputGetter;
        return this;
    }
    
    public AsyncCallback<INPUT,OUTPUT> setName(String name) {
        updateTitle(name);
        updateMessage(name);
        return this;
    }

    public AsyncCallback<INPUT, OUTPUT> run(Callback<INPUT, OUTPUT> callback) {
        this.callback = callback;
        return this;
    }

    public AsyncCallback<INPUT, OUTPUT> run(Runnable runnable) {
        if (runnable == null) {
            System.out.println("Cannot run null :-S");
            return this;
        }
        this.runnable = runnable;
        return this;
    }

    public AsyncCallback<INPUT, OUTPUT> run(LongCallback<ProgressHandler, INPUT, OUTPUT> longCallback) {
        this.longCallback = longCallback;
        return this;
    }

    public AsyncCallback<INPUT, OUTPUT> runLongCallable(Callback<ProgressHandler, OUTPUT> longCallable) {
        this.longCallable = longCallable;
        return this;
    }

    public OUTPUT call() {

        // first we check if the task was cancelled BEFORE RUNNING IT
        if (isCancelled()) {
            return null;
        }

        if(inputGetter != null) try {
            input = inputGetter.call();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        
        if (longCallback != null) {
            OUTPUT output = longCallback.handle(this, input);

            if (isCancelled()) {
                return null;
            } else {
                return output;
            }

        } // now if we should execute a callback
        else if (callback != null) {
            // executing and betting the output
            OUTPUT output = callback.call(input);
            // if the task has been cancelled during execution,
            // we cancel the output and return null
            if (isCancelled()) {
                return null;
            } else {
                return output;
            }
        } else if (longCallable != null) {
            if (isCancelled()) {
                return null;
            } else {
                OUTPUT output = longCallable.call(this);
                if (isCancelled()) {
                    return null;
                } else {
                    return output;
                }
            }
        } // now if we have a runnable as part of the task
        else if (runnable != null) {
            runnable.run();
            // same if it was cancelled during the problem

        } else {
            return null;
        }
        return null;
    }
    
    @Override
    protected void failed() {
        super.failed();
        if(errorHandler != null) errorHandler.accept(getException());
    }

    public AsyncCallback<INPUT, OUTPUT> setInput(INPUT input) {
        this.input = input;
        return this;
    }

    public INPUT getInput() {
        return input;
    }

    public AsyncCallback<INPUT, OUTPUT> startIn(ExecutorService executorService) {
        executorService.execute(this);
        return this;
    }

    public AsyncCallback<INPUT, OUTPUT> start() {
        executor.execute(this);
        return this;
    }

    public AsyncCallback<INPUT, OUTPUT> queue() {
        executor = ImageJFX.getThreadQueue();
        executor.execute(this);
        return this;
    }

  
    @Override
    public void succeeded() {
        logger.info("succeeded");
         successHandler.accept(getValue());
        super.succeeded();
       
    }
    
    @Override
    public void cancelled() {
        logger.info("cancelled...");
        super.cancelled();
    }
    

    public AsyncCallback<INPUT, OUTPUT> then(Consumer<OUTPUT> consumer) {
        successHandler = consumer;
        return this;
    }
    
    public <NEXTOUTPUT> AsyncCallback<OUTPUT,NEXTOUTPUT> then(Callback<OUTPUT,NEXTOUTPUT> callback) {
        AsyncCallback<OUTPUT,NEXTOUTPUT> task = new AsyncCallback<OUTPUT,NEXTOUTPUT>()
                .setInput(this::getValue)
                .run(callback);
        then(task);
        return task;       
    }

    public AsyncCallback<INPUT, OUTPUT> ui() {
        if (Platform.isFxApplicationThread()) {
            call();
        } else {
            Platform.runLater(this);
        }

        return this;
    }

    @Override
    public void setProgress(double progress) {
        updateProgress(progress, 1);
    }

    @Override
    public void setProgress(double workDone, double total) {
        updateProgress(workDone, total);
    }

    @Override
    public void setProgress(long workDone, long total) {
        updateProgress(workDone, total);
    }

    @Override
    public void setStatus(String message) {
        updateMessage(message);
    }

    @Override
    public void accept(INPUT t) {

        logger.info("Consumed a new input "+t);
        setInput(t);
        start();
    }

    public AsyncCallback<INPUT, OUTPUT> error(Consumer<Throwable> handler) {
        errorHandler = handler;
        return this;
    }

    public AsyncCallback<INPUT, OUTPUT> setExecutor(ExecutorService executor) {
        this.executor = executor;
        return this;
    }

    public AsyncCallback<INPUT,OUTPUT> setIn(Property<Task> taskProperty) {
        Platform.runLater(()->taskProperty.setValue(this));
        return this;
    }
    
    public ExecutorService getExecutor() {
        return executor;
    }

}
