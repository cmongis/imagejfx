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
import mongis.utils.DraggableListCell;
import org.scijava.Context;

/**
 *
 * @author cyril
 */
public class DraggableStepCell extends DraggableListCell<WorkflowStep> {
    
    WorkflowStepController ctrl = new WorkflowStepController();
    ActionHandler<WorkflowStep> deleteHandler;
  

    public DraggableStepCell(Context context, ActionHandler<WorkflowStep> deleteHandler) {
        super();
       
        // functionnal interface that call the delete function of this object
        ctrl.setDeleteHandler(deleteHandler);
        this.deleteHandler = deleteHandler;
        context.inject(ctrl);
    }

    @Override
    public void updateItem(WorkflowStep step, boolean isEmpty) {
        super.updateItem(step, isEmpty);
        if (isEmpty) {
            setGraphic(null);
        }
        if (step == null) {
            setGraphic(null);
            return;
        } else {
            if (ctrl.getItem() != step) {
                ctrl.setItem(step);
            }
            setGraphic(ctrl);
        }
    }
    
}
