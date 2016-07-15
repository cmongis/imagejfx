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
package mongis.utils;

import java.lang.invoke.MethodHandles;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.imglib2.display.ArrayColorTable;
import net.imglib2.display.ColorTable;
import net.imglib2.display.ColorTable16;
import net.imglib2.display.ColorTable8;
import org.apache.commons.lang.ArrayUtils;

/**
 *
 * @author Tuan anh TRINH
 */
public class ColorTableUtil {

    public static int[][] getDoubleArray(ColorTable colorTable) throws ClassCastException {
        ArrayColorTable arrayColorTable = (ArrayColorTable) colorTable;
        int[][] output = null;
        if (colorTable instanceof ColorTable8) {
            ColorTable8 colorTable8 = (ColorTable8) colorTable;
            final byte[][] values = colorTable8.getValues();
            output = new int[values.length][values[0].length];
            for (int i = 0; i < output.length; i++) {
                for (int j = 0; j < output[i].length; j++) {
                    output[i][j] = values[i][j];
                }

//                IntBuffer intBuf
//                        = ByteBuffer.wrap(values[i].clone())
//                        .order(ByteOrder.BIG_ENDIAN)
//                        .asIntBuffer();
//                int[] array = new int[intBuf.remaining()];
//                intBuf.get(array);
//                byte[] r = values[i].clone();
//                int[] o = ByteBuffer.wrap(r).order(ByteOrder.BIG_ENDIAN).asIntBuffer().array();
//                result[i] = o;
            }
            return output;
        } //        else if (colorTable instanceof ColorTable16) {
        //            ColorTable16 colorTable16 = (ColorTable16) colorTable;
        //            short[][] values = colorTable16.getValues();
        //            for (short[] value : values) {
        //                ArrayUtils.reverse(value);
        //            }
        //            ColorTable16 intvertColorTable16 = new ColorTable16(values);
        //            return intvertColorTable16;
        //        } 
        else {
            //Has to be implemented for an other type
            return null;
        }
    }

    public static int[] convert(Class r, Object byteArray) {
        Object n = r.cast(byteArray);
        System.out.println("mongis.utils.ColorTableUtil.convert()");
        return null;
    }
}
