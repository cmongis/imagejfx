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
package ijfx.core.project;

import ijfx.core.project.event.PossibleMetaDataKeysChangeEvent;
import ijfx.core.project.event.PossibleTagListChangeEvent;
import ijfx.core.project.event.ProjectCloseEvent;
import ijfx.core.project.event.ProjectCreatedEvent;
import ijfx.ui.main.ImageJFX;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import org.scijava.Priority;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = Service.class, priority = Priority.LOW_PRIORITY)
public class DefaultProjectManagerService extends AbstractService implements ProjectManagerService {

    private final ReadOnlyListWrapper<Project> projects = new ReadOnlyListWrapper<>(this, "projects", FXCollections.observableArrayList());
    private final ReadOnlyObjectWrapper<Project> currentProject = new ReadOnlyObjectWrapper<>(this, "currentProject", null);

    Logger logger = ImageJFX.getLogger();

    @Parameter
    EventService eventService;

    @Parameter
    ProjectContextCalculatorService projectContextService;

    
    
    public static String readFile(File file) throws IOException {
        byte[] encoded = Files.readAllBytes(file.toPath());
        return new String(encoded, StandardCharsets.UTF_8);
    }

    public DefaultProjectManagerService() {
        //projects.addListener(this::changeCurrentProject);
        currentProject.addListener(this::onCurrentProjectChange);
    }

    @Override
    public boolean hasProject() {
        return (currentProject.getValue() != null);
    }

    @Override
    public void addProject(Project project) {
        if (!projects.contains(project)) {
            projects.add(project);
        }

        
         eventService.publish(new ProjectCreatedEvent(project));
        
         if(project!=null) setCurrentProject(project);
         
        
    }

    @Override
    public void removeProject(Project project) {
        projects.remove(project);
        if(projects.size() == 0) {
            setCurrentProject(null);
        }
        
        eventService.publish(new ProjectCloseEvent(project));
        
    }

    /**
     * set the project that should be used by every service or controller
     *
     * @param project , can be null.
     */
    @Override
    public void setCurrentProject(Project project) {

        currentProject.setValue(project);

    }

    private void onCurrentProjectChange(Observable obs, Project oldValue, Project project) {

        // if it's a new project
        if (oldValue != project) {

            if (project != null) {
                logger.info("Setting new project " + project.toString());
            } else {
                logger.info("There is no project open anymore");
                
            }
            if (!projects.contains(project)) {
                projects.add(project); // currentProject will be automatically updated
            }
            
            
                projectContextService.updateContext(project);
                notifyPossibleTagChange(project);
                notifyMetaDataKeyChange(project);
            
        }

    }

   

    @Override
    public Project getCurrentProject() {
        return currentProjectProperty().getValue();
    }

    @Override
    public ReadOnlyObjectProperty<Project> currentProjectProperty() {
        return currentProject.getReadOnlyProperty();
    }

    @Override
    public ReadOnlyListProperty<Project> getProjects() {
        return projects.getReadOnlyProperty();
    }

    private void notifyPossibleTagChange(Project project) {
        logger.info("Notifying new tag possibilities");
        eventService.publish(new PossibleTagListChangeEvent(getAllPossibleTag(project)));
    }

    private void notifyMetaDataKeyChange(Project project) {
        logger.info("Notifying new metadata possibilities");
        eventService.publish(new PossibleMetaDataKeysChangeEvent(getAllPossibleMetadataKeys(project)));
    }

    @Override
    public Set<String> getAllPossibleTag(Project project) {
       
        Set<String> tags = new HashSet<>();
         if(project == null) return tags;
        project.getImages().stream().parallel().forEach(image -> {
            image.getTags().forEach(tag -> {
                if (!tags.contains(tag)) {
                    tags.add(tag);
                }
            });
        });
        return tags;
    }

    @Override
    public Set<String> getAllPossibleMetadataKeys(Project project) {

        
        
        Set<String> keys = new HashSet<>();
        if(project == null) return keys;
        project.getImages().stream().parallel().forEach(image -> {
            image.getMetaDataSet().keySet().forEach(key -> {
                if (!keys.contains(key)) {
                    keys.add(key);
                }
            });
        });
        return keys;
    }

    @Override
    public void notifyProjectChange(Project project) {

        if (project == getCurrentProject()) {
            notifyMetaDataKeyChange(project);
            notifyPossibleTagChange(project);
        }

    }

}
