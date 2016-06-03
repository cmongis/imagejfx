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
import ij.process.ColorProcessor;
import ijfx.service.ImagePlaneService;
import ijfx.service.Timer;
import ijfx.service.TimerService;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.display.DatasetView;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.ops.OpService;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.converter.RealLUTConverter;
import net.imglib2.display.projector.composite.CompositeXYProjector;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

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
    private DatasetService datasetService;


    @Parameter
    private UIService uiService;


    @Override
    public void run() {

        
        
        //Dataset dataset = imageDisplayService.getActiveDataset(input);

        output = convert2(input);
    }

    public <T extends RealType<T>> Dataset convert2(Dataset inputDataset) {
        ImgPlus<T> imgPlus = (ImgPlus<T>) inputDataset.getImgPlus();

        long[] dims = new long[imgPlus.numDimensions()];
        AxisType[] axes = new AxisType[imgPlus.numDimensions()];

        for (int i = 0; i != dims.length; i++) {
            dims[i] = imgPlus.dimension(i);
            axes[i] = imgPlus.axis(i).type();
        }

        ArrayList<Converter<T, ARGBType>> converters
                = new ArrayList<>();
        long channelNumber = imgPlus.max(imgPlus.dimensionIndex(Axes.CHANNEL)) + 1;
        int channelAxisIdex = imgPlus.dimensionIndex(Axes.CHANNEL);

        dims[channelAxisIdex] = 3;
        //DatasetView view = imageDisplayService.getActiveDatasetView(input);
        for (int i = 0; i != channelNumber; i++) {
            converters.add(new RealLUTConverter<>(inputDataset.getChannelMinimum(i), inputDataset.getChannelMaximum(i), imgPlus.getColorTable(i)));
        }

        output = datasetService.create(new UnsignedByteType(), dims, "", axes);

        //RandomAccess<T> inputRandomAccess = (RandomAccess<T>) inputDataset.randomAccess();
        Cursor<T> inputCursor = (Cursor<T>) imgPlus.cursor();
        RandomAccess<? extends RealType> outputCursor = output.randomAccess();
        long[] position = new long[imgPlus.numDimensions()];

        
        final ARGBType color = new ARGBType();
        
        T t;
        inputCursor.reset();
        while (inputCursor.hasNext()) {
            inputCursor.fwd();

           
            // copy the localization of the input cursor so we can localized the output
            inputCursor.localize(position);
          
            // i don't know if it's necessary
            //inputRandomAccess.setPosition(position);
            
            t = inputCursor.get();

            int channel = inputCursor.getIntPosition(channelAxisIdex);
            
            double d = t.getRealDouble();

            
          
            // get the color associated to the the pixel depending on which channel it comes from
            converters.get(channel).convert(t, color);

            // set the output cur
            position[channelAxisIdex] = 0;
            outputCursor.setPosition(position);

            // we separate the rgb from the argb integer
            int value = color.get();

            final int red = (value >> 16 ) & 0xff;
            final int green = ( value >> 8 ) & 0xff;
            final int blue = value & 0xff;

            // put it in a array
            int[] rgb = new int[]{red, green, blue};
            for (int c = 0; c != rgb.length; c++) {

                // now we go from 0 to 2 (rgb) of the output and additionate
                // the r, g and blue of the channel
                outputCursor.setPosition(c, channelAxisIdex);

                // calculate the new value of the pixel
                double p = outputCursor.get().getRealDouble() + rgb[c];

                // make sure it's not too much
                p = p > 255 ? 255 : p;

                // set the new output
                outputCursor.get().setReal(p);
            }
        }
        
        System.out.println("over");

        //output.setCompositeChannelCount(3);
        return output;
    }

    public <T extends RealType<T> & NativeType<T>> Dataset convert(Dataset input) {

        ImgPlus<T> dataset = (ImgPlus<T>) input.getImgPlus();

        long[] dims = new long[input.numDimensions()];
        AxisType[] axes = new AxisType[input.numDimensions()];

        for (int i = 0; i != dims.length; i++) {
            dims[i] = input.dimension(i);
            axes[i] = input.axis(i).type();
        }

        ArrayList<Converter<T, ARGBType>> converters
                = new ArrayList<>();
        long channelNumber = input.max(dataset.dimensionIndex(Axes.CHANNEL)) + 1;
        for (int i = 0; i != channelNumber; i++) {
            converters.add(new RealLUTConverter<T>(input.getChannelMinimum(i), input.getChannelMaximum(i), input.getColorTable(i)));
        }

        Img<ARGBType> out = new ArrayImgFactory<ARGBType>().create(new long[]{dims[0], dims[1]}, new ARGBType());
        CompositeXYProjector<T> compositeXYProjector = new CompositeXYProjector<>(dataset, out, converters, 2);
        compositeXYProjector.setComposite(true);

        compositeXYProjector.map();

        uiService.show(out);
        return datasetService.create(new ImgPlus(out));
    }

    //@Override
    /*
    public void runold() {

        timer = timerService.getTimer(this.getClass());

        if (input.numDimensions() == 4) {
            final AxisType sliceAxis = AxisUtils.getSliceAxis(input);
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

        } else if (input.numDimensions() == 3) {
            long[] pos = new long[3];

            ImagePlus mergeChannels = mergeChannels(datasetToImagePlus(extractChannels(input, pos)), mode);
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
        final AxisType sliceAxis = AxisUtils.getSliceAxis(input);
        final int sliceAxisIndex = input.dimensionIndex(AxisUtils.getSliceAxis(input));
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
    }*/
}
