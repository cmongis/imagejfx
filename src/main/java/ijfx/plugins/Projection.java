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

import ijfx.plugins.stackconverter.DimensionConverter;
import static java.lang.Math.toIntExact;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mongis.ndarray.NDimensionalArray;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.axis.CalibratedAxis;
import net.imagej.ops.Op;
import net.imagej.ops.OpService;
import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Math.Add;
import net.imagej.ops.math.RealMath;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Attr;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Tuan anh TRINH
 */
@Plugin(type = Command.class, menuPath = "Plugins>Projection", attrs = {
    @Attr(name = "no-legacy")})
public class Projection implements Command {

    public static final String MAX = "Max";
    public static final String MIN = "Max";
    public static final String AVERAGE = "Average";
    public static final String SD = "Standard deviation";
    public static final String SUM = "Sum";

    @Parameter
    OpService opService;

    @Parameter
    DatasetService datasetService;

    @Parameter(type = ItemIO.INPUT)
    Dataset dataset;

    @Parameter(type = ItemIO.OUTPUT)
    Dataset datasetOutput;

    @Parameter(label = "Type of projection", choices = {MAX, MIN, AVERAGE, SUM, SD})
    String projectionType;

    @Parameter
    ProjectionMethod projectionMethod;

    @Parameter
    AxisType axisTypeParameter;

    @Override
    public void run() {
        ProjectionMethod projectionMethod = new MinProjection();
        AxisType[] axisType = new AxisType[dataset.numDimensions()];
        CalibratedAxis[] axeArray = new CalibratedAxis[dataset.numDimensions()];
        dataset.axes(axeArray);
        long[] dims = new long[axeArray.length];
        for (int i = 0; i < dims.length; i++) {
            axisType[i] = axeArray[i].type();
            dims[i] = toIntExact(dataset.max(i) + 1);
            if (i == 3) {
                dims[i] = 1;
            }
        }
        datasetOutput = datasetService.create(dims, dataset.getName(), axisType, dataset.getValidBits(), dataset.isSigned(), false);

        copyDataset(dataset, datasetOutput, projectionMethod);
    }

    public < T extends RealType< T>> void copyDataset(Dataset input, Dataset output, ProjectionMethod projectionMethod) {
        Img<?> inputImg = input.getImgPlus().getImg();
        long[] dims = new long[5];
        input.dimensions(dims);

        RandomAccess<T> randomAccess = (RandomAccess<T>) inputImg.randomAccess();
        RandomAccess<T> randomAccessOutput = (RandomAccess<T>) output.randomAccess();
        long[] position = new long[input.numDimensions()];
        long[] positionOutput = new long[output.numDimensions()];
        List<T> list = new ArrayList<>();
        NDimensionalArray nDimensionalArray = new NDimensionalArray(Arrays.copyOfRange(dims, 2, dims.length));
        long[][] possibilities = nDimensionalArray.getPossibilities();

        for (int x = 0; x < dims[input.dimensionIndex(Axes.X)]; x++) {
            for (int y = 0; y < dims[input.dimensionIndex(Axes.Y)]; y++) {
                for (long[] possibilitie : possibilities) {
                    for (int m = 0; m < dims[input.dimensionIndex(axisTypeParameter)]; m++) {
                        //                        for (long e = 0; e < dims[3]; e++) {
                        position[0] = x;
                        position[1] = y;
                        System.arraycopy(possibilitie, 0, position, 2, possibilities.length);
                        position[input.dimensionIndex(axisTypeParameter)] = m;
//                            position[3] = e;
//                            position[4] = c;
//                            T tmp;
//                            try {
//                                tmp = (T) randomAccess.get().getClass().newInstance();
//                            } catch (InstantiationException ex) {
//                                Logger.getLogger(Projection.class.getName()).log(Level.SEVERE, null, ex);
//                            } catch (IllegalAccessException ex) {
//                                Logger.getLogger(Projection.class.getName()).log(Level.SEVERE, null, ex);
//                            }
                        randomAccess.setPosition(position);

                        //tmp = randomAccess.get().copy();
                        list.add(randomAccess.get().copy());
                        if (m == 0) {
                            randomAccessOutput.setPosition(position);

                        } else if (m == dims[input.dimensionIndex(axisTypeParameter)] - 1) {
                            randomAccessOutput.get().set(projectionMethod.process(list));

                            list.removeAll(list);
                        }
//                        }
                    }
                }
            }
        }

    }

}
