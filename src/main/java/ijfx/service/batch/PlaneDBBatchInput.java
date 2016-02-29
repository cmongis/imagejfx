/*
 * /*
 *     This file is part of ImageJ FX.
 *
 *     ImageJ FX is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     ImageJ FX is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with ImageJ FX.  If not, see <http://www.gnu.org/licenses/>. 
 *
 * 	Copyright 2015,2016 Cyril MONGIS, Michael Knop
 *
 */
package ijfx.service.batch;

import ijfx.core.hash.HashService;
import ijfx.core.metadata.GenericMetaData;
import ijfx.core.metadata.MetaData;
import ijfx.core.project.Project;
import ijfx.core.project.imageDBService.ImageReferenceImpl;
import ijfx.core.project.imageDBService.PlaneDB;
import ijfx.ui.main.ImageJFX;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.logging.Level;
import org.scijava.plugin.Parameter;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class PlaneDBBatchInput extends ImagePlaneBatchInput{

   
    @Parameter
    private HashService hashService;
    
    private Project project;
    
    private String saveDirectory;
    
    private final PlaneDB plane;
    
    public enum SaveMode {
        REPLACE_ENTRY
        ,ADD_ENTRY
    };
    
    
    private final SaveMode saveMode;
    
    
    
    public PlaneDBBatchInput(Project project,PlaneDB plane,SaveMode saveMode) {
        this.project = project;
        this.plane = plane;
        setFileSource(plane.getFile());
        setPlaneIndex(plane.getPlaneIndex());
        
        this.saveMode = saveMode;
        
        
    }

    public String getSaveDirectory() {
        return saveDirectory;
    }

    public void setSaveDirectory(String saveDirectory) {
        this.saveDirectory = saveDirectory;
    }
    
 
    
    @Override
    public void save() {
        
        try {
            // save in a temp folder
            Path tmpDirPath = Files.createTempDirectory("ijfx");
            File tmpDir = tmpDirPath.toFile();
            String tmpName = UUID.randomUUID().toString();
            
            File tmpImageFile = new File(tmpDir,tmpName+".tif");
            
            setSavePath(tmpImageFile.getAbsolutePath());
            
            super.save();
            
            
  
            // calculate hash
            String hash = hashService.getHash(tmpImageFile);
            
            
            // copy to destimation folder with hash name
            File finalDestination = new File(getSaveDirectory(),hash+".tif");
            
            // renaming
            tmpImageFile.renameTo(finalDestination);
            
            // change save path
            setSavePath(finalDestination.getAbsolutePath());
      
            
            plane.setImageReference(new ImageReferenceImpl(hash,finalDestination.getAbsolutePath()));
            plane.setPlaneIndex(0);
            plane.addMetaData(new GenericMetaData(MetaData.WAS_MODIFIED, 1));
            plane.addTag(MetaData.WAS_MODIFIED);
            
            
        } catch (IOException ex) {
            ImageJFX.getLogger().log(Level.SEVERE, null, ex);
        }
        
        
        
    }
    
    
    
}
