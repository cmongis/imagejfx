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

import java.beans.PropertyDescriptor;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.Rectangle;
import net.imagej.overlay.Overlay;
import net.imagej.overlay.RectangleOverlay;
import org.controlsfx.property.BeanProperty;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class RectangleOverlayEditor implements OverlayEditor{

    Group group = new Group();
    
    Rectangle rectangle = new Rectangle();
    
    
    
    RectangleOverlay overlay = new RectangleOverlay();
    
    OverlayDimensionalProperty xOrigin = new OverlayOriginProperty();
    OverlayDimensionalProperty yOrigin = new OverlayOriginProperty();
    OverlayDimensionalProperty xExtent = new OverlayExtentProperty();
    OverlayDimensionalProperty yExtent = new OverlayExtentProperty(); 
    ObjectProperty<Overlay> overlayProperty = new SimpleObjectProperty<Overlay>();
    
    public RectangleOverlayEditor() {
        
        // setting the dimensions
        xOrigin.setD(0);
        yOrigin.setD(1);
        xExtent.setD(0);
        yExtent.setD(1);
        
        // binding the properties
        xOrigin.overlayProperty().bind(overlayProperty);
        yOrigin.overlayProperty().bind(overlayProperty);
        xExtent.overlayProperty().bind(overlayProperty);
        yExtent.overlayProperty().bind(overlayProperty);
        
        rectangle.xProperty().bindBidirectional(xOrigin);
        rectangle.yProperty().bindBidirectional(yOrigin);
        rectangle.widthProperty().bindBidirectional(xExtent);
        rectangle.heightProperty().bindBidirectional(xOrigin);
        
        
        
    }
    
    @Override
    public Node getNode() {
        return group;
    }

    @Override
    public boolean canEditOverlay(Overlay o) {
        return o instanceof RectangleOverlay;
    }
    
    public void setOverlay(Overlay o) {
        
        overlayProperty.set(o);
        
    }
    
    public void synchornizeRectangle() {
        
        
        
    }

    @Override
    public Property<Double> zoomProperty() {
        return null;
    }
    
    private class  OverlayDimensionalProperty<T extends Overlay> extends DoublePropertyBase{
        protected Property<T> overlay = new SimpleObjectProperty<>();
        int d=0;

        
        
        public OverlayDimensionalProperty() {
            super();
        }
        public OverlayDimensionalProperty(T overlay, int d) {
            this();
            setOverlay(overlay);
            setD(d);
            
        }
        @Override
        public Object getBean() {
            return overlay;
        }

        @Override
        public String getName() {
            return "x";
        }
        
        public void setD(int d) {
            this.d = d;
        }
        public void setOverlay(T t) {
            overlay.setValue(t);
        }
        
        public T getOverlay() {
            return overlay.getValue();
        }
        
        public Property<T> overlayProperty() {
            return overlay;
        }
    }
    
    private class OverlayOriginProperty extends OverlayDimensionalProperty<RectangleOverlay> {
        
        
        
        @Override
        public void set(double origin) {
           
            getOverlay().setOrigin(origin, d);
        }
        
        @Override
        public double get() {
            return getOverlay().getOrigin(d);
        }
    }
    
    private class OverlayExtentProperty extends OverlayDimensionalProperty<RectangleOverlay> {
        @Override
        public void set(double origin) {
            getOverlay().setExtent(origin, d);
        }
        
        @Override
        public double get() {
            return getOverlay().getExtent(d);
        }
    }
    
    
}
