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

import java.util.function.Consumer;
import net.imagej.Dataset;
import net.imagej.display.DatasetView;
import net.imagej.display.ImageDisplay;

/**
 * Implementation that wrap a single input and allow to take the result at the end and do something with the result
 * @author Cyril MONGIS, 2016
 */
public class BatchInputWrapper implements BatchSingleInput {

    
    private final BatchSingleInput singleInput;

    Consumer<BatchSingleInput> whenFinished;
    
    public BatchInputWrapper(BatchSingleInput singleInput) {
        this.singleInput = singleInput;
    }
    
    public BatchInputWrapper then(Consumer<BatchSingleInput> output) {
        
        this.whenFinished = output;
        return this;
    }
    
    
    @Override
    public DatasetView getDatasetView() {
        System.out.println("getting the dataset view");
        return singleInput.getDatasetView();
    }

    @Override
    public void setDatasetView(DatasetView datasetView) {
        singleInput.setDatasetView(datasetView);
    }

    @Override
    public void load() {
        System.out.println("loading");
        singleInput.load();
    }

    @Override
    public void setDataset(Dataset dataset) {
        singleInput.setDataset(dataset);
    }

    @Override
    public void setDisplay(ImageDisplay display) {
        singleInput.setDisplay(display);
    }

    @Override
    public Dataset getDataset() {
        System.out.println("Getting the dataset");
        return singleInput.getDataset();
    }

    @Override
    public ImageDisplay getDisplay() {
        System.out.println("Getting the display");
        return singleInput.getDisplay();
    }

    @Override
    public void save() {
        System.out.println("It's not finished");
         whenFinished.accept(singleInput);
        singleInput.save();
       
    }

    @Override
    public void dispose() {
        // singleInput.dispose();
    }

    @Override
    public String getName() {
        return singleInput.getName();
    }

    @Override
    public String getSourceFile() {
        if(getDataset() != null) {
            return getDataset().getSource();
        }
        return null;
    }
    
}
