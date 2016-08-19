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
package ijfx.service.overlay;

import ijfx.ui.main.ImageJFX;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import javafx.util.Callback;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.ImageJService;
import net.imagej.display.ImageDisplay;
import net.imagej.display.OverlayService;
import net.imagej.measure.StatisticsService;

import net.imagej.ops.OpService;
import net.imagej.overlay.LineOverlay;
import net.imagej.overlay.Overlay;
import net.imagej.overlay.PolygonOverlay;
import net.imagej.overlay.RectangleOverlay;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.ops.pointset.HyperVolumePointSet;
import net.imglib2.ops.pointset.PointSet;
import net.imglib2.ops.pointset.PointSetIterator;
import net.imglib2.ops.pointset.RoiPointSet;
import net.imglib2.roi.PolygonRegionOfInterest;
import net.imglib2.type.numeric.RealType;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.scijava.Context;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import org.scijava.util.ColorRGB;

/**
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = Service.class)
public class OverlayStatService extends AbstractService implements ImageJService {

    @Parameter
    private OverlayService overlayService;

    @Parameter
    private DatasetService datasetService;

    @Parameter
    private OpService opService;

    @Parameter
    private StatisticsService statService;

    @Parameter
    private OverlayDrawingService overlayDrawingService;

    final private Logger logger = ImageJFX.getLogger();

    public final static String LBL_MEAN = "Mean";
    public final static String LBL_MEDIAN = "Median";
    public final static String LBL_AREA = "Area";
    public final static String LBL_MAX = "Max";
    public final static String LBL_MIN = "Min";
    public final static String LBL_SD = "Std. Dev.";
    public final static String LBL_VARIANCE = "Variance";
    public final static String LBL_PIXEL_COUNT = "Pixel count";

    public final static String LBL_MBR = "Minimum Bounding Rectangle";
    public final static String LBL_CENTROID = "Center of Gravity";
    public final static String LBL_MAX_FERET_DIAMETER = "Feret Diameter";
    public final static String LBL_MIN_FERET_DIAMETER = "Min. Feret Diameter";
    public final static String LBL_LONG_SIDE_MBR = "Long Side MBR";
    public final static String LBL_SHORT_SIDE_MBR = "Short Side MBR";
    public final static String LBL_ASPECT_RATIO = "Aspect ratio";
    public final static String LBL_CONVEXITY = "Convexity";
    public final static String LBL_SOLIDITY = "Solidity";
    public final static String LBL_CIRCULARITY = "Circularity";
    public final static String LBL_THINNES_RATIO = "Thinnes ratio";

    public final static String LBL_CENTER_X = "Center X";
    public final static String LBL_CENTER_Y = "Center Y";

    public Double[] getValueListFromImageDisplay(ImageDisplay imageDisplay, Overlay overlay) {

        if (overlay instanceof LineOverlay) {
            return getValueList(imageDisplay, (LineOverlay) overlay);
        }

        // getting the dataset corresponding to the display
        final Dataset ds = datasetService.getDatasets(imageDisplay).get(0);

        // Array containing all the pixel values
        ArrayList<Double> values = new ArrayList<>(10000);

        // RoiPointSet used to iterate through the pixel inside the Roi
        RoiPointSet rps = new RoiPointSet(overlay.getRegionOfInterest());

        PointSetIterator psc = rps.cursor();

        // Random access of the dataset
        RandomAccess<RealType<?>> randomAccess = ds.randomAccess();

        // position of the image display
        long[] position = new long[imageDisplay.numDimensions()];
        imageDisplay.localize(position);
        psc.reset();
        int c = 0;

        // getting the 
        while (psc.hasNext()) {
            psc.fwd();
            long[] roiPosition = psc.get();

            for (int i = 0; i != roiPosition.length; i++) {
                position[i] = roiPosition[i];
            }
            randomAccess.setPosition(position);
            values.add(randomAccess.get().getRealDouble());
            c++;
        }
        System.out.printf("%d values retrieved\n", c);
        return values.toArray(new Double[values.size()]);

    }

    public <T extends RealType<T>> Double[] getValueList(RandomAccessible<? extends RealType<?>> accessible, Overlay overlay, long[] position) {

        // we hack the PixelDrawingService to measure pixel inside the overlay by hacking the drawing method;
        RandomAccess randomAccess = accessible.randomAccess();
        randomAccess.setPosition(position);
        PixelMeasurer<T> measurer = new PixelMeasurer<>(randomAccess);
        overlayDrawingService.drawOverlay(overlay, OverlayDrawingService.FILLER, measurer);
        return measurer.getValuesAsArray();

    }

    protected Double[] getValueList(ImageDisplay imageDisplay, LineOverlay overlay) {

        final Dataset ds = datasetService.getDatasets(imageDisplay).get(0);

        System.out.printf("Num dimensions %d\n", overlay.numDimensions());

        RandomAccess<RealType<?>> randomAccess = ds.randomAccess();

        int x0 = new Double(overlay.getLineStart(0)).intValue();
        int y0 = new Double(overlay.getLineStart(1)).intValue();
        int x1 = new Double(overlay.getLineEnd(0)).intValue();
        int y1 = new Double(overlay.getLineEnd(1)).intValue();

        List<int[]> pixels = Bresenham.findLine(x0, y0, x1, y1);

        long[] position = new long[imageDisplay.numDimensions()];
        Double[] values = new Double[pixels.size()];

        int i = 0;
        for (int[] coordinate : pixels) {
            position[0] = coordinate[0];
            position[1] = coordinate[1];
            randomAccess.setPosition(position);

            values[i] = randomAccess.get().getRealDouble();
            i++;
        }

        return values;
    }

    private interface ParallelMeasurement {

        public Double measure();
    }

    public class OverlayStats extends HashMap<String, Double> {

    }

    private PointSet getRegion(ImageDisplay display, Overlay overlay) {
        if (overlay != null) {
            return new RoiPointSet(overlay.getRegionOfInterest());
        }
        long[] pt1 = new long[display.numDimensions()];
        long[] pt2 = new long[display.numDimensions()];
        // current plane only
        pt1[0] = 0;
        pt1[1] = 0;
        pt2[0] = display.dimension(0) - 1;
        pt2[1] = display.dimension(1) - 1;
        for (int i = 2; i < display.numDimensions(); i++) {
            pt1[i] = pt2[i] = display.getLongPosition(i);
        }
        return new HyperVolumePointSet(pt1, pt2);
    }

    public OverlayStatistics getOverlayStatistics(ImageDisplay display, Overlay overlay) {
        return new DefaultOverlayStatistics(display, overlay);
    }

    public OverlayShapeStatistics getShapeStatistics(Overlay overlay) {

        OverlayShapeStatistics overlayStatistics;

        if (overlay instanceof LineOverlay) {
            overlayStatistics = new LineOverlayStatistics(overlay, this.context());
        } else if (overlay instanceof RectangleOverlay) {
            overlayStatistics = new RectangleOverlayStatistics(overlay, this.context());
        } else if(overlay instanceof PolygonOverlay){
            overlay = cleanOverlay((PolygonOverlay)overlay);
        }
         overlayStatistics = new PolygonOverlayStatistics(overlay, this.context());
        

        return overlayStatistics;
    }

    public HashMap<String, Double> getStatisticsAsMap(ImageDisplay imageDisplay, Overlay overlay) {

        return getStatisticsAsMap(getOverlayStatistics(imageDisplay, overlay));
    }

    public HashMap<String, Double> getStatisticsAsMap(OverlayStatistics overlayStatistics) {
        // then an hash map containing the statistics
        HashMap<String, Double> statistics = getShapeStatisticsAsMap(overlayStatistics.getShapeStatistics());

        // then complete it with pixel statistics
        getPixelStatisticsAsMap(overlayStatistics.getPixelStatistics()).forEach((key, value) -> {
            statistics.put(key, value);
        });
        return statistics;
    }

    public HashMap<String, Double> getPixelStatisticsAsMap(PixelStatistics overlayStats) {
        HashMap<String, Double> statistics = new HashMap<>();
        // if(overlayStats.getPixelStatistics() != null) {
        statistics.put(LBL_MEAN, overlayStats.getMean());
        statistics.put(LBL_MIN, overlayStats.getMin());
        statistics.put(LBL_MAX, overlayStats.getMax());
        statistics.put(LBL_SD, overlayStats.getStandardDeviation());
        statistics.put(LBL_VARIANCE, overlayStats.getVariance());
        statistics.put(LBL_MEDIAN, overlayStats.getMedian());
        statistics.put(LBL_PIXEL_COUNT, (double) overlayStats.getPixelCount());
        // }
        return statistics;
    }

    public HashMap<String, Double> getShapeStatisticsAsMap(Overlay overlay) {

        return getShapeStatisticsAsMap(getShapeStatistics(overlay));
    }

    public HashMap<String, Double> getShapeStatisticsAsMap(OverlayShapeStatistics overlayStats) {

        HashMap<String, Double> statistics = new HashMap<>();
        statistics.put(LBL_AREA, overlayStats.getArea());

        statistics.put(LBL_MAX_FERET_DIAMETER, overlayStats.getFeretDiameter());
        statistics.put(LBL_MIN_FERET_DIAMETER, overlayStats.getMinFeretDiameter());
        statistics.put(LBL_LONG_SIDE_MBR, overlayStats.getLongSideMBR());
        statistics.put(LBL_SHORT_SIDE_MBR, overlayStats.getShortSideMBR());
        statistics.put(LBL_ASPECT_RATIO, overlayStats.getAspectRatio());
        statistics.put(LBL_CONVEXITY, overlayStats.getConvexity());
        statistics.put(LBL_SOLIDITY, overlayStats.getSolidity());
        statistics.put(LBL_CIRCULARITY, overlayStats.getCircularity());
        statistics.put(LBL_THINNES_RATIO, overlayStats.getThinnesRatio());
        statistics.put(LBL_CENTER_X, overlayStats.getCenterX());
        statistics.put(LBL_CENTER_Y, overlayStats.getCenterY());

        return statistics;
    }

    public <T extends RealType<T>> OverlayStatistics getStatistics(Overlay overlay, Dataset dataset, long[] position) {

        PixelStatistics pixelStats = new PixelStatisticsBase(new DescriptiveStatistics(ArrayUtils.toPrimitive(getValueList(dataset, overlay, position))));
        OverlayShapeStatistics shapeState = getShapeStatistics(overlay);

        return new OverlayStatisticsBase(overlay, shapeState, pixelStats);
    }

    public void setRandomColor(List<Overlay> overlays) {

        double GOLDEN_RATIO_CONJUGATE = 0.618033988749895;
        double SATURATION = 0.99;
        double VALUE = 0.99;

        double hue = Math.random();

        for (int i = 0; i < overlays.size(); i++) {

            hue = hue + GOLDEN_RATIO_CONJUGATE;
            hue = hue % 1;
            ColorRGB randomColor = hsvtoRGB(hue, SATURATION, VALUE);

            overlays.get(i).setFillColor(randomColor);
            overlays.get(i).setLineColor(randomColor);
            overlays.get(i).update();
        }
    }

    public ColorRGB hsvtoRGB(double hue, double saturation, double value) {

        int h = (int) (hue * 6);
        double f = hue * 6 - h;
        double p = value * (1 - saturation);
        double q = value * (1 - f * saturation);
        double t = value * (1 - (1 - f) * saturation);

        double r1, g1, b1;
        int r, g, b;

        switch (h) {
            case 0:
                r1 = saturation;
                g1 = t;
                b1 = p;
                break;
            case 1:
                r1 = q;
                g1 = value;
                b1 = p;
                break;
            case 2:
                r1 = p;
                g1 = value;
                b1 = t;
                break;
            case 3:
                r1 = p;
                g1 = q;
                b1 = value;
                break;
            case 4:
                r1 = t;
                g1 = p;
                b1 = value;
                break;
            case 5:
                r1 = value;
                g1 = p;
                b1 = q;
                break;
            default:
                throw new RuntimeException(
                        String.format("Could not convert from HSV (%f, %f, %f) to RGB", hue, saturation, value));
        }
        r = (int) (r1 * 256);
        g = (int) (g1 * 256);
        b = (int) (b1 * 256);

        return new ColorRGB(r, g, b);
    }

    public Overlay cleanOverlay(PolygonOverlay overlay) {

        PolygonRegionOfInterest roi = (PolygonRegionOfInterest) overlay.getRegionOfInterest();
        int npoints = roi.getVertexCount();

        if (npoints <= 3) {
            return overlay;
        }

        Callback<RealLocalizable, Point2D> converter = real -> new Point2D.Double(real.getDoublePosition(0), real.getDoublePosition(1));

        List<RealLocalizable> points = new ArrayList<>();
        
        points.add(overlay.getRegionOfInterest().getVertex(0));
        
        
        for (int i = 1; i != npoints - 2; i++) {

            Point2D p1 = converter.call(roi.getVertex(i - 1));
            Point2D p2 = converter.call(roi.getVertex(i));
            Point2D p3 = converter.call(roi.getVertex(i + 1));
            
            if(!areColinear(p1, p2, p3))  points.add(roi.getVertex(i));
            

        }
        
        points.add(overlay.getRegionOfInterest().getVertex(npoints-1));
        
        return createPolytonOverlay(getContext(), points, p->new RealPoint(p));

        /*
        if (npoints >= 3) {

            int i;
            for (i = 0; i <= npoints - 1; i++) {
                int j = i + 1;
                int k = i + 2;
                if (i == npoints - 1) {
                    j = 0;
                    k = 1;
                }
                if (i == npoints - 2) {
                    k = 0;
                }
                Point pt1 = new Point((int) roi.getVertex(i).getDoublePosition(0), (int) roi.getVertex(i).getDoublePosition(1));
                Point pt2 = new Point((int) roi.getVertex(j).getDoublePosition(0), (int) roi.getVertex(j).getDoublePosition(1));
                Point pt3 = new Point((int) roi.getVertex(k).getDoublePosition(0), (int) roi.getVertex(k).getDoublePosition(1));

                if (areColinear(pt1, pt2, pt3)) {
                    roi.removeVertex(j);
                    i--;
                    npoints--;
                }
            }
            
        }

        return overlay;*/
    }

    public PolygonOverlay createPolygonOverlay(Context context, List<Point> pointList) {
        return createPolytonOverlay(context, pointList, point -> {
            return new RealPoint(point.getX(), point.getY());
        });
    }

    public <T> PolygonOverlay createPolytonOverlay(Context context, List<T> pointList, Callback<T, RealPoint> pointFactory) {

        PolygonOverlay overlay = new PolygonOverlay(context);

        for (int i = 0; i != pointList.size(); i++) {

            T t = pointList.get(i);
            overlay.getRegionOfInterest().addVertex(i, pointFactory.call(t));

        }

        return overlay;

    }

    public boolean areColinear(Point pt1, Point pt2, Point pt3) {

        boolean colinear = false;

        double signedArea = ((pt2.getX() - pt1.getX()) * (pt3.getY() - pt1.getY())) - ((pt3.getX() - pt1.getX()) * (pt2.getY() - pt1.getY()));

        colinear = (signedArea == 0.0);

        return colinear;
    }

    public boolean areColinear(Point2D pt1, Point2D pt2, Point2D pt3) {

        boolean colinear = false;

        double signedArea = ((pt2.getX() - pt1.getX()) * (pt3.getY() - pt1.getY())) - ((pt3.getX() - pt1.getX()) * (pt2.getY() - pt1.getY()));

        colinear = (signedArea == 0.0);

        return colinear;
    }

    private class PixelMeasurer<T extends RealType<T>> implements PixelDrawer {

        private final RandomAccess<T> randomAccess;

        private final ArrayList<Double> stats = new ArrayList<Double>(10000);

        public PixelMeasurer(RandomAccess<T> r) {
            this.randomAccess = r;
        }

        @Override
        public void drawPixel(long x, long y) {

            randomAccess.setPosition(x, 0);
            randomAccess.setPosition(y, 1);
            stats.add(randomAccess.get().getRealDouble());

        }

        public Double[] getValuesAsArray() {
            return stats.toArray(new Double[stats.size()]);
        }

    }

}
