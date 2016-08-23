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

import ijfx.core.utils.DimensionUtils;
import ijfx.service.ImagePlaneService;
import net.imagej.Dataset;
import net.imagej.ImageJService;

import net.imagej.overlay.Overlay;
import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RealInterval;
import net.imglib2.RealRandomAccess;
import net.imglib2.RealRandomAccessibleRealInterval;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.roi.RegionOfInterest;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.RandomAccessibleOnRealRandomAccessible;
import net.imglib2.view.Views;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 * Code adapted from OverlayService drawing part. This new form allows a more
 * flexible use of the drawing process.
 *
 * @author Curtis Rueden
 * @author Barry DeZonia
 * @author Cyril MONGIS
 */
@Plugin(type = Service.class)
public class OverlayDrawingService extends AbstractService implements ImageJService {

    
    @Parameter
    ImagePlaneService imagePlaneService;
    
    public void drawOverlay(Overlay o, Drawer drawer, PixelDrawer tool) {
        drawer.draw(o, tool);
    }

    public <T extends RealType<T>> void drawOverlay(Overlay o, Drawer drawer, Dataset dataset, T value) {
        drawOverlay(o,drawer, new RandomAccessDrawer(dataset.randomAccess(),value));
    }

     public <T extends RealType<T>> void drawOverlay(Overlay o, Drawer drawer, Dataset dataset, double value) {
        drawOverlay(o,drawer, new DoubleRandomAccessDrawer(dataset.randomAccess(),value));
    } 
    
     
     public <T extends RealType<T>> Dataset extractObject(Overlay o, Dataset source, long[] nonSpacialPosition) {
         
         
         //imagePlaneService.isolatePlane(source, DimensionUtils.nonPlanarToPlanar(nonSpacialPosition));
         
         long minX = Math.round(o.getRegionOfInterest().realMin(0));
         long minY = Math.round(o.getRegionOfInterest().realMin(1));
         
         long maxX = Math.round(o.getRegionOfInterest().realMax(0));
         long maxY = Math.round(o.getRegionOfInterest().realMax(1));
         
         long width = maxX-minX;
         long height = maxY-minY;
         
        Dataset overlayDataset = imagePlaneService.createEmptyPlaneDataset(source,width+1,height+1);
        
        FILLER.draw(o, new RandomAccessCopier(imagePlaneService.planeView(source, nonSpacialPosition).randomAccess(), overlayDataset.randomAccess(), -minX, -minY));
        
         //drawOverlay(o,new RandomAccessCopier<>(source, overlayDataset, -minX, -minY),FILLER);
         return overlayDataset;
     }
    
     
     
    private class RandomAccessCopier<T extends RealType<T>> implements PixelDrawer {

        
        final private RandomAccess<T> source;
        final private RandomAccess<T> target;
        
        
        final private long xTranslation;
        final private long yTranslation;

        
        
        public RandomAccessCopier(RandomAccess source, RandomAccess target) {
            this(source,target,0,0);
        }
        
        public RandomAccessCopier(RandomAccess<T> source, RandomAccess<T> target, long xTranslation, long yTranslation) {
            this.source = source;
            this.target = target;
            this.xTranslation = xTranslation;
            this.yTranslation = yTranslation;
        }
        

        @Override
        public void drawPixel(long x, long y) {
            
            source.setPosition(x, 0);
            source.setPosition(y, 1);
            
            target.setPosition(x+xTranslation,0);
            target.setPosition(y+yTranslation,1);
            
            target.get().set(source.get());
            
        }
        
    }
     
    private class RandomAccessDrawer<T extends RealType<T>> implements PixelDrawer {

      
        private final T value;
        protected RandomAccess<T> r;
        public RandomAccessDrawer(RandomAccess<T> dataset, T value) {
            this.value = value;
            r = (RandomAccess<T>) dataset;
        }
        @Override
        public void drawPixel(long x, long y) {

            r.setPosition(x, 0);
            r.setPosition(y, 1);
            r.get().set(value);

        }
    }
    
    private class DoubleRandomAccessDrawer<T extends RealType<T>> implements PixelDrawer{
        
        double value;
        
        protected RandomAccess<T> r;
        public DoubleRandomAccessDrawer(RandomAccess<T> dataset, double value) {
            this.value = value;
            r = (RandomAccess<T>) dataset;
        }
        
        @Override
        public void drawPixel(long x, long y) {
            r.setPosition(x, 0);
            r.setPosition(y, 1);
            r.get().setReal(value);
        }
        
    }
    
    

    public static final Drawer OUTLINER = new OverlayOutliner();
    public static final Drawer FILLER = new OverlayFiller();

    private static class OverlayOutliner implements Drawer {

        @Override
        public void draw(final Overlay o, final PixelDrawer tool) {
            final RegionOfInterest region = o.getRegionOfInterest();
            RectangleShape shape = new RectangleShape(3, true);
            final IterableInterval<BitType> ii = iterableInterval(region);

            
            
            final long[] max = new long[region.numDimensions()];
            ii.max(max);

            final Cursor<BitType> cursor = ii.localizingCursor();
            final RealRandomAccess<BitType> accessor = region.realRandomAccess();

            long[] pos = new long[region.numDimensions()];
            while (cursor.hasNext()) {
                cursor.fwd();
                cursor.localize(pos);
                accessor.setPosition(pos);
                if (accessor.get().get()
                        && isBorderPixel(accessor, pos, max[0], max[1])) {
                    tool.drawPixel(pos[0], pos[1]);
                }
                
                
                
            }
        }
        
        

        private boolean isBorderPixel(final RealRandomAccess<BitType> accessor,
                final long[] pos, final long maxX, final long maxY) {
            if (pos[0] == 0) {
                return true;
            }
            if (pos[0] == maxX) {
                return true;
            }
            if (pos[1] == 0) {
                return true;
            }
            if (pos[1] == maxY) {
                return true;
            }
            accessor.setPosition(pos[0] - 1, 0);
            if (!accessor.get().get()) {
                return true;
            }
            accessor.setPosition(pos[0] + 1, 0);
            if (!accessor.get().get()) {
                return true;
            }
            accessor.setPosition(pos[0], 0);
            accessor.setPosition(pos[1] - 1, 1);
            if (!accessor.get().get()) {
                return true;
            }
            accessor.setPosition(pos[1] + 1, 1);
            if (!accessor.get().get()) {
                return true;
            }
            return false;
        }

    }

    private static class OverlayFiller implements Drawer {

        @Override
        public void draw(final Overlay o, final PixelDrawer tool) {
            final RegionOfInterest region = o.getRegionOfInterest();

            final Cursor<BitType> cursor
                    = iterableInterval(region).localizingCursor();

            long[] pos = new long[region.numDimensions()];
            while (cursor.hasNext()) {
                cursor.fwd();
                cursor.localize(pos);
                if (cursor.get().get()) {
                   
                    tool.drawPixel(pos[0], pos[1]);
                }
            }
        }
    }

    // TODO: Consider contributing this method to net.imglib2.view.Views.
    // See also: net.imglib2.roi.IterableRegionOfInterest
    private static <T> IterableInterval<T> iterableInterval(
            final RealRandomAccessibleRealInterval<T> realInterval) {
        final RandomAccessibleOnRealRandomAccessible<T> raster
                = Views.raster(realInterval);
        final IntervalView<T> interval
                = Views.interval(raster, findMin(realInterval), findMax(realInterval));
        return Views.iterable(interval);
    }

    private static long[] findMin(final RealInterval realInterval) {
        final long[] boundMin = new long[realInterval.numDimensions()];
        for (int i = 0; i < boundMin.length; i++) {
            boundMin[i] = (long) Math.floor(realInterval.realMin(i));
        }
        return boundMin;
    }

    private static long[] findMax(final RealInterval realInterval) {
        final long[] boundMax = new long[realInterval.numDimensions()];
        for (int i = 0; i < boundMax.length; i++) {
            boundMax[i] = (long) Math.ceil(realInterval.realMax(i));
        }
        return boundMax;
    }
}
