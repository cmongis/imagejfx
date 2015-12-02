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
package ijfx.service.overlay;

import net.imagej.display.ImageDisplay;
import net.imagej.overlay.Overlay;
import org.scijava.event.SciJavaEvent;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class OverlaySelectionEvent extends SciJavaEvent{
    
    ImageDisplay display;
    
    Overlay overlay;

    
    public OverlaySelectionEvent(ImageDisplay display, Overlay overlay) {
        setDisplay(display);
        setOverlay(overlay);
    }
    
    public ImageDisplay getDisplay() {
        return display;
    }

    public void setDisplay(ImageDisplay display) {
        this.display = display;
    }

    public Overlay getOverlay() {
        return overlay;
    }

    public void setOverlay(Overlay overlay) {
        this.overlay = overlay;
    }
    
  
    
    
    
}
