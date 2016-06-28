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
package ijfx.service.batch;

import ijfx.service.overlay.OverlayStatService;
import ijfx.service.overlay.PixelStatistics;
import ijfx.service.overlay.PixelStatisticsBase;
import ijfx.service.overlay.PolygonOverlayStatistics;
import net.imagej.overlay.Overlay;
import net.imglib2.RandomAccessible;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import ijfx.service.overlay.OverlayShapeStatistics;

/**
 *
 * @author cyril
 */
public class DefaultSegmentedObject implements SegmentedObject {

    @Parameter
    OverlayStatService overlayStatsService;

    @Parameter
    Context context;

    PixelStatistics pixelStats;

    OverlayShapeStatistics shapeStatistics;

    long[] position;

    public DefaultSegmentedObject(Overlay overlay, long[] position, RandomAccessible r) {
        overlay.getContext().inject(this);

        this.position = position;
        this.overlay = overlay;

        pixelStats = new PixelStatisticsBase(new DescriptiveStatistics(ArrayUtils.toPrimitive(overlayStatsService.getValueList(r, overlay, position))));
        shapeStatistics = overlayStatsService.getShapeStatistics(overlay);

    }

    Overlay overlay;

    @Override
    public Overlay getOverlay() {
        return overlay;
    }

    @Override
    public PixelStatistics getPixelStatistics() {
        return pixelStats;
    }

    @Override
    public OverlayShapeStatistics getShapeStatistics() {
        shapeStatistics = new PolygonOverlayStatistics(overlay, context);
        return shapeStatistics;
    }

  
   

}
