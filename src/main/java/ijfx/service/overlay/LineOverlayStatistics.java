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
package ijfx.service.overlay;

import javafx.geometry.Point2D;
import javafx.scene.shape.Polygon;
import net.imagej.display.ImageDisplay;
import net.imagej.overlay.LineOverlay;
import net.imagej.overlay.Overlay;
import org.scijava.Context;

/**
 *
 * @author Pierre BONNEAU
 */
public class LineOverlayStatistics extends AbstractOverlayStatistics{
    
    public LineOverlayStatistics(ImageDisplay display, Overlay overlay, Context context){
        super(display, overlay, context);
        
        super.area = setArea();
        super.minimumBoundingRectangle = setMinimumBoundingRectangle();
        super.centerOfGravity = setCenterOfGravity();
        super.feretDiameter = setFeretDiameter();
        super.minFeretDiameter = setMinFeretDiameter();
        super.longSideMBR = setLongSideMBR();
        super.shortSideMBR = setShortSideMBR();
        super.aspectRatio = super.setAspectRatio();
        super.convexity = setConvexity();
        super.solidity = setSolidity();
        super.circularity = setCircularity();
        super.thinnesRatio = setThinnesRatio();
    }
    
    
    public double setArea(){
        return 0.0;
    }

    
    public Polygon setMinimumBoundingRectangle(){
        return null;
    }
    
    public Point2D setCenterOfGravity(){
        
        LineOverlay overlay = (LineOverlay) getOverlay();
        
        double xstart = overlay.getLineStart(0);
        double ystart = overlay.getLineStart(1);
        double xend = overlay.getLineEnd(0);
        double yend = overlay.getLineEnd(1);

        double x = Math.abs((xend + xstart)/2);
        double y = Math.abs((yend + ystart)/2);


        return new Point2D(x, y);
    }
    
    
    public double setFeretDiameter(){
        
        LineOverlay overlay = (LineOverlay) getOverlay();
        
        double xstart = overlay.getLineStart(0);
        double ystart = overlay.getLineStart(1);
        double xend = overlay.getLineEnd(0);
        double yend = overlay.getLineEnd(1);

        double dx = xend - xstart;
        double dy = yend - ystart;

        return Math.sqrt(dx*dx + dy*dy);
    }
    
    public double setMinFeretDiameter(){
        return 0.0;
    }
    
    
    public double setLongSideMBR(){
        return 0.0;
    }
    
    
    public double setShortSideMBR(){
        return 0.0;
    }
    
    
    public double setConvexity(){
        return 0.0;
    }
    
    
    public double setSolidity(){
        return 0.0;
    }
    
    
    public double setCircularity(){
        return 0.0;
    }
    
    
    public double setThinnesRatio(){
        return 0.0;
    }
}
