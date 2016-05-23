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

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point2D;

// class used to switch between polar and cartesian coordinates.
public class PolarCoord {

    private final PolarSystem ps;
    private final DoubleProperty angle = new SimpleDoubleProperty();
    private final DoubleProperty radius = new SimpleDoubleProperty();

    
    
    private final DoubleBinding xProperty;
    private final DoubleBinding yProperty;
    
    
    
    
    public PolarCoord(PolarSystem ps, double radius, double angle) {
        this.angle.setValue(angle);
        this.radius.setValue(radius);
        this.ps = ps;
        
        xProperty = Bindings.createDoubleBinding(this::getX, ps.centerXProperty(),ps.centerYProperty(),this.angle,this.radius);
        yProperty = Bindings.createDoubleBinding(this::getY, ps.centerXProperty(),ps.centerYProperty(),this.angle,this.radius);
    }

    public Point2D getPointLocation() {
        return ps.degreeToPolar(radius.getValue(), angle.getValue());
    }

    private double getX() {
        System.out.println("Getting the x ?");
        System.out.println(ps.centerXProperty().getValue());
        return getPointLocation().getX();
    }

    private double getY() {
        return getPointLocation().getY();
    }

    public DoubleBinding xProperty() {
        
        return xProperty;
        
    }
    public DoubleBinding yProperty() {
        return yProperty;
    }
    
    
    public Point2D getLocationCloserToCenter(double xFromShape) {
        return ps.degreeToPolar(radius.getValue() - xFromShape, angle.getValue());
    }

}
