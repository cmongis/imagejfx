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
import java.util.Iterator;
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
    
    private List<List<List<Double>>> profiles;

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
        
        RandomAccess<RealType<?>> randomAccess = dataset.randomAccess();
        
        for(Point2D c : centers){
            double cX = c.getX();
            double cY = c.getY();
            
            SearchArea area = new MidPointCircle(cX, cY, (int)lenght);
            /*Alternative area shape to look for possible membrane patterns*/
//            SearchArea area = new SquareFrame(cX, cY, (int)lenght);
            area.setAllPossibleSegments();
            
            for(int i = 0; i < area.getSegmentsSet().size(); i++){
                
                List<int[]> segment = area.getSegmentsSet().get(i);
                Iterator<int[]> it = segment.iterator();
                
                /*
                Variable point contains all the caracteristics of a point in a sequence.
                Thus we can still have several features for each element of the sequence, as so many inputs in a neural network.
                For example, we just consider here the intensity of the point, but we could decide to add its coordinates.
                */
                List<Double> point = new ArrayList<>();
                
                List<List<Double>> profile = new ArrayList<>();
                
                while(it.hasNext()){
                    int[] xy = it.next();
                    randomAccess.setPosition(xy[0], 0);
                    randomAccess.setPosition(xy[1], 1);
                    Double pixelValue = new Double(randomAccess.get().getRealDouble());
                    
                    point.add(pixelValue);
                    
                    profile.add(point);
                }
                profiles.add(profile);
            }
        }
    }
    
    
    @Override
    public List<List<List<Double>>> getProfiles(){
        return this.profiles;
    }
}
