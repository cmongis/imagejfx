
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

import ijfx.plugins.commands.AxisUtils;
import ijfx.service.overlay.OverlaySelectionService;
import ijfx.service.overlay.OverlaySelectionEvent;
import mongis.utils.RequestBuffer;
import net.imagej.Dataset;
import net.imagej.ImageJService;
import net.imagej.axis.Axes;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.OverlayService;
import net.imagej.table.TableDisplay;
import org.scijava.display.Display;
import org.scijava.display.DisplayService;
import org.scijava.display.event.DisplayActivatedEvent;
import org.scijava.display.event.DisplayDeletedEvent;
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
    public final static String CTX_MULTI_N_IMG = "multi-n-img";
    public final static String CTX_MULTI_TIME_IMG = "multi-time-img";
    public final static String CTX_TABLE_DISPLAY = "table-open";
    public final static String CTX_IMAGE_DISPLAY = "image-open";
    public final static String CTX_IMAGE_BINARY = "binary";

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

    private final RequestBuffer requestBuffer = new RequestBuffer(2);

    public void determineContext(Display display) {
        requestBuffer.queue(() -> {

            if (overlaySelectionService == null) {
                overlayService.getContext().inject(this);
            }
            ImageDisplay imageDisplay = null;

            contextService.toggleContext(CTX_IMAGE_DISPLAY, display != null && ImageDisplay.class.isAssignableFrom(display.getClass()));
            contextService.toggleContext(CTX_TABLE_DISPLAY, display != null && TableDisplay.class.isAssignableFrom(display.getClass()));

            // calculation specific to iamge display
            if (display instanceof ImageDisplay) {
                imageDisplay = (ImageDisplay) display;

                contextService.toggleContext(CTX_OVERLAY_SELECTED, imageDisplay != null && overlaySelectionService.getSelectedOverlays(imageDisplay).size() > 0);

                contextService.toggleContext(CTX_MULTI_Z_IMAGE, imageDisplay != null && AxisUtils.hasAxisType(imageDisplay, Axes.Z));

                contextService.toggleContext(CTX_MULTI_CHANNEL_IMG, imageDisplay != null && AxisUtils.hasAxisType(imageDisplay, Axes.CHANNEL));

                contextService.toggleContext(CTX_MULTI_TIME_IMG, imageDisplay != null && AxisUtils.hasAxisType(imageDisplay, Axes.TIME));

                contextService.toggleContext(CTX_RGB_IMAGE, imageDisplay != null && imageDisplayService.getActiveDataset(imageDisplay).isRGBMerged());

                contextService.toggleContext(CTX_IMAGE_BINARY, imageDisplay != null && imageDisplayService.getActiveDataset(imageDisplay).getValidBits() == 1);

                contextService.toggleContext(CTX_MULTI_N_IMG, imageDisplay != null && imageDisplay.numDimensions() > 2);
                
                Dataset dataset = null;
                if (display != null) {
                    dataset = (Dataset) imageDisplay.getActiveView().getData();
                    for (int i = 1; i <= 32; i *= 2) {
                        contextService.toggleContext(String.valueOf(dataset.getValidBits()) + "-bits", dataset.getValidBits() == i);
                    }
                } else {
                    for (int i = 1; i <= 32; i *= 2) {
                        contextService.toggleContext(String.valueOf(i) + "-bits", false);
                    }
                }
            }
            contextService.update();

        });
    }

    @EventHandler
    public void handleEvent(DisplayUpdatedEvent event) {
//        displayService.getDisplays().stream().forEach((display) -> {
//            if (display != event.getDisplay()) {
//                determineContext(display, false);
//            }
//        });
        determineContext(event.getDisplay());
        // contextService.update();

    }

    @EventHandler
    public void handleEvent(DisplayActivatedEvent event) {
        displayService.getDisplays().stream().forEach((display) -> {
            if (display != event.getDisplay()) {
                determineContext(display);
            }
        });
        determineContext(event.getDisplay());
        // contextService.update();

    }

    @EventHandler
    public void handleEvent(OverlaySelectionEvent event) {
        determineContext(event.getDisplay());
        contextService.update();

    }

    @EventHandler
    public void handleEvent(DisplayDeletedEvent event) {

        if (imageDisplayService.getImageDisplays().size() > 0) {
            displayService.setActiveDisplay(imageDisplayService.getImageDisplays().get(0));
            determineContext(imageDisplayService.getImageDisplays().get(0));
        } else {
            determineContext(null);
        }

    }

}
