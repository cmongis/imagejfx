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
import ijfx.core.project.AnnotationRuleImpl;
import ijfx.core.project.event.ProjectActivatedEvent;
import ijfx.core.project.query.UniversalSelector;
import ijfx.ui.UiConfiguration;
import ijfx.ui.UiPlugin;
import ijfx.ui.main.ImageJFX;
import ijfx.ui.main.Localization;
import java.io.IOException;
import java.util.logging.Level;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import mongis.utils.FXUtilities;
import mongis.utils.ListCellController;
import org.scijava.Context;
import org.scijava.event.EventHandler;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = UiPlugin.class)
@UiConfiguration(id="rule-list-view",context="project-rule-edition",localization=Localization.CENTER)
public class RuleListViewer extends BorderPane implements UiPlugin{
    
    @FXML
    ListView<AnnotationRule> listView;
    
    @Parameter
    Context context;
    
    
    
    public RuleListViewer()  {
        try {
            FXUtilities.injectFXML(this);
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
        
        return this;
    }
    
     public ListCellController<AnnotationRule> createController() {
        
        
        ListCellController<AnnotationRule> ctrl = new RuleCellController();
        context.inject(ctrl);
        return ctrl;
        
        
        
    }
     
     @EventHandler
     public void onProjectActivated(ProjectActivatedEvent event) {
         listView.setItems(event.getProject().getAnnotationRules());
     }
   
    
    
    
}
