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

import ijfx.ui.main.Localization;
import ijfx.service.history.HistoryService;
import ijfx.service.workflow.WorkflowStep;
import java.io.IOException;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import org.scijava.module.ModuleService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import mongis.utils.EasyCellFactory;
import mongis.utils.FXUtilities;
import mongis.utils.ListCellController;
import ijfx.ui.UiPlugin;
import ijfx.ui.UiConfiguration;

/**
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = UiPlugin.class)
@UiConfiguration(id = "favoritePanel", localization = Localization.BOTTOM_CENTER, context = "imagej")
public class FavoritePanel extends HBox implements UiPlugin {

    @Parameter
    HistoryService editService;

    @Parameter
    ModuleService moduleService;

    @FXML
    ListView<WorkflowStep> listView;

    public FavoritePanel() throws IOException {
        super();

        getStyleClass().add("favorite-panel");

        //FXUtilities.injectFXML(this, "FavoritePanel.fxml");
    }

    @Override
    public Node getUiElement() {
        return this;
    }

    @Override
    public UiPlugin init() {

        editService.getFavoriteList().addListener(this::onListChanged);
        return this;
    }

    public void addStep(WorkflowStep step) {
        FavoriteButton button = new FavoriteButton();
        getChildren().add(button);
    }

    public void onListChanged(ListChangeListener.Change<? extends WorkflowStep> c) {
        c.next();

        c.getAddedSubList().forEach(step -> {
            addStep(step);
        });

    }

    public class FavoriteButton extends Button implements ListCellController<WorkflowStep> {

        WorkflowStep step;

        public FavoriteButton() {
            super();
            setOnAction(this::onAction);
        }

        public FavoriteButton(WorkflowStep t) {
            this();
            setItem(t);
        }

        @Override
        public void setItem(WorkflowStep t) {

            if (t == null) {
                return;
            }
            this.step = t;

            setText(t.getModule().getInfo().getLabel());

        }

        @Override
        public WorkflowStep getItem() {
            return step;
        }

        public void onAction(ActionEvent event) {

            //moduleService.run(getItem().getModule);
        }

    }

}
