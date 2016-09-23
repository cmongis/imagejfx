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
package ijfx.ui.datadisplay.metadataset;

import ijfx.core.metadata.MetaDataSet;
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
public class DefaultMetaDataDisplay extends AbstractDisplay<MetaDataSet> implements MetaDataSetDisplay{
    
    @Parameter
    EventService eventService;
    
    public DefaultMetaDataDisplay(Class<MetaDataSet> type) {
        super(type);
    }
    
     @Override
    public void update() {
        if(eventService == null) {
            super.getContext().inject(this);
        }
        super.update();
        eventService.publish(new DisplayUpdatedEvent(this, DisplayUpdatedEvent.DisplayUpdateLevel.UPDATE));
    }
    
    
}
