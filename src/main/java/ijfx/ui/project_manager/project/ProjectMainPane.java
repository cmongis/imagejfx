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
package ijfx.ui.project_manager.project;

import ijfx.core.project.Project;
import mongis.utils.FXUtilities;
import ijfx.ui.project_manager.BrowsingModel;

import ijfx.core.listenableSystem.Listening;
import ijfx.core.project.ProjectManagerService;
import ijfx.core.project.query.QueryService;
import ijfx.core.project.query.DefaultQueryService;
import ijfx.core.project.query.Selector;
import ijfx.core.project.hierarchy.MetaDataHierarchy;
import ijfx.core.project.imageDBService.PlaneDB;
import ijfx.ui.project_manager.BrowsingModel.ViewMode;
import ijfx.ui.project_manager.FolderOrganisationService;
import ijfx.ui.project_manager.singleimageview.SingleImageViewPane;
import ijfx.ui.project_manager.other.ModifierEditor;
import ijfx.ui.project_manager.other.SelectorEditor;
import ijfx.ui.main.LoadingScreen;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.scijava.Context;
import org.scijava.event.EventHandler;
import org.scijava.plugin.Parameter;

/**
 * FXML Controller class
 *
 * @author Cyril Quinton
 */
public class ProjectMainPane extends BorderPane implements Initializable, Listening, MainProjectController {



    private ProjectViewModel projectSpecificViewModel;
    private final Project project;
  
   
    @FXML
    private StackPane queryStackPane;


    @FXML
    private ToggleButton galleryToggleButton;
    @FXML
    private ToggleButton singleToggleButton;

   

    @FXML
    private Label browserTitleLabel;

    // Added by Mongis Cyril, for better look
    @FXML
    private BorderPane innerPane;

    private final ChangeListener<List<String>> hierarchyListener;
    private final ChangeListener<BrowsingModel.ViewMode> viewModeListener;

    @Parameter
    private Context context;

    @Parameter
    private QueryService queryEngine;

    @Parameter
    FolderOrganisationService folderOrgService;

    @Parameter
    ProjectViewModelService viewModelService;

    @Parameter
    ProjectManagerService projectService;
    
    private final MetaDataHierarchy mainTreeOrganisation;
 
    private final SelectorEditor selectorEditor;


    private Pane displayedBrowser;

    SingleImageViewPane singleImageViewPane;

    IconView folderDisplayBrowser;

    public ProjectMainPane(Project project, Context context) {
        this.project = project;

        //queryEngine = contextService.getContext().getService(DefaultQueryService.class);
        context.inject(this);

        // Listen for change in the tree structure
        hierarchyListener = (ObservableValue<? extends List<String>> observable, List<String> oldValue, List<String> newValue) -> {
            handleHierarchyChange();
        };
        
        // Add the listener to the project
        project.getHierarchy().addListener(hierarchyListener);
        
        // get the view model for the project
        projectSpecificViewModel = context
                .getService(ProjectViewModelService.class).getViewModel(project);
        
        //add a listener for the View Mode
        //TODO: depreacted ths part
        viewModeListener = (ObservableValue<? extends BrowsingModel.ViewMode> observable, BrowsingModel.ViewMode oldValue, BrowsingModel.ViewMode newValue) -> {
            selectViewModeButton(newValue);
            createImageView(newValue);
        };

        // editor for a selector
        selectorEditor = new SelectorEditor(context);
      
        
       

        // projectSpecificViewModel.viewModeProperty().addListener(viewModeListener);
        //create a tree for the dataset
        mainTreeOrganisation = folderOrgService.getFolderCreator(project, FolderOrganisationService.PlaneClass.ALL_PLANE);
        
     
        handleHierarchyChange();
        
        // load the FXML into this controller
        FXUtilities.loadView(getClass().getResource("ProjectMainPane.fxml"), this, true);

        // add listener that will look for change of the selected item
        projectSpecificViewModel.nodeProperty().addListener(this::onTreeItemChanged);
        
        // update the title depending of the currently selected item
        updateTitle(projectSpecificViewModel.nodeProperty().getValue());
        
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        createListProjectView();
        ToggleGroup group = new ToggleGroup();
        galleryToggleButton.setToggleGroup(group);
        singleToggleButton.setToggleGroup(group);
        singleToggleButton.disableProperty().bind(projectSpecificViewModel.singleViewAvailableProperty().not());
        galleryToggleButton.setOnAction((ActionEvent event) -> {
            projectSpecificViewModel.setViewMode(ViewMode.GALLERY);
        });
        singleToggleButton.setOnAction((ActionEvent event) -> {
            projectSpecificViewModel.setViewMode(ViewMode.SINGLE_STACK);
        });
        ViewMode currentViewMode = projectSpecificViewModel.viewModeProperty().get();
        selectViewModeButton(currentViewMode);

        //****** setting up tooltips ******
        galleryToggleButton.setTooltip(new Tooltip(rb.getString("switchToGalleryView")));
        singleToggleButton.setTooltip(new Tooltip(rb.getString("switchToSingleImageView")));
       
        createImageView(currentViewMode);

        

    }

   

    private void handleHierarchyChange() {
        projectSpecificViewModel.setCurrent(mainTreeOrganisation.getRoot());
    }

    private void createListProjectView() {
        this.setLeft(new TreeViewPane(project, context));
    }

    private void createImageView(ViewMode viewMode) {
        Runnable r = new Runnable() {

            @Override
            public void run() {
                if (displayedBrowser != null) {
                    FXUtilities.emptyPane(displayedBrowser);

                }
                switch (viewMode) {
                    case GALLERY:

                        createGalleryView();
                        break;
                    case SINGLE_STACK:
                        createSingleImageView();
                }
            }
        };
        FXUtilities.modifyUiThreadSafe(r);

    }

    public void createGalleryView() {
        // displayedBrowser = new FXMLGalleryController(project, context);

        if (folderDisplayBrowser == null) {
            folderDisplayBrowser = new IconView(context);
        }

        innerPane.setCenter(folderDisplayBrowser);

    }

    private void createSingleImageView() {
        //int currentIndex = projectSpecificViewModel.indexProperty().get();
        // displayedBrowser = new FXMLSingleImageNavigatorController(project, context);

        if (singleImageViewPane == null) {
            singleImageViewPane = new SingleImageViewPane(projectSpecificViewModel);
            context.inject(singleImageViewPane);
            singleImageViewPane.onTreeItemChanged(null, null, projectSpecificViewModel.nodeProperty().get());
        }

        innerPane.setCenter(singleImageViewPane);
    }

    @Override
    public void stopListening() {
        projectSpecificViewModel.viewModeProperty().removeListener(viewModeListener);
        project.getHierarchy().removeListener(hierarchyListener);
    }

    @Override
    public Project getProject() {
        return project;
    }



    private void selectViewModeButton(ViewMode viewMode) {
        switch (viewMode) {
            case GALLERY:
                galleryToggleButton.setSelected(true);
                break;
            case SINGLE_STACK:
                singleToggleButton.setSelected(true);
                break;
        }
    }

    @EventHandler
    private void handleEvent(DefaultQueryService.QueryStart event) {
        Platform.runLater(() -> LoadingScreen.getInstance().showOn(queryStackPane));
    }

    @EventHandler
    private void handleEvent(DefaultQueryService.QueryStop event) {
        Platform.runLater(() -> LoadingScreen.getInstance().hideFrom(queryStackPane));
    }

    private void updateTitle(TreeItem item) {

        List<TreeItem> hierarchy = new ArrayList<>(10);

        while (item != null) {
            hierarchy.add(item);
            item = item.getParent();
        }

        Collections.reverse(hierarchy);

        StringBuilder builder = new StringBuilder(50);

        for (int i = 0; i != hierarchy.size(); i++) {
            TreeItem treeItem = hierarchy.get(i);
            if (treeItem != null && treeItem.getValue() != null) {
                builder.append(treeItem.getValue().toString());
                if (i != hierarchy.size() - 1) {
                    builder.append("   >   ");
                }
            }
        };

        browserTitleLabel.setText(builder.toString());

    }

    private void onTreeItemChanged(Observable obs, TreeItem oldValue, TreeItem newValue) {
        if (newValue != null) {
            updateTitle(newValue);
        }
        else {
            return;
        }
        if(newValue.getValue() == null) return;

        if (PlaneDB.class.isAssignableFrom(newValue.getValue().getClass())) {
            
          
                createSingleImageView();
                
           
        } else {
            createGalleryView();
        }

       

    }

    @EventHandler
    public void createNewGalleryView(DebugRefreshEvent event) {

        // displayedBrowser = new IconView(context);
        singleImageViewPane = new SingleImageViewPane(viewModelService.getViewModel(project));

        context.inject(singleImageViewPane);

        Platform.runLater(() -> {
            innerPane.setCenter(singleImageViewPane);
            singleImageViewPane.onTreeItemChanged(null, null, viewModelService.getViewModel(project).nodeProperty().getValue());

        });

    }

}
