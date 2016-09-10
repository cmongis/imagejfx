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

import javafx.geometry.Point2D;
import javafx.scene.shape.Polygon;
import net.imagej.overlay.Overlay;

/**
 *
 * @author cyril
 */
public interface OverlayShapeStatistics {

    Overlay getOverlay();

    double getCenterX();

    double getCenterY();

    double getArea();

    Polygon getMinimumBoundingRectangle();

    Point2D getCenterOfGravity();

    double getFeretDiameter();

    double getMinFeretDiameter();
//    double getOrientationMajorAxis();
//    double getOrientationMinorAxis();

    double getLongSideMBR();

    double getShortSideMBR();

    double getAspectRatio();

    double getConvexity();

    double getSolidity();

    double getCircularity();

    double getThinnesRatio();

    @Override
    String toString();
    
    public static OverlayShapeStatistics EMPTY = new OverlayShapeStatistics() {
        @Override
        public Overlay getOverlay() {
            return null;
        }

        @Override
        public double getCenterX() {
            return Double.NaN;
        }

        @Override
        public double getCenterY() {
            return Double.NaN;
        }

        @Override
        public double getArea() {
            return Double.NaN;
        }

        @Override
        public Polygon getMinimumBoundingRectangle() {
            return new Polygon();
        }

        @Override
        public Point2D getCenterOfGravity() {
            return new Point2D(Double.NaN, Double.NaN);
        }

        @Override
        public double getFeretDiameter() {
            return Double.NaN;
        }

        @Override
        public double getMinFeretDiameter() {
            return Double.NaN;
        }

        @Override
        public double getLongSideMBR() {
            return Double.NaN;
        }

        @Override
        public double getShortSideMBR() {
            return Double.NaN;
        }

        @Override
        public double getAspectRatio() {
            return Double.NaN;
        }

        @Override
        public double getConvexity() {
            return Double.NaN;
        }

        @Override
        public double getSolidity() {
            return Double.NaN;
        }

        @Override
        public double getCircularity() {
            return Double.NaN;
        }

        @Override
        public double getThinnesRatio() {
            return Double.NaN;
        }
    };
    
}
