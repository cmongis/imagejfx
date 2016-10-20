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
package ijfx.benchmark;

import net.imagej.Dataset;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.type.numeric.RealType;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = BenchMarkPlugin.class)
public class DoubleAccess extends DatasetRelatedBenchmark{
    
    Dataset dataset2;
    
    @Override
    public void init() {
        super.init();
        dataset2 = dataset.duplicateBlank();
    }

    @Override
    public void repeat() {
        Cursor<RealType<?>> cursor = dataset.cursor();
        Cursor<RealType<?>> cursor2 = dataset2.cursor().copyCursor();
        cursor.reset();
        
        RandomAccess<RealType<?>> randomAccess = dataset2.randomAccess();
        double[] position = new double[cursor.numDimensions()];
        while(cursor.hasNext()) {
            cursor.fwd();
            //randomAccess.setPosition(cursor);
            //randomAccess.get().setReal(cursor.get().getRealDouble());
            cursor.localize(position);
            
            cursor2.get().setReal(cursor.get().getRealDouble());
            //randomAccess.setPosition(cursor.getIntPosition(0),1);
            //randomAccess.setPosition(cursor.getIntPosition(0), 0);
        }
        
        
        
    }
    
    
}
