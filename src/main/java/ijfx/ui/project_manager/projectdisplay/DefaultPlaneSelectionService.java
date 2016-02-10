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
import ijfx.service.uicontext.UiContextService;
import ijfx.ui.UiContexts;
import ijfx.ui.project_manager.project.TreeItemUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Platform;
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
    
    @Parameter
    UiContextService uiContextService;
    
    @Override
    public void selectPlane(Project project, PlaneDB planeDB) {
        
        
        setPlaneSelection(project,planeDB,true);
    }

    @Override
    public void selectPlanes(Project project, List<PlaneDB> planeList) {
        
        
        
        setPlaneSelection(project,planeList,true);
       
       
        
        
    }

    @Override
    public void setPlaneSelection(Project project, PlaneDB planeDB, boolean selected) {
        ArrayList<PlaneDB> planes = new ArrayList<>(1);
        planes.add(planeDB);
        setPlaneSelection(project, planes, selected);
    }

    @Override
    public void setPlaneSelection(Project project, List<PlaneDB> planeList, boolean selected) {
        PlaneSet selectionPlaneSet = getSelectionPlaneSet(project);
        
        if(selected) {
            // adding plane list to the tree
            // (only adds it if the plane is not already inside)
            selectionPlaneSet.getPlaneList().addAll(
                    planeList
                            .parallelStream()
                            .filter(plane->selectionPlaneSet.getPlaneList().contains(plane) == false)
                            .collect(Collectors.toList())
            );
            
           
        }    
        else {
            selectionPlaneSet.getPlaneList().removeAll(planeList);
        }
        
        
        
        Platform.runLater(()->{
        for(PlaneDB plane : planeList) {
            plane.select(selected);
        }
        });//.start();
        
        
        updateContext();
    }

    @Override
    public List<PlaneDB> getSelectedPlane(Project project) {
        return getSelectionPlaneSet(project).getPlaneList();
    }
    
    private PlaneSet getSelectionPlaneSet(Project project) {
        ProjectDisplay projectDisplay = projectDisplayService.getProjectDisplay(project);
        
         PlaneSet selectedPlaneSet = projectDisplay.getPlaneSet(ProjectDisplay.SELECTED_IMAGES);
         
         if(selectedPlaneSet == null) {
             projectDisplay.getPlaneSetList().add(new DefaultPlaneSet(projectDisplay,ProjectDisplay.SELECTED_IMAGES));
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

        updateContext();
    }
    
    private void updateContext() {
        
        System.out.println(uiContextService.getActualContextListAsString());
        
        PlaneSet selectedImages  = projectDisplayService.getActiveProjectDisplay().getPlaneSet(ProjectDisplay.SELECTED_IMAGES);
        
        
        
        if(selectedImages != null && selectedImages.getPlaneList().size() > 0) {
            uiContextService.enter(UiContexts.PROJECT_PLANE_SELECTED);
            uiContextService.update();
        }
        else {
            uiContextService.leave(UiContexts.PROJECT_PLANE_SELECTED);
            uiContextService.update();
        }
    }
    
    
    @Override
    public boolean isPlaneSelected(Project project, PlaneDB plane) {
        ProjectDisplay projectDisplay = projectDisplayService.getProjectDisplay(project);
        
        return (projectDisplay.getPlaneSet(ProjectDisplay.SELECTED_IMAGES) != null
                && projectDisplay
                        .getPlaneSet(ProjectDisplay.SELECTED_IMAGES)
                        .getPlaneList()
                        .contains(plane));
        
    }
}
