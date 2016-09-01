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
import ijfx.service.IjfxService;
import ijfx.ui.main.ImageJFX;
import java.util.logging.Logger;
import org.scijava.display.DisplayService;
import org.scijava.display.event.DisplayCreatedEvent;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 *
 * @author cyril
 */
@Plugin(type = Service.class)
public class MetaDataSetDisplayService extends AbstractService implements IjfxService {

    @Parameter
    DisplayService displayService;

    @Parameter
    EventService eventService;

    Logger logger = ImageJFX.getLogger();

    public MetaDataSetDisplay createDisplay(String name) {

        DefaultMetaDataDisplay display = new DefaultMetaDataDisplay(MetaDataSet.class);

        display.setName(name);

        displayService.getDisplays().add(display);

        eventService.publish(new DisplayCreatedEvent(display));
        getContext().inject(display);
        return display;

    }

    public MetaDataSetDisplay findDisplay(String name) {
        return displayService.getDisplaysOfType(MetaDataSetDisplay.class)
                .stream()
                .filter(display -> name.equals(display.getName()))
                .findFirst()
                .orElseGet(() -> createDisplay(name));
    }

    public void addMetaDataset(MetaDataSet metaDataSet) {
        MetaDataSetDisplay activeDisplay = displayService.getActiveDisplay(MetaDataSetDisplay.class);

        if (activeDisplay == null) {
            displayService.createDisplay("Measures", metaDataSet);
            
        }
        activeDisplay = displayService.getActiveDisplay(MetaDataSetDisplay.class);
        activeDisplay.add(metaDataSet);

        activeDisplay.update();
    }

    public void addMetaDataSetToDisplay(MetaDataSet metaDataSet, String displayName) {
        MetaDataSetDisplay display = findDisplay(displayName);

        display.add(metaDataSet);
        display.update();
    }

}
