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
import ijfx.core.assets.DatasetAsset;
import java.io.File;
import net.imagej.Dataset;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.type.numeric.RealType;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = Command.class, menuPath = "Process > Correction > Darkfield substraction")
public class DarkfieldSubstraction extends ContextCommand{

    @Parameter(label = "Darkfield image")
    File file;

    @Parameter(type = ItemIO.BOTH)
    Dataset dataset;

    @Parameter
    AssetService assetService;

    public void run() {
        Dataset darkfield = assetService.load(new DatasetAsset(file));
        substract(darkfield);
    }

    public <T extends RealType<T>> void substract(Dataset darkfield) {
        long[] xyPosition = new long[2];

        RandomAccess<RealType<?>> darkfieldAccess = darkfield.randomAccess();
        Cursor<T> cursor = (Cursor<T>) dataset.cursor();

        cursor.reset();
        double value;
        while (cursor.hasNext()) {
            cursor.fwd();
            xyPosition[0] = cursor.getLongPosition(0);
            xyPosition[1] = cursor.getLongPosition(1);
            darkfieldAccess.setPosition(xyPosition);
            value = cursor.get().getRealDouble() - darkfieldAccess.get().getRealDouble();
            cursor.get().setReal(value);
        }
    }

}
