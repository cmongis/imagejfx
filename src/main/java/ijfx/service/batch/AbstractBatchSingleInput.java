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

import net.imagej.Dataset;
import net.imagej.display.DatasetView;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import org.scijava.Context;
import org.scijava.display.DisplayService;
import org.scijava.plugin.Parameter;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public abstract class AbstractBatchSingleInput implements BatchSingleInput {

    private DatasetView datasetView;

    private Dataset dataset;

    private ImageDisplay display;

    private String sourceFile;
    
    @Parameter
    protected ImageDisplayService imageDisplayService;

    @Parameter
    protected DisplayService displayService;

    
    
    @Override
    public void setDatasetView(DatasetView datasetView) {
        this.datasetView = datasetView;
        dataset = this.datasetView.getData();
    }

    @Override
    public DatasetView getDatasetView() {
        return this.datasetView;
    }
    @Parameter
    Context context;

    @Override
    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
        ImageDisplay imageDisplay = new SilentImageDisplay();
        context.inject(imageDisplay);
        imageDisplay.display(dataset);
        display = imageDisplay;
       // datasetView = (DatasetView) imageDisplayService.createDataView(dataset);
        //datasetView.rebuild();
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
    public void dispose() {
        dataset = null;
        displayService.getDisplays().remove(display);
        imageDisplayService.getImageDisplays().remove(display);
        display.clear();
        display.close();
        display = null;
        System.gc();
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }
    
    public String getDefaultSaveName() {
        return null;
    }
    
    

}
