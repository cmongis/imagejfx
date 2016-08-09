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

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import javafx.scene.Node;
import mongis.utils.CallbackTask;
import org.reactfx.StateMachine;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Tuan anh TRINH
 */
@Plugin(type = Activity.class, name = "imagej", label = "Correction")
public class CorrectionActivity implements Activity {

    @Parameter
    Context context;

    private Node node;

    private static Context staticContext;

    public Flow flow;

    public CorrectionActivity() throws FlowException {
    }

    public void init() {
        if (staticContext == null) {
            staticContext = context;
        }
        if (flow == null) {
            flow = new Flow(WelcomeWorkflow.class)
                    .withLink(WelcomeWorkflow.class, "nextAction", FlatfieldWorkflow.class)
                    .withLink(FlatfieldWorkflow.class, "nextAction", BUnwarpJWorkflow.class)
                    .withLink(BUnwarpJWorkflow.class, "nextAction", ProcessWorkflow.class)
                    .withLink(ProcessWorkflow.class, "nextAction", EndWorkflow.class)
                    .withGlobalLink("finishAction", EndWorkflow.class);
            try {
                node = flow.start();
            } catch (FlowException ex) {
                Logger.getLogger(CorrectionActivity.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

    }

    @Override
    public Node getContent() {
        init();
        return node;
    }

    @Override
    public Task updateOnShow() {
        return new CallbackTask<Void, Void>();
    }

    public static Context getStaticContext() {
        return staticContext;
    }

    public void reset() {
        flow = null;
        init();
        try {
            node = flow.start();
        } catch (FlowException ex) {
            Logger.getLogger(CorrectionActivity.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
