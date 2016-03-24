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
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.shape.Line;
import net.imagej.overlay.LineOverlay;
import net.imagej.overlay.Overlay;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = OverlayDrawer.class)
public class LineDrawer implements OverlayDrawer<LineOverlay> {

    Line line;

    LineHelper helper;

    @Override
    public boolean canHandle(Overlay t) {
        return t instanceof LineOverlay;
    }

    @Override
    public Node update(LineOverlay overlay, ViewPort viewport) {

        if (line == null) {

            line = new Line();

        }
        helper = new LineHelper(overlay);

        Point2D startOnScreen = viewport.getPositionOnCamera(helper.getLineStart());
        Point2D endOnScreen = viewport.getPositionOnCamera(helper.getLineEnd());
        
        line.setStartX(startOnScreen.getX());
        line.setStartY(startOnScreen.getY());
        line.setEndX(endOnScreen.getX());
        line.setEndY(endOnScreen.getY());
        
        OverlayDrawer.color(overlay, line);
        
        return line;
        
    }

}
