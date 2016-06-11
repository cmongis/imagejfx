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
package ijfx.plugins.projection;

import java.util.List;
import net.imglib2.Sampler;
import net.imglib2.type.numeric.RealType;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Tuan anh TRINH
 */
@Plugin(type = ProjectionMethod.class)
public class SumProjection implements ProjectionMethod {

    private final String name = "Sum";

    @Override

    public <T extends RealType<T>> void process(List<T> list, Sampler<T> sampler) {
        T result = list.get(0).createVariable();
        list.stream()
                .forEach((t) -> result.add(t));

        sampler.get().add(result);
    }

    @Override
    public String toString() {
        return this.name;
    }
}
