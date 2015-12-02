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
package ijfx.bridge;

import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.process.ActiveDatasetPreprocessor;
import org.scijava.Priority;
import org.scijava.display.DisplayService;
import org.scijava.module.process.PreprocessorPlugin;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = PreprocessorPlugin.class, priority=Priority.VERY_HIGH_PRIORITY)
public class PowerActiveDatasetPreprocessor extends ActiveDatasetPreprocessor{
        
    
    @Parameter
    ImageDisplayService imgDisplayService;
    
    @Parameter
    DisplayService displayService;
    
    @Parameter
    DatasetService datasetSetvice;
    
    @Override
    public Dataset getValue() {
        
        return imgDisplayService.getActiveDataset(displayService.getActiveDisplay(ImageDisplay.class)); // Solves the problem :-D
    }
}
