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
package ijfx.ui.tool.overlay;

import ijfx.ui.canvas.utils.CanvasCamera;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 *
 * @author cyril
 */
public class MoveablePoint extends Rectangle{
    
    
    CanvasCamera camera;
    
    private Property<Point2D> positionOnScreen = new SimpleObjectProperty<>();
    private Property<Point2D> positionOnImage = new SimpleObjectProperty<>();
    
    
    
    public MoveablePoint() {
        super();
        
        
        setFill(Color.YELLOW);
        
        translateXProperty().bind(Bindings.createDoubleBinding(this::calculateTranslateX,widthProperty()));
        translateYProperty().bind(Bindings.createDoubleBinding(this::calculateTranslateY, heightProperty()));
        setWidth(9);
        setHeight(9);
        
        setOnMouseDragged(this::onMouseDragged);
        positionOnScreen.addListener(this::onPositionOnScreenChange);
    }
    
    public MoveablePoint(CanvasCamera camera) {
        this();
        this.camera = camera;
        camera.addListener(this::onCameraChange);
    }
    
    public void onCameraChange(CanvasCamera camera) {
        
        System.out.println("######\nSeen rectangle :");
        System.out.println(camera.getSeenRectangle());
        
        Rectangle2D r = camera.getSeenRectangle();
        
        
        Point2D newPosition = camera.getPositionOnCamera(positionOnImage.getValue());
        System.out.println("new position");
        System.out.println(newPosition);
        System.out.println("position on image");
        System.out.println(positionOnImage.getValue());
        setVisible(camera.isVisibleOnCamera(newPosition));
        
        
        if(isVisible()) {
             setX(newPosition.getX());
             setY(newPosition.getY());
        }
        
    }
    
    // place the points without alerting the listeners
    public void placeOnScreen(Point2D positionOnScreen) {
        setX(positionOnScreen.getX());
        setY(positionOnScreen.getY());
    }
    
    public void onPositionOnScreenChange(Observable value, Point2D oldValue, Point2D newValue) {
        if(camera != null) positionOnImage.setValue(camera.getPositionOnImage(positionOnScreen.getValue()));
         System.out.println(String.format("Point (%.0f x %.0f)",getX(),getY()));
    }
    
    public Double calculateTranslateX() {
        return  -getWidth()/2;
    }
    
    public Double calculateTranslateY() {
        return -getHeight()/2;
    }
    
    private void onMouseDragged(MouseEvent event) {
        setX(event.getX());
        setY(event.getY());
        
        positionOnScreen.setValue(new Point2D(getX(),getY()));
        
         
         System.out.println(positionOnImage.getValue());
    }
   
    public Property<Point2D> positionOnScreenProperty() {
        return positionOnScreen;
    }
    public Property<Point2D> positionOnImagePropety() {
        return positionOnImage;
    }
    
    
    
    
    
}
