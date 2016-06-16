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
import ijfx.service.overlay.OverlayStatistics;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Point2D;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.overlay.Overlay;
import net.imglib2.RandomAccess;
import net.imglib2.type.numeric.RealType;
import org.scijava.Context;
import org.scijava.ItemIO;
import org.scijava.display.DisplayService;
import org.scijava.plugin.Parameter;
import org.scijava.ui.UIService;

/**
 *
 * @author Pierre BONNEAU
 */
public class DefaultProfilesSet implements ProfilesSet{
    
    private final double COLOR = 1.0;
    private final int PATTERN_HALF_WIDTH = 3;
    private List<List<Double>> profiles;

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
        
    public DefaultProfilesSet(List<Overlay> overlays, Context context) {
        
        profiles = new ArrayList<>();
        ArrayList<Point2D> centers = new ArrayList<>();
        
        double maxDiameter = 0.0;
        double minDiameter = -1.0;

        context.inject(this);
        
        ImageDisplay display = imageDisplayService.getActiveImageDisplay();
        Dataset dataset = imageDisplayService.getActiveDataset(display);
        Dataset labels = imagePlaneService.createEmptyPlaneDataset(dataset);
        
        for(Overlay o : overlays){
            OverlayStatistics stats = overlayStatService.getOverlayStatistics(o);
            centers.add(stats.getCenterOfGravity());
            if(stats.getFeretDiameter() > maxDiameter)
                maxDiameter = stats.getFeretDiameter();
            if(minDiameter < 0)
                minDiameter = stats.getMinFeretDiameter();
            else if(stats.getMinFeretDiameter() < minDiameter)
                minDiameter = stats.getMinFeretDiameter();
            overlayDrawingService.drawOverlay(o, OverlayDrawingService.OUTLINER, labels, COLOR);
        }
        
        System.out.printf("Data retrieved from %d overlays\n", overlays.size());
        RandomAccess<RealType<?>> randomAccess = labels.randomAccess();
        
        for(Point2D c : centers){
            double cX = c.getX();
            double cY = c.getY();
            
            SearchArea area = new MidPointCircle(cX, cY, (int)maxDiameter);
//            SearchArea area = new SquaredFrame(cX, cY, (int)maxDiameter);
            area.setPossibleProfiles();
            
            for(int i = 0; i < area.getProfilesSet().size(); i++){
                List<int[]> segment = area.getProfilesSet().get(i);
                
                for(int j = 0; j < segment.size(); j++){
                    if(randomAccess.get().getRealDouble()!= COLOR){
                        List<Double> profile = new ArrayList<>();
                        
                        for(int k = j-PATTERN_HALF_WIDTH; k <= j+PATTERN_HALF_WIDTH; k++){
                            if(k>=segment.size() || k<0){
                                break;
                            }
                            randomAccess.setPosition(segment.get(k)[0], 0);
                            randomAccess.setPosition(segment.get(k)[1], 1);
                            profile.add(randomAccess.get().getRealDouble());
                        }
                        profiles.add(profile);
                        break;
                    }
                }
            }
        }
        uis.show(labels);
        System.out.printf("%d profiles have been computed\n", profiles.size());
    }    
    
    @Override
    public List<List<Double>> getProfiles() {
        return this.profiles;
    }
    
}
