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
package ijfx.plugins;

import ij.ImagePlus;
import ij.ImageStack;
import net.imagej.Dataset;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;

/**
 *
 * @author tuananh
 */
public  class HyperStackConverter {

    public static < T extends RealType< T>> void copyDataset(Dataset input, Dataset output, DimensionConverter dimensionConverter) {
        Img<?> inputImg = input.getImgPlus().getImg();
        Cursor<T> cursorInput = (Cursor<T>) inputImg.localizingCursor();
        RandomAccess<T> randomAccessOutput = (RandomAccess<T>) output.randomAccess();
        long[] position = new long[input.numDimensions()];
        long[] positionOutput = new long[output.numDimensions()];

        while (cursorInput.hasNext()) {
            cursorInput.fwd();
            cursorInput.localize(position);
            System.arraycopy(position, 0, positionOutput, 0, positionOutput.length);
            dimensionConverter.convert(positionOutput, position, output, input);
            randomAccessOutput.setPosition(positionOutput);
            randomAccessOutput.get().set(cursorInput.get());

        }
    }
}
