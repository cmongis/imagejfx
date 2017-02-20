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
package ijfx.plugins.extraction;

import ijfx.plugins.LongInterval;
import net.imagej.axis.AxisType;
import org.scijava.command.Command;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = Command.class, menuPath = "Image > Axis > Extract data")
public class Extract extends ExtractCommand{

    @Parameter(label = "Axis",callback = "updateInterval")
    AxisType axisType;
    
    @Parameter(label = "To keep")
    LongInterval interval;
    
    @Parameter
    EventService eventService;
    
    @Override
    protected AxisType getAxisType() {
       return axisType;
    }
    
    public void updateInterval() {
        interval = null;
        interval = getDefaultInterval();
        
    }

    @Override
    public LongInterval getInterval() {
       return interval;
    }
    
    
    
    
}
