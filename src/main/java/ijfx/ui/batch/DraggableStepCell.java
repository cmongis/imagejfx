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
import java.util.function.Consumer;
import javafx.beans.Observable;
import mongis.utils.DraggableListCell;
import org.scijava.Context;

/**
 *
 * @author cyril
 */
public class DraggableStepCell extends DraggableListCell<WorkflowStep> {
    
    WorkflowStepController ctrl = new WorkflowStepController();
    Consumer<WorkflowStep> deleteHandler;
  

    public DraggableStepCell(Context context, Consumer<WorkflowStep> deleteHandler) {
        super();
       
        // functionnal interface that call the delete function of this object
        ctrl.setDeleteHandler(deleteHandler);
        this.deleteHandler = deleteHandler;
        context.inject(ctrl);
    }

    

    @Override
    protected void onItemChanged(Observable obs, WorkflowStep oldValue, WorkflowStep newValue) {
        if(newValue == null) {
            setGraphic(null);
        }
        else {
            ctrl.setItem(newValue);
            setGraphic(ctrl);
        }
    }
    
}
