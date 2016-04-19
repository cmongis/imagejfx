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
package ijfx.service.batch;

import com.sun.javafx.geom.transform.BaseTransform;
import java.util.List;
import java.util.stream.Collectors;
import net.imagej.Dataset;
import net.imagej.display.DataView;
import net.imagej.display.DatasetView;
import net.imagej.display.DefaultDatasetView;
import net.imagej.display.DefaultImageDisplay;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import org.scijava.display.Display;
import org.scijava.display.DisplayService;
import org.scijava.plugin.Parameter;

/**
 *
 * @author cyril
 */
public abstract class AbstractBatchSingleInput implements BatchSingleInput{
    DatasetView datasetView;
    
    Dataset dataset;
    
    DefaultImageDisplay display;
    
   
    @Parameter
    protected ImageDisplayService imageDisplayService;
    
    @Parameter
    protected DisplayService displayService;
    
    @Override
    public void setDatasetView(DatasetView datasetView)
    {
        this.datasetView = datasetView;
        dataset = this.datasetView.getData();
    }
    
    @Override
    public DatasetView getDatasetView()
    {
        return this.datasetView;
    }
      @Override
    public void setDataset(Dataset dataset) {
        //ImageDisplay activeDisplay = (ImageDisplay) displayService.getActiveDisplay();
        this.dataset = dataset;
        //display = (DefaultImageDisplay) displayService.createDisplay(dataset);

        //imageDisplayService.getImageDisplays().remove(display);

       //boolean b = display.isDisplaying(dataset);
        
        display = new DefaultImageDisplay();
        //display = (ImageDisplay) displayService.getActiveDisplay();
        datasetView = (DatasetView) imageDisplayService.createDataView(dataset);
        datasetView.rebuild();
        imageDisplayService.context().inject(display);
        //display.display(dataset);
        //datasetView = imageDisplayService.getActiveDatasetView();
        //datasetView = (DatasetView) imageDisplayService.createDataView(dataset);
    }

    @Override
    public void setDisplay(ImageDisplay display) {
        this.display = (DefaultImageDisplay) display;
        dataset = imageDisplayService.getActiveDataset(display);
        
    }

    @Override
    public Dataset getDataset() {

        dataset = datasetView.getData();

        return dataset;
    }

    @Override
    public ImageDisplay getDisplay() {
        //displayService.setActiveDisplay(display);
      
        return display;
    }
    
    
    @Override
    public void dispose() {
        dataset = null;                
        displayService.getDisplays().remove(display);
        imageDisplayService.getImageDisplays().remove(display);
        display.clear();
        display.close();
        display = null;
        System.gc();
     
        
    }
    
   
            
            
            
}
