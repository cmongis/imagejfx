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
import ijfx.ui.canvas.utils.ViewPort;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import net.imagej.event.OverlayUpdatedEvent;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class MoveablePoint extends Rectangle {

    ViewPort viewport;

    private Property<Point2D> positionOnScreen = new SimpleObjectProperty<>();
    private Property<Point2D> positionOnImage = new SimpleObjectProperty<>();

    public MoveablePoint() {
        super();

        setFill(Color.YELLOW);

        translateXProperty().bind(Bindings.createDoubleBinding(this::calculateTranslateX, widthProperty()));
        translateYProperty().bind(Bindings.createDoubleBinding(this::calculateTranslateY, heightProperty()));
        setWidth(9);
        setHeight(9);

        setOnMouseDragged(this::onMouseDragged);
        positionOnScreen.addListener(this::onPositionOnScreenChange);
    }

    public MoveablePoint(ViewPort camera) {
        this();
        this.viewport = camera;
        camera.addListener(this::onCameraChange);
    }

    public void onCameraChange(CanvasCamera camera) {

        Rectangle2D r = camera.getSeenRectangle();

        Point2D newPosition = camera.getPositionOnCamera(getPositionOnImage());

        setVisible(camera.isVisibleOnCamera(newPosition));

        if (isVisible()) {
            setX(newPosition.getX());
            setY(newPosition.getY());
        }

    }

    public Point2D getPositionOnImage() {

        return positionOnImage.getValue();
    }

    // place the points without alerting the listeners
    public void placeOnScreen(Point2D positionOnScreen) {
        setX(positionOnScreen.getX());
        setY(positionOnScreen.getY());
    }

    // set the position on the screen without alerting the
    // the observers
    public void setPositionSilently(Point2D positionOnScreen) {
        placeOnScreen(positionOnScreen);
    }

    public void onPositionOnScreenChange(Observable value, Point2D oldValue, Point2D newValue) {
        if (viewport != null) {
            positionOnImage.setValue(viewport.getPositionOnImage(positionOnScreen.getValue()));
        }

    }

    public Double calculateTranslateX() {
        return -getWidth() / 2;
    }

    public Double calculateTranslateY() {
        return -getHeight() / 2;
    }

    private void onMouseDragged(MouseEvent event) {
        setX(event.getX());
        setY(event.getY());
        positionOnScreen.setValue(new Point2D(getX(), getY()));
        event.consume();
    }

    public Property<Point2D> positionOnScreenProperty() {
        return positionOnScreen;
    }

    public Property<Point2D> positionOnImageProperty() {
        return positionOnImage;
    }

}
