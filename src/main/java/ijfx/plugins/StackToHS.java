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
package ijfx.plugins;

import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.CompositeConverter;
import ij.plugin.frame.Recorder;
import ij.process.LUT;
import static java.lang.Math.toIntExact;
import java.util.Arrays;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.axis.CalibratedAxis;
import net.imagej.display.ImageDisplayService;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.menu.MenuConstants;
import org.scijava.plugin.Attr;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

/**
 *
 * @author tuananh
 */
//@Plugin(type = Command.class, menuPath = "Plugins>StacktoHS", label = "Test plugin")
@Plugin(type = Command.class, menu = {
    @Menu(label = MenuConstants.IMAGE_LABEL, weight = MenuConstants.IMAGE_WEIGHT,
            mnemonic = MenuConstants.IMAGE_MNEMONIC),
    @Menu(label = "Plugins", mnemonic = 't'),
    @Menu(label = "Rotate...", mnemonic = 'r')}, attrs = {
    @Attr(name = "no-legacy")})
public class StackToHS implements Command {

    @Parameter
    UIService uiService;

    /**
     *
     */
    @Parameter(type = ItemIO.INPUT)
    protected Dataset dataset;

    /**
     *
     */
    @Parameter(type = ItemIO.OUTPUT)
    protected Dataset datasetOutput;

    @Parameter
    DatasetService datasetService;

    @Parameter
    ImageDisplayService imageDisplayService;

    @Parameter(label = "Channels (c)")
    Integer channels;
    @Parameter(label = "Slices (z)")
    Integer slices;

    @Parameter(label = "Frames (t)")
    Integer frames;

    @Override
    public void run() {
        AxisType[] axisType = new AxisType[5];
        axisType[0] = Axes.X;
        axisType[1] = Axes.Y;
        axisType[2] = Axes.CHANNEL;
        axisType[3] = Axes.TIME;
        axisType[4] = Axes.Z;
        long[] dims = new long[axisType.length];
        dims[0] = dataset.max(0)+1;
        dims[1] = dataset.max(1)+1;
        dims[2] = channels;
        dims[3] = frames;
        dims[4] = slices;

        datasetOutput = emptyDataset(dims, axisType, dataset);
        copyDataset(dataset, datasetOutput);
    }

    private Dataset emptyDataset(long[] dims, AxisType[] axisType, Dataset input) {
        return datasetService.create(dims, input.getName(), axisType, input.getValidBits(), input.isSigned(), false);
    }

    private < T extends RealType< T>> void copyDataset(Dataset input, Dataset output) {
        Img<?> inputImg = input.getImgPlus().getImg();
        Cursor<T> cursorInput = (Cursor<T>) inputImg.localizingCursor();
        RandomAccess<T> randomAccessOutput = (RandomAccess<T>) output.randomAccess();
        long[] position = new long[input.numDimensions()];
        long[] positionOutput = new long[output.numDimensions()];

        while (cursorInput.hasNext()) {
            cursorInput.fwd();
            cursorInput.localize(position);
            System.arraycopy(position, 0, positionOutput, 0, position.length);

            positionOutput = conversionDimension(positionOutput, position, output);
           // System.out.println(Arrays.toString(positionOutput));

            randomAccessOutput.setPosition(positionOutput);
            try {
                
            randomAccessOutput.get().set(cursorInput.get());
            } catch (Exception e) {
                System.out.println(Arrays.toString(positionOutput));
            }

        }
    }

    private long[] conversionDimension(long[] positionOutput, long[] position, Dataset output) {
        int rest;
        for (int i = 2; i < positionOutput.length; i++) {
            positionOutput[i] = position[2] / output.max(i);
            rest = (int) (position[2] % output.max(i));
            if (positionOutput[i] == 0.0) {
                positionOutput[i] = rest;
                rest = 0;
            } else if (rest != 0) {
                positionOutput[i + 1] = rest;
            }
        }
        return positionOutput;
    }
}
