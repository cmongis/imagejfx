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
import ijfx.core.project.imageDBService.PlaneDB;
import ijfx.ui.project_manager.project.TreeItemUtils;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.TreeItem;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 *
 * @author cyril
 */
@Plugin(type=Service.class)
public class DefaultPlaneSelectionService extends AbstractService implements PlaneSelectionService{

    @Parameter
    ProjectDisplayService projectDisplayService;
    
    @Override
    public void selectPlane(Project project, PlaneDB planeDB) {
        
        
        
    }

    @Override
    public void selectPlanes(Project project, List<PlaneDB> planeDB) {
        
        
        
        
       
       
        
        
    }

    @Override
    public void setPlaneSelection(Project project, PlaneDB planeDB, boolean selected) {
        if(selected) {
            
            
            
            
        }
    }

    @Override
    public void setPlaneSelection(Project project, List<PlaneDB> planeList, boolean selected) {
        PlaneSet selectionPlaneSet = getSelectionPlaneSet(project);
        if(selected) {  
            // adding plane list to the tree
            selectionPlaneSet.getPlaneList().addAll(planeList);
        }    
        else {
            selectionPlaneSet.getPlaneList().removeAll(planeList);
        }
    }

    @Override
    public List<PlaneDB> getSelectedPlane(Project project) {
        return getSelectionPlaneSet(project).getPlaneList();
    }
    
    private PlaneSet getSelectionPlaneSet(Project project) {
        ProjectDisplay projectDisplay = projectDisplayService.getProjectDisplay(project);
        
         PlaneSet selectedPlaneSet = projectDisplay.getPlaneSet(ProjectDisplay.SELECTED_IMAGES);
         
         if(selectedPlaneSet == null) {
             projectDisplay.getPlaneSetList().add(new DefaultPlaneSet(project,ProjectDisplay.SELECTED_IMAGES));
         }
         
         return projectDisplay.getPlaneSet(ProjectDisplay.SELECTED_IMAGES);
    }

    @Override
    public void setPlaneSelection(Project project, TreeItem<PlaneOrMetaData> treeItem, boolean selected) {
        
        List<PlaneDB> children = new ArrayList<>();
        
        
        TreeItemUtils.goThroughLeaves(treeItem, leaf->{
            children.add(leaf.getValue().getPlaneDB());
        });
        
        setPlaneSelection(project, children, selected);

    }
    
}
