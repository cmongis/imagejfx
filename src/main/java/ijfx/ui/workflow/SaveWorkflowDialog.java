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

import ijfx.service.workflow.MyWorkflowService;
import ijfx.service.workflow.Workflow;
import ijfx.ui.widgets.TextPromptDialog;
import org.scijava.Context;
import org.scijava.plugin.Parameter;

/**
 *
 * @author cyril
 */
public class SaveWorkflowDialog {
    
    @Parameter
    private Context context;

    @Parameter
    private MyWorkflowService myWorkflowService;
    
    public SaveWorkflowDialog(Context context) {
        context.inject(this);
    }
    
    public void save(Workflow workflow) {
        
        TextPromptDialog textPromptDialog = new TextPromptDialog();
        textPromptDialog
                .getContent()
                .setTitle("Give a name to your workflow : ")
                .setSubtitle("e.g. yeast cell segmentation");
                
        
        String title  = textPromptDialog.showAndWait().orElse(null);
        if(title != null) {
            workflow.setName(title);
            myWorkflowService.addWorkflow(workflow);
        }

    }
    
    
}
