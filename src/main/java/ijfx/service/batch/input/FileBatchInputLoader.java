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

import ijfx.ui.main.ImageJFX;
import io.scif.services.DatasetIOService;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.scijava.plugin.Parameter;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class FileBatchInputLoader extends AbstractLoaderWrapper<File>{

    Logger logger = ImageJFX.getLogger();
    
    @Parameter
    DatasetIOService datasetIoService;
    
    public FileBatchInputLoader(File f) {
        super(f);
    }

    
    
    @Override
    public void load() {
       if(getDataset() != null) return;
       try {
           setDataset(datasetIoService.open(getWrappedValue().getAbsolutePath()));
       }
       catch(Exception e) {
            logger.log(Level.SEVERE,"Error when loading file "+getWrappedValue().getName(),e);
       }
    }

    @Override
    public String getName() {
       return getWrappedValue().getName();
    }

    @Override
    public String getSourceFile() {
        return getWrappedValue().getAbsolutePath();
    }
    
}
