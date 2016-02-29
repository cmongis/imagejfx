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
package ijfx.ui.project_manager.project.deprecated;

import ijfx.core.listenableSystem.Listening;
import ijfx.core.metadata.MetaData;
import ijfx.core.project.Project;
import ijfx.core.project.ProjectModifierService;

import mongis.utils.FXUtilities;

import ijfx.ui.project_manager.other.NodeController;
import ijfx.ui.project_manager.project.DefaultProjectViewModelService;
import ijfx.ui.project_manager.project.FolderContextMenu;
import ijfx.ui.project_manager.project.MetaDataStringFormaterService;
import ijfx.ui.project_manager.project.ProjectViewModel;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import ijfx.ui.project_manager.ProjectManagerUtils;

/**
 *
 * @author Cyril Quinton
 */
public class FXMLMetadataIFolderController extends AnchorPane implements NodeController, Initializable, Listening {

    private final BooleanProperty selectedProperty;
    private final BooleanProperty indeterminateProperty;
    private final ProjectViewModel projectSpecificViewModel;
    private final CheckBoxTreeItem item;
    private final ChangeListener<Boolean> selectFolderActionListener;
    private final ChangeListener<Boolean> itemSelectionListener;
    private final ChangeListener<Boolean> itemIndeterminateListener;
    
    @Parameter
    DefaultProjectViewModelService projectViewModelManagerService;
    
    @Parameter
    private MetaDataStringFormaterService formater;
    
    @Parameter
    private ProjectModifierService modifier;
    
    
    private final MetaData metaData;
    private final Project project;
    private FolderContextMenu contextMenu;

    @FXML
    private Label label;
    @FXML
    private CheckBox checkBox;

    @Parameter
    Context context;
    
    public FXMLMetadataIFolderController(Project project, MetaData metaData, Context context, CheckBoxTreeItem item) {
        this.project = project;
        context.inject(this);
        
        
        this.projectSpecificViewModel = projectViewModelManagerService.getViewModel(project);
        this.item = item;
        this.metaData = metaData;
        
        
        
       
        
        
        selectedProperty = new SimpleBooleanProperty(item.isSelected());
        indeterminateProperty = new SimpleBooleanProperty(item.isIndeterminate());
        selectFolderActionListener = (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (newValue) {
                boolean bu = checkBox.isSelected();
                selectSubFolders(!checkBox.isSelected());
                checkBox.setSelected(bu);
            }
        };
        itemSelectionListener = (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            handleItemSelectionStateChange(oldValue, newValue);
        };
        itemIndeterminateListener = (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            handleItemIndeterminateStateChange(oldValue, newValue);
        };
        item.selectedProperty().addListener(itemSelectionListener);
        item.indeterminateProperty().addListener(itemIndeterminateListener);
        this.setOnMouseClicked(this::handleClickEvent);
        //contextMenu = new FolderContextMenu(item, project, context);
        this.setOnContextMenuRequested((ContextMenuEvent event) -> {
            //contextMenu.show(this, Side.RIGHT, 0, 0);
        });
        this.setOnKeyReleased(this::handleKeyReleased);
        FXUtilities.loadView(getClass().getResource("FXMLMetaDataFolder.fxml"), this, true);

    }

    ;
     @Override
    public void initialize(URL location, ResourceBundle resources) {
        label.setText(formater.getFormatedString(metaData));
        checkBox.armedProperty().addListener(selectFolderActionListener);
        if (item.indeterminateProperty().get()) {
            checkBox.indeterminateProperty().set(true);
        } else {
            checkBox.selectedProperty().set(item.selectedProperty().get());
        }
        ProjectManagerUtils.tooltipLabeled(label);
    }

    public void onAction() {
        projectSpecificViewModel.setCurrent(item);
    }

    @FXML
    public void onFolderClicked(MouseEvent event) {
        if (event.getClickCount() == 2) {
            onAction();
        }
    }

    @Override
    public TreeItem getNodeReference() {
        return item;
    }

    private void selectSubFolders(boolean select) {
        modifier.selectSubImages(project,item, select);
    }

    private void handleItemSelectionStateChange(Boolean oldVal, Boolean newVal) {
        checkBox.setSelected(newVal);
    }

    private void handleItemIndeterminateStateChange(Boolean oldVal, Boolean newVal) {
        checkBox.setIndeterminate(newVal);
    }

    @Override
    public void stopListening() {
        item.indeterminateProperty().removeListener(itemIndeterminateListener);
        item.selectedProperty().removeListener(itemSelectionListener);
    }

    private void handleClickEvent(MouseEvent event) {
        if (event.getClickCount() == 2) {
            onAction();
        }
    }
    private void handleKeyReleased(KeyEvent event) {
        if (event.getCode() == KeyCode.DELETE) {
            contextMenu.remove(null);
        }
    }
}
