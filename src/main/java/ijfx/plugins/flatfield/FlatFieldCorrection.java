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
package ijfx.plugins.flatfield;

import ijfx.core.assets.AssetService;
import ijfx.core.assets.FlatfieldAsset;
import ijfx.service.ui.CommandRunner;
import java.io.File;
import net.imagej.Dataset;
import net.imagej.axis.Axes;
import net.imagej.ops.transform.intervalView.DefaultIntervalView;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import org.scijava.Context;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = Command.class, menuPath = "Process > Correction > Flatfield correction")
public class FlatFieldCorrection extends ContextCommand {

    @Parameter(label = "Flatfield image")
    File flatfield;

    @Parameter(label = "Darkfield image (non requied, only for the flatfield)", required= false)
    File darkfield;
    
    @Parameter(label = "Convert original type")
    boolean keepType = true;

    @Parameter
    AssetService assetService;

    @Parameter(type = ItemIO.BOTH)
    Dataset dataset;

    @Parameter
    Context context;

    @Parameter(label = "Channel to correct",style = "Channel",description = "Channel id from 0 to n-1. (indicate -1 to correcto all channels)")
    Integer channel = 0;
    
    @Override
    public void run() {

        Dataset flatfieldData = assetService.load(new FlatfieldAsset(flatfield,darkfield));
        
        
        if (keepType == false) {
            dataset = new CommandRunner(context)
                    .set("dataset", dataset)
                    .runSync(ConvertTo32Bits.class)
                    .getOutput("dataset");
        }
        
       
        
        double value;
        double coeff;
        Cursor<RealType<?>> datasetCursor;
       
        
        if(channel == -1 || dataset.dimension(Axes.CHANNEL) <= 0 || dataset.dimensionIndex(Axes.CHANNEL) == -1) {
            datasetCursor  = dataset.cursor();
        }
        else {
            int channelAxisId = dataset.dimensionIndex(Axes.CHANNEL);
            datasetCursor = Views.hyperSlice(dataset, channelAxisId, channel).cursor();
        }

        RandomAccess<RealType<?>> flatfieldAccess = flatfieldData.randomAccess();
        datasetCursor.reset();
        long[] xy = new long[2];
        while (datasetCursor.hasNext()) {
            datasetCursor.fwd();
            xy[0] = datasetCursor.getLongPosition(0);
            xy[1] = datasetCursor.getLongPosition(1);
            value = datasetCursor.get().getRealDouble();

            flatfieldAccess.setPosition(xy);

            coeff = flatfieldAccess.get().getRealDouble();

            value = value / coeff;

            datasetCursor.get().setReal(value);

        }
    }
    
    public static <T extends RealType<T>,U extends RealType<T>> void correct(RandomAccessibleInterval<T> dataset, RandomAccess<T> flatfield,int channelAxisId,int channel) {
        
        double value;
        double coeff;
        Cursor<T> datasetCursor;
       
     
        
        if(channel == -1 || dataset.dimension(channelAxisId) <= 0 || channelAxisId == -1) {
            datasetCursor  = Views.iterable(dataset).cursor();
        }
        else {
            datasetCursor = Views.hyperSlice(dataset, channelAxisId, channel).cursor();
        }

        //RandomAccess<U> flatfield = flatfieldData.randomAccess();
        datasetCursor.reset();
        long[] xy = new long[2];
        while (datasetCursor.hasNext()) {
            datasetCursor.fwd();
            xy[0] = datasetCursor.getLongPosition(0);
            xy[1] = datasetCursor.getLongPosition(1);
            value = datasetCursor.get().getRealDouble();

            flatfield.setPosition(xy);

            coeff = flatfield.get().getRealDouble();

            value = value / coeff;

            datasetCursor.get().setReal(value);

        }
        
        
        
    }
    
    
}
