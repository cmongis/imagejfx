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

import java.util.Iterator;
import net.imagej.Dataset;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.labeling.Watershed;
import net.imglib2.labeling.Labeling;
import net.imglib2.type.numeric.RealType;
import org.scijava.ItemIO;
import org.scijava.command.ContextCommand;
import org.scijava.plugin.Parameter;

/**
 *
 * @author Cyril MONGIS, 2016
 */

public class WatershedCommand<T extends RealType<T>> extends ContextCommand

{

    @Parameter(type = ItemIO.BOTH)
    Dataset input;
    
    @Override
    public void run() {
        watershed((RandomAccessibleInterval<T>) input);
    }
    
    public void watershed(RandomAccessibleInterval<T> img) {
        
        Watershed watershed = new Watershed();
        
        watershed.setIntensityImage(img);
       
        watershed.process();
        
        Labeling result = watershed.getResult();
        Iterator<T> iterator = result.iterator();
        T t;
        while(iterator.hasNext()) {
            t = iterator.next();
            System.out.println(t);
        }
        
    }
    
}
