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

import java.util.List;
import net.imglib2.Sampler;
import net.imglib2.type.numeric.RealType;
import org.scijava.plugin.Plugin;

@Plugin(type = ProjectionMethod.class, name = "Min", label = "Minimum")
public class MinProjection implements ProjectionMethod {

    private final String name = "Min";

    @Override
    public <T extends RealType<T>> void process(List<T> list, Sampler<T> sampler) {

        T min = null;

        for (T t : list) {
            if (min == null) {

                min = t.copy();
            } else if (t.compareTo(min) < 0) {
                min = t.copy();
            }
        }
        sampler.get().set(min);

    }

    @Override
    public String toString() {
        return this.name;
    }

}
