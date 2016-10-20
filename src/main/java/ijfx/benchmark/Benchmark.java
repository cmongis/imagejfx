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
package ijfx.benchmark;

import ijfx.service.Timer;
import ijfx.service.TimerService;
import static org.controlsfx.control.action.ActionMap.action;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.PluginService;

/**
 *
 * @author cyril
 */
public class Benchmark {
    
    public static void main(String... args) {
        
        new Benchmark().init().go();
        
    }
    
    
    @Parameter
    TimerService timerService;
    
    @Parameter
    PluginService pluginService;
    
    public Benchmark init() {
        Context context = new Context();
        
        context.inject(this);
        return this;
    }
    
    
    public void go() {
        
        pluginService
                .createInstancesOfType(BenchMarkPlugin.class)
                .forEach(this::benchmark);
        
        System.exit(0);
        
        
    }
    
    private void benchmark(BenchMarkPlugin plugin) {
        plugin.init();
        
        Timer timer = timerService.getTimer(plugin.getClass());
        
        for(int i = 0;i!= plugin.repeatNumber();i++) {
            
            timer.start();
            plugin.repeat();
            timer.measure("execution");
            
            
        }
        plugin.finish();
        timer.logAll();

    }
}
