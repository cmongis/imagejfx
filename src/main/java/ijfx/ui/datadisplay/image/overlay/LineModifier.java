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
import ijfx.ui.tool.overlay.MoveablePoint;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Point2D;
import net.imagej.overlay.LineOverlay;
import net.imagej.overlay.Overlay;
import org.scijava.Context;
import org.scijava.convert.ConvertService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = OverlayModifier.class)
public class LineModifier implements OverlayModifier<LineOverlay> {

    @Parameter
    Context context;

    LineHelper lineHelper;

    MoveablePoint a;
    MoveablePoint b;

    List<MoveablePoint> points = new ArrayList<>();

    @Parameter
    ConvertService convertService;
    
    @Override
    public List getModifiers(ViewPort viewport, LineOverlay overlay) {
        if (lineHelper == null) {
            lineHelper = new LineHelper(overlay);

            context.inject(lineHelper);
            a = new MoveablePoint(viewport);
            b = new MoveablePoint(viewport);
            
            
            
            Point2D startOnScreen = lineHelper.getStartOnScreen(viewport);
            Point2D endOnScreen = lineHelper.getEndOnScreen(viewport);

            a.setPositionSilently(startOnScreen);
            b.setPositionSilently(endOnScreen);

            lineHelper.lineStartProperty().bind(a.positionOnImageProperty());
            lineHelper.lineEndProperty().bind(b.positionOnImageProperty());
            
            points.add(a);
            points.add(b);
            
          
            
            
            
        }
        return points;
    }

    @Override
    public boolean canHandle(Overlay t) {
        return t instanceof LineOverlay;
    }

}
