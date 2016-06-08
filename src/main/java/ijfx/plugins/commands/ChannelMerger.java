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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.display.ImageDisplayService;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.converter.RealLUTConverter;
import net.imglib2.display.ColorTable;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import org.scijava.Context;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

/**
 * The Channel Merger is a command but also an utility class that can be used with any dataset.
 * 
 * Use : 
 * 
 *  ChannelMerger<? extends RealType<?>> merger = new ChannelMerger(context);
 *  merger.setInput(myDataset);
 *  merger.run();
 *  Dataset output = merger.getOutput();
 * 
 * @author cyril
 */
@Plugin(type = Command.class, menuPath = "Image > Stacks > Merge channels")
public class ChannelMerger<T extends RealType<T>> extends ContextCommand {

     @Parameter(type=ItemIO.INPUT)
    Dataset input;

    @Parameter(type = ItemIO.OUTPUT)
    Dataset output;

    @Parameter
    private ImageDisplayService imageDisplayService;

    @Parameter
    private DatasetService datasetService;

    long[] dims;

    AxisType[] axes;

    boolean inputMonoChannel;

    

    List<RealLUTConverter<T>> converters;

    public ChannelMerger() {
    }

    
    
    
    public ChannelMerger(Context context) {
        context.inject(this);
    }

    public void setInput(Dataset input) {
        this.input = input;
    }

    public Dataset getOutput() {
        return output;
    }
    
    
    
    
    @Parameter
    private UIService uiService;

    @Override
    public void run() {
        

        inputMonoChannel = input.dimensionIndex(Axes.CHANNEL) == -1;

        initializeOutputDataset();
        initializeConverter();
        if (inputMonoChannel) {

            processMonoChannelInput();
        }
        else processMultiChannelInput();
    }

    protected void initializeOutputDataset() {

        int numDimensions = input.numDimensions();

        if (inputMonoChannel) {

            axes = new AxisType[numDimensions + 1];
            dims = new long[numDimensions + 1];
            int d = 0;
            axes[0] = Axes.X;
            axes[1] = Axes.Y;
            axes[2] = Axes.CHANNEL;
            dims[0] = input.dimension(0);
            dims[1] = input.dimension(1);
            dims[2] = 3;
            for (int i = 3; i <= numDimensions; i++) {
                axes[i] = input.axis(i - 1).type();
                dims[i] = input.dimension(i - 1);
            }
        } else {
            axes = new AxisType[numDimensions];
            dims = new long[numDimensions];

            for (int i = 0; i != numDimensions; i++) {
                axes[i] = input.axis(i).type();
                dims[i] = input.dimension(i);
            }

            // makes sure the output dataset is 3-channel dataset;
            dims[getChannelAxisIndex()] = 3;
        }

        output = datasetService.create(new UnsignedByteType(), dims, "", axes);

    }

    protected <T> void initializeConverter() {
        converters = new ArrayList<>();
        if (inputMonoChannel) {

            converters.add(getConverter(0));
        } else {
            for (int i = 0; i <= input.max(getChannelAxisIndex()); i++) {
                converters.add(getConverter(i));
            }
        }
    }

    protected RealLUTConverter<T> getConverter(int channel) {
        double min = input.getChannelMinimum(channel);
        double max = input.getChannelMaximum(channel);
        ColorTable table = input.getColorTable(channel);
        return new RealLUTConverter<>(min, max, table);
    }

    protected int getChannelAxisIndex() {
        return input.dimensionIndex(Axes.CHANNEL);
    }

    protected  void processMonoChannelInput() {
        ImgPlus<T> imgPlus = (ImgPlus<T>) input.getImgPlus();

        Cursor<T> inputCursor = (Cursor<T>) imgPlus.cursor();
        RandomAccess<? extends RealType> outputCursor = output.randomAccess();
        
        long[] inputPosition = new long[input.numDimensions()];
        long[] outputPosition = new long[output.numDimensions()];
        
        
        final ARGBType color = new ARGBType();
       
        T t;
        inputCursor.reset();
        while (inputCursor.hasNext()) {

            inputCursor.fwd();

            // copy the localization of the input cursor so we can localized the output
            inputCursor.localize(inputPosition);

            // i don't know if it's necessary
            //inputRandomAccess.setPosition(position);
            t = inputCursor.get();

           

            double d = t.getRealDouble();

            // get the color associated to the the pixel depending on which channel it comes from
            converters.get(0).convert(t, color);

            for(int i = 0;i!=inputPosition.length;i++) {
                
                // if the position is after 2, it must be incremented because
                // the output has more dimensions than the input.
                // The channel axis was inserted at the position 2
                outputPosition[i >= 2 ? i+1 : i] = inputPosition[i];
                //outputCursor.setPosition(inputPosition[i], i >= 2 ? i+1 : i);
            }
            outputCursor.setPosition(outputPosition);

            // we separate the rgb from the argb integer
            int value = color.get();

            final int red = (value >> 16) & 0xff;
            final int green = (value >> 8) & 0xff;
            final int blue = value & 0xff;

            // put it in a array
            int[] rgb = new int[]{red, green, blue};
            
          
            for (int c = 0; c != rgb.length; c++) {

                // now we go from 0 to 2 (rgb) of the output and additionate
                // the r, g and blue of the channel
                outputCursor.setPosition(c, 2);
                //if(outputCursor.getLongPosition(1) > 1000) {
               
                    
                //}
                // calculate the new value of the pixel
                double p = outputCursor.get().getRealDouble() + rgb[c];

                // make sure it's not too much
                p = p > 255 ? 255 : p;

                // set the new output
                outputCursor.get().setReal(p);
            }
        }
    }

    protected void processMultiChannelInput() {

        ImgPlus<T> imgPlus = (ImgPlus<T>) input.getImgPlus();

        Cursor<T> inputCursor = (Cursor<T>) imgPlus.cursor();
        RandomAccess<? extends RealType> outputCursor = output.randomAccess();
        long[] position = new long[imgPlus.numDimensions()];

        final ARGBType color = new ARGBType();
        int channelAxisIndex = getChannelAxisIndex();
        T t;
        inputCursor.reset();
        while (inputCursor.hasNext()) {

            inputCursor.fwd();

            // copy the localization of the input cursor so we can localized the output
            inputCursor.localize(position);

            // i don't know if it's necessary
            //inputRandomAccess.setPosition(position);
            t = inputCursor.get();

            int channel = inputCursor.getIntPosition(channelAxisIndex);

            double d = t.getRealDouble();

            // get the color associated to the the pixel depending on which channel it comes from
            converters.get(channel).convert(t, color);

            // set the output cur
            if (channelAxisIndex != -1) {
                position[channelAxisIndex] = 0;
            }
            outputCursor.setPosition(position);

            // we separate the rgb from the argb integer
            int value = color.get();

            final int red = (value >> 16) & 0xff;
            final int green = (value >> 8) & 0xff;
            final int blue = value & 0xff;

            // put it in a array
            int[] rgb = new int[]{red, green, blue};
            for (int c = 0; c != rgb.length; c++) {

                // now we go from 0 to 2 (rgb) of the output and additionate
                // the r, g and blue of the channel
                outputCursor.setPosition(c, channelAxisIndex);

                // calculate the new value of the pixel
                double p = outputCursor.get().getRealDouble() + rgb[c];

                // make sure it's not too much
                p = p > 255 ? 255 : p;

                // set the new output
                outputCursor.get().setReal(p);
            }
        }
    }
}
