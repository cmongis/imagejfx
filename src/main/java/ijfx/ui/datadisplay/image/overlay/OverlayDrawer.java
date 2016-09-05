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
import ijfx.ui.datadisplay.image.OverlayViewConfiguration;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import net.imagej.overlay.Overlay;
import org.scijava.plugin.SciJavaPlugin;
import org.scijava.util.ColorRGB;

/**
 * The OverlayDrawer takes an Overlay and a ViewPort and returns a Node object depending
 * on the Overlay condition.
 * 
 * 
 * @author cyril
 */
public interface OverlayDrawer<T extends Overlay> extends SciJavaPlugin{
    

    
    // returns a node updated according to the overlay parameter
    // with size and position depending on the viewport
    public void update(OverlayViewConfiguration<T> overlay, ViewPort viewport, Canvas canvas);

    public boolean canHandle(Class<?> o);
    
    public static Color toFxColor(ColorRGB color) {
        return toFxColor(color,1.0);
    }
    
    public static Color toFxColor(ColorRGB colorRGB,double f) {
        double red = 1.0 * colorRGB.getRed() / 255;
        double green = 1.0 * colorRGB.getGreen() / 255;
        double blue = 1.0 * colorRGB.getBlue() / 255;
        double alpha = colorRGB.getAlpha() / 255 * f;
        //return new fillColor
        return new Color(red, green, blue, alpha);
    }
    
    public default boolean isOnOverlay(Overlay overlay, ViewPort viewport, double xOnImage, double yOnImage) {
        double x1 = overlay.getRegionOfInterest().realMin(0);
        double y1 = overlay.getRegionOfInterest().realMin(1);
        double x2 = overlay.getRegionOfInterest().realMax(0);
        double y2 = overlay.getRegionOfInterest().realMax(1);
        
        //System.out.println(String.format("(%.0f,%.0f), (%.0f,%.0f)", x1, y1, x2, y2));
        Rectangle2D r = new Rectangle2D(x1, y1, x2 - x1, y2 - y1);
        //System.out.println(String.format("contains  (%.0f,%.0f) ? : %b",x,y,r.contains(x,y)));
        return (r.contains(xOnImage, yOnImage));
        
        
    }
    
    
    public static Rectangle2D getOverlayBounds(Overlay overlay) {
         double x1 = overlay.getRegionOfInterest().realMin(0);
        double y1 = overlay.getRegionOfInterest().realMin(1);
        double x2 = overlay.getRegionOfInterest().realMax(0);
        double y2 = overlay.getRegionOfInterest().realMax(1);
        
        //System.out.println(String.format("(%.0f,%.0f), (%.0f,%.0f)", x1, y1, x2, y2));
        Rectangle2D r = new Rectangle2D(x1, y1, x2 - x1, y2 - y1);
        return r;
    }
    
    public default boolean isOverlayOnViewPort(Overlay overlay, ViewPort viewport) {
        return viewport.getSeenRectangle().contains(getOverlayBounds(overlay));
    }

    public static void color(Overlay overlay, Shape shape) {
        ColorRGB fillColor = overlay.getFillColor();
        Color fxFillColor = toFxColor(fillColor,0).deriveColor(1.0, 0, 0, 0.0);
        shape.setFill(fxFillColor);
        shape.setStroke(toFxColor(overlay.getLineColor(),1.0));
       
        shape.setStrokeWidth(overlay.getLineWidth());
        shape.setOpacity(1.0);
    }
    
}