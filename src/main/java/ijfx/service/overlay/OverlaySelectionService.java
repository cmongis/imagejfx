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

import java.util.ArrayList;
import java.util.List;
import net.imagej.ImageJService;
import net.imagej.display.DataView;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.OverlayService;
import net.imagej.display.OverlayView;
import net.imagej.overlay.Overlay;
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
public class OverlaySelectionService extends AbstractService implements ImageJService{

    @Parameter
    OverlayService overlayService;

    @Parameter
    EventService eventService;

    @Parameter
    ImageDisplayService imageDisplayService;
    
    public void selectOnlyOneOverlay(ImageDisplay imageDisplay, Overlay overlay) {
        
        //System.out.println(overlay);
        
        for (DataView view : imageDisplay) {
            if (view instanceof OverlayView) {
                OverlayView overlayView = (OverlayView) view;
                overlayView.setSelected(overlay == overlayView.getData());
            }
        }

        eventService.publish(new OverlaySelectedEvent(imageDisplay, overlay));

    }

    public void selectedAll(ImageDisplay imageDisplay) {
        for (DataView view : imageDisplay) {
            if (view instanceof OverlayView) {
                ((OverlayView) view).setSelected(true);
            }
        }

        eventService.publish(new OverlayAllSelectedEvent(imageDisplay));
    }

    public void unselectedAll(ImageDisplay imageDisplay) {
        for (DataView view : imageDisplay) {
            if (view instanceof OverlayView) {
                ((OverlayView) view).setSelected(false);
            }
        }

    }

    public List<Overlay> getSelectedOverlays(ImageDisplay imageDisplay) {
        ArrayList<Overlay> selected = new ArrayList<>();
        imageDisplay.forEach(view -> {
            if (view instanceof OverlayView) {
                if (((OverlayView) view).isSelected()) {
                    selected.add((Overlay) view.getData());
                }
            }
        });
        return selected;
    }

    public boolean isMultipleSelection(ImageDisplay imageDisplay) {

        //
        int count = 0;

        // naive implementation to buy some processing cycles.
        for (DataView view : imageDisplay) {
            if (view instanceof OverlayView) {
                if (((OverlayView) view).isSelected()) {
                    count++;
                }
                if (count > 1) {
                    return true;
                }
            }
        }

        return false;
    }

    public void setOverlaySelection(ImageDisplay imageDisplay, Overlay selectedOverlay, boolean selected) {
        for (DataView view : imageDisplay) {
            
            if (view instanceof OverlayView && view.getData() == selectedOverlay) {
                OverlayView overlayView = (OverlayView) view;
                overlayView.setSelected(selected);
                if (selected) {
                   
                   
                    eventService.publishLater(new OverlaySelectedEvent(imageDisplay, selectedOverlay));
                }
                return;
            }
        }
    }

}
