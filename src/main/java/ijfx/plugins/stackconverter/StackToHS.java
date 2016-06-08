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
package ijfx.plugins.stackconverter;

import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Attr;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.DialogPrompt;
import org.scijava.ui.UIService;

/**
 *
 * @author tuananh
 */
//@Plugin(type = Command.class, menuPath = "Plugins>StacktoHS", label = "Test plugin")
@Plugin(type = Command.class, menuPath = "Image > Hyperstacks > Stack to Hyperstack...", attrs = {
    @Attr(name = "no-legacy")})
public class StackToHS implements Command {

    public final static String CZT = "CZT";
    public final static String CTZ = "CTZ";
    public final static String ZCT = "ZCT";
    public final static String ZTC = "ZTC";
    public final static String TZC = "TZC";
    public final static String TCZ = "TCZ";

    @Parameter
    UIService uIService;
    @Parameter(type = ItemIO.INPUT)
    protected Dataset dataset;

    @Parameter(type = ItemIO.OUTPUT)
    protected Dataset datasetOutput;

    @Parameter
    private DatasetService datasetService;

    @Parameter(required = true, label = "Order", choices = {CZT, CTZ, ZCT, ZTC, TZC, TCZ})
    public String order;
    
    @Parameter(label = "Channels (c)")
    public Integer channels=5;
    
    @Parameter(label = "Slices (z)")
    public Integer slices=5;

    @Parameter(label = "Frames (t)")
    public Integer frames=5;

    @Override
    public void run() {
        AxisType[] axisType = new AxisType[5];
        axisType[0] = Axes.X;
        axisType[1] = Axes.Y;

        long[] dims = new long[axisType.length];
        dims[0] = dataset.max(0) + 1;
        dims[1] = dataset.max(1) + 1;
        applyOrder(axisType, dims);
        if (slices*channels*frames!=dataset.max(2)+1)
        {
            String message = "slices*channels*frames is not equal to the number of slices";
            uIService.showDialog(message, DialogPrompt.MessageType.ERROR_MESSAGE);
            return;
        }
        datasetOutput = emptyDataset(dims, axisType, dataset);
        HyperStackConverter.copyDataset(dataset, datasetOutput, new OneToMultiDimensions());
    }

    public void applyOrder(AxisType[] axisType, long[] dims) {
        switch (order) {
            case CZT:
                axisType[2] = Axes.CHANNEL;
                axisType[3] = Axes.Z;
                axisType[4] = Axes.TIME;
                dims[2] = channels;
                dims[3] = slices;
                dims[4] = frames;
            case CTZ:
                axisType[2] = Axes.CHANNEL;
                axisType[3] = Axes.TIME;
                axisType[4] = Axes.Z;
                dims[2] = channels;
                dims[3] = frames;
                dims[4] = slices;
            case ZCT:
                axisType[2] = Axes.Z;
                axisType[3] = Axes.CHANNEL;
                axisType[4] = Axes.TIME;
                dims[2] = slices;
                dims[3] = channels;
                dims[4] = frames;
            case ZTC:
                axisType[2] = Axes.Z;
                axisType[3] = Axes.TIME;
                axisType[4] = Axes.CHANNEL;
                dims[2] = slices;
                dims[3] = frames;
                dims[4] = channels;
            case TZC:
                axisType[2] = Axes.TIME;
                axisType[3] = Axes.Z;
                axisType[4] = Axes.CHANNEL;
                dims[2] = frames;
                dims[3] = slices;
                dims[4] = channels;
            case TCZ:
                axisType[2] = Axes.TIME;
                axisType[3] = Axes.CHANNEL;
                axisType[4] = Axes.Z;
                dims[2] = frames;
                dims[3] = channels;
                dims[4] = slices;
        }
    }

    private Dataset emptyDataset(long[] dims, AxisType[] axisType, Dataset input) {
        return datasetService.create(dims, input.getName(), axisType, input.getValidBits(), input.isSigned(), false);
    }


//
//    private long[] conversionDimension(long[] positionOutput, long[] position, Dataset output) {
//        int rest;
//        int coorOrigin = (int) position[2];
//        for (int i = 2; i < positionOutput.length; i++) {
//            positionOutput[i] = coorOrigin / (output.max(i) + 1);
//            rest = (int) (position[2] % (output.max(i)+1));
//            if (positionOutput[i] == 0.0) {
//                positionOutput[i] = rest;
//            } else if (rest != 0) {
//                coorOrigin = rest;
//            }
//        }
//        return positionOutput;
//    }
}
