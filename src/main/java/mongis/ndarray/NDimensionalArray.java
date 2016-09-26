/*
 * /*
 *     This file is part of ImageJ FX.
 *
 *     ImageJ FX is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     ImageJ FX is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with ImageJ FX.  If not, see <http://www.gnu.org/licenses/>. 
 *
 * 	Copyright 2015,2016 Cyril MONGIS, Michael Knop
 *
 */
package mongis.ndarray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class NDimensionalArray extends ArrayList<Dimension> {

    
    public NDimensionalArray(String names, Integer... sizes) {
        super();
        for (int i = 0; i != sizes.length; i++) {
            add(new Dimension(String.valueOf(names.charAt(i)), sizes[i], this));

        }
    }
    public NDimensionalArray(String names, int... sizes) {
        super();
        for (int i = 0; i != sizes.length; i++) {
            add(new Dimension(String.valueOf(names.charAt(i)), sizes[i], this));

        }
    }

    public NDimensionalArray(long[] dims) {
        for (int i = 0; i != dims.length; i++) {
            add(new Dimension("", (int) dims[i], this));
        }
    }

    public NDimensionalArray() {
        super();
    }

    public boolean add(String dimensionName, int size) {
        return add(new Dimension(dimensionName, size, this));
    }

    @Override
    public boolean add(Dimension dimension) {

        if (size() > 0) {
            get(size() - 1).setSubDimension(dimension);
        }
        return super.add(dimension);
    }

    public class Element extends ArrayList<Dimension> {

    }

    public class DimensionValue {

        int value;
        Dimension d;
    }

    public long[] getIndexes(int flatIndex) {
            return get(0).generateAllPossibilities()[flatIndex];
        }
    
    public long[][] getPossibilities(){
        
        if(size() == 0) {
            return new long[0][];
        }
        
            return get(0).generateAllPossibilities();
        
    }

        

    

}
