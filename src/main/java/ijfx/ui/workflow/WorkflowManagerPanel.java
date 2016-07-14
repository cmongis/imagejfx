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
package ijfx.ui.workflow;

import ijfx.service.workflow.MyWorkflowService;
import ijfx.service.workflow.Workflow;
import ijfx.service.workflow.WorkflowIOService;
import java.io.IOException;
import java.util.logging.Level;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import org.scijava.plugin.Parameter;
import mongis.utils.FXUtilities;
import ijfx.ui.main.ImageJFX;
import java.io.File;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.stage.FileChooser;
import mongis.utils.ListCellController;
import mongis.utils.ListCellControllerFactory;
import org.scijava.Context;

public class WorkflowManagerPanel extends BorderPane {

    @FXML
    ListView<Workflow> listView;

    @Parameter
    MyWorkflowService myWorkflowService;

    
    
    @Parameter
    Context context;

    Property<Workflow> selectedWorkflowProperty;

    public WorkflowManagerPanel(Context context) {
        try {
            FXUtilities.injectFXML(this);
            context.inject(this);

            listView.setItems(myWorkflowService.getWorkflowList());
            listView.setCellFactory(new ListCellControllerFactory<>(this::createCell));

        } catch (IOException ex) {
            ImageJFX.getLogger().log(Level.SEVERE, null, ex);
        }
    }

    

    private ListCellController<Workflow> createCell() {
        WorkflowListCellController ctrl = new WorkflowListCellController();
        context.inject(ctrl);
        return ctrl;
    }

  

    @FXML
    public void importWorkflow() {
        
        
        FileChooser fileChooser = new FileChooser();

                File file = null;
                fileChooser.setTitle("Import workflow from JSON");
                //fileChooser.setInitialDirectory(new File(defaultFolder));
                fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("JSON workflow", "json"));
                file = fileChooser.showOpenDialog(null);
               
        if (file != null) {
            myWorkflowService.importWorkflow(file);
        }
    }

    
    public ReadOnlyObjectProperty<Workflow> selectedWorkflowProperty() {
        return listView.getSelectionModel().selectedItemProperty();
    }
    
}
