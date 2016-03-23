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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

/**
 *
 * @author cyril
 */
public class DefaultTimer implements Timer {

    Map<String, SummaryStatistics> stats = new HashMap<>();
    long start;
    long last;

    Logger logger = ImageJFX.getLogger();
    
    String id = "DefaultTimer";
    
    public DefaultTimer(String id) {
        this();
        this.id = id;
    }
    
    public DefaultTimer() {

    }

    @Override
    public Map<String, SummaryStatistics> getStats() {
        return stats;
    }

    @Override
    public SummaryStatistics getStats(String id) {
        stats.putIfAbsent(id, new SummaryStatistics());
        return stats.get(id);
    }

    public void start() {
        start = System.currentTimeMillis();
        last = System.currentTimeMillis();
    }

    public long now() {
        return System.currentTimeMillis();
    }

    public long elapsed(String text) {
        long now = System.currentTimeMillis();
        long elapsed = (now - last);
        last = now;
        getStats(text).addValue(elapsed);
        logger.info(String.format("[%s] %s : %dms",id,text,elapsed));
        return elapsed;
    }
    
    
    protected String getLog(String id) {
        return  new StringBuilder()
                .append("####")
                .append(id)
                .append("#####")
                .append(getStats(id).toString())
                .toString();
        
    }

    @Override
    public void logAll() {
        stats.forEach((key,value)->{
            log(key);
        });
    }

    @Override
    public void log(String id) {
        logger.info(getLog(id));
    }

}
