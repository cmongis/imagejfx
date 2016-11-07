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

import ijfx.ui.datadisplay.image.OverlayViewConfiguration;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import net.imagej.display.OverlayView;
import net.imagej.overlay.Overlay;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class DefaultOverlayViewConfiguration<T extends Overlay> implements OverlayViewConfiguration{
    
    
    private final T overlay;
    
    private final OverlayView view;
    
    public DefaultOverlayViewConfiguration(OverlayView view, T overlay) {
        this.overlay = overlay;
        this.view = view;
    }

    @Override
    public boolean isSelected() {
        return view.isSelected();
        
    }

    @Override
    public Paint getStrokeColor() {
       return OverlayDrawer.toFxColor(overlay.getLineColor());
    }

    @Override
    public T getOverlay() {
        return overlay;
    }

    @Override
    public double getStrokeWidth() {
        if(view. isSelected() == false) return 1d;
        else return 2.0;
        //return overlay.getLineWidth();
    }

    @Override
    public Paint getFillCollor() {
        if(view.isSelected() == false) return Color.TRANSPARENT;
        return OverlayDrawer.toFxColor(overlay.getFillColor());
    }
    
    
    
}
