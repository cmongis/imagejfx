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
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.controlsfx.glyphfont.Glyph;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = FxTool.class)
public class PolygonTool extends AbstractPathTool {

    FxPath polygonPath = new FxPath();

    FxPath smoothPath = new FxPath();
    
    //Button button = new Button("", new FontAwesomeIconView(FontAwesomeIcon.STAR_ALT));
    
    
    
    @Override
    public void beforeDrawing(FxPath path) {

    }

    @Override
    public void duringDrawing(FxPath fxPath) {

    }

    @Override
    public void afterDrawing(FxPath path) {

    }

    public void updateSmoothPath() {
       
    }
    
    @Override
    public void onClick(MouseEvent event) {
        
        
        
        elongPath(event, polygonPath);
        
        if(event.getButton() == MouseButton.SECONDARY) {
            addOverlay(polygonPath);
            polygonPath = new FxPath();
            smoothPath = new FxPath();
            event.consume();
            return;
        }
        
        elongPath(event, polygonPath);
      
        updateSmoothPath();
        drawPath(polygonPath);
        drawPath(smoothPath);

        //getCanvas().repaint();

    }

    @Override
    public Node getIcon() {
        
        return GlyphsDude.createIcon(FontAwesomeIcon.STAR_ALT);
    }

    @Override
    public void onMouseMoved(MouseEvent event) {

        if (polygonPath.size() > 1) {
            polygonPath.removeLast();
            elongPath(event,polygonPath);
            drawPath(polygonPath);
        }
    }
    
    
    @Override
    public void onActivated() {
        polygonPath = new FxPath();
    }
}
