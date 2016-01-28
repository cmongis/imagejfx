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
package ijfx.ui.project_manager.action;

import ijfx.core.metadata.GenericMetaData;
import ijfx.core.project.DefaultPlaneModifierService;
import ijfx.core.project.Project;
import ijfx.core.project.DefaultProjectManagerService;
import ijfx.core.project.ProjectModifierService;
import ijfx.core.project.ProjectToImageJService;
import ijfx.ui.project_manager.project.ProjectViewModel;
import ijfx.ui.project_manager.project.ProjectViewModelService;
import ijfx.ui.project_manager.search.PopoverToggleButton;
import ijfx.ui.main.Localization;
import ijfx.service.uicontext.UiContextService;
import java.io.IOException;
import java.util.Arrays;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.util.Pair;
import org.controlsfx.control.PopOver;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import mongis.utils.FXUtilities;
import ijfx.ui.UiPlugin;
import ijfx.ui.UiConfiguration;
import ijfx.ui.UiContexts;
import ijfx.ui.main.ImageJFX;
import ijfx.ui.notification.NotificationService;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = UiPlugin.class)
@UiConfiguration(id = "browser-bottom-action-bar", localization = Localization.BOTTOM_CENTER, context = "project-manager+plane-selected")
public class BottomActionBar extends BorderPane implements UiPlugin {

    @FXML
    ToggleButton addMetaDataButton;

    @FXML
    ToggleButton addTagButton;

    @FXML
    Button openButton;

    @Parameter
    DefaultProjectManagerService projectManager;

    @Parameter
    ProjectModifierService modifierService;

    @Parameter
    DefaultPlaneModifierService planeModifierService;

    @Parameter
    ProjectToImageJService projectToImageJService;

    @Parameter
    ProjectViewModelService projectViewModelService;

    @Parameter
    UiContextService contextService;

    @Parameter
    Context context;

    @Parameter
    NotificationService notificationService;

    String lastTag;

    ObservableSet<String> possibleTags = FXCollections.observableSet();

    AddTagPanel addTagPanel;

    AddMetaDataPanel addMetaDataPanel;

    Logger logger = ImageJFX.getLogger();

    public BottomActionBar() throws IOException {
        super();

        FXUtilities.injectFXML(this);

    }

    @Override
    public Node getUiElement() {
        return this;
    }

    public void onCurrentProjectChanged(Observable observable, Project oldProject, Project newProject) {

    }

    @Override
    public UiPlugin init() {

        projectManager.currentProjectProperty().addListener(this::onCurrentProjectChanged);

        addTagPanel = new AddTagPanel(context);

        addMetaDataPanel = new AddMetaDataPanel(context);

        PopoverToggleButton.bind(addTagButton, addTagPanel, PopOver.ArrowLocation.BOTTOM_CENTER);
        PopoverToggleButton.bind(addMetaDataButton, addMetaDataPanel, PopOver.ArrowLocation.BOTTOM_CENTER);

        addTagPanel.setAction(this::onTagAction);
        addMetaDataPanel.setAction(this::onMetaDataAction);

        return this;
    }

    public Project getCurrentProject() {
        return projectManager.currentProjectProperty().get();
    }

    @FXML
    public void openSelection() {
        ProjectViewModel projectViewModel = projectViewModelService.getViewModel(projectManager.getCurrentProject());
        projectToImageJService.convert(projectManager.getCurrentProject(), projectViewModel.getCheckedChildren().get(0));
    }

    @FXML
    public void processSelection() {
        contextService.leave(UiContexts.PROJECT_MANAGER, UiContexts.IMAGEJ);
        contextService.enter(UiContexts.PROJECT_BATCH_PROCESSING);
        contextService.update();
    }

    public Boolean onTagAction(String tags) {

        try {
            planeModifierService.addTag(getCurrentProject(), getCurrentProject().getSelection(), Arrays.asList(tags.split(",")));
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error when adding tag", e);
            return false;
        }

    }

    public Boolean onMetaDataAction(Pair<String, String> keyValue) {

        try {
            String key = keyValue.getKey();
            String value = keyValue.getValue();

            planeModifierService.addMetaData(getCurrentProject(), getCurrentProject().getSelection(), new GenericMetaData(key, value));
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error when adding metadata", e);
            return false;
        }
      
    }
}
