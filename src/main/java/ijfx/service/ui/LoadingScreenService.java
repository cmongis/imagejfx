/*
 * /*
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
package ijfx.service.ui;

import ijfx.bridge.FxStatusBar;
import ijfx.ui.main.LoadingScreen;
import javafx.concurrent.Task;
import net.imagej.ImageJService;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = Service.class)
public class LoadingScreenService extends AbstractService implements ImageJService{
    
    @Parameter
    EventService eventService;
    
    
    
    
    
    public void frontEndTask(Task task){
        frontEndTask(task,false);
    }
    
    public void frontEndTask(Task task, boolean canCancel) {
       eventService.publish(new FontEndTaskSubmitted().setCancelable(canCancel).setObject(task));
    }
    
    public void backgroundTask(Task task, boolean cancelable) {
        
        FxStatusBar.getInstance().submitTask(task, cancelable);
        
    }

    public void submitTask(Task task) {
           backgroundTask(task,true);
    }

    
}
