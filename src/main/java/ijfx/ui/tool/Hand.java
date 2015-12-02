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
package ijfx.ui.tool;

import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import ijfx.ui.canvas.FxImageCanvas;
import ijfx.ui.canvas.utils.CanvasCamera;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = FxTool.class)
public class Hand extends AbstractPathTool {

    Point2D previousPoint;
    
    @Override
    public void beforeDrawing(FxPath path) {
        previousPoint = path.getPathOnScreen().get(0);
    
    }

    @Override
    public void duringDrawing(FxPath fxPath) {

        Point2D lastPoint = FxPath.getLast(fxPath.getPathOnScreen());

        if (previousPoint == null) {
         
            previousPoint = new Point2D(lastPoint.getX(), lastPoint.getY());

        }

        CanvasCamera camera = getCanvas().getCamera();

        double dx = lastPoint.getX() - previousPoint.getX();
        double dy = lastPoint.getY() - previousPoint.getY();

        dx /= camera.getZoom();
        dy /= camera.getZoom();

        camera.move(-dx, -dy);
        previousPoint = lastPoint;
        getCanvas().repaint();

    }

    @Override
    public void afterDrawing(FxPath path) {
        previousPoint = null;
    }

    @Override
    public void onClick(MouseEvent event) {
    }

    @Override
    public Node getIcon() {

        return GlyphsDude.createIcon(FontAwesomeIcon.HAND_ALT_UP);

    }

}
