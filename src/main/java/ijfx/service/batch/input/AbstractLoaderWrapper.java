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
import java.util.function.Consumer;
import net.imagej.Dataset;
import net.imagej.display.DatasetView;
import net.imagej.display.ImageDisplay;
import org.scijava.Context;
import org.scijava.plugin.Parameter;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public abstract class AbstractLoaderWrapper<T> implements BatchSingleInput {

    final protected T wrappedObject;

    final protected NaiveBatchInput input = new NaiveBatchInput();
    
    @Parameter
    private Context context;
    
    protected Consumer<AbstractLoaderWrapper<T>> onSave;
    
    public AbstractLoaderWrapper(T wrappedObject) {
        this.wrappedObject = wrappedObject;
    }
    
    
   
    
    @Override
    public DatasetView getDatasetView() {
        return input.getDatasetView();
    }

    @Override
    public void setDatasetView(DatasetView datasetView) {
        input.setDatasetView(datasetView);
    }

   

    @Override
    public void setDataset(Dataset dataset) {
        input.setDataset(dataset);
    }

    @Override
    public void setDisplay(ImageDisplay display) {
        input.setDisplay(display);
    }

    @Override
    public Dataset getDataset() {
        return input.getDataset();
    }

    @Override
    public ImageDisplay getDisplay() {
        return input.getDisplay();
    }

    @Override
    public void save() {
        if(onSave != null) {
            onSave.accept(this);
        }
        input.save();
    }
    
    public AbstractLoaderWrapper<T> onSave(Consumer<AbstractLoaderWrapper<T>> onSave) {
        this.onSave = onSave;
        return this;
    }

    @Override
    public void dispose() {
        input.dispose();
    }

    public T getWrappedValue() {
        return wrappedObject;
    }
    
    protected Context getContext() {
        return context;
    }
    
}
