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

import ijfx.plugins.DefaultInterval;
import ijfx.plugins.LongInterval;
import ijfx.service.dataset.DatasetUtillsService;
import ijfx.service.sampler.DatasetSamplerService;
import ijfx.service.sampler.SamplingDefinition;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.axis.AxisType;
import net.imagej.axis.CalibratedAxis;
import net.imagej.display.ImageDisplayService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.view.Views;
import org.scijava.ItemIO;
import org.scijava.command.InteractiveCommand;
import org.scijava.plugin.Parameter;

/**
 *
 * @author Cyril MONGIS, 2016
 */

public abstract class ExtractCommand extends InteractiveCommand{

    
    @Parameter(type = ItemIO.INPUT)
    Dataset input;
    
    @Parameter(type = ItemIO.OUTPUT)
    Dataset output;
    
   
    
    @Parameter
    protected ImageDisplayService imageDisplayService;
    
    @Parameter
    protected DatasetSamplerService datasetSamplerService;
    
    @Parameter
    protected DatasetUtillsService datasetUtilsService;
    
    @Parameter
    protected DatasetService datasetService;
    
    @Override
    public void run() {
        
        
        
        SamplingDefinition def = new SamplingDefinition(input);
        LongInterval interval = getInterval();
        long[] mins = new long[input.numDimensions()];
        long[] maxs = new long[input.numDimensions()];
        input.min(mins);
        input.max(maxs);
        
        int axisId = input.dimensionIndex(getAxisType());
        if(axisId == -1) {
            cancel(String.format("The input doesn't have an %s axis",getAxisType().getLabel()));
            return;
        }
        
        mins[axisId] = getInterval().getLowValue();
        maxs[axisId] = getInterval().getHighValue();
        
        output = datasetService.create(Views.interval((RandomAccessibleInterval)input, mins, maxs));
        
        CalibratedAxis[] axes = new CalibratedAxis[input.numDimensions()];
        
        input.axes(axes);
        
        output.setAxes(axes);
        output.setName(input.getName());  
        datasetUtilsService.addSuffix(output, String.format("%s %d-%d",getAxisType().getLabel(),interval.getLowValue(),interval.getHighValue()), " ");
    }
    
    protected abstract AxisType getAxisType();
    
    
    public abstract LongInterval getInterval();
    
    
    
    public LongInterval getDefaultInterval() {    
        if(getInterval() == null && imageDisplayService.getActiveDataset() == input) {
           AxisType axis = getAxisType();
           int axisId = input.dimensionIndex(axis);
           if(axisId == -1) axisId = 2;
           long min = input.min(axisId);
           long max = input.max(axisId);
           long low = min;
           long high = max;
           return new DefaultInterval(low,high,min,max);
       }
        else {
            return getInterval();
        }
    }
    
    
    
    
    
}
