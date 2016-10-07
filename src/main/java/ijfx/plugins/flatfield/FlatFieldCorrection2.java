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
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.type.numeric.RealType;
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
@Plugin(type = Command.class, menuPath = "Plugins > Test > Flatfield bis")
public class FlatFieldCorrection2 extends ContextCommand {

    @Parameter(label = "Flatfield image")
    File flatfield;

    @Parameter(label = "Convert original type")
    boolean keepType = true;

    @Parameter
    AssetService assetService;

    @Parameter(type = ItemIO.BOTH)
    Dataset dataset;

    @Parameter
    Context context;

    @Override
    public void run() {

        Dataset flatfieldData = assetService.load(new FlatfieldAsset(flatfield));

        if (keepType == false) {
            dataset = new CommandRunner(context)
                    .set("dataset", dataset)
                    .runSync(ConvertTo32Bits.class)
                    .getOutput("dataset");
        }
        double value;
        double coeff;
        Cursor<RealType<?>> datasetCursor = dataset.cursor();
        RandomAccess<RealType<?>> flatfieldAccess = flatfieldData.randomAccess();
        datasetCursor.reset();
        //long x,y;
        while (datasetCursor.hasNext()) {
            datasetCursor.fwd();
            //x = datasetCursor.getLongPosition(0);
            //y = datasetCursor.getLongPosition(1);
            value = datasetCursor.get().getRealDouble();

            flatfieldAccess.move(datasetCursor);

            coeff = flatfieldAccess.get().getRealDouble();

            value = value / coeff;

            datasetCursor.get().setReal(value);

        }
    }
}
