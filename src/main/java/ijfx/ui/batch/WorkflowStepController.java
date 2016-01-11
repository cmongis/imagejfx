/*
 * /*
 *     This file is part of ImageJ FX.
 *
 *     ImageJ FX is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     ImageJ FX is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with ImageJ FX.  If not, see <http://www.gnu.org/licenses/>. 
 *
 * 	Copyright 2015,2016 Cyril MONGIS, Michael Knop
 *
 */
package ijfx.ui.batch;

import ijfx.ui.module.ModuleConfigPane;
import ijfx.service.workflow.WorkflowStep;
import ijfx.ui.main.ImageJFX;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import org.controlsfx.control.PopOver;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import mongis.utils.FXUtilities;
import mongis.utils.ListCellController;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class WorkflowStepController extends BorderPane implements ListCellController<WorkflowStep> {

    ModuleConfigPane configPane;

    PopOver popover = new PopOver();

    @FXML
    Label titleLabel;

    @FXML
    Button configButton;

    @Parameter
    Context context;

    public WorkflowStepController() {

        try {
            FXUtilities.injectFXML(this);
        } catch (IOException ex) {
           ImageJFX.getLogger().log(Level.SEVERE, null, ex);
        }

    }

    ActionHandler<WorkflowStep> deleteHandler;

    public ActionHandler getDeleteHandler() {
        return deleteHandler;
    }

    public void setDeleteHandler(ActionHandler<WorkflowStep> deleteHandler) {
        this.deleteHandler = deleteHandler;
    }

    WorkflowStep step;

    @Override
    public void setItem(WorkflowStep t) {

        if (configPane == null) {
            configPane = new ModuleConfigPane();
            context.inject(configPane);
            
            popover.setContentNode(configPane);
            //popover.setAnchorLocation(PopupWindow.AnchorLocation.WINDOW_TOP_RIGHT);
            popover.setArrowLocation(PopOver.ArrowLocation.RIGHT_TOP);
        }
        this.step = t;
        
        configPane.configure(step);

        titleLabel.setText(step.getModule().getInfo().getTitle());
    }

    @Override
    public WorkflowStep getItem() {

        return step;
    }

    @FXML
    public void toggleConfigPane() {
        popover.show(configButton);
    }

    @FXML
    public void remove() {
        deleteHandler.execute(step);
    }

}
