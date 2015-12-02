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
package ijfx.ui.plugin;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.concurrent.Task;
import mongis.utils.MemoryUtils;

/**
 *
 * @author cyril
 */
public class RamCheckTask extends Task<Void> {

    
    long refreshDelay = 1000;

    public long getRefreshDelay() {
        return refreshDelay;
    }

    public void setRefreshDelay(long refreshDelay) {
        this.refreshDelay = refreshDelay;
    }
    
    public LongProperty occupiedRam = new SimpleLongProperty();
    public LongProperty totalRam = new SimpleLongProperty();
    
    
    
    @Override
    protected Void call() throws Exception {
        long available,maximum;
        while(true) {
            
            available = MemoryUtils.getAvailableMemory();
            maximum = MemoryUtils.getMaximumMemory();
            
            occupiedRam.setValue(maximum-available);
            totalRam.setValue(maximum);
            
            updateProgress(maximum-available,maximum);
            
            Thread.sleep(getRefreshDelay());
            
            if(isCancelled()) break;
        }
        
        return null;
       
    }
    
}
