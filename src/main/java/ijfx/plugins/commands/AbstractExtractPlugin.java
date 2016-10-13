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

import ijfx.service.sampler.DatasetSamplerService;
import net.imagej.Dataset;
import net.imagej.axis.AxisType;
import net.imagej.display.ImageDisplayService;
import org.scijava.ItemIO;
import org.scijava.command.ContextCommand;
import org.scijava.plugin.Parameter;
import org.scijava.ui.UIService;

/**
 *
 * @author cyril
 */
public abstract class AbstractExtractPlugin extends ContextCommand{
    

    @Parameter(type =ItemIO.INPUT)
    protected Dataset input;
    
    @Parameter(type = ItemIO.OUTPUT)
    Dataset output;
    
    @Parameter
    DatasetSamplerService samplerService;
    
    @Parameter
    ImageDisplayService imageDisplayService;
    
    @Parameter
    UIService uiService;
    
    abstract AxisType getAxis();
    
    long defaultPosition;
    
    
    
    public void run() {
        
        if(isCanceled()) {
            return;
        }
        
        
        
        if(input.dimensionIndex(getAxis()) == -1) {
            
            cancel(String.format("This dataset don't present any %s axis",getAxis().getLabel()));
            return;
            
        }
    
        
        
        output = samplerService.isolateDimension(input, getAxis(), getPosition());
        
        
    }
    
    public abstract long getPosition();
    public abstract void setPosition(long position);
    public void init() {
        
        if(imageDisplayService.getActiveDataset() == input) {
           setPosition(imageDisplayService.getActiveImageDisplay().getLongPosition(getAxis()));
        }
        
    }
    
    
    
}
