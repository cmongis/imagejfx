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
package ijfx.service.batch;

import ijfx.core.project.ProjectToImageJService;
import ijfx.ui.main.ImageJFX;
import io.scif.FormatException;
import io.scif.services.DatasetIOService;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.imagej.Dataset;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import org.scijava.display.DisplayService;
import org.scijava.plugin.Parameter;

// simple implementation used for single plane processing

public class ImagePlaneBatchInput implements BatchSingleInput {
    private File
            fileSource;
    private long planeIndex;
    private String savePath;

    @Parameter
    DisplayService displayService;
    
    @Parameter
    ImageDisplayService imageDisplayService;
    
    @Parameter
    ProjectToImageJService projectToImageJService;
    
    @Parameter
    DatasetIOService datasetIOService;
    
    Logger logger = ImageJFX.getLogger();
    
    public ImagePlaneBatchInput() {
        
    }
    
    public ImagePlaneBatchInput(File fileSource, long planeIndex, String savePath) {
        this.fileSource = fileSource;
        this.planeIndex = planeIndex;
        this.savePath = savePath;
       
    }
    Dataset dataset;
    ImageDisplay display;

    public File getFileSource() {
        return fileSource;
    }

    public String getName() {
        return String.format("%s (%d)",fileSource.getName(),planeIndex);
    }
    
    public void setFileSource(File fileSource) {
        this.fileSource = fileSource;
    }

    public long getPlaneIndex() {
        return planeIndex;
    }

    public void setPlaneIndex(long planeIndex) {
        this.planeIndex = planeIndex;
    }

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    @Override
    public void load() {
             try {
                setDataset(projectToImageJService.getDataset(fileSource, (int)planeIndex));
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            } catch (FormatException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
    }

    @Override
    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
        display = (ImageDisplay) displayService.createDisplay(dataset);
    }

    @Override
    public void setDisplay(ImageDisplay display) {
        this.display = display;
        dataset = imageDisplayService.getActiveDataset(display);
    }

    @Override
    public Dataset getDataset() {
        

        return dataset;
    }

    @Override
    public ImageDisplay getDisplay() {
        return display;
    }

    @Override
    public void save() {
        
        try {
            datasetIOService.save(dataset, savePath);
        } catch (IOException ex) {
            ImageJFX.getLogger();
        }
        
    }
    
    public void dispose() {
        dataset = null;
        display.close();
        display = null;
    }
    
}
