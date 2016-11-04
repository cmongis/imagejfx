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

import ijfx.core.RandomAccessibleStream;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.imglib2.Cursor;
import net.imglib2.type.numeric.RealType;
import org.scijava.plugin.Plugin;
import rx.schedulers.Schedulers;

/**
 *
 * @author cyril
 */
@Plugin(type = BenchMarkPlugin.class)
public class DoubleAccessAsynchonous extends DoubleAccessSynchronous {

    @Override
    public <T extends RealType<T>> void copy() {

        Cursor<T> cursor = (Cursor<T>) dataset.cursor();
        Cursor<T> cursor2 = (Cursor<T>) dataset2.cursor();
        
        final Boolean lock = new Boolean(false);
        
        AtomicReference<Throwable> ref = new AtomicReference<>();
        RandomAccessibleStream
                .synchronous(cursor, cursor2)
                .subscribeOn(Schedulers.io())
                .subscribe(pair -> pair.getB().set(pair.getA()));
        
    }

}
