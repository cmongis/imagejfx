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
package ijfx.plugins.stack;

import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.axis.Axes;
import net.imagej.axis.CalibratedAxis;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = Command.class, label="Z-stack to multi-channel",menuPath = "Image > Stacks > Z-Axis to channels",description = "Transform the 3-rd axis into a channel axis. Only works on 3D images.")
public class ZStackToMultichannel extends ContextCommand{

    @Parameter
    DatasetService datasetSrv;
    
    @Parameter(type = ItemIO.BOTH)
    Dataset dataset;
    
    @Parameter
    EventService eventService;
  
    
    @Override
    public void run() {
        
        if(dataset.numDimensions() != 3) {
            cancel("This plugin only change the third axis of a 3-d image into a channel axis.");
            return;
        }
        CalibratedAxis axis = dataset.axis(2);
        axis.setType(Axes.CHANNEL);
        dataset.rebuild();
        
        
    }
    
}
