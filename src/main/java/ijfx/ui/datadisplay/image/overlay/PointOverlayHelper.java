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
package ijfx.ui.datadisplay.image.overlay;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import net.imagej.overlay.PointOverlay;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public final class PointOverlayHelper {

    final private PointOverlay pointOverlay;
    final private int index;

    
    final private Property<Point2D> positionProperty = new SimpleObjectProperty();

    public PointOverlayHelper(PointOverlay pointOverlay, int index) {
        this.pointOverlay = pointOverlay;
        this.index = index;
        
        
    }

    public int getIndex() {
        return index;
    }

    public PointOverlay getPointOverlay() {
        return pointOverlay;
    }
    
    

    public Property<Point2D> positionProperty() {
        return positionProperty;
    }
    
    
    public static Point2D getPointPositionOnImage(PointOverlay pointOverlay, int index) {
        double[] point = pointOverlay.getPoint(index);
        return new Point2D(point[0], point[1]);
    }

    public static Point2D getOverlayPosition(PointOverlay pointOverlay) {
        double[] point = pointOverlay.getPoint(0);

        return new Point2D(point[0], point[1]);

    }

    public static void setOverlayPosition(PointOverlay overlay, Point2D point2DonImage) {
        overlay.setPoint(0, new double[]{point2DonImage.getX(), point2DonImage.getY()});
    }

}
