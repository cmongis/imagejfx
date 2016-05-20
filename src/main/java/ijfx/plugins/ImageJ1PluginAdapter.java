/*
 * /*
 *     This file is part of ImageJ FX.
 *
 *     ImageJ FX is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     ImageJ FX is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with ImageJ FX.  If not, see <http://www.gnu.org/licenses/>. 
 *
 * 	Copyright 2015,2016 Cyril MONGIS, Michael Knop
 *
 */
package ijfx.plugins;

import ij.ImagePlus;
import static java.lang.Math.toIntExact;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.axis.CalibratedAxis;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.event.DatasetCreatedEvent;
import net.imagej.event.DatasetUpdatedEvent;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;

import net.imglib2.type.numeric.integer.UnsignedShortType;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public abstract class ImageJ1PluginAdapter implements Command {

    @Parameter(type = ItemIO.BOTH)
    protected Dataset dataset;

    @Parameter
    DatasetService service;

    @Parameter
    ImageDisplayService imageDisplayService;

    @Parameter
    EventService eventService;

    boolean createCopy = false;

    public ImagePlus getInput(Dataset dataset) {
        return unwrapDataset(dataset);
    }

    public void setOutput(ImagePlus imp, Dataset dataset) {
        dataset = wrapDataset(imp);
    }

    public static ImagePlus unwrapDataset(Dataset dataset) {
        RandomAccessibleInterval<UnsignedShortType> r = (RandomAccessibleInterval<UnsignedShortType>) dataset.getImgPlus();
        ImagePlus wrapImage = ImageJFunctions.wrap(r, "");
        return wrapImage;
    }

    public Dataset wrapDataset(ImagePlus imp) {
        Img img = ImageJFunctions.wrap(imp);
        return service.create(img);
    }

    public static void configureImagePlus(ImagePlus imp, ImageDisplay imageDisplay) {

        imp.setC(imageDisplay.getIntPosition(Axes.CHANNEL));
        imp.setZ(imageDisplay.getIntPosition(Axes.Z));
        imp.setT(imageDisplay.getIntPosition(Axes.TIME));

    }

    private Dataset emptyDataset(Dataset input) {
        AxisType[] axisType = new AxisType[input.numDimensions()];
        CalibratedAxis[] axeArray = new CalibratedAxis[input.numDimensions()];
        input.axes(axeArray);

        long[] dims = new long[2];
        for (int i = 0; i < 2; i++) {
            axisType[i] = axeArray[i].type();
            dims[i] = toIntExact(input.max(i) + 1);
        }
        return service.create(dims, input.getName(), axisType, input.getValidBits(), input.isSigned(), false);
    }

    private Dataset chooseDataset() {
        if (createCopy) {
            return dataset.duplicateBlank();
        }
        return dataset;
    }

    public boolean isCreateCopy() {
        return createCopy;
    }

    public void setCreateCopy(boolean createCopy) {
        this.createCopy = createCopy;
    }

    public abstract ImagePlus run(ImagePlus input);

    public int getNumberOfSlices(Dataset dataset) {

        return (int) (dataset.getImgPlus().size()/(dataset.dimension(0)*dataset.dimension(1)));
 
    }

    @Override
    public void run() {
        setCreateCopy(true);
        Dataset datasetToModify = chooseDataset();

        IntStream
                .range(0, getNumberOfSlices(datasetToModify))
                .forEach(i -> {
                    byte[] plane = (byte[]) this.dataset.getPlane(i);
                    Dataset datasetOnePlane = emptyDataset(this.dataset);
                    datasetOnePlane.setPlane(0, plane);
                    ImagePlus result = run(getInput(datasetOnePlane));
                    setOutput(result, datasetOnePlane);
                    datasetToModify.setPlane(i, datasetOnePlane.getPlane(0));
                });
        dataset = datasetToModify;
    }

}
