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
package ijfx.service.ui;

import ijfx.service.IjfxService;
import ijfx.service.batch.DefaultSegmentedObject;
import ijfx.service.batch.SegmentedObject;
import ijfx.service.overlay.OverlaySelectionService;
import ijfx.service.overlay.OverlayStatService;
import ijfx.ui.datadisplay.object.DisplayedSegmentedObject;
import ijfx.ui.datadisplay.object.SegmentedObjectDisplay;
import ijfx.ui.main.ImageJFX;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.OverlayService;
import net.imagej.overlay.Overlay;
import net.imagej.overlay.ThresholdOverlay;
import org.scijava.display.Display;
import org.scijava.display.DisplayService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import org.scijava.ui.UIService;

/**
 *
 * @author cyril
 */
@Plugin(type = Service.class)
public class MeasurementService extends AbstractService implements IjfxService {

    @Parameter
    OverlaySelectionService overlaySelectionSrv;

    @Parameter
    ImageDisplayService imageDisplaySrv;

    @Parameter
    DisplayService displayService;

    @Parameter
    OverlayStatService overlayStatsSrv;

    @Parameter
    OverlayService overlayService;

    @Parameter
            UIService uiService;
    
    Logger logger = ImageJFX.getLogger();

    public void measureSelectedOverlay() {
        ImageDisplay display = imageDisplaySrv.getActiveImageDisplay();

        logger.info("Measuring selected overlay");
        overlaySelectionSrv.getSelectedOverlays(display)
                .stream()
                .map(o -> measure(display,o))
                .forEach(this::display);

    }

    public void measureAllOverlay() {

        measureAllOverlay(imageDisplaySrv.getActiveImageDisplay());

    }

    public void measureOverlays(ImageDisplay imageDisplay, List<Overlay> overlayList, Predicate<SegmentedObject> filter) {
        
        // the filter always return true if null
        if(filter == null) filter = o->true;
        
        List<SegmentedObject> objectList = overlayList
                .stream()
                .map(o->measure(imageDisplay,o))
                .filter(o->o!=null)
                .filter(filter)
                .collect(Collectors.toList());
        
        
        if(objectList.size() > 0) {
            
            
            Display display = displayService.createDisplay("Measures from "+imageDisplay.getName(),objectList.get(0));
            display.addAll(objectList);
            display.update();
            
        }
        else {
            uiService.showDialog("There is no object to measure.");
        }
        
    }
    
    public void measureAllOverlay(ImageDisplay imageDisplay) {
        measureOverlays(imageDisplay,overlayService.getOverlays(imageDisplay),null); 
    }

    public SegmentedObject measure(ImageDisplay display, Overlay overlay) {
        try {
        SegmentedObject object = new DefaultSegmentedObject(overlay,overlayStatsSrv.getOverlayStatistics(display, overlay));
        return new DisplayedSegmentedObject(display, object);
        }
        catch(Exception e) {
            logger.log(Level.SEVERE,"Error when creating SegmentedObject",e);
            return null;
        }
    }
    
    
    public synchronized Display getCurrentDisplay(SegmentedObject object) {
        return displayService.getDisplays()
                .stream()
                .filter(display -> display instanceof SegmentedObjectDisplay)
                .findFirst()
                .orElseGet(() -> {
                    logger.info("Creating display");
                    return displayService.createDisplay("Measures",object);
                });
    }
    
    public void display(SegmentedObject object) {
        logger.info("Dislaying segmented object " + object.getOverlay().getName());
        getCurrentDisplay(object).display(object);

    }
    public Optional<ThresholdOverlay> getThresholdOverlay(ImageDisplay imageDisplay) {
         return overlayService
                .getOverlays(imageDisplay)
                .stream()
                .filter(o->o instanceof ThresholdOverlay)
                .map(o->(ThresholdOverlay)o)
                .findFirst();
    }
}
