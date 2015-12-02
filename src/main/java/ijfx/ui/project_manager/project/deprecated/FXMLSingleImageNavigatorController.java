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
import ijfx.core.project.imageDBService.PlaneDB;
import mongis.utils.FXUtilities;

import ijfx.ui.project_manager.project.DefaultProjectViewModelService;
import ijfx.ui.project_manager.project.ProjectViewModel;
import ijfx.ui.project_manager.project.deprecated.FXMLImageInGalleryController;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.scijava.Context;
import org.scijava.plugin.Parameter;

/**
 *
 * @author Cyril Quinton
 */
@Deprecated
public class FXMLSingleImageNavigatorController extends BorderPane implements Initializable, Listening {

    @FXML
    private Pane singleImagePane;
    @FXML
    private Button leftButton;
    @FXML
    private Button rightButton;

    private FXMLImageInGalleryController imageController;
    private final ProjectViewModel projectSpecificViewModel;
    private final ReadOnlyIntegerProperty indexProperty;
    private final ReadOnlyListProperty<CheckBoxTreeItem<PlaneDB>> planeListProperty;
    private final Project project;

    private final ReadOnlyBooleanProperty rightSlideAvailableProperty;
    private final ReadOnlyBooleanProperty leftSlideAvailableProperty;
    private final ChangeListener<? super Number> indexChangeListener;

    @Parameter
    Context context;
    
    private FXMLSingleImageNavigatorController(Project project, Context context) {
        this.context = context;
        this.projectSpecificViewModel = context.getService(DefaultProjectViewModelService.class).getViewModel(project);
        this.project = project;
        
        indexProperty = projectSpecificViewModel.indexProperty();
        planeListProperty = projectSpecificViewModel.planeListProperty();
        rightSlideAvailableProperty = projectSpecificViewModel.rightSlideAvailable();
        leftSlideAvailableProperty = projectSpecificViewModel.leftSlideAvailable();
        indexChangeListener = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            handleIndexChanged(observable, oldValue, newValue);
        };
        planeListProperty.addListener(new ChangeListener<ObservableList<CheckBoxTreeItem<PlaneDB>>>() {

            @Override
            public void changed(ObservableValue<? extends ObservableList<CheckBoxTreeItem<PlaneDB>>> observable, ObservableList<CheckBoxTreeItem<PlaneDB>> oldValue, ObservableList<CheckBoxTreeItem<PlaneDB>> newValue) {
            createView();}
        });
        indexProperty.addListener(indexChangeListener);

        FXUtilities.loadView(getClass().getResource("FXMLSingleImageNavigator.fxml"),
                this, true);
        FXUtilities.modifyUiThreadSafe(this::createView);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        leftButton.visibleProperty().bind(leftSlideAvailableProperty);
        rightButton.visibleProperty().bind(rightSlideAvailableProperty);
        leftButton.setOnAction((ActionEvent event) -> {
            horizontalSlide(-1);
        });
        rightButton.setOnAction((ActionEvent event) -> {
            horizontalSlide(1);
        });
    }

    private void createView() {
        if (!planeListProperty.isEmpty()) {
            FXUtilities.emptyPane(singleImagePane);
            CheckBoxTreeItem<PlaneDB> imageItem = planeListProperty.get(indexProperty.get());
            singleImagePane.getChildren().add(new FXMLSingleImageController(imageItem, project, context));
        }
    }

    private void horizontalSlide(int shift) {
        projectSpecificViewModel.setCurrent(indexProperty.get() + shift);
    }

    private void handleIndexChanged(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        FXUtilities.modifyUiThreadSafe(this::createView);

    }

    @Override
    public void stopListening() {
        indexProperty.removeListener(indexChangeListener);
    }

}
