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
package ijfx.plugins.stackconverter;


import net.imagej.Dataset;

public class MultiToOneDimension implements DimensionConverter {

    @Override
    public void convert(long[] positionOutput, long[] position, Dataset output, Dataset input) {
        positionOutput[2] = 0;
        long b = (input.max(2)+1);
        long c = ((input.max(2)+1)*(input.max(3)+1));
        positionOutput[2] = position[2]+position[3]*b+position[4]*c;

    }

}
