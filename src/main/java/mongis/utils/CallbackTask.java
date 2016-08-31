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
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.concurrent.Task;
import javafx.util.Callback;

/**
 * The callback task allows you to easily run a method/lambda in a new thread
 * and use the result in the FX Application Thread.
 *
 * E.g.
 *
 * Let say I want to process a list and transform it into a node that I will
 * later add to my view.
 *
 *
 * HBox hbox = ...
 *
 * List<String> myList = ...
 *
 * new CallbackTask<List<String>>,List<Node>>() .setInput(myList)
 * .run(list->list .stream() .map(str->new Label(str))
 * .collect(Collectors.toList())) .then(hbox::addAll); // equivalent of
 * .then(list->hbox.addAll(list)); .start();
 *
 *
 *
 *
 * @author cyril
 */
public class CallbackTask<INPUT, OUTPUT> extends Task<OUTPUT> implements ProgressHandler, Consumer<INPUT> {

    private INPUT input;

    private Callable<INPUT> inputGetter;

    private Callback<INPUT, OUTPUT> callback;
    private LongCallback<ProgressHandler, INPUT, OUTPUT> longCallback;
    private Callback<ProgressHandler, OUTPUT> longCallable;

    private Consumer<Throwable> errorHandler = e -> logger.log(Level.SEVERE, null, e);
    private Throwable error;

    private Runnable runnable;

    private ExecutorService executor = ImageJFX.getThreadPool();

    private Consumer<OUTPUT> successHandler;

    private Consumer<INPUT> consumer;

    private BiConsumer<ProgressHandler, INPUT> longConsumer;

    private final static Logger logger = Logger.getLogger(CallbackTask.class.getName());

    double total = 1.0;
    double progress = 0;

    public CallbackTask() {
        super();
    }

    public CallbackTask(INPUT input) {
        this();
        setInput(input);
    }

    public CallbackTask(Callback<INPUT, OUTPUT> callback) {
        this();
        this.callback = callback;
    }

    public CallbackTask<INPUT, OUTPUT> setInput(Callable<INPUT> inputGetter) {
        this.inputGetter = inputGetter;
        return this;
    }

    public CallbackTask<INPUT, OUTPUT> setName(String name) {
        updateTitle(name);
        updateMessage(name);
        return this;
    }

    public CallbackTask<INPUT, OUTPUT> run(Callback<INPUT, OUTPUT> callback) {
        this.callback = callback;
        return this;
    }

    public CallbackTask<INPUT, OUTPUT> run(Runnable runnable) {
        if (runnable == null) {
            logger.warning("Setting null as runnable");
            return this;
        }
        this.runnable = runnable;
        return this;
    }

    public CallbackTask<INPUT, OUTPUT> consume(Consumer<INPUT> consumer) {
        this.consumer = consumer;
        return this;
    }

    public CallbackTask<INPUT, OUTPUT> conusmer(BiConsumer<ProgressHandler, INPUT> biConsumer) {
        this.longConsumer = biConsumer;
        return this;
    }

    public CallbackTask<INPUT, OUTPUT> run(LongCallback<ProgressHandler, INPUT, OUTPUT> longCallback) {
        this.longCallback = longCallback;
        return this;
    }

    public CallbackTask<INPUT, OUTPUT> runLongCallable(Callback<ProgressHandler, OUTPUT> longCallable) {
        this.longCallable = longCallable;
        return this;
    }

    public OUTPUT call() {

        // first we check if the task was cancelled BEFORE RUNNING IT
        if (isCancelled()) {
            return null;
        }

        if (inputGetter != null) {
            try {
                input = inputGetter.call();
            } catch (Exception ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }

        try {
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
            } else if (consumer != null) {
                if (isCancelled()) {
                    return null;
                } else {
                    consumer.accept(input);
                    return null;
                }
            } else if (longConsumer != null) {
                if (isCancelled()) {
                    return null;
                } else {
                    longConsumer.accept(this, input);
                    return null;
                }
            } // now if we have a runnable as part of the task
            else if (runnable != null) {
                runnable.run();
                // same if it was cancelled during the problem

            } else {
                return null;
            }
        } catch (Exception e) {

            Logger.getLogger(CallbackTask.class.getSimpleName()).log(Level.SEVERE, "Error when executing callback task ", e);
            throw e;
        }
        return null;
    }

    @Override
    protected void failed() {
        super.failed();
        if (errorHandler != null) {
            errorHandler.accept(getException());
        }
    }

    public CallbackTask<INPUT, OUTPUT> setInput(INPUT input) {
        this.input = input;
        return this;
    }

    public INPUT getInput() {
        return input;
    }

    public CallbackTask<INPUT, OUTPUT> startIn(ExecutorService executorService) {
        executorService.execute(this);
        return this;
    }

    public CallbackTask<INPUT, OUTPUT> start() {
        executor.execute(this);
        return this;
    }

    public CallbackTask<INPUT, OUTPUT> queue() {
        executor = ImageJFX.getThreadQueue();
        executor.execute(this);
        return this;
    }

    @Override
    public void succeeded() {
        logger.info("succeeded");
        if (successHandler != null) {
            successHandler.accept(getValue());
        }
        super.succeeded();

    }

    @Override
    public void cancelled() {
        logger.info("cancelled...");
        super.cancelled();
    }

    public CallbackTask<INPUT, OUTPUT> then(Consumer<OUTPUT> consumer) {
        successHandler = consumer;
        return this;
    }

    public CallbackTask<INPUT, OUTPUT> thenRunnable(Runnable runnable) {
        return then(item -> runnable.run());
    }

    public <NEXTOUTPUT> CallbackTask<OUTPUT, NEXTOUTPUT> thenTask(Callback<OUTPUT, NEXTOUTPUT> callback) {
        CallbackTask<OUTPUT, NEXTOUTPUT> task = new CallbackTask<OUTPUT, NEXTOUTPUT>()
                .setInput(this::getValue)
                .run(callback);
        then(task);
        return task;
    }

    public CallbackTask<INPUT, OUTPUT> ui() {
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

        logger.info("Consumed a new input " + t);
        setInput(t);
        start();
    }

    public CallbackTask<INPUT, OUTPUT> error(Consumer<Throwable> handler) {
        errorHandler = handler;
        return this;
    }

    public CallbackTask<INPUT, OUTPUT> setExecutor(ExecutorService executor) {
        this.executor = executor;
        return this;
    }

    public CallbackTask<INPUT, OUTPUT> setIn(Property<Task> taskProperty) {
        Platform.runLater(() -> taskProperty.setValue(this));
        return this;
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public CallbackTask<INPUT, OUTPUT> submit(Consumer<Task> consumer) {
        consumer.accept(this);
        return this;
    }

    @Override
    public void setTotal(double total) {
        this.total = total;
    }

    @Override
    public void increment(double d) {
        progress += d;
        setProgress(progress, total);
    }

    public CallbackTask<INPUT, OUTPUT> setInitialProgress(double p) {
        setProgress(progress);
        return this;
    }

}
