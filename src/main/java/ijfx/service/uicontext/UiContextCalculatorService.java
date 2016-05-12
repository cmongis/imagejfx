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
import net.imagej.Dataset;
import net.imagej.ImageJService;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.axis.CalibratedAxis;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.OverlayService;
import net.imagej.table.TableDisplay;
import org.scijava.SciJava;
import org.scijava.display.Display;
import org.scijava.display.DisplayService;
import org.scijava.display.event.DisplayActivatedEvent;
import org.scijava.display.event.DisplayDeletedEvent;
import org.scijava.display.event.DisplayEvent;
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

    public void determineContext(Display display) {
        if (overlaySelectionService == null) {
            overlayService.getContext().inject(this);
        }

        ImageDisplay imageDisplay = (ImageDisplay) display;

        contextService.toggleContext(CTX_OVERLAY_SELECTED, overlaySelectionService.getSelectedOverlays(imageDisplay).size() > 0);

        contextService.toggleContext(CTX_MULTI_Z_IMAGE, hasAxisType(imageDisplay, Axes.Z));

        contextService.toggleContext(CTX_MULTI_CHANNEL_IMG, hasAxisType(imageDisplay, Axes.CHANNEL));

        contextService.toggleContext(CTX_MULTI_TIME_IMG, hasAxisType(imageDisplay, Axes.TIME));

        contextService.toggleContext(CTX_RGB_IMAGE, imageDisplayService.getActiveDataset(imageDisplay).isRGBMerged());

        contextService.toggleContext(CTX_IMAGE_BINARY, imageDisplayService.getActiveDataset(imageDisplay).getValidBits() == 1);

        contextService.toggleContext(CTX_IMAGE_DISPLAY, ImageDisplay.class.isAssignableFrom(display.getClass()));

        contextService.toggleContext(CTX_TABLE_DISPLAY, TableDisplay.class.isAssignableFrom(display.getClass()));
        Dataset dataset = (Dataset) imageDisplay.getActiveView().getData();

        for (int i = 1; i <= 32; i *= 2) {
            contextService.toggleContext(String.valueOf(dataset.getValidBits()) + "-bits", dataset.getValidBits() == i);
        }

    }

    @EventHandler
    public void handleEvent(DisplayUpdatedEvent event) {
//        displayService.getDisplays().stream().forEach((display) -> {
//            if (display != event.getDisplay()) {
//                determineContext(display, false);
//            }
//        });
        determineContext(event.getDisplay());
        contextService.update();

    }

    @EventHandler
    public void handleEvent(DisplayActivatedEvent event) {
        displayService.getDisplays().stream().forEach((display) -> {
            if (display != event.getDisplay()) {
                determineContext(display);
            }
        });
        determineContext(event.getDisplay());
        contextService.update();

    }

    @EventHandler
    public void handleEvent(OverlaySelectionEvent event) {
        determineContext(event.getDisplay());
        contextService.update();

    }

    @EventHandler
    public void handleEvent(DisplayDeletedEvent event) {
        determineContext(event.getObject());
        displayService.getDisplays().stream().forEach((display) -> {
            if (display != event.getObject()) {
                determineContext(display);
            }
        });
        contextService.update();

    }

    public boolean hasAxisType(ImageDisplay display, AxisType axisType) {
        CalibratedAxis[] calibratedAxises = new CalibratedAxis[display.numDimensions()];
        display.axes(calibratedAxises);
        for (CalibratedAxis calibratedAxis : calibratedAxises) {
            if (calibratedAxis.type() == axisType) {
                return true;
            }
        }
        return false;
    }

}
