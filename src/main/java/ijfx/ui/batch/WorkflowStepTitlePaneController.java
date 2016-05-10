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
package ijfx.ui.batch;

import ijfx.service.workflow.WorkflowStep;
import ijfx.ui.module.ModuleConfigPane;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.TitledPane;
import mongis.utils.FXUtilities;
import mongis.utils.ListCellController;
import org.scijava.Context;
import org.scijava.plugin.Parameter;

/**
 *
 * @author cyril
 */
public class WorkflowStepTitlePaneController extends TitledPane implements ListCellController<WorkflowStep>{

    
    @Parameter
    Context context;
    
    ModuleConfigPane configPane;
    
    WorkflowStep step;

    Consumer<WorkflowStep> deleteHandler;
    
    public WorkflowStepTitlePaneController() {
        try {
            FXUtilities.injectFXML(this,"/ijfx/ui/batch/WorkflowStepControllerTitlePane.fxml");
        } catch (IOException ex) {
            Logger.getLogger(WorkflowStepTitlePaneController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    
    
    @Override
    public void setItem(WorkflowStep t) {
        
        if(t == null) return;
        step = t;
        setText(t.getId());
        getPane().configure(step);
        
        
    }
    
    public ModuleConfigPane getPane() {
        if(configPane == null) {
            configPane = new ModuleConfigPane();
            context.inject(configPane);
            setContent(configPane);
        }
        
        return configPane;
    }

    @Override
    public WorkflowStep getItem() {
        return step;
    }

    public void setDeleteHandler(Consumer<WorkflowStep> deleteHandler) {
        this.deleteHandler = deleteHandler;
    }
    
    
    
}
