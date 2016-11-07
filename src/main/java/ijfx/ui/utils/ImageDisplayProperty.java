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
package ijfx.ui.utils;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectPropertyBase;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import org.scijava.Context;
import org.scijava.display.Display;
import org.scijava.display.event.DisplayActivatedEvent;
import org.scijava.display.event.DisplayUpdatedEvent;
import org.scijava.event.EventHandler;
import org.scijava.plugin.Parameter;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class ImageDisplayProperty extends ReadOnlyObjectPropertyBase<ImageDisplay> {

    ImageDisplay current = null;

    @Parameter
    ImageDisplayService imageDisplayService;

 

    public ImageDisplayProperty(Context context) {
        context.inject(this);
        current = imageDisplayService.getActiveImageDisplay();
        fireValueChangedEvent();
    }

    @Override
    public ImageDisplay get() {
        return current;
    }

    @Override
    public Object getBean() {
        return imageDisplayService;
    }

    @Override
    public String getName() {
        return "activeImageDisplay";
    }

    private void checkNew(Display<?> display) {
         if(display == current) return;
        if (display != null && ImageDisplay.class.isAssignableFrom(display.getClass())) {
            ImageDisplay imageDisplay = (ImageDisplay)display;
            
            current = imageDisplay;
             Platform.runLater(this::fireValueChangedEvent);
        } else {
           // current = null;
        }
       
    }
    
    @EventHandler
    public void onActiveImageDisplayChanged(DisplayActivatedEvent event) {
        Display<?> display = event.getDisplay();
       checkNew(display);
    }
    
    @EventHandler
    public void onDisplayUpdated(DisplayUpdatedEvent event) {
        if(event.getDisplay() == imageDisplayService.getActiveImageDisplay()) {
            checkNew(event.getDisplay());
        }
    }

    

}
