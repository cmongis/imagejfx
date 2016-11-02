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
package ijfx.service.overlay;

import net.imagej.overlay.Overlay;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class OverlayStatisticsBase implements OverlayStatistics{
    Overlay overlay;
    
    OverlayShapeStatistics shapeStatistics;
    
    PixelStatistics pixelStatistics;

    public OverlayStatisticsBase(Overlay overlay, OverlayShapeStatistics shapeStatistics, PixelStatistics stats) {
        this.overlay = overlay;
        this.shapeStatistics = shapeStatistics;
        this.pixelStatistics = stats;
    }

    public Overlay getOverlay() {
        return overlay;
    }

    public OverlayShapeStatistics getShapeStatistics() {
        return shapeStatistics;
    }

    public PixelStatistics getPixelStatistics() {
        return pixelStatistics;
    }
    
    
    
    
    
    
}
