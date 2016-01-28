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

import ijfx.core.project.event.ProjectCreatedEvent;
import ijfx.ui.UiConfiguration;
import ijfx.ui.UiContexts;
import ijfx.ui.UiPlugin;
import ijfx.ui.main.Localization;
import javafx.scene.Node;
import javafx.scene.control.TreeView;
import org.scijava.event.EventHandler;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type=UiPlugin.class)
@UiConfiguration(id="project-display-test",localization = Localization.RIGHT,context=UiContexts.PROJECT_MANAGER)
public class ProjectDisplayTest extends TreeView implements UiPlugin
{

    @Parameter
    DefaultProjectViewService projectViewService;
    
    
    @Override
    public Node getUiElement() {
        return this;
    }

    @Override
    public UiPlugin init() {
        return this;
    }

    @EventHandler
    public void onProjectCreated(ProjectCreatedEvent event) {
        System.out.println("a project has been created !");
        
       
        
        projectViewService
                .getProjectDisplay(event.getProject())
                .forEach(planeSet->System.out.println(planeSet));
        
        setRoot(projectViewService.getProjectDisplay(event.getProject()).get(0).getRoot());
        
        
    }
    
}
