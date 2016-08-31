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
package ijfx.ui.datadisplay.object;

import ijfx.service.batch.SegmentedObject;
import java.util.Collection;
import java.util.stream.Collectors;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import org.scijava.display.AbstractDisplay;
import org.scijava.display.Display;
import org.scijava.display.event.DisplayUpdatedEvent;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = Display.class)
public class DefaultObjectDisplay  extends AbstractDisplay<SegmentedObject> implements SegmentedObjectDisplay{
    
    
   
    
    @Parameter
    private EventService eventService;
    
    @Parameter
    private ImageDisplayService imageDisplayService;
    
    public DefaultObjectDisplay() {
        super(SegmentedObject.class);
    }
    
    @Override
    public void display(final Object o) {
        
        if(contains(o)) return;
        super.display(o);
        eventService.publish(new SegmentedObjectAddedEvent(this, (SegmentedObject)o));
    }

    
    @Override
    public boolean add(SegmentedObject o) {
        o = ensureType(o);
        return super.add(o);
    }
    
    protected SegmentedObject ensureType(SegmentedObject o) {
        if(o instanceof DisplayedSegmentedObject == false) {
            o = new DisplayedSegmentedObject(imageDisplayService.getActiveImageDisplay(),o);
        }
        return o;
    }
    
    @Override
    public ImageDisplay getImageDisplay() {
        return null;
    }

    @Override
    public void delete(SegmentedObject o) {
        remove(o);
        eventService.publish(new DisplayUpdatedEvent(this,DisplayUpdatedEvent.DisplayUpdateLevel.UPDATE));
    }
    
    @Override
    public void close() {
        super.close();
    }
    
    @Override
    public boolean addAll(Collection<? extends SegmentedObject> collection) {
        
        collection = collection
                .parallelStream()
                .map(this::ensureType)
                .collect(Collectors.toList());
      
        return super.addAll(collection);
    }
    
}
