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

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point2D;

/**
 * Class describing the parameters of a PolarSystem
 * @author Cyril MONGIS, 2015
 */
public class PolarSystem {
    private final DoubleProperty centerX = new SimpleDoubleProperty(0.0f);
    private final  DoubleProperty centerY = new SimpleDoubleProperty(0.0f);

    
    
    public PolarSystem(ReadOnlyDoubleProperty centerX, ReadOnlyDoubleProperty centerY) {
        this.centerX.bind(centerX);
        this.centerY.bind(centerY);
    }


    public Point2D degreeToPolar(double r, double a) {
        a = toRadian(a);
        double x = centerX.getValue() + (r * Math.cos(a));
        double y = centerY.getValue() + (r * Math.sin(a));
        return new Point2D(x, y);
    }

    public double toRadian(double a) {
        return a * Math.PI / 180;
    }
    
    
    public DoubleProperty centerXProperty() {
        return centerX;
        
    }
    public DoubleProperty centerYProperty() {
        return centerY;
        
    }

}
