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
import javafx.beans.Observable;
import javafx.geometry.Point2D;
import net.imagej.overlay.Overlay;
import net.imagej.overlay.PointOverlay;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Cyril MONGIS, 2016
 */
@Plugin(type=OverlayModifier.class)
public class PointModifier implements OverlayModifier<PointOverlay>{

    List<MoveablePoint> points;
    
    PointOverlay overlay;
    
    @Override
    public List<MoveablePoint> getModifiers(ViewPort viewport, PointOverlay overlay) {
        
        if(points == null) {
            points = new ArrayList<>();
            
            MoveablePoint mp = new MoveablePoint(viewport);
            
            this.overlay = overlay;
            
            mp.positionOnImageProperty().addListener(this::onPositionOnImageChanged);
            
            points.add(mp);
            
            mp.placeOnScreen(viewport.getPositionOnCamera(PointOverlayHelper.getOverlayPosition(overlay)));
            
        }
        
        return points;
        
    }

    public void onPositionOnImageChanged(Observable obs, Point2D before, Point2D after) {
        
        PointOverlayHelper.setOverlayPosition(overlay, after);
        
    }
    
    @Override
    public boolean canHandle(Overlay t) {
        return t instanceof PointOverlay;
    }
    
}
