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

import ijfx.plugins.segmentation.search_area.MidPointCircle;
import ijfx.plugins.segmentation.search_area.SearchArea;
import ijfx.service.ImagePlaneService;
import ijfx.service.overlay.OverlayDrawingService;
import ijfx.service.overlay.OverlayStatService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javafx.geometry.Point2D;
import net.imagej.Dataset;
import net.imagej.DatasetService;
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
    private List<int[]> masks;
    private int maxLenght;

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
        masks = new ArrayList<>();
        maxLenght = 0;
        
        context.inject(this);
        
        for(Point2D c : centers){
            double cX = c.getX();
            double cY = c.getY();
            
            SearchArea area = new MidPointCircle(cX, cY, (int)lenght);
            /*Alternative area shape to look for possible membrane patterns*/
//            SearchArea area = new SquareFrame(cX, cY, (int)lenght);
            area.setAllPossibleSegments();
            
            Iterator<List<int[]>> it_seg = area.getSegmentsSet().iterator();
            
            while(it_seg.hasNext()){
                List<int[]> seg = it_seg.next();
                if(seg.size() > maxLenght)
                    maxLenght = seg.size();
                profiles.add(seg);
            }

            area.getSegmentsSet().stream().forEach((s) -> {
                masks.add(newMask(s, maxLenght));
            });
        }
    }
    
    @Override
    public List<List<int[]>> getProfiles(){
        return this.profiles;
    }

    @Override
    public List<double[]> getPointsAsIntensities(Dataset ds, int startIdx, int endIdx) {
        
        List<double[]> values = new ArrayList<>();
        
        for(int i = startIdx; i < endIdx; i++){
            List<int[]> points = profiles.get(i);
            // We pad the shorter series so all of them are the same lenght.
            double[] intensities = new double[maxLenght];
            
            Arrays.fill(intensities, 0.0);
            
            RandomAccess<RealType<?>> randomAccess = ds.randomAccess();
        
            for(int p = 0; p < points.size(); p++){
            
                randomAccess.setPosition(points.get(p)[0], 0);
                randomAccess.setPosition(points.get(p)[1], 1);
            
                double pixelValue = randomAccess.get().getRealDouble();

                intensities[p] = pixelValue;
            }
            values.add(intensities);
        }
        return values;
    }

    @Override
    public int size() {
        return profiles.size();
    }

    @Override
    public int getMaxLenght() {
        return maxLenght;
    }

    @Override
    public List<int[]> getMasks() {
        return masks;
    }

    public int[] newMask(List<int[]> line, int maxSize) {
        
        int[] mask = new int[maxSize];
        Arrays.fill(mask, 1);
        
        if(maxSize > line.size())
            for(int i = line.size(); i < maxSize; i++)
                mask[i] = 0;
        
        return mask;
    }
}
