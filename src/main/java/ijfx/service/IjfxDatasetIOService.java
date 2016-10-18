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
package ijfx.service;

import ijfx.ui.main.ImageJFX;
import io.scif.ImageMetadata;
import io.scif.config.SCIFIOConfig;
import io.scif.img.ImgIOException;
import io.scif.img.ImgOpener;
import io.scif.img.SCIFIOImgPlus;
import io.scif.services.DatasetIOService;
import io.scif.services.DefaultDatasetIOService;
import java.io.IOException;
import java.util.List;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.CalibratedAxis;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import org.scijava.Priority;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.Service;

/**
 *
 * @author cyril
 */
@Plugin(type = Service.class, priority = Priority.HIGH_PRIORITY)
public class IjfxDatasetIOService extends DefaultDatasetIOService implements IjfxService, DatasetIOService {

    @Parameter
    DatasetService datasetService;

    @Override
    public Dataset open(String source, SCIFIOConfig config) throws IOException {

        ImgOpener imageOpener = new ImgOpener(getContext());
        Dataset dataset;
        try {
            config.imgOpenerSetOpenAllImages(true);
            config.imgOpenerSetComputeMinMax(false);
            List<SCIFIOImgPlus<?>> openImgs = imageOpener.openImgs(source, config);

            if (openImgs.size() <= 1) {
                final SCIFIOImgPlus<?> imgPlus
                        = imageOpener.openImgs(source, config).get(0);
                //@SuppressWarnings({"rawtypes", "unchecked"})
                dataset = datasetService.create((ImgPlus) imgPlus);
            } else {
                dataset = concatenate(openImgs.toArray(new SCIFIOImgPlus[openImgs.size()]));
            }
            final ImageMetadata imageMeta = openImgs.get(0).getImageMetadata();
            updateDataset(dataset, imageMeta);
            return dataset;

        } catch (final ImgIOException exc) {
            throw new IOException(exc);
        }

    }

    private <T extends NativeType<T> & RealType<T>> Dataset concatenate(SCIFIOImgPlus<T>... openImgs) {
        RandomAccessibleInterval<T> stack = Views.stack(openImgs);
        Dataset dataset = datasetService.create(stack);
        dataset.setName(openImgs[0].getName());
        dataset.setSource(openImgs[0].getSource());       
        for(int d = 0;d!=openImgs[0].numDimensions();d++) {
            dataset.setAxis(openImgs[0].axis(d), d);
            try {
            dataset.setColorTable(openImgs[0].getColorTable(d), d);
            }
            catch(Exception e) {
                
            }
            
        }
        
        dataset.axis(dataset.numDimensions() - 1).setType(ImageJFX.SERIES);
        return dataset;

    }

    private void updateDataset(final Dataset dataset,
            final ImageMetadata imageMeta) {
        // If the original image had some level of merged channels, we should set
        // RGBmerged to true for the sake of backwards compatibility.
        // See https://github.com/imagej/imagej-legacy/issues/104

        // Look for Axes.CHANNEL in the planar axis list. If found, set RGBMerged to
        // true.
        boolean rgbMerged = false;

        for (final CalibratedAxis axis : imageMeta.getAxesPlanar()) {
            if (axis.type().equals(Axes.CHANNEL)) {
                rgbMerged = true;
            }
        }

        dataset.setRGBMerged(rgbMerged);
        
        
        
    }

}
