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
import ijfx.service.ImagePlaneService;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

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
    
    @Parameter
    UIService uis;
    
    @Parameter
    ImagePlaneService imagePlaneService;
    
    @Override
    public void run() {
        ImageDisplay display = imageDisplayService.getActiveImageDisplay();
        Dataset ds = datasetService.getDatasets(display).get(0);
        Dataset ds2 = imagePlaneService.createEmptyPlaneDataset(ds);
        
        int cX = 200;
        int cY = 200;
        int r = 145;
        
        int cX2 = 400;
        int cY2 = 300;
        int r2 = 30;

        SearchArea area1 = new MidPointCircle(cX,cY,r);
        area1.drawArea(ds2);
        area1.setAllPossibleSegments();
        area1.drawProfiles(ds2);
        
        
        SearchArea area2= new SquareFrame(cX2, cY2, r2);
        area2.drawArea(ds2);
        area2.setAllPossibleSegments();
        area2.drawProfiles(ds2);
        
        uis.show(ds2);
    }
    
}
