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
package ijfx.service.ui;

import ijfx.core.project.Project;
import ijfx.core.project.ProjectManagerService;
import ijfx.core.project.event.ProjectActivatedEvent;
import ijfx.ui.main.ImageJFX;
import java.util.Collection;
import java.util.stream.Collectors;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import net.imagej.ImageJService;
import org.scijava.Priority;
import org.scijava.event.EventHandler;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 * This class provides methods useful for providing suggestion related to a
 * project (possible tags, values, etc)
 *
 * @author cyril
 */
@Plugin(type = Service.class, priority = Priority.VERY_LOW_PRIORITY)
public class SuggestionService extends AbstractService implements ImageJService {

    @Parameter
    private ProjectManagerService projectManagerService;

    private final ObservableList<String> possibleTags = FXCollections.observableArrayList();

    private Project currentProject;

    @Override
    public void initialize() {
        super.initialize();

    }

    public Collection<String> suggestPossibleTags(String userInput) {
        return suggestPossibleTags(projectManagerService.getCurrentProject(), userInput);
    }

    public Collection<String> suggestPossibleTags(Project project, String userInput) {
        return projectManagerService.getAllPossibleMetadataKeys(project) // we get all the possible keys
                .stream() // we stream it
                .filter(keyName -> keyName.toLowerCase().contains(userInput.toLowerCase())) // filter the key names that contain the text entered by the user
                .collect(Collectors.toList()); // we collect the results in a list that we return
    }

    /*
    public Collection<String> suggestPossibleMetadataKeys(Project project, String userInput) {
        
    }
    
    public Collection<String> suggestPossibleValues(Project project, String metadataKey, String userInput) {
        
    }
    
     */
    public ObservableList<String> getPossibleTagsAutoupdatedList() {
        return possibleTags;
    }

    @EventHandler
    public void onCurrentProjectChanged(ProjectActivatedEvent event) {
        System.out.println("something should happen");
        if (event.getProject() != null) {
            // listening to the current project
            bind(event.getProject());
            updateCurrentPossibleTagList();

        }
        currentProject = event.getProject();
    }

    private void bind(Project newProject) {
        if (currentProject != null) {
            currentProject.hasChangedProperty().removeListener(onProjectChangedListener);
        }
        newProject.hasChangedProperty().addListener(onProjectChangedListener);
    }

    public void updateCurrentPossibleTagList() {
        System.out.println("updating !");
        if (currentProject == null) {
            return;
        }

        // running the search in the backgroudn
        Task<Collection<String>> task = new Task<Collection<String>>() {
            @Override
            protected Collection<String> call() throws Exception {

                return projectManagerService.getAllPossibleTag(currentProject);
            }

        };
        
        // updating through the FX Thread when over
        task.setOnSucceeded(event->{
            possibleTags.clear();
             possibleTags.addAll(task.getValue());
        });
        
        // starting the thread
        ImageJFX.getThreadPool().submit(task);

        // clearing the field
        
    }

    private final ChangeListener<Boolean> onProjectChangedListener = (obs, newValue, oldValue) -> {
        
        updateCurrentPossibleTagList();
    };

    //TODO:
    // on project changed -> update the whole list, undind to the old list, bind to the new list
    // 
}
