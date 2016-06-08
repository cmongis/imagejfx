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
package ijfx.ui.plugin.panel;

import ijfx.ui.main.ImageJFX;
import ijfx.ui.main.Localization;
import ijfx.service.history.HistoryService;
import ijfx.service.workflow.DefaultWorkflow;
import ijfx.service.workflow.WorkflowIOService;
import ijfx.service.workflow.WorkflowStep;
import ijfx.ui.history.HistoryStepCellFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.DialogPrompt;
import org.scijava.ui.UIService;
import ijfx.ui.UiPlugin;
import ijfx.ui.UiConfiguration;
import mongis.utils.FXUtilities;

/**
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = UiPlugin.class)
@UiConfiguration(id = "workflowEditor", localization = Localization.RIGHT, context = "imagej -overlay-selected", order = 2)
public class WorkflowEditor extends TitledPane implements UiPlugin {

    @FXML
    ListView<WorkflowStep> listView;

    @Parameter
    Context context;

    @Parameter
    HistoryService editService;

    @Parameter
    WorkflowIOService workflowIOService;
    
    @Parameter
    UIService uiService;

    @FXML
    StackPane stackPane;

    private final static String SAVE_WORKFLOW = "Save workflow";
    private final static String LOAD_WORKFLOW = "Load workflow";
    private final static String ERROR_MESSAGE = "Error when reading the workflow.";

    public WorkflowEditor() throws IOException {
        super();

        FXUtilities.injectFXML(this);

    }

    @Override
    public Node getUiElement() {
        return this;
    }

    @Override
    public UiPlugin init() {


        listView.setItems(editService.getStepList());

        HistoryStepCellFactory factory = new HistoryStepCellFactory();
        context.inject(factory);

        listView.setCellFactory(factory);

        return this;

    }

    FileChooser chooser;

    private FileChooser getChooser(String title) {
        chooser = null;
        if (chooser == null) {
            chooser = new FileChooser();
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Workflow JSON File", "*.json"));
        }

        return chooser;

    }

    @FXML
    public void saveWorkflow(ActionEvent event) {

        File file = getChooser(SAVE_WORKFLOW).showSaveDialog(null);

        if (file != null) {

            workflowIOService.saveWorkflow(new DefaultWorkflow(editService.getStepList()),file);
        }

    }

    @FXML
    public void loadWorkflow(ActionEvent event) {
        File file = getChooser(LOAD_WORKFLOW).showOpenDialog(null);
        if (file != null) {

            Task<Void> task = new Task() {
                public Void call() {
                    try {

                        updateMessage("Loading workflow...");
                        updateProgress(0,3);
                        Thread.sleep(500);
                        //editService.loadWorkflow(new String(Files.readAllBytes(Paths.get(file.getAbsolutePath()))));
                        editService.setCurrentWorkflow(workflowIOService.loadWorkflow(file));
                        updateProgress(3,3);
                    } catch (Exception ex) {
                        ImageJFX.getLogger().log(Level.SEVERE,null,ex);;
                        uiService.showDialog(ERROR_MESSAGE, DialogPrompt.MessageType.ERROR_MESSAGE);
                    }
                    return null;
                }
            };
            //LoadingScreen.getInstance().submitTask(task, false, stackPane);
            ImageJFX.getThreadQueue().submit(task);

        }

    }

    @FXML
    public void repeatAll() {
        editService.repeatAll();
    }

    @FXML
    void deleteAll() {
        editService.getStepList().clear();
    }

}
