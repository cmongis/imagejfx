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
import javafx.scene.Node;
import net.imagej.overlay.Overlay;
import org.scijava.plugin.SciJavaPlugin;

/**
 * The OverlayDrawer takes an Overlay and a ViewPort and returns a Node object depending
 * on the Overlay condition.
 * 
 * 
 * @author cyril
 */
public interface OverlayDrawer<T extends Overlay> extends ClassHandler<T> {
    
    @Override
    public boolean canHandle(T t);
    
    // returns a node updated according to the overlay parameter
    // with size and position depending on the viewport
    public Node update(T overlay, ViewPort viewport);

}