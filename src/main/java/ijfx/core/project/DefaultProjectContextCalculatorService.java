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

import ijfx.service.uicontext.UiContextService;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = Service.class)
public class DefaultProjectContextCalculatorService extends AbstractService implements ProjectContextCalculatorService{

    
    public static final String PROJECT_OPEN = "project-open";
    public static final String PROJECT_PLANE_SELECTED = "plane-selected";
    public static final String PROJECT_EMPTY = "project-empty";
    
    
    @Parameter
    Context context;
    
    @Parameter
    private UiContextService contextService;
    
  
    
    private ProjectManagerService projectService;
    
    @Override
    public ProjectContextCalculatorService updateContext(Project project) {

        
        if(project == null) {
            contextService.leave(PROJECT_OPEN,PROJECT_EMPTY,PROJECT_PLANE_SELECTED);
            contextService.update();
            return
            this;
        }
        
        // check if project are open
       contextService.toggleContext(PROJECT_OPEN, getProjectService().getProjects().size() > 0);
       
       // check if the project is empty of not
       contextService.toggleContext(PROJECT_EMPTY, project.getImages().size() == 0);
       
       // check if any plane is selected
       contextService.toggleContext(PROJECT_PLANE_SELECTED, project.getSelection().size() > 0);
       
       contextService.update();
        
        return this;
    }
    
    public ProjectManagerService getProjectService() {
        if(projectService == null) {
           projectService =  context.getService(ProjectManagerService.class);
        }
        return projectService;
    }

   
    
}
