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

import ijfx.ui.canvas.utils.ViewPort;
import ijfx.ui.utils.Point2DUtils;
import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import net.imagej.event.OverlayUpdatedEvent;
import net.imagej.overlay.LineOverlay;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class LineHelper {

    private final LineOverlay overlay;

    private final Property<Point2D> lineStartProperty = new SimpleObjectProperty();
    private final Property<Point2D> lineEndProperty = new SimpleObjectProperty();

    @Parameter
    EventService eventService;

    public LineHelper(LineOverlay overlay) {
        this.overlay = overlay;
        
        lineStartProperty.setValue(getLineStart(overlay));
        lineEndProperty.setValue(getLineEnd(overlay));
        
        lineStartProperty.addListener(this::onLineStartChange);
        lineEndProperty.addListener(this::onLineEndChange);
        
        
        
        
    }

    private void onLineStartChange(Observable obs, Point2D oldValue, Point2D newValue) {
        overlay.setLineStart(Point2DUtils.asArray(newValue));
        fireChangeEvent();
    }

    private void onLineEndChange(Observable obs, Point2D oldValue, Point2D newValue) {
        if(newValue == null) return;
        overlay.setLineEnd(Point2DUtils.asArray(newValue));
        fireChangeEvent();
    }

    public void setLineStart(Point2D lineStart) {
        lineStartProperty.setValue(lineStart);

    }

    public void setLineEnd(Point2D lineEnd) {
        lineStartProperty.setValue(lineEnd);
    }
    
    public Point2D getLineStart() {
        return lineStartProperty.getValue();
    }
    
    public Point2D getLineEnd() {
        return lineEndProperty.getValue();
    }

    private void fireChangeEvent() {
        if (eventService != null) {
            System.out.println("Firing change...");
            eventService.publishLater(new OverlayUpdatedEvent(overlay));
        }
    }

    
    public static Point2D getLineStart(LineOverlay overlay) {
        double x = overlay.getLineStart(0);
        double y = overlay.getLineStart(1);
       
        return new Point2D(x,y);
    }
    
    public static Point2D getLineEnd(LineOverlay overlay) {
        double x = overlay.getLineEnd(0);
        double y = overlay.getLineEnd(1);
        return new Point2D(x,y);
    }
    
    
    public Point2D getStartOnScreen(ViewPort viewport) {
        return viewport.getPositionOnCamera(getLineStart());
    }
    
    public Point2D getEndOnScreen(ViewPort viewport) {
        return viewport.getPositionOnCamera(getLineEnd());
    }
    
    public Property<Point2D> lineStartProperty() {
        return lineStartProperty;
    }
    
    public Property<Point2D> lineEndProperty() {
        return lineEndProperty;
    }
    
}
