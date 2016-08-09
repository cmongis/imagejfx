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
package ijfx.plugins.segmentation.search_area;

import ijfx.plugins.segmentation.search_area.SearchArea;
import ijfx.service.overlay.Bresenham;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Point2D;
import net.imagej.Dataset;
import net.imglib2.RandomAccess;
import net.imglib2.type.numeric.RealType;

/**
 *
 * @author Pierre BONNEAU
 */

public abstract class AbstractSearchArea implements SearchArea{
    
    protected Point2D centre;
    protected int radius;
    protected ArrayList<Point2D> points;
    protected ArrayList<List<int[]>> segments;
    
    public AbstractSearchArea(double centreX, double centreY, int radius){
        this.radius = radius;
        this.centre = new Point2D(centreX, centreY);
        this.points = new ArrayList();
        this.segments = new ArrayList<>();
    }
    
    @Override
    public ArrayList<Point2D> getPoints(){
        return this.points;
    }
    
    @Override
    public ArrayList<List<int[]>> getSegmentsSet(){
        return this.segments;
    }
    
    @Override
    public void setAllPossibleSegments(){
        
        int cX = (int)centre.getX();
        int cY = (int)centre.getY();
        
        for(int i = 0; i < points.size(); i++){
            int x = (int)points.get(i).getX();
            int y = (int)points.get(i).getY();
            
            List<int[]> line = Bresenham.findLine(cX, cY, x, y);
            segments.add(line);
        }

    }
    
    @Override
    public void drawArea(Dataset ds){
        RandomAccess<RealType<?>> ra = ds.randomAccess();        
        for(int i = 0; i< points.size(); i++){
            ra.setPosition((int)points.get(i).getX(), 0);
            ra.setPosition((int)points.get(i).getY(), 1);
            ra.get().setReal(2300.0);
        }
    }
    
    @Override
    public void drawProfiles(Dataset ds){
        for(int i = 0; i < segments.size(); i++){
            List<int[]> line = segments.get(i);
            RandomAccess<RealType<?>> ra = ds.randomAccess();            
            for(int j = 0; j < line.size(); j++){
                ra.setPosition(line.get(j)[0],0);
                ra.setPosition(line.get(j)[1], 1);
                ra.get().setReal(2300.0);
            }
        }        
    }
}
