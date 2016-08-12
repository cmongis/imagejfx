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

import ijfx.service.overlay.OverlayDrawingService;
import ijfx.service.overlay.OverlayShapeStatistics;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Point2D;
import net.imagej.Dataset;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.OverlayService;
import net.imagej.overlay.Overlay;
import net.imagej.overlay.PointOverlay;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Pierre BONNEAU
 */

@Plugin(type = Command.class, menuPath = "Plugins>Sandbox>Simple test")

public class SimpleCommandTester implements Command{

    private List<Dataset> imgDatasets;    
    
    @Parameter
    ImageDisplayService imageDisplayService;
    
    @Parameter
    OverlayService overlayService;
    
    @Parameter
    MLSegmentationService segmentationService;
    
    @Override
    public void run() {
//        segmentationService.generateTestSet();
//        System.out.println("Test set generated");
//        segmentationService.saveModel();
    }
    
}
