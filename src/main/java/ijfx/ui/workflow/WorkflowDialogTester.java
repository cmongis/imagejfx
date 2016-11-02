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
import ijfx.service.workflow.WorkflowIOService;
import ijfx.ui.utils.BaseTester;
import ijfx.ui.widgets.TextPromptDialog;
import org.scijava.Context;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class WorkflowDialogTester extends BaseTester{

    public WorkflowDialogTester() {
        super();
        
        addAction("Workflow selection",this::showDialog);
        addAction("Prompt text",this::promptText);
    }

    
    
    
    
    @Override
    public void initApp() {
        
        
        
        
    }
    
    
    public void showDialog() {
        
        
        Context context = new Context(WorkflowIOService.class, MyWorkflowService.class);
        
        WorkflowSelectionDialog dialog = new WorkflowSelectionDialog(context);
        
        dialog.show();
        
        
    }
    
    public void promptText() {
        
        TextPromptDialog dialog = new TextPromptDialog();
        
        String text = dialog.showAndWait().orElse(null);
        
        System.out.println(text);
        
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
}
