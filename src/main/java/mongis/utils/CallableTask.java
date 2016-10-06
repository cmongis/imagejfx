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

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.concurrent.Task;

/**
 *
 * @author cyril
 */
public class CallableTask<T> extends Task<T>{

    
    Callable<T> callable;

    Consumer<T> onSuccess;
    
    public void setCallable(Callable<T> callable) {
        this.callable = callable;
    }

    public CallableTask(Callable<T> callable) {
        this.callable = callable;
    }
    
    
    
    @Override
    protected T call() throws Exception {
        return callable.call();
    }
    
    protected CallableTask<T> startInFXThread() {
        Platform.runLater(this);
        return this;
    }
    
    public CallableTask<T> then(Consumer<T> t) {
        onSuccess = t;
        return this;
    }
    
    @Override
    protected void succeeded() {
        super.succeeded();
        if(onSuccess !=null)
        onSuccess.accept(getValue());
    }
    
}
