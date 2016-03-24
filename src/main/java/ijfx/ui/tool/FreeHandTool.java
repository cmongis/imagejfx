/*
 * /*
 *     This file is part of ImageJ FX.
 *
 *     ImageJ FX is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     ImageJ FX is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with ImageJ FX.  If not, see <http://www.gnu.org/licenses/>. 
 *
 * 	Copyright 2015,2016 Cyril MONGIS, Michael Knop
 *
 */
package ijfx.ui.tool;

import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import net.imagej.overlay.PolygonOverlay;
import net.imglib2.RealPoint;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.util.ColorRGB;

/**
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = FxTool.class,priority=98)
public class FreeHandTool extends AbstractPathTool {

    
    
  // @Parameter
   //LegacyService legacyService;
    
    @Parameter
    Context context;
    
    @Override
    public void beforeDrawing(FxPath path) {
    }
    
    

    @Override
    public void duringDrawing(FxPath fxPath) {

        List<Point2D> points = fxPath.getPathOnScreen();

        double[] xList = FxPath.xList(points);
        double[] yList = FxPath.yList(points);

        getCanvas().repaint();
        getCanvas().getGraphicsContext2D().setStroke(Color.YELLOW);
        getCanvas().getGraphicsContext2D().setLineWidth(1.0);
        getCanvas().getGraphicsContext2D().strokePolygon(xList, yList, xList.length);

    }

    
    
    @Override
    public void afterDrawing(FxPath path) {
        
        
        if(path == null) return;
        ArrayList<Point2D> pathOnImage = path.getPathOnImage();
        
        
        PolygonOverlay overlay = new PolygonOverlay(context);
        
        overlay.setName("new overlay");
        overlay.setFillColor(new ColorRGB(244,14,14));
        overlay.setLineColor(new ColorRGB(0,0,244));


        
        for(int i = 0;i!=pathOnImage.size();i++) {
            Point2D p = pathOnImage.get(i);
            overlay.getRegionOfInterest().addVertex(i, new RealPoint(p.getX(),p.getY()));
        }

        overlay.rebuild();
        addOverlays(overlay);
        
        
    }
    
   

    @Override
    public void onClick(MouseEvent event) {
    }

    @Override
    public Node getIcon() {
        return GlyphsDude.createIcon(FontAwesomeIcon.PENCIL);
    }
    
    
    
    
}
