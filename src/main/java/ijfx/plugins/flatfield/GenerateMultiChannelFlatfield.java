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
package ijfx.plugins.flatfield;

import ijfx.core.stats.IjfxStatisticService;
import ijfx.service.dataset.DatasetUtillsService;
import net.imagej.Dataset;
import net.imagej.ops.OpService;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.scijava.ItemIO;
import org.scijava.app.StatusService;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Cyril MONGIS, 2016
 */
@Plugin(type = Command.class,menuPath = "Process > Correction > Generate Flafield (multi-channel)")
public class GenerateMultiChannelFlatfield extends ContextCommand{
    
    @Parameter(type = ItemIO.INPUT)
    Dataset input;

    @Parameter(type = ItemIO.OUTPUT)
    Dataset output;
    
    @Parameter
    IjfxStatisticService statsService;
    
    @Parameter
    OpService opService;
    
   
    @Parameter
    DatasetUtillsService datasetUtilsService;
    
    @Parameter
    StatusService statusService;
    
    @Override
    public void run() {
        
        if(input.numDimensions() != 3) {
            cancel("This plugin only accept 3 dimensional images (X,Y, Channel).");
            return;
        }
        statusService.showStatus("Converting...");
        
        if(input.getTypeLabelLong().contains("float") == false) {
            output = datasetUtilsService.convert(input, new FloatType());
        }
        else {
            output = input;
        }
       long channelNumber = output.dimension(2);
        
       for(long i = 0; i!= channelNumber;i++) {
           statusService.showStatus((int)i, (int)channelNumber, "Normalizing...");
            normalize(Views.hyperSlice((RandomAccessibleInterval)output,2,i));
       }
    }
    
    
    
    
    public <T extends RealType<T>> void normalize(RandomAccessibleInterval<T> interval) {
        DoubleType result = new DoubleType();

        
        opService.stats().median(result,Views.iterable(interval));
        double median = result.getRealDouble();
        
        Cursor<T> cursor = Views.iterable(interval).cursor();
        
        cursor.reset();
        while(cursor.hasNext()) {
            cursor.fwd();
            cursor.get().setReal(cursor.get().getRealDouble()/median);
        }
    }
    
    
    
    
    
    
}
