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
package ijfx.ui.correction;

import io.datafx.controller.flow.action.ActionTrigger;
import io.datafx.controller.flow.action.BackAction;
import io.datafx.controller.flow.action.LinkAction;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javax.inject.Inject;

/**
 *
 * @author Tuan anh TRINH
 */
public class AbstractWizardController {

    @FXML
    @ActionTrigger("nextAction")
    protected Button nextButton;

    @FXML
    @BackAction
    private Button backButton;

    @FXML
    @LinkAction(EndWorkflow.class)
    private Button finishButton;

    @Inject
    WorkflowModel workflowModel;

    public Button getBackButton() {
        return backButton;
    }

    public Button getFinishButton() {
        return finishButton;
    }
}
