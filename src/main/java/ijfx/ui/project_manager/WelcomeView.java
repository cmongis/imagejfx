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
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import ijfx.ui.activity.Activity;
import javafx.concurrent.Task;

/**
 *
 * @author Cyril Quinton
 */
@Plugin(type = Activity.class)
//@UiConfiguration(id = "fx-welcome-view", context = "no-project", localization = Localization.CENTER)
public class WelcomeView extends BorderPane implements Activity, Initializable {

    @Parameter
    EventService eventService;
    @FXML
    private Button openProjectButton;
    @FXML
    private Button createNewProjectButton;

    public WelcomeView() {
        FXUtilities.loadView(getClass().getResource("WelcomeView.fxml"), this, true);
    }

    @Override
    public Node getContent() {
        return this;
    }

    @Override
    public Task updateOnShow() {
        return null;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        openProjectButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                eventService.publish(new OpenProjectEvent());
            }
        });
        createNewProjectButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                eventService.publish(new AddProjectEvent());
            }
        });
    }

}
