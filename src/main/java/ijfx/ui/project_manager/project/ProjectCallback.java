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
package ijfx.ui.project_manager.project;

import ijfx.core.project.Project;
import ijfx.core.project.ProjectManagerService;
import javafx.util.Callback;

/**
 *
 * @author cyril
 */
public abstract class ProjectCallback<A,B> implements Callback<A,B> {
    
     protected final ProjectManagerService projectService;

    private Project project;
    
  
    
    public ProjectCallback(ProjectManagerService projectService) {
        this.projectService = projectService;
    }
    
    public ProjectCallback setProject(Project project) {
        this.project = project;
        return this;
    }

    public ProjectManagerService getProjectService() {
        return projectService;
    }
    
    
    
    
    protected Project getProject() {
        if(project == null) {
            return projectService.getCurrentProject();
        }
        else { 
            return project;
        }
    }
    
    //public abstract  A call(B b);
    
}
