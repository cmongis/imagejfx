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
package ijfx.ui.service.angular;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import ijfx.core.metadata.MetaData;
import ijfx.core.project.AnnotationRule;
import ijfx.core.project.DefaultImageLoaderService;
import ijfx.core.project.query.Modifier;
import ijfx.core.project.Project;
import ijfx.core.project.DefaultProjectIoService;
import ijfx.core.project.DefaultProjectManagerService;
import ijfx.core.project.DefaultProjectModifierService;
import ijfx.core.project.query.DefaultQueryService;
import ijfx.core.project.query.Selector;
import ijfx.core.project.query.DefaultSelector;
import ijfx.core.project.imageDBService.PlaneDB;
import ijfx.ui.main.ImageJFX;
import ijfx.ui.main.LoadingScreen;
import ijfx.service.ui.LoadingScreenService;
import ijfx.service.log.LogService;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.beans.property.ReadOnlyMapProperty;
import javafx.concurrent.Task;
import mercury.core.AngularMethod;
import mercury.core.Deferred;
import mercury.core.JSONParameters;
import mercury.core.JSONUtils;
import mercury.core.LogEntry;
import mercury.core.MapResultToJSON;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import mongis.utils.FXUtilities;

/**
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = AngularService.class)
public class ProjectServiceBinder implements AngularService {

    @Parameter
    DefaultProjectManagerService projectService;

    @Parameter
    DefaultProjectIoService projectIo;

    @Parameter
    DefaultImageLoaderService imageLoader;

    @Parameter
    LogService logService;

    @Parameter
    LoadingScreenService LoadingService;

    @Parameter
    DefaultProjectModifierService modifierService;

    @Parameter
    DefaultQueryService queryService;
    
    @Parameter
            Logger logger = ImageJFX.getLogger();
    
    ObjectMapper objectMapper = new ObjectMapper();

    
    @Parameter
    LoadingScreenService loadingScreenService;
    
    public Project createProject() {
        
        logger.info(""+getCurrentProject());
        if(getCurrentProject() == null) {
            Project project = projectIo.createProject();
            projectService.addProject(project);
            projectService.setCurrentProject(project);
        }
        return getCurrentProject();
    }

    public Project getCurrentProject() {
        if(projectService.currentProjectProperty().get() == null) {
            Project project = projectIo.createProject();
            projectService.addProject(project);
            projectService.setCurrentProject(project);
        }
        return projectService.currentProjectProperty().get();
    }

    @AngularMethod(sync = false, description = "Opens dialog message and import single files")
    public Task importFile(Deferred deferred, JSONParameters params) {

        if (getCurrentProject() == null) {
            createProject();
        }

        
        List<File> files = FXUtilities.openFiles("Import images into project", null, null);
        try {
            Task task = imageLoader.loadImageFromFile(files, getCurrentProject());
            LoadingScreen.getInstance().submitTask(task);
            return task;
        }
        catch(IOException ioe) {
            return null;
        }
        
    }
    
    @AngularMethod(sync = false, description="Add a regular annotation rule")
    public Task addRule(Deferred deferred, JSONParameters params) {
        
        
        
        final String selectorStr = params.getString("selector");
        final String modifierStr = params.getString("modifier");
        final Selector selector = queryService.getSelector(selectorStr);
        final Modifier modifier = queryService.getModifier(modifierStr);


        
        
        Task<Void> task = new Task<Void>() {
            public Void call() {
                
                AnnotationRule rule = queryService.addAnnotationRule(getCurrentProject(), selector, modifier);
                
                queryService.applyAnnotationRules(getCurrentProject());
                
                
                return null;
            }
        };
        
        return task;
    }
    
  

    @AngularMethod(description = "Open dialog to import a folder",sync = false)
    public Task importFolder(Deferred deferred, JSONParameters params) {
       
        if (getCurrentProject() == null) {
            createProject();
        }
        
        
        try {
            File files = FXUtilities
                    .openFolder("Import image files into project", null);

            Task task = imageLoader.loadImageFromDirectory(files, projectService.currentProjectProperty().getValue());
            
            LoadingScreen.getInstance().submitTask(task,true);
            
            return task;

        } catch (Exception e) {
            deferred.resolveSimpleJSON("error", "Error when importing the images");
            e.printStackTrace();
            logService.notifyError(new LogEntry(e));
        }
        return null;
    }

    @AngularMethod(description = "return the planes actually in the database")
    public void getData(Deferred deferred, JSONParameters params) {
        if (getCurrentProject() == null) {
            createProject();
        }
        //ArrayList<MetaDataSet> data = new ArrayList<>();
        int total = getCurrentProject().getImages().size();
        int count = 0;

        ArrayList<HashMap> data = new ArrayList<>();

        for (PlaneDB planeDB : getCurrentProject().getImages()) {

            if (count++ % 10 == 0) {
                deferred.notifySimpleJSON("progress", 1.0 * count / total);
            }
            data.add(createHash(planeDB));
        };
        deferred.mapAndResolve(data);
    }

    public HashMap<String, Object> createHash(PlaneDB planeDB) {
        HashMap<String, Object> map = new HashMap<>();

        ReadOnlyMapProperty<String, MetaData> set = planeDB.getMetaDataSet();

        map.put("Hash", planeDB.getImageID());

        set.getValue().forEach((key, value) -> map.put(key.toString(), value.getValue()));

        return map;
    }

    @AngularMethod(description = "return infos about the files added to the database", inputDescription = "None", outputDescription = "List of JSON object", outputExample = "[{\"File name\":\"some name\",\"Plane count\":10]")
    public void getFileList(Deferred deferred, JSONParameters params) {
        ArrayList<HashMap> data = new ArrayList<>();

        final String NAME = "File name";
        final String COUNT = "Plane count";

        HashMap<String, ImageFileInfo> infos = new HashMap<>();

        getCurrentProject().getImages().forEach(planeDB -> {

            String key = planeDB.getFile().getAbsolutePath();
            if (infos.containsKey(key)) {

                infos.get(key).incrementPlaneCount();

            } else {
                infos.put(key, new ImageFileInfo(planeDB));
            }
        });

        infos.forEach((hash, info) -> {
            data.add(info);
        });

        deferred.mapAndResolve(data);

    }

    public List<PlaneDB> getPlanesFromFile(String imageID) {
        return getCurrentProject().getImages().filtered(plane -> plane.getImageID().equals(imageID));
    }

    @Override
    public String getAngularName() {
        return "ProjectService";
    }

    public class ImageFileInfo extends HashMap<String, Object> {

        @JsonIgnore
        public static final String PLANE_COUNT = "Plane count";
        @JsonIgnore
        public static final String FILE_NAME = "File name";
        @JsonIgnore
        public static final String FILE_PATH = "File path";

        private int count = 0;

        @JsonIgnore
        private File file;

        public ImageFileInfo(PlaneDB planeDB) {
            this(planeDB.getFile());
        }

        public ImageFileInfo(File file) {

            super();

            this.file = file;

            put(FILE_NAME, file.getName());
            put(FILE_PATH, file.getAbsolutePath());
            incrementPlaneCount();

        }

        @JsonIgnore
        public File getFile() {
            return file;
        }

        public void incrementPlaneCount() {
            count++;
            put(PLANE_COUNT, count);
        }

        @JsonGetter("Plane count")
        public Integer getCount() {
            return count;
        }

        public boolean equals(Object o) {
            if (o instanceof ImageFileInfo) {
                return ((ImageFileInfo) o).getFile().equals(getFile());
            } else {
                return false;
            }
        }

    }

    @AngularMethod(sync = true)
    public void deleteImage(String filePath) {

        modifierService.removePlaneFromProject(getCurrentProject(), getCurrentProject()
                .getImages()
                .filtered(
                        plane -> plane.getFile()
                        .getAbsolutePath()
                        .equals(filePath)
                ));

    }
    
    
    @AngularMethod(sync = true) 
    public boolean createRule(String name, String regExp) {
        
       
        
        return true;
    }
    
    @AngularMethod(description="Returns a array containing the list of possible metadata of the current project")
    @MapResultToJSON
    public Task<List<String>> getPossibleMetadata(Deferred angular,JSONParameters params) {
        Task<List<String>> task = new Task<List<String>>() {
            public List<String> call() {
               return
                modifierService
                .getPossibleNewHierarchyKey(getCurrentProject())
                .stream().sorted((a,b)->a.compareTo(b))
                .collect(Collectors.toList());
            }
        };
        
        return task;
    }
    
    @AngularMethod(description = "Return a array containing the hierarchy")
    @MapResultToJSON
    public Task<List<String>> getProjectHierarchy(Deferred angular, JSONParameters params) {
        Task<List<String>> task = new Task<List<String>>() {
            public List<String> call() {
                List<String> hierarchy = new ArrayList<>();
                hierarchy.addAll(getCurrentProject().getHierarchy());
                return hierarchy;
            }
        };
        return task;
                
              
    }
    
    @AngularMethod(description = "Save the hierarchy from a list of string",sync=true)
    public void saveHierarchy(Object strings) {

        List<String> list = JSONUtils.mapFromJSONList(strings);

    modifierService.setHierarchy(getCurrentProject(), list);
        
        
    }
    
    
}
