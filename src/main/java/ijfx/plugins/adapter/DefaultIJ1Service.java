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
package ijfx.plugins.adapter;

import ij.ImagePlus;
import ijfx.core.utils.DimensionUtils;
import ijfx.service.ImagePlaneService;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.axis.Axes;
import net.imagej.axis.CalibratedAxis;
import net.imagej.axis.VariableAxis;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 *
 * @author Cyril MONGIS, 2015
 * @author Tuan anh TRINH
 */
@Plugin(type = Service.class)
public class DefaultIJ1Service extends AbstractService implements IJ1Service {

    @Parameter
    public DatasetService datasetService;

    @Parameter
    ImageDisplayService imageDisplayService;

    @Parameter
    EventService eventService;


    @Parameter
    ImagePlaneService imagePlaneService;


    @Override
    public ImagePlus getInput(Dataset dataset) {
        return unwrapDataset(dataset);
    }

    /**
     *
     * @param imp
     * @param dataset
     * @return
     */
    @Override
    public Dataset setOutput(ImagePlus imp, Dataset dataset) {
//        if (true) {
            dataset = wrapDataset(imp);
//        } 
//        else {
//            ImagePlus resultCopy = imp.duplicate();
//            if (createCopy) {
//                dataset = emptyDataset(dataset, dataset.numDimensions());
//            }
//            Dataset dataset2 = wrapDataset(resultCopy);
//            for (int i = 0; i < getNumberOfSlices(dataset); i++) {
//                dataset.setPlane(i, dataset2.getPlane(i));
//            }
//
//        }
        return dataset;
    }

    /**
     *
     * @param dataset
     * @return
     */
    @Override
    public <T extends NumericType<T>> ImagePlus  unwrapDataset(Dataset dataset) {
        RandomAccessibleInterval<T> r = (RandomAccessibleInterval<T>) dataset.<T>getImgPlus();
        ImagePlus wrapImage = ImageJFunctions.wrap((RandomAccessibleInterval<T>)dataset, "");
        return wrapImage;
    }

    @Override
    public Dataset wrapDataset(ImagePlus imp) {
        Img img = ImagePlusAdapter.wrapImgPlus(imp.duplicate());
        return datasetService.create(img);
    }

    @Override
    public void configureImagePlus(ImagePlus imp, ImageDisplay imageDisplay) {

        imp.setC(imageDisplay.getIntPosition(Axes.CHANNEL));
        imp.setZ(imageDisplay.getIntPosition(Axes.Z));
        imp.setT(imageDisplay.getIntPosition(Axes.TIME));

    }

//    private Dataset emptyDataset(Dataset input, int sizeDims) {
//        AxisType[] axisType = new AxisType[input.numDimensions()];
//        CalibratedAxis[] axeArray = new CalibratedAxis[input.numDimensions()];
//        input.axes(axeArray);
//
//        long[] dims = new long[sizeDims];
//        for (int i = 0; i < sizeDims; i++) {
//            axisType[i] = axeArray[i].type();
//            dims[i] = toIntExact(input.max(i) + 1);
//        }
//        return datasetService.create(dims, input.getName(), axisType, input.getValidBits(), input.isSigned(), false);
//    }

//    private Dataset chooseDataset(Dataset dataset) {
//        if (createCopy) {
//            return dataset.duplicateBlank();
//        }
//        return dataset;
//    }

    public int getNumberOfSlices(Dataset dataset) {

        return (int) (dataset.getImgPlus().size() / (dataset.dimension(0) * dataset.dimension(1)));

    }
    
    @Override
     public void copyColorTable(Dataset dataset, Dataset output) {
        output.initializeColorTables(dataset.getColorTableCount());
        for (int i = 0; i < dataset.getColorTableCount(); i++) {
            output.setColorTable(dataset.getColorTable(i), i);
        }
    }
    
    @Override
    public void copyAxesInto(Dataset dataset, Dataset output){
        for (int d = 0; d < dataset.numDimensions(); d++) {
            final CalibratedAxis axis = dataset.axis(d);
            final CalibratedAxis axisOutput = output.axis(d);
            axisOutput.setType(axis.type());
            axisOutput.setUnit(axis.unit());
            if (!(axisOutput instanceof VariableAxis)) {
                continue; // nothing else to do
            }
            final VariableAxis varAxis = (VariableAxis) axisOutput;

            varAxis.vars().stream().forEach((var) -> {
                varAxis.set(var, varAxis.get(var));
            });
        }
    }
//
//    public Dataset processDataset(Dataset dataset) {
//        Dataset datasetToModify = chooseDataset(dataset);
//
//        for (int i = 0; i < getNumberOfSlices(dataset); i++) {
//            Dataset datasetOnePlane = emptyDataset(dataset, 2);
//            datasetOnePlane.setPlane(0, dataset.getPlane(i));
//            ImagePlus result = processImagePlus(getInput(datasetOnePlane));
//            setOutput(result.duplicate(), datasetOnePlane);
//            datasetToModify.setPlane(i, datasetOnePlane.getPlane(0));
//        }
//        dataset = datasetToModify;
//        return dataset;
//    }
//
//    /**
//     * Wrap the whole Dataset. Use more memory
//     *
//     * @param dataset
//     * @return
//     */
//    public Dataset processDatasetWholeWrap(Dataset dataset) {
//        ImagePlus result = processImagePlus(getInput(dataset));
//        ImagePlus resultCopy = result.duplicate();
//        if (createCopy) {
//            dataset = emptyDataset(dataset, dataset.numDimensions());
//        }
//        Dataset dataset2 = wrapDataset(resultCopy);
//        for (int i = 0; i < getNumberOfSlices(dataset); i++) {
//            dataset.setPlane(i, dataset2.getPlane(i));
//        }
//        return dataset;
//    }

  

    @Override
    public <T extends RealType<T>> ImagePlus copyPlane(RandomAccessibleInterval<T> source, long[] position) {
        
        IntervalView<T> plane = imagePlaneService.plane(source, position);
        
        return ImageJFunctions.wrap(plane, "");
        
    }

   @Override
    public <R extends RealType<R>, T extends RealType<T> & NativeType<T>> void copyPlaneBack(ImagePlus imagePlus, RandomAccessibleInterval<R> target, long[] position) {
        Img<T> source = ImageJFunctions.wrapRealNative(imagePlus);
        Cursor<T> sourceCursor = source.cursor();
        RandomAccess<R> targetCursor = imagePlaneService.plane(target, position).randomAccess();
        while(sourceCursor.hasNext()) {
            
            sourceCursor.fwd();
            targetCursor.setPosition(sourceCursor);
            
            targetCursor.get().setReal(sourceCursor.get().getRealDouble());
            
        }
    }

}
