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
import ijfx.ui.utils.Point2DUtils;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import net.imagej.event.OverlayUpdatedEvent;
import net.imagej.overlay.LineOverlay;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = FxTool.class)
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

            Point2D begin = path.getPathOnImage().get(0);
            Point2D end = path.getPathOnImage().get(path.size() - 1);
            
            if (currentOverlay == null) {

                currentOverlay = new LineOverlay(context);
                currentOverlay.setLineStart(Point2DUtils.asArray(begin));
                addOverlays(currentOverlay);
            }
          
            currentOverlay.setLineEnd(Point2DUtils.asArray(end));
           eventService.publishLater(new OverlayUpdatedEvent(currentOverlay));
        }
    }

    @Override
    public void afterDrawing(FxPath path) {
        currentOverlay = null;
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
