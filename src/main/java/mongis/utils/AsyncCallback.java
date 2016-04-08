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
import java.util.function.Consumer;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.util.Callback;

/**
 *
 * @author cyril
 */
public class AsyncCallback<INPUT, OUTPUT> extends Task<OUTPUT> {

    INPUT input;

    Callback<INPUT, OUTPUT> callback;
    Runnable runnable;

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

    public OUTPUT call() {
        try {
            // first we check if the task was cancelled BEFORE RUNNING IT
            if (isCancelled()) {
                return null;
            }
            try {
                // now if we should execute a callback
                if (callback != null) {
                    // executing and betting the output
                    OUTPUT output = callback.call(input);
                    // if the task has been cancelled during execution,
                    // we cancel the output and return null
                    if (isCancelled()) {
                        return null;
                    } else {
                        return output;
                    }
                } // now if we have a runnable as part of the task
                else if (runnable != null) {
                    runnable.run();
                    // same if it was cancelled during the problem

                } else {
                    return null;
                }
            } catch (Exception e) {
                ImageJFX.getLogger().log(Level.SEVERE, "Error when executing callback", e);
                return null;
            }
        } catch (Exception e) {
            ImageJFX.getLogger().log(Level.SEVERE, "Error when executing AsyncCallback " + callback.toString(), e);
        }
        return null;
    }

    public AsyncCallback<INPUT, OUTPUT> setInput(INPUT input) {
        this.input = input;
        return this;
    }

    public INPUT getInput() {
        return input;
    }

    public AsyncCallback<INPUT, OUTPUT> start() {
        ImageJFX.getThreadPool().submit(this);
        return this;
    }

    public AsyncCallback<INPUT, OUTPUT> queue() {
        ImageJFX.getThreadQueue().submit(this);
        return this;
    }

    public AsyncCallback<INPUT, OUTPUT> then(Consumer<OUTPUT> consumer) {
        setOnSucceeded(event -> {
            // if it wasn't cancelled and it was a callback executed
            if (!isCancelled() && getValue() != null && runnable == null) {
                consumer.accept(getValue());
            } else if (!isCancelled() && runnable != null) {
                consumer.accept(null);
            }
            if (getValue() == null && runnable == null) {
                ImageJFX.getLogger().warning("Return value was null :-(");
            }
        });
        return this;
    }

    public AsyncCallback<INPUT, OUTPUT> ui() {
        if (Platform.isFxApplicationThread()) {
            call();
        } else {
            Platform.runLater(this);
        }

        return this;
    }

}
