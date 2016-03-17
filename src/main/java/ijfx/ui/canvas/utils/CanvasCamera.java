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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class CanvasCamera implements ViewPort  {

    // coordinates of the center of the camera
    double x = Double.NaN;
    double y = Double.NaN;

    // width and heiht of the camera
    double width;
    double height;

    double zoom = 1.0;

    Rectangle2D imageSpace;

    public CanvasCamera() {
        
    }
    
    // define the bounds of the images regardless of zoom level
    public Rectangle2D getImageSpace() {
        return imageSpace;
    }

    public void setImageSpace(Rectangle2D imageSpace) {
       // System.out.println("Setting image space : " + imageSpace);
        this.imageSpace = imageSpace;
        x = Double.NaN;
        y = Double.NaN;
    }

    // width of the part extracted from the picture after taking account the zoom effect
    @Override
    public double getEffectiveWidth() {
        return width / zoom;
    }

    // height of the part extracted from the picture after taking account the zoom effect
    @Override
    public double getEffectiveHeight() {
        return height / zoom;
    }

    public double getX() {
        if (Double.isNaN(x)) {
            x = imageSpace.getWidth() / 2;
        }
        return x;
    }

    public void setX(double x) {

        this.x = x;
    }

    public double getY() {
        if (Double.isNaN(y)) {
            y = imageSpace.getHeight() / 2;
        }
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    @Override
    public double getZoom() {
        return zoom;
    }

    @Override
    public void setZoom(double zoom) {
        this.zoom = zoom;
        fireValueChangedEvent();
    }

    public void zoomIn() {
        zoom *= 1.1;
        fireValueChangedEvent();
       // System.out.println(zoom);
    }

    public void zoomOut() {
        zoom *= 0.9;
        fireValueChangedEvent();
       // System.out.println(zoom);
    }

    private double getCameraLeftLimit() {
        return getEffectiveWidth() / 2;
    }

    private double getCameraUpLimit() {
        return getEffectiveHeight() / 2;
    }

    private double getCameraDownLimit() {
        return getImageSpace().getHeight() - (getEffectiveHeight() / 2);
    }

    private double getCameraRightLimit() {
        return getImageSpace().getWidth() - (getEffectiveWidth() / 2);
    }

    public double getImageEffectiveWidth() {
        return getImageSpace().getWidth() * getZoom();
    }

    public double getImageEffectiveHeight() {
        return getImageSpace().getHeight() * getZoom();
    }

    public CanvasCamera moveX(double x) {

        double newX = this.x + x;
        if (newX >= getCameraRightLimit()) {
            newX = getCameraRightLimit() - 1;
        }
        if (newX < getCameraLeftLimit()) {
            newX = getCameraLeftLimit() + 1;
        }
        setX(newX);
        fireValueChangedEvent();
        return this;
    }

    public CanvasCamera moveY(double y) {

        double newY = this.y + y;
        if (newY >= getCameraDownLimit()) {
            newY = getCameraDownLimit() - 1;
        }
        if (newY < getCameraUpLimit()) {
            newY = getCameraUpLimit() + 1;
        }

        setY(newY);
        fireValueChangedEvent();
        return this;
    }

    public CanvasCamera move(double x, double y) {
        return moveX(x)
                .moveY(y);
    }

    @Override
    public Rectangle2D getSeenRectangle() {

        double rx = getX() - getEffectiveWidth() / 2;
        double ry = getY() - getEffectiveHeight() / 2;

        double rwidth = getEffectiveWidth();
        double rheight = getEffectiveHeight();
        return new Rectangle2D(rx, ry, rwidth, rheight);
    }

    public CanvasCamera accordToSpace(Rectangle2D space) {

        if (getEffectiveWidth() > space.getWidth()) {

            setX(space.getWidth() / 2);

        }
        if (getEffectiveHeight() > space.getHeight()) {

            setY(space.getHeight() / 2);

        }

        return this;
    }

    public void accordToSpace() {
        accordToSpace(imageSpace);
    }


    @Override
    public Point2D getPositionOnImage(Point2D point) {
        final double mouseX = point.getX();
        final double mouseY = point.getY();
        
        final double dx = ((mouseX) - (getWidth()/2))/getZoom();
        final double dy = ((mouseY) - (getHeight()/2))/getZoom();
        
        
        return new Point2D(getX()+dx,getY()+dy);
    }
    
    @Override
    public Point2D getPositionOnCamera(Point2D point) {
        
        Rectangle2D r = getSeenRectangle();
        
       double x,y;
        
        //double x = (point.getX() - getX()) * zoom + (width/2);
        x = (point.getX()-r.getMinX())*zoom;
        y = (point.getY()-r.getMinY())*zoom;
       
       // double y = (point.getY() - getY()) * zoom + (height/2);
        
        return new Point2D(x,y);
        
    }
    
     public boolean isVisibleOnCamera(Point2D p) {
        
        return p.getX() >= 0 && p.getX() <= width && p.getY() >= 0  && p.getY() <= height;
        
    }
    


   
    @Override
    public void addListener(Consumer<CanvasCamera> listener) {
        listeners.add(listener);
    }
    public void removeListener(Consumer<CanvasCamera> listener) {
        listeners.remove(listener);
    }
    
    public void fireValueChangedEvent() {
        
        for(Consumer<CanvasCamera> listener : listeners) {
            listener.accept(this);
        }
        
    }
    
    List<Consumer<CanvasCamera>> listeners = new ArrayList<>();

   
}
