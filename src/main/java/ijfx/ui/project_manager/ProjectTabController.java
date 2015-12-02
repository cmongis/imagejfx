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
package ijfx.ui.project_manager;

import mongis.utils.FXUtilities;
import ijfx.core.project.Project;
import ijfx.core.project.ProjectManagerService;
import ijfx.core.project.DefaultProjectManagerService;
import ijfx.service.uicontext.UiContextService;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import org.scijava.Context;
import org.scijava.plugin.Parameter;

/**
 *
 * @author Cyril Quinton
 */
public class ProjectTabController extends AnchorPane implements Initializable{
    
    private final Project project;
    
    @Parameter
    private IOProjectUIService2 saveService;
    
    
    private ResourceBundle rb;
    
    @FXML
    private Button removeButton;
    
    public ProjectTabController(Project project, Context context) {
        this.project = project;
        
        context.inject(this);
        /*
        pm = contextService.getContext().getService(DefaultProjectManagerService.class);
        this.contextService = contextService;
        saveService = contextService.getContext().getService(IOProjectUIService2.class);
         */
        FXUtilities.loadView(getClass().getResource("ProjectTab.fxml"), this,true);
    }
    
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rb = resources;
     removeButton.setOnAction(new EventHandler<ActionEvent>() {

         @Override
         public void handle(ActionEvent event) {
             removeAction();   }
     });
    }
    private void removeAction(){
       saveService.removeProject(project, getScene().getWindow());
    }
    
}
