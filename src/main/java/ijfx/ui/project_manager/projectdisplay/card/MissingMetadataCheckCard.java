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
package ijfx.ui.project_manager.projectdisplay.card;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import ijfx.core.project.Project;
import ijfx.core.project.imageDBService.PlaneDB;
import ijfx.core.project.predicate.MissingHierarchyMetaData;
import ijfx.ui.project_manager.projectdisplay.DefaultPlaneSet;
import ijfx.ui.project_manager.projectdisplay.PlaneSet;
import ijfx.ui.project_manager.projectdisplay.ProjectDisplay;
import ijfx.ui.project_manager.projectdisplay.ProjectDisplayService;
import java.util.List;
import java.util.stream.Collectors;
import javafx.concurrent.Task;
import mongis.utils.TaskButtonBinding;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = ProjectCard.class, priority=5)
public class MissingMetadataCheckCard extends MessageActionCardBase {

    @Parameter
    ProjectDisplayService projectDisplayService;
    
    public MissingMetadataCheckCard() {
        super("Warning","Some planes don't contain the metadata indicated in your \"Plane Organisation\". Do you want to set them all to 0 ?",FontAwesomeIcon.WARNING);
        
        addButton("Show planes",FontAwesomeIcon.EYE, this::showIncompletePlanes, "warning");
    }
    
    @Override
    protected void configureBinding(TaskButtonBinding binding) {
        binding.setTextBeforeTask("Set all to 0")
                .setTextWhenSucceed("Done");
                
    }

    @Override
    protected Task onClick(TaskButtonBinding binding) {
        
        return null;
        
    }
    
    
    
    @Override
    public Boolean shouldDisplay(Project project) {
        
        return project
                .getImages()
                .parallelStream()
                .filter(new MissingHierarchyMetaData(project.getHierarchy()))
                .count() == 0;
        
        
    }
    
    public List<PlaneDB> getImagesWithMissingMetadata(Project project) {
        return project.getImages()
                .parallelStream()
                .filter(new MissingHierarchyMetaData(project.getHierarchy()))
                .collect(Collectors.toList());
    }

    public void showIncompletePlanes() {
        ProjectDisplay display = projectDisplayService.getActiveProjectDisplay();
        PlaneSet planeSet = new DefaultPlaneSet("Incompletes", display, getImagesWithMissingMetadata(display.getProject()));
        display.getPlaneSetList().add(planeSet);
    }
  
}
