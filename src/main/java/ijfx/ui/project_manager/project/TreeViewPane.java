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

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import ijfx.core.project.Project;

import mongis.utils.FXUtilities;
import ijfx.core.listenableSystem.Listening;
import ijfx.core.project.DefaultProjectModifierService;
import ijfx.core.project.hierarchy.MetaDataHierarchy;
import ijfx.core.project.imageDBService.PlaneDB;
import static ijfx.ui.project_manager.ProjectManagerUtils.capitalize;
import ijfx.ui.project_manager.BrowsingModel;
import ijfx.ui.project_manager.FolderOrganisationService;
import ijfx.ui.project_manager.hierarchy.creator.FXMLHierarchyCreatorController;
import ijfx.ui.project_manager.hierarchy.creator.HierarchyCreator;
import ijfx.ui.main.ImageJFX;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import ijfx.ui.project_manager.ProjectManagerUtils;

/**
 *
 * @author Cyril Quinton
 */
public class TreeViewPane extends BorderPane implements Initializable, Listening {

    @FXML
    private TreeView mainTreeView;
    @FXML
    private TreeView selectionTreeView;
    @FXML
    private HBox topHbox;
    @FXML
    private VBox treeViewVbox;
    @FXML
    private Button setHierarchyButton;
    private final ProjectViewModel projectSpecificViewModel;
    private final Project project;
    private final MetaDataHierarchy mainHierarchy;
    private final MetaDataHierarchy selectedHierarchy;
    private final ChangeListener<List<String>> hierachyListener;
    private final ChangeListener<TreeItem> currentNodeListener;
    
    
    @Parameter
    private DefaultProjectModifierService hierarchySetter;
    
    @Parameter
    private DefaultProjectViewModelService projectViewModelManagerService;
    
    @Parameter
    Context context;
    
    private ResourceBundle rb;
    private TreeItem selectionItem;
    private TreeItem mainTreeItem;

    Logger logger = ImageJFX.getLogger();

    public TreeViewPane(Project project, Context context) {
        
        context.inject(this);
        this.projectSpecificViewModel = projectViewModelManagerService.getViewModel(project);
        this.project = project;
       
        FolderOrganisationService service = context.getService(FolderOrganisationService.class);
        this.mainHierarchy = service.getFolderCreator(project, FolderOrganisationService.PlaneClass.ALL_PLANE);
        this.selectedHierarchy = service.getFolderCreator(project, FolderOrganisationService.PlaneClass.SELECTED_PLANE);
       // this.hierarchySetter = contextService.getContext().getService(DefaultProjectModifierService.class);
        hierachyListener = (ObservableValue<? extends List<String>> observable, List<String> oldValue, List<String> newValue) -> {
            FXUtilities.modifyUiThreadSafe(this::createView);
        };
        project.getHierarchy().addListener(hierachyListener);
        currentNodeListener = new ChangeListener<TreeItem>() {

            @Override
            public void changed(ObservableValue<? extends TreeItem> observable, TreeItem oldValue, TreeItem newValue) {
                selectItem(newValue);
            }
        };
        projectSpecificViewModel.nodeProperty().addListener(currentNodeListener);
        mainHierarchy.updatingProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                showLoadingTreeView(mainTreeView, mainTreeItem, !newValue);
            }
        });
        selectedHierarchy.updatingProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                showLoadingTreeView(selectionTreeView, selectionItem, !newValue);
            }
        });
        FXUtilities.loadView(getClass().getResource("TreeViewPane.fxml"), this, true);

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rb = resources;

        /*
         EventHandler<MouseEvent> clickHandler = new EventHandler<MouseEvent>() {

         @Override
         public void handle(MouseEvent event) {
                
         }
         };*/
        for (TreeView treeView : Arrays.asList(mainTreeView, selectionTreeView)) {
            treeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            treeView.setOnMouseClicked(this::onItemClicked);

        }
        setHierarchyButton.setOnAction((ActionEvent event) -> {
            setHierarchyAction();
        });
        topHbox.getChildren().add(new HierarchyValueDisplay(project));
        FXUtilities.modifyUiThreadSafe(this::createView);
        selectItem(projectSpecificViewModel.nodeProperty().get());

        //**** Setting toolTips ****
        setHierarchyButton.setTooltip(new Tooltip(rb.getString("setHierarchy")));
    }

    public void onItemClicked(MouseEvent event) {
        Object selected = ((TreeView) event.getSource()).getSelectionModel().getSelectedItem();

        // if the selected item is a tree item
        if (selected instanceof TreeItem) {

            // casting
            TreeItem selectedItem = (TreeItem) selected;

            // if it's a checkboxTreeItem
            if (selectedItem instanceof CheckBoxTreeItem) {
               
                
               projectSpecificViewModel.setCurrent((CheckBoxTreeItem)selectedItem);
                
                /*
                if (selectedItem.getValue() instanceof PlaneDB) {
                    if (selectedItem.getParent() instanceof CheckBoxTreeItem) {
                        //current Item is set to the parent of the image
                        projectSpecificViewModel.setCurrent((CheckBoxTreeItem) selectedItem.getParent());
                        int index = projectSpecificViewModel.planeListProperty().indexOf(selectedItem);
                        projectSpecificViewModel.setCurrent(index);
                        //switch to single image view
                        projectSpecificViewModel.setViewMode(BrowsingModel.ViewMode.SINGLE_STACK);
                    }
                } else {
                    projectSpecificViewModel.setCurrent((CheckBoxTreeItem) selectedItem);
                    logger.info("Selecting " + selectedItem);
                }*/
            }
        }
    }

    public void createView() {
        mainTreeItem = mainHierarchy.getRoot();
        mainTreeItem.setValue(capitalize(rb.getString("allImages")));

        selectionItem = selectedHierarchy.getRoot();
        selectionItem.setValue(capitalize(rb.getString("selectedImages")));
        FontAwesomeIconView icon = new FontAwesomeIconView();
        icon.setGlyphName("CHECK");
        selectionItem.setGraphic(icon);
        selectionTreeView.setRoot(selectionItem);
        mainTreeView.setRoot(mainTreeItem);
        for (TreeView treeView : Arrays.asList(mainTreeView, selectionTreeView)) {
            treeView.getRoot().setExpanded(true);
        }
        selectItem(projectSpecificViewModel.nodeProperty().get());
    }

    private void selectItem(TreeItem treeItem) {
        for (TreeView treeView : Arrays.asList(mainTreeView, selectionTreeView)) {
            treeView.getSelectionModel().clearSelection();
            treeView.getSelectionModel().select(treeItem);
        }

        /*
         TreeItem rootItem = treeView.getRoot();
         List<TreeItem> toBeVisited = new ArrayList<>();
         happendChildrenToList(toBeVisited, rootItem);
         while (!toBeVisited.isEmpty()) {
         TreeItem currentItem = toBeVisited.remove(0);
         happendChildrenToList(toBeVisited, currentItem);
         if (currentItem.getValue() instanceof NodeController) {
         if (((NodeController) currentItem.getValue()).getNodeReference()
         == treeItem) {
         treeView.getSelectionModel().select(currentItem);
         return;
         }
         }
         }
         */
    }

    private void happendChildrenToList(List<TreeItem> list, TreeItem root) {
        for (Object child : root.getChildren()) {
            if (child instanceof TreeItem) {
                list.add((TreeItem) child);
            }
        }
    }

    private void setHierarchyAction() {
        HierarchyCreator hierarchyCreator = new FXMLHierarchyCreatorController(project, context);
        Stage hierarchyStage = ProjectManagerUtils.createDialogWindow(getScene().getWindow(), (Pane) hierarchyCreator, rb.getString("setHierarchy"));
        hierarchyStage.showAndWait();
        List<String> newHierarchy = hierarchyCreator.getHierarchy();
        hierarchySetter.setHierarchy(project, newHierarchy);
    }

    @Override
    public void stopListening() {
        project.getHierarchy().removeListener(hierachyListener);
        projectSpecificViewModel.nodeProperty().removeListener(currentNodeListener);
    }

    private void showLoadingTreeView(TreeView treeView, TreeItem rootItem, boolean show) {
        
        if(rootItem == null || rootItem.getValue() == null) return;
        
        if (show) {
            treeView.setRoot(rootItem);

        } else {
            FontAwesomeIconView spinner = ProjectManagerUtils.getSpinnerIcon();
            
            
            
            TreeItem spinnerItem = new TreeItem(rootItem.getValue(), spinner);
            treeView.setRoot(spinnerItem);
        }
    }

}
