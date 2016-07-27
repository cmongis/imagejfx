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

import ijfx.plugins.segmentation.search_area.MidPointCircle;
import ijfx.plugins.segmentation.search_area.SquareFrame;
import ijfx.plugins.segmentation.search_area.SearchArea;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Pierre BONNEAU
 */

@Plugin(type = Command.class, menuPath = "Analyze>Segmentation>Test Search Area")

public class SearchAreaCommandTester implements Command{
    
    @Parameter
    ImageDisplayService imageDisplayService;
    
    @Parameter
    DatasetService datasetService;
    
    @Override
    public void run() {
        ImageDisplay display = imageDisplayService.getActiveImageDisplay();
        Dataset ds = datasetService.getDatasets(display).get(0);
        int cX = 150;
        int cY = 150;
        int r = 145;
        
        int cX2 = 80;
        int cY2 = 80;
        int r2 = 30;

        SearchArea area1 = new MidPointCircle(cX,cY,r);
        area1.drawArea(ds);
        
        SearchArea area2= new SquareFrame(cX2, cY2, r2);
        area2.drawArea(ds);
        
        ds.update();        
        
        area1.setAllPossibleSegments();
        area2.setAllPossibleSegments();
        
        area1.drawProfiles(ds);
        area2.drawProfiles(ds);
        
        ds.update();
    }
    
}
