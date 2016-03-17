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
import java.util.Set;
import net.imagej.overlay.Overlay;
import net.imagej.overlay.RectangleOverlay;

/**
 *
 * @author cyril
 */
public class RectangleModifier implements OverlayModifier<RectangleOverlay>{
    
    MoveablePoint a;
    MoveablePoint b;
    
    List<MoveablePoint> hashSet;

    RectangleOverlay overlay;
    
    RectangleOverlayHelper helper;
    
    @Override
    public boolean canHandle(Overlay o) {
        return o instanceof RectangleOverlay;
    }

    @Override
    public List<MoveablePoint> getModifiers(ViewPort viewport, RectangleOverlay overlay) {
        
    
        if(hashSet == null) {
            
            // initialixing
            hashSet = new ArrayList<>(2);
            hashSet.add(a);
            hashSet.add(b);
            
            // creating the helper 
            helper = new RectangleOverlayHelper(overlay);
            
            a.placeOnScreen(viewport.getPositionOnCamera(helper.getMinEdge()));
            b.placeOnScreen(viewport.getPositionOnCamera(helper.getMaxEdge()));
            
            
            
            a.positionOnScreenProperty().setValue(helper.getMinEdge());
            b.positionOnScreenProperty().setValue(helper.getMaxEdge());
            
            
            // change of A and B will update automatically the overlay via the helper
            // because each time the point is moved on the screen, the position
            // on the image is also updated
            helper.minEdgeProperty().bind(a.positionOnImagePropety());
            helper.maxEdgeProperty().bind(b.positionOnImagePropety());

        }
        
        return hashSet;
    
    }
    
    
}
