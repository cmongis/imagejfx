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
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class TimedBuffer<T> {
    
    final private long millis;
    
    private final List<T> queue = new ArrayList<>();
    
    private Timer timer = new Timer();
    private Boolean set = Boolean.FALSE;
    private Consumer<List<T>> consumer;
    
    public TimedBuffer(long refreshDelay) {
        this.millis = refreshDelay;
    }
    public TimedBuffer() {
        this(50);
    }
    
    
    public TimedBuffer<T> setAction(Consumer<List<T>> consumer) {
        this.consumer = consumer;
        return this;
    }
    
    public void add(T t) {
        synchronized(queue) {
            queue.add(t);
            
            if(!set) {
            setTimer();
            }
        }
        
        
    }
    
    public void setTimer() {
        if(timer !=null) timer.cancel();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                consume();
            }
        }, millis);
        set = true;
    }
    
    public synchronized void consume() {
        List<T> toTreat;
        synchronized(queue) {
            toTreat  = new ArrayList<>(queue);
            queue.clear();
            set = false;
            
        }
        consumer.accept(toTreat);
    }
    
    
    
    public static void main(String... args) {
        
       TimedBuffer<Boolean> buffer =  new TimedBuffer<>();
      
       buffer.setAction(list->System.out.println("Treating a list of " +list.size()));
      
       
       
       BiConsumer<Integer,Integer> pulse = (count,delay)->{
       
           for(int i = 0;i!=count;i++) {
               buffer.add(Boolean.TRUE);
               try {
                   Thread.sleep(delay);
               } catch (InterruptedException ex) {
                   Logger.getLogger(TimedBuffer.class.getName()).log(Level.SEVERE, null, ex);
               }
           }
       
       };
       
       long start = System.currentTimeMillis();
       
       pulse.accept(100,1);
       
        System.out.println("process time = "+(System.currentTimeMillis() - start));
       
       
        
    }
}
