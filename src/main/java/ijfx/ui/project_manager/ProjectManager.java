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
import ijfx.ui.project_manager.project.BrowserUIService;
import ijfx.ui.project_manager.project.ProjectMainPane;
import ijfx.ui.project_manager.project.MainProjectController;
import ijfx.ui.project_manager.search.SearchBar;
import ijfx.ui.context.animated.Animation;
import ijfx.ui.main.ImageJFX;
import ijfx.ui.main.Localization;
import ijfx.service.uicontext.UiContextService;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import ijfx.ui.UiPlugin;
import ijfx.ui.UiConfiguration;
import ijfx.ui.UiContexts;

/**
 * ProjectManager class. This is the main controller of the browser UI.
 *
 *
 * @author Cyril Quinton
 *
 */
@Plugin(type = UiPlugin.class)
@UiConfiguration(id = UiContexts.PROJECT_MANAGER, context = UiContexts.PROJECT_MANAGER, localization = Localization.CENTER)
public class ProjectManager extends BorderPane implements Initializable, UiPlugin {

    @FXML
    private TabPane projectViewTabPane;

    @FXML
    private ToolBar toolBar;

    @FXML
    private Button openProjectButton;
    @FXML
    private Button newProjectButton;
    @FXML
    private Button addImagesButton;
    @FXML
    private Button addImagesFromFolderButton;
    @FXML
    private Button undoButton;
    @FXML
    private Button redoButton;

    Logger logger = ImageJFX.getLogger();

    private final BooleanProperty undoDisableProperty;
    private final BooleanProperty redoDisableProperty;
    private final StringProperty undoMessageProperty;
    private final StringProperty redoMessageProperty;
    /**
     * Used to set the undoMessageProperty whenever the Invoker of the current
     * Project changes its undoCommandNameProperty.
     */
    private final ChangeListener<String> undoCmdNameListener;
    /**
     * Used to set the redoMessageProperty whenever the Invoker of the current
     * Project changes its redoCommandNameProperty.
     */
    private final ChangeListener<String> redoCmdNameListener;
    /**
     * HashMap that used to store a Tab instance associated with a project
     */
    private final HashMap<Project, Tab> tabMap = new HashMap<>();
    /**
     * The Project Tab selection model. It gives information on what tab is
     * selected.
     */
    private SingleSelectionModel<Tab> projectSelectionModel;

    private final ResourceBundle rb;

    //*********set of services*****************
    @Parameter
    private ProjectManagerService projectManager;

    @Parameter
    BrowserUIService browserUiService;

    @Parameter
    Context context;

    @Parameter
    UiContextService contextService;

    SearchBar queryBar;

    BooleanProperty isProjectOpen = new SimpleBooleanProperty(false);

    public ProjectManager() {

        // get the resource bundle for string propoerties
        rb = FXUtilities.getResourceBundle();

        // Creating the properties
        undoMessageProperty = new SimpleStringProperty();
        redoMessageProperty = new SimpleStringProperty();
        undoDisableProperty = new SimpleBooleanProperty(true);
        redoDisableProperty = new SimpleBooleanProperty(true);

        undoCmdNameListener = (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            setUndoCmdName(newValue);
        };
        redoCmdNameListener = (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            setRedoCmdName(newValue);
        };

        try {
            // injecting the FXML
            //FXUtilities.loadView(getClass().getResource("ProjectManager.fxml"), this, true);
            FXUtilities.injectFXML(this);
        } catch (IOException ex) {
            ImageJFX.getLogger();
        }
    }

    @Override
    public Node getUiElement() {
        return this;
    }

    @Override
    public UiPlugin init() {

        // listen for changes in the list of opened projects
        projectManager.getProjects().addListener(this::handleProjectListChange);

        // listen for change of the current project
        projectManager.currentProjectProperty().addListener(this::handleCurrentProjectChange);

        // create the query bar
        // queryBar = new QueryBar(context);
        queryBar = new SearchBar(context);
        // add the query bar to the 
        toolBar.getItems().add(queryBar);
        // bind the "visibleProperty" of these items to the isProjectOpen property
        showItemsOnlyWhenAProjectIsOpen(addImagesButton, addImagesFromFolderButton, undoButton, redoButton, queryBar);

        // create a tab for each of these project
        projectManager.getProjects().forEach(project -> {
            createProjectTab(project);
        });

        // change the value of isProjectOpen to true if projects are already opened
        isProjectOpen.setValue(projectManager.hasProject());

        // toolBar.visibleProperty().bind(projectManager.currentProjectProperty().isNotNull());
        for (Node node : new Node[]{
            queryBar, addImagesButton, addImagesFromFolderButton, undoButton, redoButton
        }) {
            node.visibleProperty().bind(projectManager.currentProjectProperty().isNotNull());
        }

        queryBar.visibleProperty().addListener((obs, oldVaue, newValue) -> {

            if (newValue) {
                Animation.QUICK_EXPAND
                        .configure(queryBar, ImageJFX.getAnimationDurationAsDouble())
                        .play();
            }
        });

        return this;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // adding events to undo and redo buttons
        undoButton.disableProperty().bind(undoDisableProperty);
        redoButton.disableProperty().bind(redoDisableProperty);

        // should be done through FXML I guess...
        undoButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                undo();
            }
        });
        redoButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                redo();
            }
        });

        // Creating a tooltips and bind to the message
        // for the undo and redo buttons so a tooltip
        // appears explaining what will be the action
        Tooltip toolTip = new Tooltip();
        toolTip.textProperty().bind(undoMessageProperty);
        undoButton.setTooltip(toolTip);
        toolTip = new Tooltip();
        toolTip.textProperty().bind(redoMessageProperty);
        redoButton.setTooltip(toolTip);

        // ???
        projectSelectionModel = projectViewTabPane.getSelectionModel();
        projectSelectionModel.selectedItemProperty().addListener(new ChangeListener<Tab>() {

            @Override
            public void changed(ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) {
                if (newValue != null && newValue.getUserData() != null && newValue.getUserData() instanceof Project) {
                    //listeners won't be notify if the current project is 
                    //already equal to the one corresponding to the tab. 
                    if (projectManager != null) {
                        projectManager.setCurrentProject((Project) newValue.getUserData());
                    }
                }
            }
        });

        // when the button are enabled, a animation is triggered
        undoButton.disableProperty().addListener((obs, old, newValue) -> {
            if (newValue == false) {
                Animation.QUICK_EXPAND.configure(undoButton, ImageJFX.getAnimationDurationAsDouble());

            }
        });

        redoButton.disableProperty().addListener((obs, old, newValue) -> {
            if (newValue == false) {
                Animation.QUICK_EXPAND.configure(redoButton, ImageJFX.getAnimationDurationAsDouble());

            }
        });

        //*******Setting toolTips *********
        addImagesButton.setTooltip(new Tooltip(rb.getString("addImages")));
        newProjectButton.setTooltip(new Tooltip(rb.getString("createNewProject")));
        openProjectButton.setTooltip(new Tooltip(rb.getString("openAnExistingProject")));

        if (projectManager != null) {
            isProjectOpen.setValue(projectManager.hasProject());
        }
    }

    private void showItemsOnlyWhenAProjectIsOpen(Node... nodes) {
        for (Node n : nodes) {
            n.visibleProperty().bind(isProjectOpen);
        }
    }

    @FXML
    public void undo() {
        projectManager.currentProjectProperty().get().getInvoker().undo();
    }

    @FXML
    public void redo() {
        projectManager.currentProjectProperty().get().getInvoker().redo();
    }

    @FXML
    public void openProject() {
        browserUiService.openProject();
    }

    @FXML
    public void addImagesFromFiles() {
        browserUiService.addImageAction();
    }

    @FXML
    public void addImagesFromFolder() {
        browserUiService.addImageFromDirectoryAction();
    }

    @FXML
    public void newProject() {
        browserUiService.newProject();
    }

    /*
     private void updateListenedProject(Project oldVal, Project newVal) {
     if (oldVal != null) {
     undoDisableProperty.unbind();
     redoDisableProperty.unbind();
     Invoker invoker = oldVal.getInvoker();
     invoker.undoNameProperty().removeListener(undoCmdNameListener);
     invoker.redoNameProperty().removeListener(redoCmdNameListener);
     }
     if (newVal != null) {
     Invoker invoker = newVal.getInvoker();
     undoDisableProperty.bind(invoker.undoDisableProperty());
     redoDisableProperty.bind(invoker.redoDisableProperty());
     invoker.undoNameProperty().addListener(undoCmdNameListener);
     invoker.redoNameProperty().addListener(redoCmdNameListener);
     }
     }
     */
    private void setUndoCmdName(String name) {
        undoMessageProperty.set(name == null ? "" : rb.getString("undo") + " " + name);
    }

    private void setRedoCmdName(String name) {
        redoMessageProperty.set(name == null ? "" : rb.getString("redo") + " " + name);
    }

    private void removeProjectTab(Project removedProject) {
        Tab tab = tabMap.get(removedProject);
        if (tab != null) {
            Pane pane = (Pane) getController(tab);
            FXUtilities.emptyPane(pane);
            projectViewTabPane.getTabs().remove(tab);
        }
    }

    private void createProjectTab(Project project) {

        Tab tab = new Tab(project.toString());
        tab.setContent(new ProjectMainPane(project, context));
        tab.setUserData(project);
        tab.setGraphic(new ProjectTabController(project, context));
        tabMap.put(project, tab);
        Platform.runLater(() -> {
            projectViewTabPane.getTabs().add(tab);
            projectSelectionModel.select(tab);
        });
    }

    private MainProjectController getController(Tab tab) {
        if (tab.getContent() instanceof MainProjectController) {
            return (MainProjectController) tab.getContent();
        }
        return null;
    }

    private void handleProjectListChange(ListChangeListener.Change<? extends Project> c) {
        while (c.next()) {
            if (c.wasAdded()) {
                for (Project newProject : c.getAddedSubList()) {
                    if(newProject != null)
                    createProjectTab(newProject);
                }
                //the list was empty before
                if (c.getFrom() == 0 && c.getTo() == c.getList().size()) {
                    //handleNonEmptyProjectList();
                }
            } else if (c.wasRemoved()) {
                for (Project removedProject : c.getRemoved()) {
                    removeProjectTab(removedProject);
                }
                if (c.getList().isEmpty()) {
                    //handleEmptyProjectList();
                }
            }
        }
    }

    private void handleCurrentProjectChange(ObservableValue<? extends Project> observable, Project oldValue, Project newValue) {

        if (newValue == null) {
            undoDisableProperty.unbind();
            redoDisableProperty.unbind();
            return;
        };
        projectSelectionModel.select(tabMap.get(newValue));

        undoDisableProperty.bind(newValue.getInvoker().undoDisableProperty());
        redoDisableProperty.bind(newValue.getInvoker().redoDisableProperty());

        Animation.QUICK_EXPAND.configure(undoButton, 500).play();

    }

    private void showRuleManager() {
        Stage stage = ProjectManagerUtils.createDialogWindow(getScene().getWindow(),
                new RulesController(projectManager.currentProjectProperty().get(), context),
                rb.getString("ruleManager"));
        stage.show();
    }

}
