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
package ijfx.ui.tool;

import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import ijfx.service.overlay.OverlaySelectedEvent;
import ijfx.ui.utils.Point2DUtils;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import net.imagej.event.OverlayUpdatedEvent;
import net.imagej.overlay.LineOverlay;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = FxTool.class,priority=99.5)
public class LineTool extends AbstractPathTool {

    Node icon = GlyphsDude.createIcon(FontAwesomeIcon.LONG_ARROW_LEFT);

    LineOverlay currentOverlay;

    @Parameter
    EventService eventService;

    public LineTool() {
        super();
        icon.setRotate(0.1);
    }

    @Override
    public void beforeDrawing(FxPath path) {
    }

    @Override
    public void duringDrawing(FxPath path) {
        if (path.size() >= 2) {

            Point2D begin = path.getPathOnScreen().get(0);
            Point2D end = path.getPathOnScreen().get(path.size() - 1);

            double[] xList = new double[2];
            double[] yList = new double[2];

            xList[0] = begin.getX();
            xList[1] = end.getX();
            yList[0] = begin.getY();
            yList[1] = end.getY();

            getCanvas().repaint();
            getCanvas().getGraphicsContext2D().setStroke(Color.YELLOW);
            getCanvas().getGraphicsContext2D().setLineWidth(1.0);
            getCanvas().getGraphicsContext2D().strokePolygon(xList, yList, xList.length);

        }
    }

    @Override
    public void afterDrawing(FxPath path) {

        Point2D begin = path.getPathOnImage().get(0);
        Point2D end = path.getPathOnImage().get(path.size() - 1);
        currentOverlay = new LineOverlay(context);
        currentOverlay.setLineStart(Point2DUtils.asArray(begin));
        currentOverlay.setLineEnd(Point2DUtils.asArray(end));
        addOverlays(currentOverlay);

        eventService.publishLater(new OverlayUpdatedEvent(currentOverlay));

    }

    protected void updateCurrentOverlay(FxPath path) {

    }

    @Override
    public void onClick(MouseEvent event) {

    }

    @Override
    public Node getIcon() {
        return icon;
    }

}
