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
import net.imglib2.type.numeric.RealType;


public class MinProjection implements ProjectionMethod {

    public static int cpt =0;
    private final String name = "Min";
    @Override
    
    public <T extends RealType<T>> T process(List<T> list) {
        T min = null;
        for (T t : list)
        {
//            System.out.println(t.toString());
//            try {
//            System.out.println(min.toString());
//                
//            } catch (Exception e) {
//            }
            if (min == null) {
                min = t.copy();
            }
            else if (t.compareTo(min) == 0)
            {
               cpt++; 
            }
            else if (t.compareTo(min) < 0)
            {
                min = t.copy();
            }
        }
        return min;
    }

    @Override
    public String getName() {
        return this.name;
    }
    
}
