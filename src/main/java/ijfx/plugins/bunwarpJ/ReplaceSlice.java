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
package ijfx.plugins.bunwarpJ;

import net.imagej.Dataset;
import net.imglib2.RandomAccess;
import net.imglib2.type.numeric.RealType;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Tuan anh TRINH
 */
@Plugin(type = Command.class)
public class ReplaceSlice implements Command {
    
    @Parameter(type = ItemIO.INPUT)
    Dataset stack;
    
    @Parameter(type = ItemIO.INPUT)
    long[] position;
    
    @Parameter(type = ItemIO.INPUT)
    Dataset sliceDataset;
    
    @Parameter(type = ItemIO.OUTPUT)
    Dataset outputDataset;
    
    
    
    @Override
    public void run() {
        
        long[] dimensions = new long[2];
        dimensions[0] = stack.dimension(0);
        dimensions[1] = stack.dimension(1);
        
        RandomAccess<RealType<?>> inputRandomAccess = sliceDataset.randomAccess();
        RandomAccess<RealType<?>> outputRandomAccess = stack.randomAccess();
        
        inputRandomAccess.setPosition(new long[]{0, 0});
        outputRandomAccess.setPosition(position);
        

        for(int x = 0; x < dimensions[0]; x++){
            for(int y = 0; y < dimensions[1]; y++){
                inputRandomAccess.setPosition(x, 0);
                inputRandomAccess.setPosition(y, 1);

                outputRandomAccess.setPosition(x, 0);
                outputRandomAccess.setPosition(y, 1);

                outputRandomAccess.get().setReal(inputRandomAccess.get().getRealDouble());
            }
        }
        
        outputDataset = stack;
        
    }
    
}