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
import ijfx.ui.datadisplay.image.overlay.OverlayDrawer;
import ijfx.ui.plugin.panel.OverlayOptionsService;
import java.util.List;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import net.imagej.display.OverlayService;
import net.imagej.overlay.RectangleOverlay;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = FxTool.class,priority=99)

public class RectangleTool extends AbstractPathTool {

    @Parameter
    OverlayService overlayService;
    
    @Parameter
    OverlayOptionsService overlayOptionsService;

    public void duringDrawing(FxPath fxPath) {


        List<Point2D> points = fxPath.getPathOnScreen();

        if (points.size() < 2) {
            return;
        }

        Rectangle2D r = FxPath.toRectangle(points);
        
        getCanvas().repaint();

        getCanvas().getGraphicsContext2D().setStroke(OverlayDrawer.toFxColor(overlayOptionsService.colorProperty().getValue()));
        getCanvas().getGraphicsContext2D().setLineWidth(overlayOptionsService.widthProperty().getValue());

        getCanvas().getGraphicsContext2D().strokeRect(r.getMinX(), r.getMinY(), r.getWidth(), r.getHeight());
        
        
        
        
    }

    @Override
    public void beforeDrawing(FxPath path) {
    }

    @Override
    public void afterDrawing(FxPath path) {
        
        RectangleOverlay rectangleOverlay = new RectangleOverlay(overlayService.getContext());
        Rectangle2D r = FxPath.toRectangle(path.getPathOnImage());
        rectangleOverlay.setOrigin(r.getMinX() , 0);
        rectangleOverlay.setExtent(r.getWidth(),0);
        rectangleOverlay.setOrigin(r.getMinY(), 1);
        rectangleOverlay.setExtent(r.getHeight(), 1);
        rectangleOverlay.setLineWidth(overlayOptionsService.widthProperty().getValue());
        rectangleOverlay.setLineColor(overlayOptionsService.colorProperty().getValue());
        rectangleOverlay.setFillColor(overlayOptionsService.colorProperty().getValue());
        addOverlays(rectangleOverlay);
    }

    @Override
    public void onClick(MouseEvent event) {
        getCanvas().repaint();
    }

    @Override
    public Node getIcon() {
        return GlyphsDude.createIcon(FontAwesomeIcon.SQUARE_ALT);
    }

}
