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

import ijfx.ui.correction.ChannelSelector;
import net.imagej.Dataset;
import net.imagej.axis.Axes;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import org.scijava.ItemIO;
import org.scijava.command.ContextCommand;
import org.scijava.plugin.Parameter;

/**
 *
 * @author cyril
 */
public abstract class ChannelSpecificPlugin extends ContextCommand{
    
    @Parameter(type = ItemIO.BOTH)
    Dataset dataset;
    
    @Parameter(style=ChannelSelector.STYLE)
    int channel;
    
    
    public void run() {
        
        
        
        process();
       
        
    }
    
    public <T extends RealType<T>> void  process() {
         if(dataset.dimension(Axes.CHANNEL) == -1) {
            process((RandomAccessibleInterval<T>) dataset);
        }
        
        else {
            process(Views.hyperSlice((RandomAccessibleInterval<T>)dataset, dataset.dimensionIndex(Axes.CHANNEL), channel));
        }
    }
    public abstract <T extends RealType<T>> void process(RandomAccessibleInterval<T> channel);
    
    
}
