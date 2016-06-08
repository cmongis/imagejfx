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
package ijfx.plugins.commands;

import ijfx.plugins.DefaultInterval;
import ijfx.plugins.LongInterval;
import ijfx.service.sampler.DatasetSamplerService;
import ijfx.service.sampler.SamplingDefinition;
import net.imagej.Dataset;
import net.imagej.axis.AxisType;
import net.imagej.display.ImageDisplayService;
import net.imagej.sampler.AxisSubrange;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type=Command.class,menuPath = "Image > Stacks > Extract slices...",initializer = "init")
public class ExtractSlices extends ContextCommand {
    
    @Parameter(label="Slices to extract")
    private LongInterval interval;
    
    
    @Parameter
    private ImageDisplayService imageDisplayService;

    @Parameter
    private DatasetSamplerService datasetSamplerService;
    
    @Parameter(type = ItemIO.INPUT)
    private Dataset input;
    
    @Parameter(type = ItemIO.OUTPUT)
    private Dataset output;
    
    
    public void run() {
        
        
        SamplingDefinition def = new SamplingDefinition(input);
        
        def.constrain(AxisUtils.getSliceAxis(input), new AxisSubrange(interval.getLowValue(),interval.getHighValue()));
        
        output = datasetSamplerService.duplicateData(input, def);
        output.setSource("");
    }

    
    
    
   public void init() {
       if(interval == null && imageDisplayService.getActiveDataset() == input) {
           
           AxisType axis = AxisUtils.getSliceAxis(input);
           
           long min = input.min(input.dimensionIndex(axis));
           long max = input.max(input.dimensionIndex(axis));
           long low = min;
           long high = max;
           interval = new DefaultInterval(low,high,min,max);
       }
   }
    
}