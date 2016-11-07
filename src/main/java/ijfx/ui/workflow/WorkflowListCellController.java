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
package ijfx.ui.workflow;

import ijfx.service.workflow.Workflow;
import ijfx.ui.main.ImageJFX;
import java.io.IOException;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import mongis.utils.FXUtilities;
import mongis.utils.ListCellController;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class WorkflowListCellController extends VBox implements ListCellController<Workflow>{

   @FXML
   Label titleLabel;
   
   @FXML
   Label descriptionLabel;
    
   
   Workflow workflow;
   
    public WorkflowListCellController() {
        
       try {
           FXUtilities.injectFXML(this, "/ijfx/ui/workflow/WorkflowListCell.fxml");
       } catch (IOException ex) {
          ImageJFX.getLogger().log(Level.SEVERE, null, ex);
       }
    }
    
    
    
    @Override
    public void setItem(Workflow t) {
        workflow = t;
        titleLabel.setText(getDisplayedName(t));
        descriptionLabel.setText(t
                .getStepList()
                .stream()
                .map(s->s.getId())
                .collect(Collectors.joining(", ")));
    }

    @Override
    public Workflow getItem() {
        return workflow;
    }
    
    public String getDisplayedName(Workflow w) {
        if(w.getName() == null ||  w.getName().trim().equals("")) {
            w.setName("No title :-(");
            
        }
       
        return w.getName();
    }
    
    
}
