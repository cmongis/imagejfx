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
 *
 * @author Cyril MONGIS, 2016
 */
public abstract class AbstractSaverWrapper implements BatchSingleInput{
    final private BatchSingleInput wrappedObject;

    
    String savePath;
        
    public AbstractSaverWrapper(BatchSingleInput input) {
        this.wrappedObject = input;
        
    }

    protected BatchSingleInput getWrappedObject() {
        return wrappedObject;
    }

    
    
    

    @Override
    public DatasetView getDatasetView() {
        return wrappedObject.getDatasetView();
    }

    @Override
    public void setDatasetView(DatasetView datasetView) {
         wrappedObject.setDatasetView(datasetView);
    }

    @Override
    public void load() {
        wrappedObject.load();
    }

    @Override
    public void setDataset(Dataset dataset) {
        wrappedObject.setDataset(dataset);
    }

    @Override
    public void setDisplay(ImageDisplay display) {
        wrappedObject.setDisplay(display);
    }

    @Override
    public Dataset getDataset() {
        return wrappedObject.getDataset();
    }

    @Override
    public ImageDisplay getDisplay() {
        return wrappedObject.getDisplay();
    }

   

    @Override
    public void dispose() {
        wrappedObject.dispose();
    }

    @Override
    public String getName() {
       return wrappedObject.getName();
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    protected String getSavePath() {
        return savePath;
    }

    @Override
    public String getSourceFile() {
        return wrappedObject.getSourceFile();
    }

    
    
    
    
   
    
    
    
    
    
    
}
