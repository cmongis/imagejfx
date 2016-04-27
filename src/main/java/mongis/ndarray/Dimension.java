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

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class Dimension {
    String name;
    int size;
    Dimension subDimension;
    private final NDimensionalArray outer;

    public Dimension(String name, int size, final NDimensionalArray outer) {
        this.outer = outer;
        this.name = name;
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSize() {
        return size;
    }

    public int getTotalSize() {
        if (getSubDimension() == null) {
            return getSize();
        } else {
            return getSize() * getSubDimension().getTotalSize();
        }
    }

    public void setSize(int size) {
        this.size = size;
    }
    
   

    public Dimension getSubDimension() {
        return subDimension;
    }

    public void setSubDimension(Dimension subDimension) {
        this.subDimension = subDimension;
    }

    public long[][] generateAllPossibilities() {
        long[][] possibilities = new long[getTotalSize()][];
        // if there are no subdimension, then the possibilities are just single value arrays
        if (subDimension == null) {
            for (int i = 0; i != possibilities.length; i++) {
                possibilities[i] = new long[]{i};
            }
            return possibilities;
        }
        else {
        int i = 0;
        long[][] subPossibilities = subDimension.generateAllPossibilities();
        for (int j = 0; j != getSize(); j++) {
            for (int k = 0; k != subPossibilities.length; k++) {
                possibilities[i] = incorparatePossibilities(j, subPossibilities[k]);
                i++;
            }
        }
        return possibilities;
        }
    }

    // incorporate a number in front of a set of number e.g (3,[0,3,5]) -> [3,0,3,5]
    private long[] incorparatePossibilities(int index, long[] array) {
        long[] result = new long[array.length + 1];
        result[0] = index;
        for (int i = 0; i != array.length; i++) {
            result[i + 1] = array[i];
        }
        return result;
    }

    public String displayPossibily(long[] array) {
        StringBuffer buffer = new StringBuffer(array.length * 4);
        if (array.length == 0) {
            return "[empty]";
        }
        buffer.append("[" + array[0]);
        for (int i = 1; i != array.length; i++) {
            buffer.append("," + array[i]);
        }
        buffer.append("]");
        return buffer.toString();
    }
    
}
