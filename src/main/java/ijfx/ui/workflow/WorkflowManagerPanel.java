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
package ijfx.ui.workflow;

import ijfx.service.workflow.MyWorkflowService;
import ijfx.ui.main.Localization;
import ijfx.service.workflow.Workflow;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import mongis.utils.FXUtilities;
import ijfx.ui.UiPlugin;
import ijfx.ui.UiConfiguration;
import ijfx.ui.main.ImageJFX;

/**
 *
 * @author Cyril MONGIS, 2015
 */

@Plugin(type = UiPlugin.class)
@UiConfiguration(id="workflowManagerPanel",context="batch",localization=Localization.RIGHT)
public class WorkflowManagerPanel extends BorderPane implements UiPlugin{

    
    @FXML
    ListView<Workflow> listView;
    
    @Parameter
    MyWorkflowService myWorkflowService;
    
    public WorkflowManagerPanel() {
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
        
        listView.setItems(myWorkflowService.getWorkflowList());
        
        return this;
    }
        
}
