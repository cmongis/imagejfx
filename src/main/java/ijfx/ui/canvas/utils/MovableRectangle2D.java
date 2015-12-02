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
package ijfx.ui.canvas.utils;


import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Rectangle2D;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class MovableRectangle2D extends Rectangle2D{
    
    SimpleDoubleProperty minX;
    SimpleDoubleProperty minY;
    SimpleDoubleProperty width;
    SimpleDoubleProperty height;
    
    public MovableRectangle2D(double x, double y, double w, double h) {
        super(x,y,w,h);
        minX  = new SimpleDoubleProperty(x);
        minY  = new SimpleDoubleProperty(y);
        width = new SimpleDoubleProperty(w);
        height  = new SimpleDoubleProperty(h);
        
        
        
    }
    
    public MovableRectangle2D(Rectangle2D source) {
        this(source.getMinX(), source.getMinY(), source.getWidth(), source.getHeight());
    }
    
    
    @Override
    public double getMinX() {
        return minX.getValue();
    }
    
    @Override
    public double getMinY() {
        return minY.getValue();
    }
    
    @Override
    public double getWidth() {
        return width.getValue();
    }
    
    @Override
    public double getHeight() {
        return height.getValue();
    }
    
    @Override
    public double getMaxX() {
        return getMinX()+getWidth();
    }
    
    @Override
    public double getMaxY() {
        return getMinY()+getWidth();
    }
    
    public void setMinX(double value) {
        minXProperty().setValue(value);
    }
    
    public void setMinY(double value) {
        minYProperty().setValue(value);
    }

    public SimpleDoubleProperty minXProperty() {
        return minX;
    }

    public SimpleDoubleProperty minYProperty() {
        return minY;
    }

    public SimpleDoubleProperty widthProperty() {
        return width;
    }

    public SimpleDoubleProperty heightProperty() {
        return height;
    }

    public Rectangle2D move(double x,double y) {
        if(getMinX() - x > 0)
        minX.setValue(getMinX()-x);
        if(getMinY() - y > 0)
        minY.setValue(getMinY()-y);

        return new Rectangle2D(getMinX(),getMinY(),getWidth(),getHeight());
    }
    
    
    
   
    
}
