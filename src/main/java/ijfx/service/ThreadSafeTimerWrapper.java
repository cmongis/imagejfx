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
package ijfx.service;

import ijfx.ui.main.ImageJFX;
import java.util.Map;
import java.util.concurrent.Executor;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

/**
 *
 * @author cyril
 */
public class ThreadSafeTimerWrapper implements Timer{

    public final DefaultTimer timer;

    long start;
    long last;
    
    private static final Executor executor = ImageJFX.getThreadPool();
    
    public ThreadSafeTimerWrapper(DefaultTimer timer) {
        this.timer = timer;
        start();
    }
    
    
    
    @Override
    public String getName() {
        return timer.getName();
    }

    @Override
    public void start() {
        last = System.currentTimeMillis();
        
    }

    @Override
    public long measure(String text) {
        final long now = System.currentTimeMillis();
        final long elapsed = (now - last);
        last = now;
        
        executor.execute(()->{
         timer.getStats(text).addValue(elapsed);
        });
        
       
        return elapsed;
    }

    @Override
    public long elapsed(String text) {
       long elapsed = measure(text);
        timer.logger.info(String.format("[%s] %s : %dms",text,text,elapsed));
        return elapsed;
    }

    @Override
    public SummaryStatistics getStats(String id) {
        return timer.getStats(id);
    }

    @Override
    public Map<String, SummaryStatistics> getStats() {
        return timer.getStats();
    }

    @Override
    public void logAll() {
        timer.logAll();
    }

    @Override
    public void log(String id) {
        timer.log(id);
    }
    
}
