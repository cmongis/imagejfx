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
package ijfx.ui.tool.overlay;

import ijfx.ui.canvas.utils.CanvasCamera;
import ijfx.ui.tool.FxPath;
import javafx.beans.property.DoubleProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import net.imagej.overlay.Overlay;

/**
 *
 * @author cyril
 */
public interface OverlayRepresentation<O extends Overlay> {
    
    
    //public void setCa
    
   // public ObservableList<Node> getRepresentation(CanvasCamera camera);
    
    public ObservableList<Node> getControllers();
   
    
    
    
    //public void updateFromControllers(FxPath fxPath);
    
    public void update(O overlay);
    public void updateFrom(O overlay);
    
    public DoubleProperty zoomProperty();
    
   
    
}
