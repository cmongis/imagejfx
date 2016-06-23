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

import ijfx.service.overlay.OverlayDrawingService;
import ijfx.ui.canvas.utils.ViewPort;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import net.imagej.overlay.Overlay;
import net.imagej.overlay.ThresholdOverlay;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = OverlayDrawer.class)
public class ThresholdDrawer implements OverlayDrawer<ThresholdOverlay> {

    Canvas canvas;

    @Parameter
    OverlayDrawingService drawingService;
    
    @Override
    public Node update(ThresholdOverlay overlay, ViewPort viewport) {

        if (canvas == null) {
            canvas = new Canvas(viewport.getEffectiveWidth(), viewport.getEffectiveHeight());

        }
        
        Rectangle2D r = viewport.getSeenRectangle();
        
       
    
        
        System.out.println("I'm doing something !");
        GraphicsContext graphicsContext2D = canvas.getGraphicsContext2D();
        graphicsContext2D.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
         for(double x = r.getMinX();x!=r.getMaxX();x++) {
            for(double y = r.getMinY();y!=r.getMaxY();y++) {
                if(overlay.getRegionOfInterest().contains(new double[]{x,y})) {
                    graphicsContext2D.setFill(Color.RED);
                    graphicsContext2D.fillRect(x, y, 1, 1);
                    System.out.println(x);
                }
                else {
                    graphicsContext2D.setFill(Color.YELLOW);
                }
                
            }
        }
     
        
        return canvas;

    }

    @Override
    public boolean canHandle(Overlay t) {
        return t instanceof ThresholdOverlay;
    }

}
