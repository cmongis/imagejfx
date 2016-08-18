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

import java.util.Arrays;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.RandomAccess;
import net.imglib2.type.numeric.RealType;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.display.DisplayService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Tuan anh TRINH
 */


@Plugin(type = Command.class)
public class ExtractSliceCommand implements Command {
    
    @Parameter
    DatasetService datasetService;   
    
    @Parameter
    DisplayService displayService;
    
    @Parameter(type = ItemIO.INPUT, persist = false)
    Dataset inputDataset;
    
    @Parameter
    long[] position;
    
    @Parameter(type = ItemIO.OUTPUT, persist = false)
    Dataset outputDataset;
    
    
    @Override
    public void run() {
           
        long[] dimensions = new long[2];
        dimensions[0] = inputDataset.dimension(0);
        dimensions[1] = inputDataset.dimension(1);
        String inputDatasetName = inputDataset.getName();
        String[] split = inputDatasetName.split("\\.");
        String outputDatasetName = split[0] + "_slice" + Arrays.toString(position) + "." + split[1];
        AxisType[] axisArray = new AxisType[]{Axes.X, Axes.Y};
        outputDataset = datasetService.create(dimensions, outputDatasetName, axisArray, inputDataset.getType().getBitsPerPixel(), inputDataset.isSigned(), !inputDataset.isInteger());
        RandomAccess<RealType<?>> inputRandomAccess = inputDataset.randomAccess();
        RandomAccess<RealType<?>> outputRandomAccess = outputDataset.randomAccess();

        inputRandomAccess.setPosition(position);
        outputRandomAccess.setPosition(new long[]{0, 0});

        for(int x = 0; x < dimensions[0]; x++){
            for(int y = 0; y < dimensions[1]; y++){
                inputRandomAccess.setPosition(x, 0);
                inputRandomAccess.setPosition(y, 1);

                outputRandomAccess.setPosition(x, 0);
                outputRandomAccess.setPosition(y, 1);

                outputRandomAccess.get().setReal(inputRandomAccess.get().getRealDouble());
            }
        }
       
    }
}
