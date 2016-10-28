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

    // Possible operation
    private FailableCallback<INPUT, OUTPUT> callback;
    private FailableCallable<OUTPUT> callable;
    private LongCallback<ProgressHandler, INPUT, OUTPUT> longCallback;
    private LongCallable<OUTPUT> longCallable;
    private FailableRunnable runnable;
    private FailableConsumer<INPUT> consumer;
    private FailableBiConsumer<ProgressHandler,INPUT> longConsumer;
    
    
    // handlers
    private Consumer<Throwable> onError = e -> logger.log(Level.SEVERE, null, e);

    private Consumer<OUTPUT> onSuccess = e->logger.info("Callback is a success.");
    
    private ExecutorService executor = ImageJFX.getThreadPool();

   

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

    public CallbackTask(FailableCallback<INPUT, OUTPUT> callback) {
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

    public CallbackTask<INPUT, OUTPUT> run(FailableCallback<INPUT, OUTPUT> callback) {
        this.callback = callback;
        return this;
    }

    
    public CallbackTask<INPUT, OUTPUT> run(Runnable runnable) {
        if (runnable == null) {
            logger.warning("Setting null as runnable");
            return this;
        }
        this.runnable = ()->runnable.run();
        return this;
    }
    
    public CallbackTask<INPUT,OUTPUT> tryRun(FailableRunnable runnable) {
        this.runnable = runnable;
        return this;
    }

    public CallbackTask<INPUT, OUTPUT> consume(FailableConsumer<INPUT> consumer) {
        this.consumer = consumer;
        return this;
    }

    public CallbackTask<INPUT, OUTPUT> consume(FailableBiConsumer<ProgressHandler, INPUT> biConsumer) {
        this.longConsumer = biConsumer;
        return this;
    }
    
    public CallbackTask<INPUT,OUTPUT> call(FailableCallable<OUTPUT> callable) {
        this.callable = callable;
        return this;
    }

    public CallbackTask<INPUT, OUTPUT> run(LongCallback<ProgressHandler, INPUT, OUTPUT> longCallback) {
        this.longCallback = longCallback;
        return this;
    }

    public CallbackTask<INPUT, OUTPUT> runLongCallable(LongCallable<OUTPUT> longCallable) {
        this.longCallable = longCallable;
        return this;
    }

    @Override
    public OUTPUT call() throws Exception {

        // first we check if the task was cancelled BEFORE RUNNING IT
        if (isCancelled()) {
            return null;
        }
        
        if (inputGetter != null) {
                input = inputGetter.call();
        }

        OUTPUT output = null;
        
        
        if(longCallback != null) {
            output = longCallback.handle(this,input);
        }
        else if(callback != null) {
            output = callback.call(input);
        }
        else if(longCallable != null) {
            output = longCallable.call(this);
        }
        else if(callable != null){
            output = callable.call();
        }
        else if(consumer != null) {
            consumer.accept(input);
        }
        else if(longConsumer != null) {
            longConsumer.accept(this, input);
        }
        else if(runnable != null) {
            runnable.run();
        }
         
        if(isCancelled()) {
            return null;
        }
        
        return output;
    }

    @Override
    protected void failed() {
        super.failed();
        Logger.getLogger(getClass().getName()).log(Level.SEVERE,null,getException());
        if (onError != null) {
            onError.accept(getException());
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
        if (onSuccess != null) {
            onSuccess.accept(getValue());
        }
        super.succeeded();

    }

    @Override
    public void cancelled() {
        logger.info("cancelled...");
        super.cancelled();
    }

    public CallbackTask<INPUT, OUTPUT> then(Consumer<OUTPUT> consumer) {
        onSuccess = consumer;
        return this;
    }

    public CallbackTask<INPUT, OUTPUT> thenRunnable(Runnable runnable) {
        return then(item -> runnable.run());
    }

    public <NEXTOUTPUT> CallbackTask<OUTPUT, NEXTOUTPUT> thenTask(FailableCallback<OUTPUT, NEXTOUTPUT> callback) {
        CallbackTask<OUTPUT, NEXTOUTPUT> task = new CallbackTask<OUTPUT, NEXTOUTPUT>()
                .setInput(this::getValue)
                .run(callback);
        then(task);
        return task;
    }

    public CallbackTask<INPUT, OUTPUT> ui() throws Exception {
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
        onError = handler;
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

    @FunctionalInterface
    public interface FailableConsumer<T> {

        void accept(T t) throws Exception;
    }
    
    @FunctionalInterface
    public interface FailableCallable<T> {
        T call() throws Exception;
    }
    
    @FunctionalInterface
    public interface FailableBiConsumer<T,R> {
        void accept(T t, R r);
    }
    @FunctionalInterface
    public interface FailableRunnable{
        void run() throws Exception;
    }

}
