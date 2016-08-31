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
@Plugin(type = Command.class, menuPath = "Image > Adjust > Simple Threshold...")
public class SimpleThreshold extends ContextCommand {

    @Parameter(type = ItemIO.BOTH)
    Dataset dataset;

    @Parameter(label = "Threshold at")
    double value;

    @Parameter(label = "Cut values above ?")
    boolean upperCut = false;

    @Override
    public void run() {
        if (isCanceled()) {
            return;
        }
        threshold(dataset);
    }

    public <T extends RealType<T>> void threshold(Dataset dataset) {
        RandomAccess<RealType<?>> randomAccess = dataset.randomAccess();

        Cursor<T> cursor = (Cursor<T>) dataset.<T>getImgPlus().cursor();
        cursor.reset();
        while (cursor.hasNext()) {
            cursor.fwd();
            if (!upperCut && cursor.get().getRealDouble() < value) {
                cursor.get().setReal(0);
            } else if (upperCut && cursor.get().getRealDouble() > value) {
                cursor.get().setReal(0);
            }
        }
    }

}
