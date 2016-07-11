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
import net.imagej.ImageJService;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.OverlayService;
import net.imagej.overlay.Overlay;
import net.imglib2.RandomAccess;
import net.imglib2.type.numeric.RealType;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import org.scijava.ui.UIService;

/**
 *
 * @author Pierre BONNEAU
 */
@Plugin(type = Service.class)
public class SegmentationService extends AbstractService implements ImageJService{

    private final double COLOR = 1.0;
    public final int membrane_width = 6;
    
    private Dataset labels;
    private List<int[]> confirmationSet;
    
    @Parameter
    OverlayService overlayService;
    
    @Parameter
    OverlayStatService overlayStatService;
    
    @Parameter
    OverlayDrawingService overlayDrawingService;
    
    @Parameter
    ImageDisplayService imageDisplayService;
    
    @Parameter
    ImagePlaneService imagePlaneService;
    
    @Parameter
    Context context;
    
    @Parameter
    UIService uIService;
    
    public ProfilesSet generateTrainingSet(){
        System.out.println("START : Generate profiles for training dataset");
        ArrayList<Point2D> centers = new ArrayList<>();
        double maxDiameter = 0.0;
        
        ImageDisplay display = imageDisplayService.getActiveImageDisplay();
        Dataset dataset = imageDisplayService.getActiveDataset(display);
        labels = imagePlaneService.createEmptyPlaneDataset(dataset);
        List<Overlay> overlays = overlayService.getOverlays(display);
        
        for(Overlay o : overlays){
            OverlayStatistics stats = overlayStatService.getOverlayStatistics(o);
            centers.add(stats.getCenterOfGravity());
            if(stats.getFeretDiameter() > maxDiameter)
                maxDiameter = stats.getFeretDiameter();
            overlayDrawingService.drawOverlay(o, OverlayDrawingService.OUTLINER, labels, COLOR);
        }
        System.out.printf("\tData retrieved from %d overlays\n", overlays.size());
        
        ProfilesSet profiles = new DefaultProfilesSet(centers, (int)maxDiameter, context);
        System.out.printf("END : %d profiles computed\n", profiles.getProfiles().size());
        
        return profiles;
    }
    
    public void generateConfirmationSet(ProfilesSet trainingSet){
        
        confirmationSet = new ArrayList<>();
        RandomAccess<RealType<?>> randomAccess = this.labels.randomAccess();
        
        for(int i =  0; i < trainingSet.getProfiles().size(); i++){
            List<int[]> profile = trainingSet.getProfiles().get(i);
            
            for(int j = 0; j < profile.size(); j++){
                int[] point = profile.get(j);
                randomAccess.setPosition(point[0], 0);
                randomAccess.setPosition(point[1], 1);
                if(randomAccess.get().getRealDouble() == COLOR){
                    int[] labels = new int[profile.size()];
                    
                    for (int l = 0; l < labels.length; l++){
                        labels[l] = -1;
                    }
                    
                    for(int k = j-membrane_width/2; k <= j+membrane_width/2; k++){
                        if(k < 0 ||  k > labels.length)
                            continue;
                        else
                            labels[k] = 1;
                    }
                    
                    confirmationSet.add(labels);
                    break;
                }
            }
        }
    }
    
    public ProfilesSet generateTestSet(){
        return null;
    }
    
    public void saveParameters(){
        
    }
    
    public void loadParameter(){
        
    }
    
    public void clearAll(){
        
    }
}
