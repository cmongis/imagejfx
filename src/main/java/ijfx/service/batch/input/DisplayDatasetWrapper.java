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
import ijfx.service.dataset.DatasetUtillsService;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.ui.UIService;

/**
 *
 * @author cyril
 */
public class DisplayDatasetWrapper extends AbstractSaverWrapper {

    @Parameter
    UIService uiService;
    
    @Parameter
    DatasetUtillsService datasetUtilsService;
    
    String suffix;
    
    public DisplayDatasetWrapper(Context context, BatchSingleInput input, String suffix) {
        super(input);
        this.suffix = suffix;
        context.inject(this);
    }
    
    public DisplayDatasetWrapper(Context context, BatchSingleInput input) {
        super(input);  
    }
    
    

    
    
    
    @Override
    public void save() {
        if(suffix != null) {
           datasetUtilsService.addSuffix(getWrappedObject().getDataset(),suffix,null);
        }
        uiService.show(getWrappedObject().getDataset());
        getWrappedObject().save();
    }
    
    @Override
    public void dispose() {
        
    }
    
    
    
    
    
}
