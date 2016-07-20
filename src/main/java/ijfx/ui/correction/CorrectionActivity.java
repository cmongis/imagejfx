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

import ijfx.ui.activity.Activity;
import io.datafx.controller.flow.Flow;
import io.datafx.controller.flow.FlowException;

import io.datafx.core.DataFXUtils;
//import io.datafx.core.DataFXUtils;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import mongis.utils.CallbackTask;
import mongis.utils.FXUtilities;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Tuan anh TRINH
 */
@Plugin(type = Activity.class, name = "imagej", label = "Correction")
public class CorrectionActivity implements Activity {

    private Node node;

//    WorkflowModel workflowModel;

    public CorrectionActivity() {
//WorkflowModel workflowModel = new WorkflowModel();
        try {
//            this.workflowModel = workflowModel;
            node = new Flow(WelcomeWorkflow.class).withLink(WelcomeWorkflow.class, "finish", EndWorkflow.class).start();
//            System.out.println(this.workflowModel.toString());
        } catch (FlowException ex) {
            Logger.getLogger(CorrectionActivity.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public Node getContent() {
        return node;
    }

    @Override
    public Task updateOnShow() {

        return new CallbackTask<Void, Void>();
    }

}
