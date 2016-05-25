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
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Tuan anh TRINH
 */
@Plugin(type = ProjectionMethod.class)
public class StandardDeviationProjection implements ProjectionMethod {

    private final String name = "Standard Deviation";

    @Override
    public <T extends RealType<T>> void process(List<T> list, Sampler<T> sampler) {
        DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics();
        list.stream()
                .forEach((t) -> descriptiveStatistics.addValue(t.getRealDouble()));
        
        //Set result
        sampler.get().setReal(descriptiveStatistics.getStandardDeviation());
    }
        @Override
    public String toString()
    {
        return this.name;
    }

}
