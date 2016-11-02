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

import ijfx.service.ImagePlaneService;
import net.imagej.Dataset;
import net.imagej.display.DatasetView;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import org.scijava.Context;
import org.scijava.plugin.Parameter;

/**
 *  Copy the currently available image and use it as input for a batch processing
 * @author Cyril MONGIS, 2016
 */
public class ImageDisplayBatchInput implements BatchSingleInput{

    @Parameter
    ImageDisplayService imageDisplayService;
    
    private boolean useCurrentPlane;
    
    private ImageDisplay imageDisplay;
    
    private Dataset dataset;
    
    private DatasetView view;
    
    private String source;
    
    @Parameter
    ImagePlaneService imagePlaneService;
    
    @Parameter
    Context context;
    
    int i = 0;
    
    public ImageDisplayBatchInput(ImageDisplay inputDisplay, boolean useCurrentPlane) {
        
        //injecting the context
        inputDisplay.getContext().inject(this);
        
        // creating the silent image display 
        this.imageDisplay = new SilentImageDisplay();
        
        // inject the silent image display with the context
        context.inject(this.imageDisplay);
        
        // extracting the position of the current image display
        long[] position = new long[inputDisplay.numDimensions()];
        
        inputDisplay.localize(position);
        
        // creating a new dataset from this position
        dataset = imagePlaneService.isolatePlane(imageDisplayService.getActiveDataset(inputDisplay), position);
        this.source = dataset.getSource();
        this.imageDisplay.display(dataset);
    }
    
    
    @Override
    public DatasetView getDatasetView() {
        return view;
    }

    @Override
    public void setDatasetView(DatasetView datasetView) {
        this.view = datasetView;
    }

    @Override
    public void load() {
    }

    @Override
    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    @Override
    public void setDisplay(ImageDisplay display) {
  
        imageDisplay = display;
    }

    @Override
    public Dataset getDataset() {
        return dataset;
    }

    @Override
    public ImageDisplay getDisplay() {
        return imageDisplay;
    }

    @Override
    public void save() {
    }

    @Override
    public void dispose() {
    }

    @Override
    public String getName() {
        return dataset.getName();
    }

    @Override
    public String getSourceFile() {
        return source;
    }
    
}
