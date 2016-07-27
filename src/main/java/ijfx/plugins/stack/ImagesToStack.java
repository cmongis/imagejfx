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

import java.util.stream.IntStream;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.axis.AxisType;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.type.numeric.RealType;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Concatenate images to Stacks.
 *
 * @author Tuan anh TRINH
 *
 */
@Plugin(type = Command.class, menuPath = "Image>Stacks> ImagesToStack")
public class ImagesToStack extends ContextCommand {

    @Parameter
    DatasetService datasetService;

    @Parameter
    Dataset[] datasetArray;

    @Parameter(type = ItemIO.OUTPUT)
    Dataset outputDataset;

    @Parameter
    AxisType axisType;

    @Parameter(required = false)
    String title = "title";

    @Override
    public void run() {
        initOutput();
        for (int i = 0; i < datasetArray.length; i++) {
            addPlane(outputDataset, datasetArray[i], axisType, i);
            copyColorTable(datasetArray[i], outputDataset, 0);
        }
    }

    public <T extends RealType<T>> void initOutput() {
        int size = datasetArray.length;
        Dataset firstElement = datasetArray[0];

        long[] dims = new long[firstElement.numDimensions() + 1];
        firstElement.dimensions(dims);

        int axisTypeIndex = 2;
        dims[axisTypeIndex] = size;

        AxisType[] axisTypesArray = new AxisType[3];
        IntStream.range(0, firstElement.numDimensions())
                .forEach(i -> axisTypesArray[i] = firstElement.axis(i).type());
        axisTypesArray[2] = axisType;
        outputDataset = datasetService.create(dims, title, axisTypesArray, firstElement.getValidBits(), firstElement.isSigned(), false);
        outputDataset.initializeColorTables(firstElement.getColorTableCount());
        outputDataset.setName(title);
    }

    private void addPlane(Dataset datasetOutput, Dataset input, AxisType axisType, int i) {
        RandomAccess<? extends RealType> randomAccessOutput = datasetOutput.randomAccess();
        Cursor<? extends RealType> cursorInput = input.cursor();
        long[] positionOutput = new long[datasetOutput.numDimensions()];
        while (cursorInput.hasNext()) {
            cursorInput.next();
            cursorInput.localize(positionOutput);
            IntStream.range(0, input.numDimensions())
                    .forEach(j -> randomAccessOutput.setPosition(cursorInput.getIntPosition(j), j));
            randomAccessOutput.setPosition(i, 2);
            randomAccessOutput.get().setReal(cursorInput.get().getRealFloat());

        }
    }
    
    private void copyColorTable(Dataset input, Dataset output, int cpt){
        output.setColorTable(input.getColorTable(cpt), cpt);
    }

}
