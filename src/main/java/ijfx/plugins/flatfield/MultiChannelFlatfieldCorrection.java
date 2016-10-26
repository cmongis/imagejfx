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
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = Command.class, menuPath = "Process > Correction > Flatfield correction (multi-channel)")
public class MultiChannelFlatfieldCorrection extends ContextCommand {

    @Parameter
    Dataset dataset;

    @Parameter(label = "Flatfield image", required = true)
    File flatfield;

    @Parameter(label = "Darkfield image (for flatfield only)", required = false,persist = false)
    File darkfield = null;

    @Parameter
    AssetService assetService;

    @Parameter
    boolean keepType = true;

    @Parameter
    Context context;

    @Override
    public void run() {

        if (keepType == false) {
            dataset = new CommandRunner(context)
                    .set("dataset", dataset)
                    .runSync(ConvertTo32Bits.class)
                    .getOutput("dataset");
        }

        Dataset flatfieldDataset = assetService.load(new FlatfieldAsset(flatfield, darkfield).setMultiChannel(true));

        long datasetChannelNumber = dataset.dimension(Axes.CHANNEL);
        long flatfieldChannelNumber = flatfieldDataset.dimension(Axes.CHANNEL);

        if (datasetChannelNumber == -1 || flatfieldChannelNumber == -1) {
            cancel("No Channel Axis detected ! The flatfield image and the target image must both contain a channel axis.");
            return;
        }

        if (datasetChannelNumber != flatfieldChannelNumber) {
            cancel("Flatfield and target must have the same number of channel !");
            return;
        }

        int datasetChannelAxis = dataset.dimensionIndex(Axes.CHANNEL);
        int flatfieldChannelAxis = flatfieldDataset.dimensionIndex(Axes.CHANNEL);

        for (long channel = 0; channel != datasetChannelNumber; channel++) {

            IntervalView target = Views.hyperSlice(dataset, datasetChannelAxis, channel);
            IntervalView flatfieldChannel = Views.hyperSlice(flatfieldDataset, flatfieldChannelAxis, channel);
            FlatFieldCorrection.correct(target, flatfieldChannel);
        }
    }   
}
