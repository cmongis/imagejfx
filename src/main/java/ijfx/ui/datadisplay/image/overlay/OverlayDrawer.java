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
import javafx.scene.Node;
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
public interface OverlayDrawer<T extends Overlay> extends ClassHandler<Overlay> {
    
   
    
    // returns a node updated according to the overlay parameter
    // with size and position depending on the viewport
    public Node update(T overlay, ViewPort viewport);

    public static Color toFxColor(ColorRGB colorRGB) {
        double red = 1.0 * colorRGB.getRed() / 255;
        double green = 1.0 * colorRGB.getGreen() / 255;
        double blue = 1.0 * colorRGB.getBlue() / 255;
        double alpha = 1.0 * colorRGB.getAlpha() / 255;
        //return new fillColor
        return new Color(red, green, blue, alpha);
    }

    public static void color(Overlay overlay, Shape shape) {
        ColorRGB fillColor = overlay.getFillColor();
        Color fxFillColor = toFxColor(fillColor).deriveColor(0.0, 0, 0, 0.1);
        shape.setFill(fxFillColor);
        shape.setStroke(toFxColor(overlay.getLineColor()));
        shape.setStrokeWidth(2.0);
        shape.setOpacity(1.0);
    }
    
}