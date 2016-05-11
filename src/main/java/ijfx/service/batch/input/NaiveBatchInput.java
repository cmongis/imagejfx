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
package ijfx.service.batch.input;

import ijfx.service.batch.BatchSingleInput;
import net.imagej.Dataset;
import net.imagej.display.DatasetView;
import net.imagej.display.ImageDisplay;

/**
 * Naive implementation of the Batch Input
 *
 * @author cyril
 */
public class NaiveBatchInput implements BatchSingleInput {

    private Dataset dataset;

   private ImageDisplay imageDisplay;
    
    private DatasetView datasetView;



    private String name;

    private String sourceFile;
    
    @Override
    public DatasetView getDatasetView() {
        return datasetView;
    }

    @Override
    public void setDatasetView(DatasetView datasetView) {
        this.datasetView = datasetView;
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
        this.imageDisplay = display;
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
        return name;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public String getSourceFile() {
        return sourceFile;
    }
    
    
    
}

