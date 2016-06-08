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
package ijfx.service.sampler;

import ijfx.plugins.commands.AxisUtils;
import ijfx.service.IjfxService;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.axis.CalibratedAxis;
import net.imagej.sampler.AxisSubrange;

import net.imglib2.RandomAccess;
import net.imglib2.display.ColorTable;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 *
 * @author cyril
 */
@Plugin(type = Service.class)
public class DatasetSamplerService extends AbstractService implements IjfxService {

    @Parameter
    DatasetService datasetService;

    /**
     * Isolate a point of a dimension of a dataset. It meands for instance that
     * if you have a dataset with CHANNEL and Z, you can isolate a the Z stack
     * corresponding to a single channel just by doing :
     * sampleService.isolateDimension(myDataset,Axes.CHANNEL,2)
     *
     * @param dataset the input dataset
     * @param axes the axe type to isolate
     * @param position the position in the dimension
     * @return
     */
    public Dataset isolateDimension(Dataset dataset, AxisType axes, long position) {

        SamplingDefinition def = new SamplingDefinition(dataset);

        def.constrain(axes, new AxisSubrange(position));

        Dataset output = createOutputImage(dataset, def);

        copyData(def, dataset, output);
        return output;

    }

    /**
     * Extract any subset of an image
     *
     * Create a SamplingDefinition object from your input dataset and start
     * restraining dimension. For instance :
     * 
     * SamplingDefinition def = new
     * SamplingDefinition(myDataset); def.constrain(Axes.X, new
     * AxisSubrange(20,50)); def.constrain(Axes.Y, new AxisSubrange(20,50));
     * def.contrain(Axes.Channel, new AxisSubrange(2));
     *
     * Dataset output = duplicateData(myDataset,def);
     *
     * Here I just extracted the subvolume of the 3 third channel along the z
     * axis;
     *
     * @param dataset the input dataset
     * @param def Sampling definition defining the sub part to be extracted
     * @return
     */
    public Dataset duplicateData(Dataset dataset, SamplingDefinition def) {

        Dataset output = createOutputImage(dataset, def);
        copyData(def, dataset, output);
        return output;
    }

    private Dataset createOutputImage(Dataset origDisp, SamplingDefinition def) {
        final long[] dims = def.getOutputDims();
        final String name = origDisp.getName();
        final AxisType[] axes = def.getOutputAxes();
        final CalibratedAxis[] calibAxes = def.getOutputCalibratedAxes();
        final int bitsPerPixel = origDisp.getType().getBitsPerPixel();
        final boolean signed = origDisp.isSigned();
        final boolean floating = !origDisp.isInteger();
        final Dataset output
                = datasetService.create(dims, name, axes, bitsPerPixel, signed, floating);
        output.setAxes(calibAxes);
        long numPlanes = AxisUtils.calcNumPlanes(dims, axes);
        if (numPlanes > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "output image has more too many planes " + numPlanes + " (max = "
                    + Integer.MAX_VALUE + ")");
        }
        output.getImgPlus().initializeColorTables((int) numPlanes);
        if (origDisp.isRGBMerged()) {
            final int chanAxis = output.dimensionIndex(Axes.CHANNEL);
            if (chanAxis >= 0) {
                if (output.dimension(chanAxis) == 3) {
                    output.setRGBMerged(true);
                }
            }
        }

        return output;
    }

    private void copyData(final SamplingDefinition def, final Dataset input,
            final Dataset output) {
        final PositionIterator iter1 = new SparsePositionIterator(def);
        final PositionIterator iter2 = new DensePositionIterator(def);
        // TODO - remove evil casts
        //final Dataset input = (Dataset) def.getDisplay().getActiveView().getData();
        //final Dataset output = (Dataset) outputImage.getActiveView().getData();
        final long[] inputDims = Intervals.dimensionsAsLongArray(input);
        final long[] outputDims = Intervals.dimensionsAsLongArray(output);
        final RandomAccess<? extends RealType<?>> inputAccessor
                = input.getImgPlus().randomAccess();
        final RandomAccess<? extends RealType<?>> outputAccessor
                = output.getImgPlus().randomAccess();
        while (iter1.hasNext() && iter2.hasNext()) {

            // determine data positions within datasets
            final long[] inputPos = iter1.next();
            final long[] outputPos = iter2.next();
            inputAccessor.setPosition(inputPos);
            outputAccessor.setPosition(outputPos);

            // copy value
            final double value = inputAccessor.get().getRealDouble();
            outputAccessor.get().setReal(value);

            // TODO - notice there is a lot of inefficiency following here.
            // We are setting color tables once per pixel in image and do a lot of
            // calculation to figure out what table. This should be done once per
            // plane.
            // keep dataset color tables in sync
            final int inputPlaneNumber = AxisUtils.planeNum(inputDims, inputPos);
            final ColorTable lut = input.getColorTable(inputPlaneNumber);
            final int outputPlaneNumber = AxisUtils.planeNum(outputDims, outputPos);
            output.setColorTable(lut, outputPlaneNumber);
        }

    }
}
