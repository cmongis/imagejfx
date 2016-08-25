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

import ijfx.service.ui.LoadingScreenService;
import java.util.stream.Stream;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.type.numeric.RealType;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.DialogPrompt;
import org.scijava.ui.StatusBar;
import org.scijava.ui.UIService;

/**
 *
 * @author cyril
 */
@Plugin(type = Command.class, menuPath = "Image > Stacks > Merge into channels...")
public class MergeStacks extends ContextCommand {

    @Parameter(type = ItemIO.INPUT, required = true, label = "Select datasets to merge")
    Dataset[] inputs = null;

    @Parameter(type = ItemIO.OUTPUT)
    Dataset output;

    @Parameter
    UIService uiService;

    @Parameter
    DatasetService datasetService;

    @Override
    public void run() {

        if (!checkAxis(inputs)) {
            uiService.showDialog("Only 3-dimensional datasets can be merged.");
            return;
        }

        if (!checkDatasetSize(inputs)) {
            uiService.showDialog("All the images should have the same size", DialogPrompt.MessageType.ERROR_MESSAGE);
            return;
        }

        if (!checkDatasetType(inputs)) {
            uiService.showDialog("You cannot mix images from different types.", DialogPrompt.MessageType.ERROR_MESSAGE);
            return;
        }

        Dataset firstDataset = inputs[0];

        long width = firstDataset.dimension(0);
        long height = firstDataset.dimension(1);
        long depth = firstDataset.numDimensions() > 2 ? firstDataset.max(2) : 0;
        long channelNumber = inputs.length;
        int total = 1 + inputs.length;

        StatusBar statusBar = uiService.getDefaultUI().getStatusBar();

        statusBar.setProgress(1, total);
        statusBar.setStatus("Creating dataset...");

        long[] dims;
        AxisType[] axes;
        if (depth > 0) {
            axes = new AxisType[]{Axes.X, Axes.Y, Axes.CHANNEL, Axes.Z};
            dims = new long[]{width, height, channelNumber, depth};
        } else {
            axes = new AxisType[]{Axes.X, Axes.Y, Axes.CHANNEL};
            dims = new long[]{width,height,channelNumber};
        }

        output = datasetService.create(dims, firstDataset.getName(), axes, firstDataset.getValidBits(), firstDataset.isSigned(), false);
        output.initializeColorTables((int)channelNumber);
                
        statusBar.setStatus("Copying data...");
        for (int c = 0; c != channelNumber; c++) {
            statusBar.setProgress(c + 1, total);

            copyDataset(c, inputs[c]);
        }
    }

    private boolean checkAxis(Dataset[] datasets) {
        // check if all the datasets have 3 dimensions, not more, not less
        
        return datasets.length == Stream.of(datasets).filter(d -> d.numDimensions() <= 3).count();
    }

    private boolean checkDatasetSize(Dataset[] datasets) {
        return true;

    }

    private boolean checkDatasetType(Dataset[] datasets) {
        return true;
    }

    private <T extends RealType<T>> void copyDataset(int channel, Dataset dataset) {

        if (output.numDimensions() == 4) {

            long[] positionInput = new long[dataset.numDimensions()];
            long[] positionOutput = new long[4];

            positionOutput[2] = channel;

            RandomAccess<T> randomAccessInput = (RandomAccess<T>) dataset.randomAccess();
            RandomAccess<T> randomAccessOutput = (RandomAccess<T>) output.randomAccess();

            for (int x = 0; x != dataset.dimension(0); x++) {
                for (int y = 0; y != dataset.dimension(1); y++) {
                    for (int z = 0; z != dataset.dimension(2); z++) {

                        positionInput[0] = x;
                        positionInput[1] = y;
                        positionInput[2] = z;
                        positionOutput[0] = x;
                        positionOutput[1] = y;
                        positionOutput[3] = z;

                        randomAccessInput.setPosition(positionInput);
                        randomAccessOutput.setPosition(positionOutput);
                        randomAccessOutput.get().set(randomAccessInput.get());

                    }
                }
            }
        } else if (output.numDimensions() == 3) {
            long[] position = new long[3];
            RandomAccess<T> randomAccessOutput = (RandomAccess<T>) output.randomAccess();
            
            Cursor<T> cursor = (Cursor<T>)dataset.cursor();
            cursor.reset();
            
            while (cursor.hasNext()) {
               cursor.fwd();
                cursor.localize(position);
                 position[2] = channel;
                 randomAccessOutput.setPosition(position);
                randomAccessOutput.get().set(cursor.get());
            }

        }

    }

}
