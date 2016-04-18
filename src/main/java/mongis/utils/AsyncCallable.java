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
import java.util.function.Consumer;
import javafx.concurrent.Task;
import javafx.event.Event;

/**
 *
 * @author cyril
 */
public class AsyncCallable<OUTPUT> extends Task<OUTPUT> {

    Callable<OUTPUT> callable;
    
    Consumer<OUTPUT> consumer;
    
    
    public AsyncCallable() {
        super();
        setOnSucceeded(this::onSucceed);
    }
    
    public void onSucceed(Event event) {
        consumer.accept(getValue());
    }
    
    public AsyncCallable<OUTPUT> run(Callable callable) {
       
        this.callable = callable;
        
        return this;
    }
    
    public AsyncCallable<OUTPUT> then(Consumer<OUTPUT> consumer) {
        this.consumer = consumer;
        return this;
    }
    
    
    
    @Override
    protected OUTPUT call() {
        try {
            return callable.call();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public AsyncCallable<OUTPUT> start() {
        ImageJFX.getThreadPool().execute(this);
        return this;
    }
    
}
