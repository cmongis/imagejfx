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
package ijfx.ui.project_manager.projectdisplay;

import ijfx.core.project.Project;
import ijfx.core.project.ProjectManagerService;
import ijfx.core.project.event.ProjectCloseEvent;
import ijfx.core.project.event.ProjectCreatedEvent;
import java.util.HashMap;
import net.imagej.ImageJService;
import org.scijava.event.EventHandler;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;

/**
 *
 * @author cyril
 */
@Plugin(type = ImageJService.class)
public class DefaultProjectDisplayService extends AbstractService implements ProjectDisplayService{
    
    HashMap<Project,ProjectDisplay> projectDisplayMap = new HashMap<>();
    
    
    @Parameter
    EventService eventService;
    
    @Parameter
    ProjectManagerService projectService;
    
    private ProjectDisplay addProject(Project project) {
        
        ProjectDisplay projectDisplay = new DefaultProjectDisplay(project);
        projectDisplayMap.put(project, projectDisplay);
        
        eventService.publishLater(new ProjectDisplayCreatedEvent(projectDisplay));
        
        return projectDisplay;
    }
    
    @Override
    public ProjectDisplay getProjectDisplay(Project project) {
        
        if(project == Project.NO_PROJECT) return ProjectDisplay.NO_DISPLAY;
        
        if(projectDisplayMap.containsKey(project) == false) {
            return addProject(project);
        }
        else {
            return projectDisplayMap.get(project);
        }
    }
    
    
    @EventHandler
    private void onProjectCreated(ProjectCreatedEvent event) {
        addProject(event.getProject());
        
       
        
    }
    
    @EventHandler
    private void onProjectClosed(ProjectCloseEvent event) {
        ProjectDisplay projectDisplay = getProjectDisplay(event.getProject());
        
        projectDisplayMap.remove(projectDisplay.getProject());
        eventService.publishLater(new ProjectDisplayClosedEvent(projectDisplay));
         setActiveProjectDisplay(projectDisplayMap.values().stream().findFirst().orElse(null));
    }

    @Override
    public ProjectDisplay getActiveProjectDisplay() {
        return getProjectDisplay(projectService.getCurrentProject());
    }

    @Override
    public void setActiveProjectDisplay(ProjectDisplay display) {
        projectService.setCurrentProject(display.getProject());
        eventService.publishLater(new ProjectDisplayActived(display));
        
    }
    
   
    
}
