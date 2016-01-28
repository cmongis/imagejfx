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
package ijfx.ui.project_manager.projectview;

import ijfx.core.project.Project;
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
public class DefaultProjectViewService extends AbstractService implements ImageJService{
    
    HashMap<Project,ProjectDisplay> projectDisplayMap = new HashMap<>();
    
    
    @Parameter
    EventService eventService;
    
    public ProjectDisplay addProject(Project project) {
        
        ProjectDisplay projectDisplay = new DefaultProjectDisplay(project);
        projectDisplayMap.put(project, projectDisplay);
        
        eventService.publishLater(new ProjectDisplayCreatedEvent(projectDisplay));
        
        return projectDisplay;
    }
    
    public ProjectDisplay getProjectDisplay(Project project) {
        if(projectDisplayMap.containsKey(project) == false) {
            return addProject(project);
        }
        else {
            return projectDisplayMap.get(project);
        }
    }
    
    
    @EventHandler
    public void onProjectCreated(ProjectCreatedEvent event) {
        addProject(event.getProject());
        
        getProjectDisplay(event.getProject()).add(new  DefaultPlaneSet(event.getProject()));
        
    }
    
    @EventHandler
    public void onProjectClosed(ProjectCloseEvent event) {
        projectDisplayMap.remove(event.getProject());
    }
    
   
    
}
