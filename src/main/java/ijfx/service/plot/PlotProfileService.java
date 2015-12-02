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
package ijfx.service.plot;

import ijfx.ui.main.ImageJFX;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import net.imagej.ImageJService;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.overlay.LineOverlay;
import net.imagej.widget.HistogramBundle;
import net.imglib2.histogram.Histogram1d;
import net.imglib2.roi.LineRegionOfInterest;
import org.scijava.display.Display;
import org.scijava.display.DisplayService;
import org.scijava.display.event.DisplayDeletedEvent;
import org.scijava.event.EventHandler;
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
public class PlotProfileService extends AbstractService implements ImageJService{
    
    
   protected final HashMap<Display<?>,LineList> list = new HashMap<>();
        
    
    @Parameter
    protected EventService eventService;
    
    @Parameter
    protected DisplayService displayService;
    
    @Parameter
    protected ImageDisplayService imageDisplayService;
    
    protected final Logger logger = ImageJFX.getLogger();
    
    protected List<LineOverlay> getLines(Display<?> display) {
            if(!list.containsKey(display)) {
                list.put(display,new LineList());
            }
            return list.get(display);
    }
    
    
    public Display<?> getCurrentDisplay() {
        return displayService.getActiveDisplay(ImageDisplay.class);
    }
    
    
    public List<LineOverlay> getDisplayLines(Display<?> display) {
        
        return Collections.unmodifiableList(getLines(display));
    }
    
    public List<LineOverlay> getActiveDisplayLine() {
        return Collections.unmodifiableList(getLines(getCurrentDisplay()));
    }
    
    public void addLine(Display<?> display, LineOverlay line) {
        getLines(display).add(line);
        eventService.publish(new ProfilePlotAddedEvent(display, line));
        
    }
    
    public void removeLine(Display<?> display, LineOverlay line) {
        if(getLines(display).contains(line)) {
            getLines(display).remove(line);
            eventService.publish(new ProfilePlotDeletedEvent(display,line));
        }
    }
    
    
    
    
    @EventHandler
    public void handleEvent(DisplayDeletedEvent event) {
         if(list.keySet().contains(event.getObject())) {
             list.remove(event.getObject());
         }
    }
    
    public double[] getProfilePlot(Display<?> display, LineOverlay lineOverlay) {
        return null;
    }
    
    
    private class LineList extends ArrayList<LineOverlay>{}
    
    public Histogram1d<?> lineOverlayToHistogram(LineOverlay lineOverlay) {
        
       
        return null;
    }
    
    
}
