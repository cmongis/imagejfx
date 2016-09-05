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
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import net.imagej.overlay.RectangleOverlay;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = OverlayDrawer.class)
public class RectangleDrawer implements OverlayDrawer<RectangleOverlay> {

    RectangleOverlayHelper helper;

    public void update(OverlayViewConfiguration<RectangleOverlay> viewConfig, ViewPort viewport, Canvas canvas) {

        RectangleOverlay overlay = viewConfig.getOverlay();
        
        GraphicsContext context2d = canvas.getGraphicsContext2D();
        
        helper = new RectangleOverlayHelper(overlay);
        Point2D a = helper.getMinEdge();
        Point2D b = helper.getMaxEdge();
        a = viewport.getPositionOnCamera(a);
        b = viewport.getPositionOnCamera(b);

        double x = a.getX();
        double y = a.getY();
        double w = b.getX() - a.getX();
        double h = b.getY() - a.getY();
        
        //viewConfig.configureContext(context2d);
        context2d.setFill(Color.YELLOW.deriveColor(1.0, 1.0, 1.0, 0.5));
        context2d.setStroke(Color.YELLOW);
        context2d.fillRect(x, y, w, h);
        context2d.rect(x, y, w, h);

    }

    public boolean canHandle(Class<?> t) {
        return t == RectangleOverlay.class;
    }

}
