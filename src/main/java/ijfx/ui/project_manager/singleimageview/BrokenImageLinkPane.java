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
package ijfx.ui.project_manager.singleimageview;

import ijfx.core.project.Project;
import ijfx.core.project.imageDBService.ImageReference;
import ijfx.core.project.imageDBService.PlaneDB;
import mongis.utils.FXUtilities;
import ijfx.ui.project_manager.IOProjectUIService2;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import org.scijava.Context;
import ijfx.ui.project_manager.ProjectManagerUtils;

/**
 *
 * @author Cyril Quinton
 */
public class BrokenImageLinkPane extends AnchorPane implements Initializable {

    private final Project project;
    private final PlaneDB plane;
    private final Context context;
    private final IOProjectUIService2 projectUIService;
    @FXML
    private Button solveButton;
    @FXML
    private Label messageLabel;

    public BrokenImageLinkPane(Project project, PlaneDB plane, Context context) {
        this.project = project;
        this.plane = plane;
        this.context = context;
        projectUIService = context.getService(IOProjectUIService2.class);
        FXUtilities.loadView(getClass().getResource("BrokenImageLinkPane.fxml"), this, true);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        solveButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                List<ImageReference> ls = new ArrayList<>();
                ls.add(plane.getImageReference());
                projectUIService.openFindLostImageDialogue(ls,
                        getScene().getWindow());
            }
        });
        ProjectManagerUtils.tooltipLabeled(messageLabel);
    }
}
