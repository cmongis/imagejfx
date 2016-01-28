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

import ijfx.core.hash.HashService;
import ijfx.core.project.command.AddPlaneCommand;
import ijfx.core.project.command.Command;
import ijfx.core.project.command.Invoker;
import ijfx.core.project.command.SelectImageCommand;
import ijfx.core.project.command.SetHierarchyCommand;
import ijfx.core.project.hierarchy.MetaDataHierarchy;
import ijfx.core.project.imageDBService.ImageReferenceImpl;
import ijfx.core.project.imageDBService.PlaneDB;
import ijfx.ui.main.ImageJFX;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.TreeItem;
import static jdk.nashorn.internal.runtime.Debug.id;
import org.scijava.Priority;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 *
 * @author Cyril Quinton
 */
@Plugin(type = Service.class, priority = Priority.LOW_PRIORITY)
public class DefaultProjectModifierService extends AbstractService implements HierarchySetter, ProjectModifierService {

    
    Logger logger = ImageJFX.getLogger();
    
    @Parameter
    ProjectContextCalculatorService projectContextService;
    
    @Parameter
    HashService hashService;
    
    
    
    
    @Override
    public List<String> getPossibleNewHierarchyKey(Project project) {
        List<String> keys = project.getMetaDataKeys();
        
        /*
        for (String hierarchyKey : project.getHierarchy()) {
            keys.remove(hierarchyKey);
        }*/
        return keys;
    }

    @Override
    public void setHierarchy(Project project, List<String> hierarchyKeys) {
        //check if hierarchy is different
        boolean same = true;
        int size = hierarchyKeys.size();
        if (size != project.getHierarchy().getSize()) {
            same = false;
        } else {
            for (int i = 0; i < size; i++) {
                if (!hierarchyKeys.get(i).equals(project.getHierarchy().get(i))) {
                    same = false;
                    break;
                }
            }
        }
        if (!same) {
            project.getInvoker().executeCommand(new SetHierarchyCommand(project, hierarchyKeys));
        }
    }

    public void selectAll(Project project) {
        Command unSelectCmd = new SelectImageCommand(project, project.getSelection(), false);
        Command selectCmd = new SelectImageCommand(project, project.getImages(), true);
        Invoker.executeCommandList(Arrays.asList(unSelectCmd, selectCmd), ProjectManagerService.rb.getString("selectAll"), project.getInvoker());
    }

    public void deselectAll(Project project) {
        Command deselectCmd = new SelectImageCommand(project, project.getSelection(), false);
        deselectCmd.setName(ProjectManagerService.rb.getString("deselectAll"));
        project.getInvoker().executeCommand(deselectCmd);
    }

    @Override
    public void removePlaneFromProject(Project project, PlaneDB plane) {
        project.getInvoker().executeCommand(new AddPlaneCommand(project, plane, false));
    }

    @Override
    public void removePlaneFromProject(Project project, List<PlaneDB> planes) {
        project.getInvoker().executeCommand(new AddPlaneCommand(project, planes, false));
    }

    @Override
    public void removeSubPlanes(Project project, TreeItem rootItem) {
        List<PlaneDB> planes = getPlaneFromTree(rootItem);
        if (!planes.isEmpty()) {
            removePlaneFromProject(project, planes);
        }
    }

    @Override
    public void selectPlane(Project project, List<PlaneDB> planes, boolean select) {
        if (!planes.isEmpty()) {
            if (planes.size() == 1) {
                selectPlane(project, planes.get(0), select);
            } else {
                executeCmd(project, new SelectImageCommand(project, planes, select));
            }
        }
        
       
    }

    @Override
    public void selectPlane(Project project, List<PlaneDB> planesToUnselect, List<PlaneDB> planesToSelect) {
        if (planesToUnselect.isEmpty() || planesToSelect.isEmpty()) {
            if (planesToUnselect.isEmpty() && planesToSelect.isEmpty()) {
                return;
            }
            boolean select = planesToUnselect.isEmpty();
            List<PlaneDB> planeList = select ? planesToSelect : planesToUnselect;
            selectPlane(project, planeList, select);
        } else {
            Command deselectCmd = new SelectImageCommand(project, planesToUnselect, false);
            Command selectCmd = new SelectImageCommand(project, planesToSelect, true);
            Invoker.executeCommandList(Arrays.asList(deselectCmd, selectCmd), ProjectManagerService.rb.getString("query"), project.getInvoker());
            updateContext(project);
        }
    }

    @Override
    public void selectPlane(Project project, PlaneDB plane, boolean select) {
        if (plane.selectedProperty().get() != select) {
            executeCmd(project, new SelectImageCommand(project, plane, select));
        }
    }

    @Override
    public void selectSubImages(Project project, TreeItem rootItem, boolean select) {
        List<PlaneDB> planeToSelect = new ArrayList<>();
        List<PlaneDB> planes = getPlaneFromTree(rootItem);
        if (!planes.isEmpty()) {
            for (PlaneDB plane : planes) {
                if (plane.selectedProperty().get() != select) {
                    planeToSelect.add(plane);

                }
            }
            selectPlane(project, planeToSelect, select);
        }
        
        
    }
    
    public List<PlaneDB> getPlaneFromTree(TreeItem rootItem) {
        List<PlaneDB> planes = new ArrayList<>();
        List<TreeItem> leaves = MetaDataHierarchy.getLeaves(rootItem);
        for (TreeItem leaf : leaves) {
            if (leaf.getValue() instanceof PlaneDB) {
                planes.add((PlaneDB) leaf.getValue());
            }

        }
        return planes;

    }
     private void executeCmd(Project project, Command cmd) {
        project.getInvoker().executeCommand(cmd);
        
         updateContext(project);
         
        
    }

    
     private void updateContext(Project project) {
         projectContextService.updateContext(project);
     }

    @Override
    public void updatePlaneSource(Project project, PlaneDB plane, File file, long planeIndex) {
        try {
            String hash = hashService.getHash(file);
            plane.setImageReference(new ImageReferenceImpl(hash, file.getAbsolutePath()));
            plane.setPlaneIndex(planeIndex);
            
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error when calculating hash for "+file.getName(), ex);
        }
        
    }
     
}
