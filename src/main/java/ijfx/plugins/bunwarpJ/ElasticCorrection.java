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
package ijfx.plugins.bunwarpJ;

import bunwarpj.Transformation;
import ij.ImagePlus;
import ijfx.core.assets.AssetService;
import ijfx.core.assets.BUnwarpJTransformationAsset;
import ijfx.core.utils.DimensionUtils;
import ijfx.plugins.adapter.IJ1Service;
import ijfx.ui.main.ImageJFX;
import java.io.File;
import java.util.logging.Logger;
import net.imagej.Dataset;
import net.imagej.axis.Axes;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type =  Command.class)
public class ElasticCorrection extends ContextCommand {

    @Parameter
    IJ1Service ij1Service;

    @Parameter(label = "Landmark file")
    File landmarkFile;

    @Parameter(label = "Advanced parameters")
    bunwarpj.Param parameters = new bunwarpj.Param();

    @Parameter(type = ItemIO.BOTH)
    Dataset dataset;

    @Parameter(label = "Channel to correct")
    int channel = 0;

    @Parameter
    AssetService assetService;

    @Parameter(required = false)
    Transformation transformation;

    Logger logger = ImageJFX.getLogger();    
    
    @Override
    public void run() {
        correctChannel();
    }

    public <T extends RealType<T>> void correctChannel() {

        int channelAxisIndex = dataset.dimensionIndex(Axes.CHANNEL);
        
        assetService.clear();

        transformation = assetService
                .load(new BUnwarpJTransformationAsset()
                        .setLandmarkFile(landmarkFile)
                        .setParameters(parameters)
                        .setSourceDimension(dataset)
                        .setTargetDimension(dataset)
                );

        if(channelAxisIndex > -1 ) {
            IntervalView<T> hyperSlice = Views.hyperSlice((RandomAccessibleInterval<T>) dataset, channelAxisIndex, channel);
            correct(hyperSlice);
        }
        else {
            correct((RandomAccessibleInterval <T>)dataset);
        }
    }
    public <T extends RealType<T>> void correct(RandomAccessibleInterval<T> hyperSlice) {
        // returns all the possible plane position possibilities
        long[][] planes = DimensionUtils.allNonPlanarPossibilities(DimensionUtils.getDimension(hyperSlice));

        
        if(planes.length == 0) {
            logger.warning("This file is a normal file");
        }
        
        // for each plane
        for (long[] planePosition : planes) {

            ImagePlus plane = ij1Service.copyPlane(hyperSlice, planePosition);

            ImagePlus modified = plane.duplicate();

            bunwarpj.MiscTools.applyTransformationToSourceMT(plane, modified, transformation.getIntervals(), transformation.getDirectDeformationCoefficientsX(), transformation.getDirectDeformationCoefficientsY());

            ij1Service.copyPlaneBack(plane, hyperSlice, planePosition);

        }

    }

}
