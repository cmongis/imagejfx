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

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import net.imagej.overlay.Overlay;

/**
 *
 * @author cyril
 */
public abstract class OverlayRepresentationBase<O extends Overlay> implements OverlayRepresentation<O> {

    private final SimpleDoubleProperty zoomProperty =  new SimpleDoubleProperty(1.0);

    public DoubleProperty zoomProperty() {
        return zoomProperty;
    }
}


