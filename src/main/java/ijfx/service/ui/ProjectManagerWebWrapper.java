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
package ijfx.service.ui;

import ijfx.service.log.LogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import ijfx.core.project.DefaultImageLoaderService;
import ijfx.core.project.Project;
import ijfx.core.project.DefaultProjectIoService;
import ijfx.core.project.DefaultProjectManagerService;
import ijfx.core.project.imageDBService.PlaneDB;
import ijfx.ui.main.ImageJFX;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ReadOnlyMapProperty;
import mercury.core.AngularMethod;
import mercury.core.Deferred;
import mercury.core.JSONParameters;
import mercury.core.LogEntry;
import net.imagej.ImageJService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import mongis.utils.FXUtilities;

/**
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = Service.class)
public class ProjectManagerWebWrapper extends AbstractService implements ImageJService{

    @Parameter
    private DefaultProjectManagerService projectManager;

    @Parameter
    private DefaultProjectIoService projectIo;

    @Parameter
    private DefaultImageLoaderService imageLoader;

    @Parameter
    private LogService logService;

    private final Logger logger = ImageJFX.getLogger();
    
    
    ObjectMapper objectMapper = new ObjectMapper();

    public Project createProject() {
        return projectIo.createProject();
    }

    public Project getCurrentProject() {
        return projectManager.currentProjectProperty().get();
    }

    @AngularMethod(sync = false, description = "Opens dialog message and import single files")
    public void importFile(Deferred deferred, JSONParameters params) {

        if (getCurrentProject() == null) {
            createProject();
        }
        
        try {
            FXUtilities.openFiles("Import image files into project", null, null)
                    .forEach(
                            file -> {
                                try {
                                    logger.info("Loading file : "+file);
                                    
                                    imageLoader.loadImageFromFile(file, getCurrentProject());
                                    
                                   getCurrentProject().getImages().forEach(plane->{

                                   
                                   });
                                } catch (IOException ex) {
                                   ImageJFX.getLogger().log(Level.SEVERE, null, ex);
                                }
                            }
                    );
            logger.fine("It's not over until I win !");
            deferred.resolveSimpleJSON("success", true);
        } catch (Exception e) {
            deferred.resolveSimpleJSON("error", "Error when importing the images");
            e.printStackTrace();
            logService.notifyError(new LogEntry(e));
        }
    }

    @AngularMethod(description = "Open dialog to import a folder")
    public void importFolder(Deferred deferred, JSONParameters params) {

        try {
            FXUtilities
                    .openFiles("Import image files into project", null, null)
                    .forEach(
                            file -> {
                                try {
                                    imageLoader.loadImageFromFile(file, getCurrentProject());
                                } catch (IOException ex) {
                                    ImageJFX.getLogger().log(Level.SEVERE,null,ex);;
                                }
                            }
                    );
            deferred.resolveSimpleJSON("success", true);
        } catch (Exception e) {
            deferred.resolveSimpleJSON("error", "Error when importing the images");
            e.printStackTrace();
            logService.notifyError(new LogEntry(e));
        }
    }

    
        
     
        
        @AngularMethod(description="return the planes actually in the database")
        public void getData(Deferred deferred, JSONParameters params) {
            if(getCurrentProject() == null) createProject();
            //ArrayList<MetaDataSet> data = new ArrayList<>();
            int total = getCurrentProject().getImages().size();
            int count = 0;
            
            ArrayList<HashMap> data = new ArrayList<>();
            
           for(PlaneDB planeDB : getCurrentProject().getImages()) {
                
                if(count++ % 10 == 0)
                    deferred.notifySimpleJSON("progress",1.0*count/total);
                data.add(createHash(planeDB.metaDataSetProperty()));
            };
            deferred.mapAndResolve(data);
        }
        
        public HashMap<String,Object> createHash(ReadOnlyMapProperty set) {
            HashMap<String,Object> map = new HashMap<>();
            
            
           
            set.getValue().forEach((key,value)->map.put(key.toString(),value));
            
    
            return map;
        }
        
    
}
