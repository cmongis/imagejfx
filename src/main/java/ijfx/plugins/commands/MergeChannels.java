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

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ColorBlitter;
import ij.process.ColorProcessor;
import ijfx.plugins.RemoveSlice;
import ijfx.service.ImagePlaneService;
import ijfx.service.Timer;
import ijfx.service.TimerService;
import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.NumericType;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = Command.class, menuPath = "Image > Stacks > Merge channels")
public class MergeChannels implements Command {

    @Parameter(type = ItemIO.INPUT)
    Dataset input;

    @Parameter(type = ItemIO.OUTPUT)
    Dataset output;

    @Parameter
    ImagePlaneService imagePlaneService;

    @Parameter
    DatasetService datasetService;

    @Parameter
    int mode = 1;

    @Parameter
    TimerService timerService;

    Timer timer;

    @Override
    public void run() {

        timer = timerService.getTimer(this.getClass());

        if (input.numDimensions() == 4) {
            final AxisType sliceAxis = RemoveSlice.getSliceAxis(input);
            final int sliceAxisIndex = input.dimensionIndex(sliceAxis);
            final long sliceAxisMax = input.max(sliceAxisIndex) + 1;
            List<ImagePlus> collect = LongStream
                    .range(0, sliceAxisMax)
                    .mapToObj(n -> {
                        long[] pos = new long[4];
                        pos[sliceAxisIndex] = n;

                        System.out.println(Arrays.toString(pos));

                        return pos;
                    }) // create a set of position array that goes from z = 0 to z = zmax
                    .parallel() // we put the stream in parallel
                    .map(pos -> extractChannels(input, pos)) // we extract subset of arrays single-plane-dataset (each array is a set of plane of the same channel)
                    .map(this::datasetToImagePlus) // convert them to array of image plus
                    .map(imgPlusArray -> mergeChannels(imgPlusArray, mode)) // merge them
                    .collect(Collectors.toList()); // collect the merge result

            output = ImagePlusListToDataset(collect);

        }
        else if(input.numDimensions() == 3) {
            long[] pos = new long[3];
            
            ImagePlus mergeChannels = mergeChannels(datasetToImagePlus(extractChannels(input, pos)),mode);
            mergeChannels.show();
            output = imagePlusToDataset(mergeChannels);
        }

        final Timer t = timerService.getTimer("Channel extraction");
        t.logAll();

    }

    private Dataset[] extractChannels(Dataset input, long[] position) {

        final int channelAxisIndex = input.dimensionIndex(Axes.CHANNEL);
        final long channelNumber = input.max(channelAxisIndex) + 1;

        final Timer t = timerService.getTimer("Channel extraction");
        t.start();

        Dataset[] datasets = LongStream.range(0, channelNumber)
                .parallel()
                .mapToObj(channelId -> {
                    long[] pos = new long[position.length];
                    System.arraycopy(position, 0, pos, 0, pos.length);
                    pos[channelAxisIndex] = channelId;
                    System.out.println(Arrays.toString(pos));
                    return imagePlaneService.isolatePlane(input, pos);
                })
                .toArray(n -> new Dataset[n]);
        t.elapsed("extraction");

        return datasets;
    }

    private ImagePlus[] datasetToImagePlus(Dataset[] dataset) {
        return Stream.of(dataset).map(this::datasetToImagePlus).toArray(n -> new ImagePlus[n]);
    }

    private ImagePlus mergeChannels(ImagePlus[] imgPlusArray, int method) {

        int width = imgPlusArray[0].getWidth();
        int height = imgPlusArray[0].getHeight();

        ColorProcessor colorProcessor = new ColorProcessor(width, height);

        for (ImagePlus imp : imgPlusArray) {
            imp.getProcessor().convertToRGB().copyBits(colorProcessor, 0, 0, mode);
            //colorProcessor.copyBits(imp.getProcessor().convertToRGB(), 0, 0, ColorBlitter.ADD);
        }
        colorProcessor.drawString("Hello it's me", 10, 10, Color.YELLOW);

        return new ImagePlus("", colorProcessor);
    }

    private <T extends NumericType<T>> ImagePlus datasetToImagePlus(Dataset dataset) {
        RandomAccessibleInterval<T> imgPlus = (RandomAccessibleInterval<T>) dataset.getImgPlus();

        ImagePlus wrapImage = ImageJFunctions.wrap(imgPlus, "");

        //wrapImage.show();
        return wrapImage.duplicate();
    }

    private <T extends NumericType<T>> Dataset ImagePlusListToDataset(List<ImagePlus> imagePlusList) {

        long width = input.max(0);
        long height = input.max(1);
        final AxisType sliceAxis = RemoveSlice.getSliceAxis(input);
        final int sliceAxisIndex = input.dimensionIndex(RemoveSlice.getSliceAxis(input));
        final long sliceAxisMax = input.max(sliceAxisIndex) + 1;

        long[] dims = new long[]{width, height, sliceAxisMax};
        AxisType[] axes = new AxisType[]{Axes.X, Axes.Y, sliceAxis};

        ImageStack stack = null;

        for (ImagePlus plane : imagePlusList) {
            if (stack == null) {
                stack = new ImageStack((int) width, (int) height);
            } else {
                stack.addSlice(plane.getProcessor());
            }
        }

        ImagePlus result = new ImagePlus("", stack);
        result.show();
       
        return imagePlusToDataset(result);
    }
    
    private Dataset imagePlusToDataset(ImagePlus imp) {
         Img img = ImageJFunctions.wrap(imp);
        return datasetService.create(img);
    }

}
