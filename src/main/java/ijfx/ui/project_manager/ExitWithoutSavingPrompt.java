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
package ijfx.ui.project_manager;

import mongis.utils.FXUtilities;
import ijfx.core.project.Project;
import ijfx.core.project.ProjectManagerService;
import ijfx.core.project.DefaultProjectManagerService;
import ijfx.service.uicontext.UiContextService;
import ijfx.ui.main.ImageJFX;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

/**
 *
 * @author Cyril Quinton
 */
public class ExitWithoutSavingPrompt extends GridPane implements Initializable {

    private final Project project;
    private final ProjectManagerService pm;
    private final IOProjectUIService2 saveUI;
    @FXML
    private Button cancelButton;
    @FXML
    private Button closeWithoutSavingButton;
    @FXML
    private Button saveButton;
    @FXML
    private Label messageLabel;

    public ExitWithoutSavingPrompt(Project project, UiContextService contextService) {
        this.project = project;
        pm = contextService.getContext().getService(DefaultProjectManagerService.class);
        saveUI = contextService.getContext().getService(IOProjectUIService2.class);
        FXUtilities.loadView(getClass().getResource("ExitWithoutSavingPrompt.fxml"), this, true);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                close();
            }
        });
        closeWithoutSavingButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                removeProjectAndClose();

            }
        });
        saveButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                try {
                    boolean saved = saveUI.saveProject(project, getScene().getWindow());
                    if (saved) {
                    removeProjectAndClose();
                    }
                } catch (IOException ex) {
                   ImageJFX.getLogger().log(Level.SEVERE, null, ex);
                }
            }
        });
        String message = resources.getString("ProjectNotSavedDirection") + " " + project
                + "\n" + resources.getString("ProjectNotSavedQuestion");
        messageLabel.setText(message);

    }

    private void removeProjectAndClose() {
        pm.removeProject(project);
        close();
    }

    private void close() {
        FXUtilities.close(this);
    }

}
