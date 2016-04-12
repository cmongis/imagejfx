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
import ijfx.core.project.command.AddMetaDataCommand;
import ijfx.core.project.command.Command;
import ijfx.core.project.command.CommandList;
import ijfx.core.project.imageDBService.PlaneDB;
import ijfx.core.project.predicate.MissingHierarchyMetaData;
import ijfx.ui.project_manager.projectdisplay.DefaultPlaneSet;
import ijfx.ui.project_manager.projectdisplay.PlaneSet;
import ijfx.ui.project_manager.projectdisplay.ProjectDisplay;
import ijfx.ui.project_manager.projectdisplay.ProjectDisplayService;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import mongis.utils.AsyncCallback;
import mongis.utils.TaskButtonBinding;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = ProjectCard.class, priority = 5)
public class MissingMetadataCheckCard extends MessageActionCardBase {

    @Parameter
    ProjectDisplayService projectDisplayService;

    public MissingMetadataCheckCard() {
        super("Warning", "Some planes are missing metadata indicated in your \"Plane Organisation\". Do you want to set missing metadata to 0 ?", FontAwesomeIcon.WARNING);

        addButton("Show me the planes", FontAwesomeIcon.EYE, this::showIncompletePlanes, "warning");
        addButton("Set to 0 and add it as a rule",FontAwesomeIcon.CALENDAR_CHECK_ALT,this::setAndRule,"success");
    }

    @Override
    protected void configureBinding(TaskButtonBinding binding) {
        binding.setTextBeforeTask("Set them all to 0")
                .setBaseIcon(FontAwesomeIcon.COFFEE)
                .setTextWhenRunning("Applying");
               

    }

    @Override
    protected Task onClick(TaskButtonBinding binding) {
        return new AsyncCallback<Void, Void>()
                .run(this::setEverythingToZero)
                .then(v -> dismissed().setValue(Boolean.TRUE));
    }

    @Override
    public Boolean shouldDisplay(Project project) {
       
        return project
                .getImages()
                .parallelStream()
                .filter(new MissingHierarchyMetaData(project.getHierarchy()))
                .count() > 0;

    }

    public List<PlaneDB> getImagesWithMissingMetadata(Project project) {
        return project.getImages()
                .parallelStream()
                .filter(new MissingHierarchyMetaData(project.getHierarchy()))
                .collect(Collectors.toList());
    }

    public void setEverythingToZero() {

        
        List<PlaneDB> planeWithMissingMetaData = getImagesWithMissingMetadata(getProject());
        List<Command> commands = new ArrayList<>(planeWithMissingMetaData.size() * getProject().getHierarchy().size());
        getImagesWithMissingMetadata(getProject()).parallelStream().forEach(plane -> {
            getProject()
                    .getHierarchy()
                    .stream()
                    .filter(key -> plane.metaDataSetProperty().get(key) == null)
                    .forEach(key -> {
                        commands.add(new AddMetaDataCommand(plane, key, 0));
                    });
        });
        
        
        getProject().getInvoker().executeCommand(new CommandList(commands));
        
        
        

    }

    public void showIncompletePlanes() {
        ProjectDisplay display = projectDisplayService.getActiveProjectDisplay();
        PlaneSet planeSet = new DefaultPlaneSet("Incompletes", display, getImagesWithMissingMetadata(display.getProject()));
        display.getPlaneSetList().add(planeSet);
    }
    
    public void setAndRule() {
        new Alert(AlertType.INFORMATION,"Hehehe...\n\nNot implemented yet...\n\nbut it would have been cool, wouldn't it ?").show();
    }

}
