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

import java.util.Map;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

/**
 *
 * @author cyril
 */
public interface Timer {

    String getName();
    void start();
    
    
    /**
     * Measures the time since the last elapsed() or measure() call.
     * Doesn't display anything
     * @param id
     * @return 
     */
    long measure(String id);
    
    /**
     * Measure the time between the last elapsed() or measure command and displays it
     * @param id
     * @return return the time
     */
    long elapsed(String id);
    SummaryStatistics getStats(String id);
    Map<String,SummaryStatistics> getStats();
    
    public void logAll();
    
    public void log(String id);
    
}
