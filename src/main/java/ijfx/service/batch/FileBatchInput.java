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

import ijfx.ui.main.ImageJFX;
import io.scif.services.DatasetIOService;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import net.imagej.DatasetService;
import org.scijava.plugin.Parameter;

/**
 *
 * @author cyril
 */
public class FileBatchInput extends  AbstractBatchSingleInput{

    private final File file;
    
    @Parameter
    DatasetIOService datasetIoService;
    
    @Parameter
            DatasetService datasetService;
    
    String savePath;
    
    public FileBatchInput(File file) {
        this.file = file;
    }
    
    public FileBatchInput(File inputFile, File outputDirectory) {
       
        this(inputFile);
        savePath = new File(outputDirectory,inputFile.getName()).getAbsolutePath();
    }
    
    @Override
    public void load() {
        
        try {
            //setDataset(datasetService.open(file.getAbsolutePath()));
            setDataset(datasetIoService.open(file.getAbsolutePath()));
        } catch (IOException ex) {
            ImageJFX.getLogger().log(Level.SEVERE, null, ex);
        }
        
    }

   

    @Override
    public void save() {
        
        try {
           datasetIoService.save(getDataset(), savePath);
        } catch (IOException ex) {
            
            ImageJFX.getLogger().log(Level.SEVERE, null, ex);
        }
        
    }

    @Override
    public String getName() {
        return file.getName();
    }

    
   
    
}
