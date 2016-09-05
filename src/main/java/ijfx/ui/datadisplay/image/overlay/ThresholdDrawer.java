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
import ijfx.ui.datadisplay.image.OverlayViewConfiguration;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
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

    WritableImage image;

    int width;

    int height;

    public void update(OverlayViewConfiguration<ThresholdOverlay> viewConfig, ViewPort viewport, Canvas canvas) {

        ThresholdOverlay overlay = viewConfig.getOverlay();
        
        if (image == null) {
            width = new Double(viewport.getRealImageWidth()).intValue();
            height = new Double(viewport.getRealImageHeight()).intValue();
            image = new WritableImage(width, height);
        }

        Rectangle2D r = viewport.getSeenRectangle();

        long[] point = new long[2];
       
        for (int x = 0; x != width; x++) {
            for (int y = 0; y != height; y++) {
                point[0] = x;
                point[1] = y;
                if (overlay.classify(point) == 0) {
                    image.getPixelWriter().setColor(x, y, Color.RED);
                } else {
                    image.getPixelWriter().setColor(x, y, Color.TRANSPARENT);
                }
            }
        }

        

        GraphicsContext graphicsContext2D = canvas.getGraphicsContext2D();
       

        graphicsContext2D.setFill(Color.TRANSPARENT);
        graphicsContext2D.fill();

        double sx, sy, sw, sh;

        sx = viewport.getSeenRectangle().getMinX();
        sy = viewport.getSeenRectangle().getMinY();
        sw = viewport.getSeenRectangle().getWidth();
        sh = viewport.getSeenRectangle().getHeight();

        graphicsContext2D.drawImage(image, sx, sy, sw, sh, 0, 0, canvas.getWidth(), canvas.getHeight());

    }

    public boolean canHandle(Class<?> t) {
        return t ==  ThresholdOverlay.class;
    }

    @Override
    public boolean isOverlayOnViewPort(Overlay o, ViewPort p) {
        return true;
    }

    @Override
    public boolean isOnOverlay(ThresholdOverlay overlay, ViewPort viewport, double xOnImage, double yOnImage) {
        
        long x = Math.round(xOnImage);
        long y = Math.round(yOnImage);
   
        return overlay.classify(new long[] {x,y}) == 0;
    }
    
}
