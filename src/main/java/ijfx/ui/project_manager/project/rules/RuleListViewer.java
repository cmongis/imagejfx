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
package ijfx.ui.project_manager.project.rules;

import ijfx.core.project.AnnotationRule;
import ijfx.core.project.Project;
import ijfx.core.project.ProjectManagerService;
import ijfx.core.project.event.ProjectActivatedEvent;
import ijfx.core.project.query.QueryService;
import ijfx.service.uicontext.UiContextService;
import ijfx.ui.UiConfiguration;
import ijfx.ui.UiContexts;
import ijfx.ui.UiPlugin;
import ijfx.ui.main.ImageJFX;
import ijfx.ui.main.Localization;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import mongis.utils.FXUtilities;
import mongis.utils.ListCellController;
import mongis.utils.ListCellControllerFactory;
import org.scijava.Context;
import org.scijava.event.EventHandler;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = UiPlugin.class)
@UiConfiguration(id="rule-list-view",context="project-rule-list",localization=Localization.CENTER)
public class RuleListViewer extends BorderPane implements UiPlugin{
    
    @FXML
    ListView<AnnotationRule> listView;
    
    @Parameter
    Context context;
    
    @Parameter
    ProjectManagerService projectManagerService;
    
    @Parameter
    UiContextService uiContextService;
    
    @Parameter
    QueryService queryService;
    
    
    public RuleListViewer()  {
       
        try {
            FXUtilities.injectFXML(this);
            
            listView.setCellFactory(new ListCellControllerFactory(this::createController));
             this.projectChangeListener = new WeakChangeListener<Boolean>(this::onProjectChanged);
        } catch (IOException ex) {
            ImageJFX.getLogger().log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public Node getUiElement() {
        return this;
    }

    @Override
    public UiPlugin init() {
        
        //listView = new AnnotationRuleImpl(new UniversalSelector("actin"), new AddMetaDat)
        if(projectManagerService.getCurrentProject() != null) {
            listView.setItems(projectManagerService.getCurrentProject().getAnnotationRules());
        }
        return this;
    }
    
    ArrayList<RuleCellController> createdControllers = new ArrayList<>();
    
     public ListCellController<AnnotationRule> createController() {
        
        
        RuleCellController ctrl = new RuleCellController();
        context.inject(ctrl);
        ctrl.setDeleteHandler(this::onDeleteRequest);
        ctrl.setApplyHandler(this::onApplyRequest);
        createdControllers.add(ctrl);
        return ctrl;
        
        
        
    }
     
     ChangeListener<Boolean> projectChangeListener;
     
             
     @EventHandler
     public void onProjectActivated(ProjectActivatedEvent event) {
         listView.setItems(event.getProject().getAnnotationRules());
         event.getProject().hasChangedProperty().addListener(projectChangeListener);
     }
   
     public void onProjectChanged(Observable obs, Boolean oldValue, Boolean newValue) {
         createdControllers.forEach(ctrl->ctrl.updateUi());
     }
     
     @FXML
     public void addRule() {
         uiContextService.leave("project-rule-list");
         uiContextService.enter("project-rule-edition");
         uiContextService.update();
     }
    
     
     private void onApplyRequest(AnnotationRule rule) {
         
         System.out.println("I should apply this one...");
         queryService.applyAnnotationRule(getCurrentProject(), rule);
         
         
     }
    
     private void onDeleteRequest(AnnotationRule rule) {
          getCurrentProject().removeAnnotationRule(rule);
     }
     
     private Project getCurrentProject() {
         return projectManagerService.getCurrentProject();
     }
    
     @FXML
     private void backToProjectManager() {
         uiContextService.leave("project-rule-edition");
         uiContextService.enter(UiContexts.PROJECT_MANAGER);
         uiContextService.update();
     }
     
}
