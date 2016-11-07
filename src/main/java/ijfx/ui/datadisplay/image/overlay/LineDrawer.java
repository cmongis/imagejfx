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
import java.util.concurrent.Callable;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import net.imagej.overlay.LineOverlay;
import net.imagej.overlay.Overlay;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Cyril MONGIS, 2016
 */
@Plugin(type = OverlayDrawer.class)
public class LineDrawer implements OverlayDrawer<LineOverlay> {

    //Line line;

    LineHelper helper;

    @Override
    public boolean canHandle(Class<?> t) {
        return t == LineOverlay.class;
    }

    @Override
    public void update(OverlayViewConfiguration<LineOverlay> viewConfig, ViewPort viewport, Canvas canvas) {

        LineOverlay overlay = viewConfig.getOverlay();
        
        
        helper = new LineHelper(overlay);
        
        //if (line == null) {

        //    line = new Line();

        //}
        

        Point2D startOnScreen = viewport.getPositionOnCamera(helper.getLineStart());
        Point2D endOnScreen = viewport.getPositionOnCamera(helper.getLineEnd());
        
        GraphicsContext graphicsContext2D = canvas.getGraphicsContext2D();
        
        int ox = getAsInt(startOnScreen::getX);
        int oy = getAsInt(startOnScreen::getY);
        int dx = getAsInt(endOnScreen::getX);
        int dy = getAsInt(endOnScreen::getY);
        
        graphicsContext2D.setStroke(viewConfig.getStrokeColor());
        
        graphicsContext2D.strokeLine(ox, oy, dx, dy);
      
        
    }
    
    public int getAsInt(Callable<Double> d) {
        try {
            return new Double(d.call()).intValue();
        } catch (Exception ex) {
           return 0;
        }
    }

}
