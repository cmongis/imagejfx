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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 *
 * @author cyril
 */
@Plugin(type = Service.class)
public class DefaultTimerService extends AbstractService implements TimerService {

    private final Map<String, Timer> timerMap = new HashMap<>();

    @Override
    public Timer getTimer(Class<?> clazz) {
        return getTimer(clazz.getName());
    }

    @Override
    public Timer getTimer(String id) {
        timerMap.putIfAbsent(id, new DefaultTimer(id));
        return timerMap.get(id);
    }
    
   
    @Override
    public Collection<? extends Timer> getTimers() {
        return timerMap.values();
    }

}
