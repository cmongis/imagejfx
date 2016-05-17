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
package ijfx.ui.arcmenu;

import javafx.geometry.Point2D;

// class used to switch between polar and cartesian coordinates.
public class PolarCoord {

    PolarSystem ps;
    double angle;
    double radius;

    
    
    
    public PolarCoord(PolarSystem ps, double radius, double angle) {
        this.angle = angle;
        this.radius = radius;
        this.ps = ps;
    }

    public Point2D getPointLocation() {
        return ps.degreeToPolar(radius, angle);
    }

    public double getX() {
        return getPointLocation().getX();
    }

    public double getY() {
        return getPointLocation().getY();
    }

    public Point2D getLocationCloserToCenter(double xFromShape) {
        return ps.degreeToPolar(radius - xFromShape, angle);
    }

}
