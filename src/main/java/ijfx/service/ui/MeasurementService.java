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
package ijfx.service.ui;

import ij.ImagePlus;
import ij.blob.Blob;
import ij.blob.ManyBlobs;
import ij.process.ByteProcessor;
import ijfx.core.metadata.MetaData;
import ijfx.core.metadata.MetaDataSet;
import ijfx.core.metadata.MetaDataSetType;
import ijfx.plugins.commands.BinaryToOverlay;
import ijfx.service.IjfxService;
import ijfx.service.ImagePlaneService;
import ijfx.service.batch.DefaultSegmentedObject;
import ijfx.service.batch.SegmentedObject;
import ijfx.service.overlay.OverlaySelectionService;
import ijfx.service.overlay.OverlayShapeStatistics;
import ijfx.service.overlay.OverlayStatService;
import ijfx.ui.datadisplay.metadataset.MetaDataSetDisplayService;
import ijfx.ui.datadisplay.object.DisplayedSegmentedObject;
import ijfx.ui.datadisplay.object.SegmentedObjectDisplay;
import ijfx.ui.main.ImageJFX;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import net.imagej.Dataset;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.OverlayService;
import net.imagej.overlay.BinaryMaskOverlay;
import net.imagej.overlay.Overlay;
import net.imagej.overlay.PolygonOverlay;
import net.imagej.overlay.ThresholdOverlay;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.roi.BinaryMaskRegionOfInterest;
import net.imglib2.roi.PolygonRegionOfInterest;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import org.scijava.display.Display;
import org.scijava.display.DisplayService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import org.scijava.ui.UIService;

/**
 *
 * @author Cyril MONGIS, 2016
 */
@Plugin(type = Service.class)
public class MeasurementService extends AbstractService implements IjfxService {

    @Parameter
    OverlaySelectionService overlaySelectionSrv;

    @Parameter
    ImageDisplayService imageDisplaySrv;

    @Parameter
    DisplayService displayService;

    @Parameter
    OverlayStatService overlayStatsSrv;

    @Parameter
    OverlayService overlayService;

    @Parameter
    ImagePlaneService imagePlaneService;

    @Parameter
    UIService uiService;

    @Parameter
    MetaDataSetDisplayService metaDataDisplaySrv;

    Logger logger = ImageJFX.getLogger();

    private static final String COUNT_DISPLAY_NAME = "Object count";

    public void measureSelectedOverlay() {
        ImageDisplay display = imageDisplaySrv.getActiveImageDisplay();

        logger.info("Measuring selected overlay");
        overlaySelectionSrv.getSelectedOverlays(display)
                .stream()
                .map(o -> measure(display, o))
                .forEach(this::display);

    }

    public void measureAllOverlay() {
        measureAllOverlay(imageDisplaySrv.getActiveImageDisplay());
    }

    /**
     * Take a threshold overlay and return single polygon overlays
     *
     * @param thresholdOverlay
     * @return Polygon overlays extracted with connected component algorithm
     */
    public List<Overlay> extractOverlays(ThresholdOverlay thresholdOverlay, RandomAccessibleInterval interval) {
        long width = interval.dimension(0);
        long height = interval.dimension(1);

        Img<BitType> binaryMark = createBinaryMask(thresholdOverlay, width, height);

        return Arrays.asList(BinaryToOverlay.transform(getContext(), binaryMark, true));
    }

    public List<Overlay> extractOverlays(BinaryMaskOverlay maskOverlay) {

        BinaryMaskRegionOfInterest roi = (BinaryMaskRegionOfInterest) maskOverlay.getRegionOfInterest();

        return Arrays.asList(BinaryToOverlay.transform(getContext(), roi.getImg(), true));

    }

    private Img<BitType> createBinaryMask(ThresholdOverlay overlay, long width, long height) {

        ImgFactory<BitType> factory = new ArrayImgFactory<>();
        long[] dim = new long[2];
        dim[0] = width;
        dim[1] = height;
        Img<BitType> img = factory.create(dim, new BitType(false));

        Cursor<BitType> cursor = img.cursor();
        cursor.reset();
        while (cursor.hasNext()) {
            cursor.fwd();
            cursor.localize(dim);
            cursor.get().set(overlay.classify(dim) == 0);
        }

        return img;
    }

    public <T extends RealType<T>> void measureOverlays(List<? extends Overlay> overlaysList, RandomAccessibleInterval<T> rai, Predicate<SegmentedObject> filter) {

    }

    public void measureOverlays(ImageDisplay imageDisplay, List<? extends Overlay> overlayList, Predicate<SegmentedObject> filter) {

        // the filter always return true if null
        if (filter == null) {
            filter = o -> true;
        }

        List<SegmentedObject> objectList = overlayList
                .stream()
                .map(o -> measure(imageDisplay, o))
                .filter(o -> o != null)
                .filter(filter)
                .collect(Collectors.toList());

        if (objectList.size() > 0) {

            Display display = displayService.createDisplay("Measures from " + imageDisplay.getName(), objectList.get(0));
            display.addAll(objectList);
            display.update();

        } else {
            uiService.showDialog("There is no object to measure.");
        }

    }

    public long countObjects(List<Overlay> overlayList, Predicate<OverlayShapeStatistics> filter) {

        long count = overlayList
                .parallelStream()
                .map(overlayStatsSrv::getShapeStatistics)
                .filter(o -> o != null)
                .filter(filter)
                .count();

        return count;
    }

    public long countObjects(List<Overlay> overlayList, Predicate<OverlayShapeStatistics> filter, MetaDataSet set, boolean show) {

        long count = countObjects(overlayList, filter);

        set.putGeneric(MetaData.COUNT, count);
        set.setType(MetaDataSetType.OBJECT);
        if (show) {
            metaDataDisplaySrv.addMetaDataSetToDisplay(set, COUNT_DISPLAY_NAME);
        }

        return count;

    }

    public long countObjects(RandomAccessibleInterval interval, Predicate<OverlayShapeStatistics> filter, MetaDataSet set, boolean show) {
        return countObjects(transform(interval, true), filter, set, show);

    }

    public void measureAllOverlay(ImageDisplay imageDisplay) {
        measureOverlays(imageDisplay, overlayService.getOverlays(imageDisplay), null);
    }

    public SegmentedObject measure(ImageDisplay display, Overlay overlay) {
        try {
            SegmentedObject object = new DefaultSegmentedObject(overlay, overlayStatsSrv.getOverlayStatistics(display, overlay));
            return new DisplayedSegmentedObject(display, object);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error when creating SegmentedObject", e);
            return null;
        }
    }

    public <T extends RealType<T>> List<? extends SegmentedObject> measureOverlays(List<Overlay> overlays, Dataset dataset, long[] position) {
        IntervalView<T> planeView = imagePlaneService.planeView(dataset, position);
        return measureOverlays(overlays, planeView);
    }

    public <T extends RealType<T>> List<? extends SegmentedObject> measureOverlays(List<Overlay> overlays, RandomAccessibleInterval<T> rai) {
        return overlays
                .stream()
                .map(o -> measure(rai, o))
                .collect(Collectors.toList());
    }

    public <T extends RealType<T>> SegmentedObject measure(RandomAccessibleInterval<T> rai, Overlay overlay) {
        try {
            return new DefaultSegmentedObject(overlay, overlayStatsSrv.getOverlayStatistics(rai, overlay));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error when creating SegmentedObject", e);
        }
        return null;
    }

    public synchronized Display getCurrentDisplay(SegmentedObject object) {
        return displayService.getDisplays()
                .stream()
                .filter(display -> display instanceof SegmentedObjectDisplay)
                .findFirst()
                .orElseGet(() -> {
                    logger.info("Creating display");
                    return displayService.createDisplay("Measures", object);
                });
    }

    public void display(SegmentedObject object) {
        logger.info("Dislaying segmented object " + object.getOverlay().getName());
        getCurrentDisplay(object).display(object);

    }

    public Optional<ThresholdOverlay> getThresholdOverlay(ImageDisplay imageDisplay) {
        return overlayService
                .getOverlays(imageDisplay)
                .stream()
                .filter(o -> o instanceof ThresholdOverlay)
                .map(o -> (ThresholdOverlay) o)
                .findFirst();
    }

    public <T extends RealType<T>> List<Overlay> transform(RandomAccessibleInterval<T> dataset, boolean recognizeWhiteObject) {
        // converting 

        int width = (int) dataset.max(0);
        int height = (int) dataset.max(1);

        RandomAccess<T> randomAccess = dataset.randomAccess();

        ByteProcessor byteProcessor = new ByteProcessor(width, height);

        long[] position = new long[dataset.numDimensions()];

        for (int x = 0; x != width; x++) {
            for (int y = 0; y != height; y++) {

                position[0] = x;
                position[1] = y;

                randomAccess.setPosition(position);
                double p = randomAccess.get().getRealDouble();
                byteProcessor.set(x, y, (p > 0.0 ? 255 : 0));

            }
        }

        ImagePlus imp = new ImagePlus("Temporary mask", byteProcessor);

        ManyBlobs blobs = new ManyBlobs(imp);

        // inverting if necessary because the
        // CC algorithm recognise black objects
        if (recognizeWhiteObject) {
            imp.getProcessor().invert();
        }
        //launching the search
        blobs.findConnectedComponents();

        // creating a list
        List<Overlay> listOverlays = new ArrayList<>(blobs.size());

        int count = 1;

        // converting each Blob into a PolygonOverlay
        for (Blob b : blobs) {

            PolygonOverlay po = new PolygonOverlay(getContext());
            po.setName(String.format("%d", count++));

            // we modifiy the PolygonOverlay by modifying it region of interest
            PolygonRegionOfInterest roi = po.getRegionOfInterest();

            Polygon polygon = b.getOuterContour();

            if (polygon.getBounds().getWidth() <= 2 && polygon.getBounds().getHeight() <= 2) {

                continue;
            }

            int pointCount = polygon.npoints;
            //System.out.printf("Point count = %d\n",pointCount);
            //System.out.println(polygon);
            for (int i = 0; i != pointCount; i++) {
                RealLocalizable localizable = new RealPoint(polygon.xpoints[i], polygon.ypoints[i]);

                //System.out.println(String.format("%.0f,%.0f",localizable.getDoublePosition(0),localizable.getDoublePosition(1)));
                roi.addVertex(i, localizable);

            }
            po.setLineWidth(2.0);
            listOverlays.add(po);

        }

        return listOverlays;
    }

}
