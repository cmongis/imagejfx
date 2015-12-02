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
package ijfx.service.object_detection;

import ij.ImagePlus;
import ij.blob.ManyBlobs;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.List;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.overlay.Overlay;
import net.imagej.overlay.PolygonOverlay;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPoint;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = Service.class)
public class DefaultObjectDetectionService extends AbstractService implements ObjectDetectionService {

    @Parameter
    DatasetService datasetService;

    @Override
    public List<Overlay> getOverlayFromDataset(Dataset dataset) {

        // wrap the dataset into an ImagePlus
        ImagePlus imp = unwrapDataset(dataset);
        return getOverlayFromImagePlus(imp);
    }

    private ImagePlus unwrapDataset(Dataset dataset) {
        RandomAccessibleInterval r = dataset.getImgPlus();
        ImagePlus wrapImage = ImageJFunctions.wrap(r, "");

        return wrapImage;
    }

    // not used for now
    private Dataset wrapDataset(ImagePlus imp) {
        Img img = ImageJFunctions.wrap(imp);
        return datasetService.create(img);
    }

    /**
     * Transforms an Polygon AWT Object into an Overlay (ImageJ2)
     *
     * @param polygon
     * @return an Overlay
     */
    private Overlay polygonToOveray(Polygon polygon) {
        PolygonOverlay polygonOverlay = new PolygonOverlay();
        for (int i = 0; i != polygon.npoints; i++) {
            polygonOverlay.getRegionOfInterest().addVertex(i, new RealPoint(polygon.xpoints[i], polygon.ypoints[i]));
        }
        return polygonOverlay;
    }

    @Override
    public List<Overlay> getOverlayFromImagePlus(ImagePlus imp) {
        
        // initializing the list containing the overlay
        List<Overlay> overlays = new ArrayList<>();

        // runs the connected component algorithm on the image
        ManyBlobs blobs = new ManyBlobs(imp);

        // for each blob extracted from the mask
        blobs.forEach(blob -> {
            // an overlay is created
            Polygon pol = blob.getOuterContour();
            overlays.add(polygonToOveray(pol));
        });

        // the list of overlay is returned
        return overlays;
    }

}
