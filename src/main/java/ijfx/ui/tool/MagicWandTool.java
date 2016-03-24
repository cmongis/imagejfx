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
import ijfx.plugins.commands.MagicWand;
import java.util.HashMap;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Cyril MONGIS, 2015
 */

public class MagicWandTool extends AbstractPathTool{

    
    @Parameter
    CommandService commandService;
    
    @Override
    public void beforeDrawing(FxPath path) {
    }

    @Override
    public void duringDrawing(FxPath fxPath) {
    }

    @Override
    public void afterDrawing(FxPath path) {
    }

    @Override
    public void onClick(MouseEvent event) {
        
        if(event.getButton() != MouseButton.PRIMARY) return;
        
        HashMap<String,Object> parameters = new HashMap<>();
        
        Point2D xyOnImage = getCanvas().getPositionOnImage(event.getX(), event.getY());
        
        Double x = xyOnImage.getX();
        Double y = xyOnImage.getY();
        
        
        parameters.put("x", x);
        parameters.put("y",y);
        
        commandService.run(MagicWand.class, true, parameters);
        
        
    }

    @Override
    public Node getIcon() {
        return GlyphsDude.createIcon(FontAwesomeIcon.MAGIC);
    }

   
   
    
}
