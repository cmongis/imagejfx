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
package ijfx.service.uicontext;

import ijfx.service.overlay.OverlaySelectionService;
import ijfx.service.overlay.OverlaySelectionEvent;
import net.imagej.ImageJService;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.OverlayService;
import org.scijava.display.DisplayService;
import org.scijava.display.event.DisplayUpdatedEvent;
import org.scijava.event.EventHandler;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = Service.class)
public class UiContextCalculatorService extends AbstractService implements ImageJService {

    public final static String CTX_OVERLAY_SELECTED = "overlay-selected";
    public final static String CTX_RGB_IMAGE = "rgb-img";
    public final static String CTX_MULTI_Z_IMAGE = "multi-z-img";
    public final static String CTX_MULTI_CHANNEL_IMG = "multi-channel-img";

    @Parameter
    DisplayService displayService;

    @Parameter
    ImageDisplayService imageDisplayService;

    @Parameter
    OverlayService overlayService;

    @Parameter
    OverlaySelectionService overlaySelectionService;
    
    @Parameter
    UiContextService contextService;

    public void determineContext(ImageDisplay display) {
        if(overlaySelectionService == null) overlayService.getContext().inject(this);
        contextService.toggleContext(CTX_OVERLAY_SELECTED, overlaySelectionService.getSelectedOverlays(display).size() > 0);
        contextService.update();
    }
    
    

    @EventHandler
    public void handleEvent(DisplayUpdatedEvent event) {
        if (event.getDisplay() instanceof ImageDisplay && displayService.getActiveDisplay(ImageDisplay.class) == event.getDisplay()) {
            determineContext((ImageDisplay)event.getDisplay());
        }
    }
    
    @EventHandler
    public void handleEvent(OverlaySelectionEvent event) {
        determineContext(event.getDisplay());
    }
    

}
