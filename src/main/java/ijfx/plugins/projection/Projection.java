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

import static java.lang.Math.toIntExact;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mongis.ndarray.NDimensionalArray;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.axis.CalibratedAxis;
import net.imagej.display.ImageDisplayService;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import org.scijava.ItemIO;
import org.scijava.app.StatusService;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.plugin.Attr;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Tuan anh TRINH
 */
@Plugin(type = Command.class, menuPath = "Image > Stacks > Project...", attrs = {
    @Attr(name = "no-legacy")}, initializer = "init")
public class Projection extends ContextCommand {

    @Parameter
    DatasetService datasetService;

    @Parameter(type = ItemIO.INPUT)
    Dataset dataset;

    @Parameter(type = ItemIO.OUTPUT)
    Dataset datasetOutput;

    @Parameter(label = "Projection method")
    ProjectionMethod projectionMethod = new MeanProjection();

    @Parameter(label = "Axe to project")
    AxisType axisType = Axes.Z;

    @Parameter
    ImageDisplayService imageDisplayService;
    
    @Parameter
    StatusService statusService;
    
    @Override
    public void run() {
        AxisType[] axes = new AxisType[dataset.numDimensions()-1];
        CalibratedAxis[] axeArray = new CalibratedAxis[dataset.numDimensions()-1];
        dataset.axes(axeArray);

        if (axisType == null) {
            axisType = dataset.axis(2).type();
        }
        
        if(axisType.isXY()) {
            cancel("Only the axes TIME and Z are supported ");
            return;
        }

        long[] dims = new long[axeArray.length];
        for (int i = 0; i < dataset.numDimensions(); i++) {
            if (i == dataset.dimensionIndex(axisType)) {
                continue;
            }
            axes[i] = axeArray[i].type();
            dims[i] = toIntExact(dataset.max(i) + 1);
        }
        datasetOutput = datasetService.create(dims, dataset.getName(), axes, dataset.getValidBits(), dataset.isSigned(), !dataset.isInteger());

        project(dataset, datasetOutput, projectionMethod);
    }

    public < T extends RealType<T>> void project(Dataset input, Dataset output, ProjectionMethod projectionMethod) {
        int indexToModify = input.dimensionIndex(axisType);
        Img<?> inputImg = input.getImgPlus().getImg();
        long[] dims = new long[input.numDimensions()];
        input.dimensions(dims);

        RandomAccess<T> randomAccess = (RandomAccess<T>) inputImg.randomAccess();
        RandomAccess<T> randomAccessOutput = (RandomAccess<T>) output.randomAccess();
        long[] position = new long[input.numDimensions()];
        List<T> listToProcess = new ArrayList<>();

        //In order to avoid creation of too big array
        long[] dimensionToGenerate = Arrays.copyOfRange(dims, 2, dims.length);

        //Set the dimension to 1 in order to reduce the size of the array
        //Cannot set to 0
        dimensionToGenerate[indexToModify - 2] = 1;
        NDimensionalArray nDimensionalArray = new NDimensionalArray(dimensionToGenerate);
        long[][] possibilities = nDimensionalArray.getPossibilities();
        final long width = dims[input.dimensionIndex(Axes.X)];
        final long height = dims[input.dimensionIndex(Axes.Y)];
        for (int x = 0; x < width; x++) {
            if(x % 50 == 0) statusService.showStatus(x, (int)width, "Projecting...");
            for (int y = 0; y < height; y++) {
                for (long[] possibilitie : possibilities) {

                    //Go throug th dimension to project
                    for (int m = 0; m < dims[indexToModify]; m++) {
                        position[0] = x;
                        position[1] = y;
                        System.arraycopy(possibilitie, 0, position, 2, possibilitie.length);
                        position[indexToModify] = m;

                        randomAccess.setPosition(position);

                        listToProcess.add(randomAccess.get().copy());
                        if (m == 0) {
                            randomAccessOutput.setPosition(position);
                        } else if (m == dims[indexToModify] - 1) {
                            projectionMethod.process(listToProcess, randomAccessOutput);
                            listToProcess.removeAll(listToProcess);
                        }
                    }
                }
            }
        }
        
        statusService.showStatus(1, 1, "Projection finished");
    }

    public void init() {
        if (axisType == null && dataset != null) {
            if (dataset.numDimensions() > 2) {
                axisType = dataset.axis(2).type();
            }
        }
        else if( axisType == null) {
            
            axisType = imageDisplayService.getActiveDataset().axis(2).type();
            
        }
    }
}
