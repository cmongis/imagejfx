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
package ijfx.plugins.segmentation;

import ijfx.service.ImagePlaneService;
import ijfx.service.overlay.OverlayDrawingService;
import ijfx.service.overlay.OverlayStatService;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Point2D;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imglib2.RandomAccess;
import net.imglib2.type.numeric.RealType;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.ui.UIService;

/**
 *
 * @author Pierre BONNEAU
 */
public class DefaultProfilesSet implements ProfilesSet{
    
    private List<List<int[]>> profiles;

    @Parameter
    OverlayStatService overlayStatService;
    
    @Parameter
    OverlayDrawingService overlayDrawingService;
    
    @Parameter
    ImagePlaneService imagePlaneService;
    
    @Parameter
    ImageDisplayService imageDisplayService;
    
    @Parameter
    DatasetService datasetService;
    
    @Parameter
    UIService uis;
        
    public DefaultProfilesSet(List<Point2D> centers, int lenght, Context context) {
        
        profiles = new ArrayList<>();
        
        context.inject(this);
        
        ImageDisplay display = imageDisplayService.getActiveImageDisplay();
        Dataset dataset = imageDisplayService.getActiveDataset(display);
        Dataset labels = imagePlaneService.createEmptyPlaneDataset(dataset);
        
        RandomAccess<RealType<?>> randomAccess = labels.randomAccess();
        
        for(Point2D c : centers){
            double cX = c.getX();
            double cY = c.getY();
            
            SearchArea area = new MidPointCircle(cX, cY, (int)lenght);
//            SearchArea area = new SquaredFrame(cX, cY, (int)lenght);
            area.setAllPossibleProfiles();
            
            for(int i = 0; i < area.getProfilesSet().size(); i++){
                List<int[]> segment = area.getProfilesSet().get(i);
                profiles.add(segment);
            }
        }
    }
    
    
    @Override
    public List<List<int[]>> getProfiles(){
        return this.profiles;
    }
}
