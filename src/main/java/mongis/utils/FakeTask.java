/*
 * 
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

import javafx.concurrent.Task;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class FakeTask<T> extends Task<T> {

    private T fakeResult = null;
    
    private int iteration = 100;
 
    /**
     * Total time in ms
     */
    private long totalTime = 3000;

    
    
    public FakeTask() {
        super();
    }
    
    public FakeTask(int totalTimeinMilliseconds) {
        this();
        
        this.totalTime = totalTimeinMilliseconds;
    }
    
    public FakeTask(T fakeResult) {
        this();
        this.fakeResult = fakeResult;
    }
    
    public FakeTask(T fakeResult, int iteration, long totalTime) {
        this();
        this.fakeResult = fakeResult;
        this.iteration = iteration;
        this.totalTime = totalTime;
    }
 
    
    
    @Override
    protected T call() throws Exception {
        
        for(int i = 0; i!= iteration;i++) {
            if(isCancelled()) break;    
            Thread.sleep(totalTime/iteration);
             updateProgress(i+1, iteration);
             String message = String.format("Wasting time %d / %d",i,iteration);

             updateMessage(message);
        }
        System.out.println("Fake task stopped");
       return fakeResult;
    }
    
    
    public FakeTask<T> start() {
        new Thread(this).start();
        return this;
    }
    
    @Override
    public String toString() {
        return String.format("Fake Task of %dms",totalTime);
    }
}
