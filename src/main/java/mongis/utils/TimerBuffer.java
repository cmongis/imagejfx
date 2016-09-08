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


import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

/**
 *
 * @author cyril
 */
public class TimerBuffer<T> {
    
    private long millis = 50;
    
    List<T> queue = new ArrayList<>();
    
    Timer timer = new Timer();
    Boolean set = Boolean.FALSE;
    Consumer<List<T>> consumer;
    
    public void add(T t) {
        queue.add(t);
        if(!set) {
            setTimer();
        }
    }
    
    public void setTimer() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                consume();
            }
        }, millis);
    }
    
    public synchronized void consume() {
        synchronized(queue) {
            consumer.accept(new ArrayList(queue));
            queue.clear();
        }
    }
}
