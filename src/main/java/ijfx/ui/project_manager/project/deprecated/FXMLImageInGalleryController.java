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
import ijfx.core.project.Project;
import ijfx.core.project.ProjectModifierService;
import ijfx.core.project.imageDBService.PlaneDB;
import mongis.utils.FXUtilities;
import ijfx.ui.project_manager.BrowsingModel;
import ijfx.ui.project_manager.singleimageview.ImageContextMenu;
import ijfx.ui.project_manager.singleimageview.ImageLoadedController;
import ijfx.ui.project_manager.singleimageview.ImageLoadedControllerAbs;
import ijfx.ui.project_manager.project.ProjectViewModel;
import ijfx.ui.project_manager.project.DefaultProjectViewModelService;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import org.scijava.Context;
import org.scijava.plugin.Parameter;

/**
 *
 * @author Cyril Quinton
 */
public class FXMLImageInGalleryController extends ImageLoadedControllerAbs implements Listening, Initializable, ImageLoadedController {

    @FXML
    private Pane imagePane;
    @FXML
    private Label nameLabel;
    @FXML
    private CheckBox checkBox;
   
    private final ProjectViewModel projectSpecificViewModel;
    private final ChangeListener<Boolean> itemSelectionListener;
    private final PlaneDB image;
    private final CheckBoxTreeItem<PlaneDB> imageItem;
    private final ChangeListener<Boolean> selectImageActionListener;
    
    @Parameter
    private ProjectModifierService projectModifier;
    
    
    private final Project project;
    private final int index;
 
    private final ContextMenu contextMenu;

    public FXMLImageInGalleryController(CheckBoxTreeItem<PlaneDB> imageItem, Project project, int index, Context context) {
        super(imageItem.getValue(),project, context);
        
        this.projectSpecificViewModel = context.getService(DefaultProjectViewModelService.class).getViewModel(project);
        
        this.imageItem = imageItem;
        this.image = imageItem.getValue();
        this.project = project;
        this.index = index;
        selectImageActionListener = (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            handleSelectAction(newValue);
        };
        itemSelectionListener = (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            handleItemSelectedPropertyChange(oldValue, newValue);
        };
        imageItem.selectedProperty().addListener(itemSelectionListener);
       
        contextMenu = new ImageContextMenu(image,project,context);
        this.setOnContextMenuRequested((ContextMenuEvent event) -> {
            contextMenu.show(this, Side.RIGHT, 0, 0);
        });
        this.setOnMouseClicked(this::handleImageClicked);
        prefWidthProperty.set(100);
        prefHeightProperty.set(100);
       

        FXUtilities.loadView(getClass().getResource("FXMLImageInGallery.fxml"), this, true);

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        checkBox.armedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    boolean bu = checkBox.isSelected();
                    handleSelectAction(!checkBox.selectedProperty().get());
                    checkBox.setSelected(bu);
                }
            }
        });
        checkBox.selectedProperty().set(image.selectedProperty().get());
        nameLabel.setText(image.getName());
        Tooltip tooltip = new Tooltip();
        tooltip.textProperty().bind(nameLabel.textProperty());
        nameLabel.setTooltip(tooltip);
        
        

    }

    
    public void handleImageClicked(MouseEvent mouseEvent) {
        //left mouse button
        if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
            projectSpecificViewModel.setCurrent(index);
            if (mouseEvent.getClickCount() == 2) {
                projectSpecificViewModel.setViewMode(BrowsingModel.ViewMode.SINGLE_STACK);
            }
        }

    }

    @Override
    public void stopListening() {
        imageItem.selectedProperty().removeListener(itemSelectionListener);

    }

    private void handleItemSelectedPropertyChange(boolean oldVal, boolean newVal) {
        checkBox.setSelected(newVal);
    }

    private void handleSelectAction(boolean select) {
        projectModifier.selectPlane(project, image, select);
    }

    public Pane getImagePane() {
        return imagePane;
    }

    public PlaneDB getPlane() {
        return image;
    }

    

}
