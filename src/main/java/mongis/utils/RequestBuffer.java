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
/**
 *
 * @author Cyril MONGIS, 2016
 */

package mongis.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class RequestBuffer {
    
    final long ms;

    Timer timer = new Timer();
    
    public RequestBuffer(long ms) {
        this.ms = ms;
        
    }
    
    List<Runnable> queue = new ArrayList<>();
    
    
    
    public void queue(Runnable runnable) {
        
        
        
        queue.add(runnable);
        if(queue.size() == 1) {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnce();
                }
            }, ms);
        }
    }
    
    
    
    public void runOnce() {
        Runnable runnable = queue.get(queue.size()-1);
        queue.clear();
        runnable.run();
    }
            
    
}
