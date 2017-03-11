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
package ijfx.service.ui.choice;

import ijfx.core.Handles;
import ijfx.service.workflow.WorkflowStep;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = Choice.class)
@Handles(type = WorkflowStep.class)
public class WorkflowStepChoice implements Choice<WorkflowStep>{
    
    private final WorkflowStep step;

    public WorkflowStepChoice() {
        this.step = null;
    }

    public WorkflowStepChoice(WorkflowStep step) {
        this.step = step;
    }

    @Override
    public Choice<WorkflowStep> create(WorkflowStep t) {
      return new WorkflowStepChoice(t);
    }

    @Override
    public String getTitle() {
        return step.getId();
    }

    @Override
    public String getDescription() {
        return step.toString();
    }

    @Override
    public PixelRaster getPixelRaster() {
       return null;
    }

    @Override
    public WorkflowStep getData() {
       return step;
    }
    
    
    
    
}
