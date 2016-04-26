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

import net.imagej.Dataset;


public class OneToMultiDimensions implements DimensionConverter {

    @Override
    public void convert(long[] positionOutput, long[] position, Dataset output, Dataset input) {
        int rest;
        int coorOrigin = (int) position[2];
        for (int i = 2; i < positionOutput.length; i++) {
            positionOutput[i] = coorOrigin / (output.max(i) + 1);
            rest = (int) (position[2] % (output.max(i)+1));
            if (positionOutput[i] == 0.0) {
                positionOutput[i] = rest;
            } else if (rest != 0) {
                coorOrigin = rest;
            }
        }
    }
    
}
