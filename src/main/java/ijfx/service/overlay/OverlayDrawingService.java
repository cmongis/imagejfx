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


import net.imagej.ImageJService;

import net.imagej.overlay.Overlay;
import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RealInterval;
import net.imglib2.RealRandomAccess;
import net.imglib2.RealRandomAccessibleRealInterval;
import net.imglib2.roi.RegionOfInterest;
import net.imglib2.type.logic.BitType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.RandomAccessibleOnRealRandomAccessible;
import net.imglib2.view.Views;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 * Code adapted from OverlayService drawing part. This new form allows a more flexible use of the drawing process.
 * @author Curtis Rueden
 * @author Barry DeZonia
 * @author Cyril MONGIS
 */
@Plugin(type = Service.class)
public class OverlayDrawingService extends AbstractService implements ImageJService{
    
    
        public void drawOverlay(Overlay o, Drawer drawer, PixelDrawer tool) {
               drawer.draw(o, tool);
        }

        
        
        public static final Drawer OUTLINER = new OverlayOutliner();
        public static final Drawer FILLER = new OverlayFiller();

	private static class OverlayOutliner implements Drawer {

		@Override
		public void draw(final Overlay o, final PixelDrawer tool) {
			final RegionOfInterest region = o.getRegionOfInterest();
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
				if (accessor.get().get() &&
					isBorderPixel(accessor, pos, max[0], max[1]))
				{
					tool.drawPixel(pos[0], pos[1]);
				}
			}
		}

		private boolean isBorderPixel(final RealRandomAccess<BitType> accessor,
			final long[] pos, final long maxX, final long maxY)
		{
			if (pos[0] == 0) return true;
			if (pos[0] == maxX) return true;
			if (pos[1] == 0) return true;
			if (pos[1] == maxY) return true;
			accessor.setPosition(pos[0] - 1, 0);
			if (!accessor.get().get()) return true;
			accessor.setPosition(pos[0] + 1, 0);
			if (!accessor.get().get()) return true;
			accessor.setPosition(pos[0], 0);
			accessor.setPosition(pos[1] - 1, 1);
			if (!accessor.get().get()) return true;
			accessor.setPosition(pos[1] + 1, 1);
			if (!accessor.get().get()) return true;
			return false;
		}

	}

	private static class OverlayFiller implements Drawer {

		@Override
		public void draw(final Overlay o, final PixelDrawer tool) {
			final RegionOfInterest region = o.getRegionOfInterest();

			final Cursor<BitType> cursor =
				iterableInterval(region).localizingCursor();

			long[] pos = new long[region.numDimensions()];
			while (cursor.hasNext()) {
				cursor.fwd();
				cursor.localize(pos);
				if (cursor.get().get()) tool.drawPixel(pos[0], pos[1]);
			}
		}
	}

	// TODO: Consider contributing this method to net.imglib2.view.Views.
	// See also: net.imglib2.roi.IterableRegionOfInterest

	private static <T> IterableInterval<T> iterableInterval(
		final RealRandomAccessibleRealInterval<T> realInterval)
	{
		final RandomAccessibleOnRealRandomAccessible<T> raster =
			Views.raster(realInterval);
		final IntervalView<T> interval =
			Views.interval(raster, findMin(realInterval), findMax(realInterval));
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
