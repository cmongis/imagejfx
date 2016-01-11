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

import ijfx.core.metadata.GenericMetaData;
import ijfx.core.metadata.MetaData;
import ijfx.core.project.PlaneDBModifierService;
import ijfx.core.project.Project;
import ijfx.core.project.ProjectManagerService;
import ijfx.core.project.ProjectToImageJService;
import ijfx.core.project.imageDBService.PlaneDB;
import ijfx.service.thumb.ThumbService;
import mongis.utils.FXUtilities;
import ijfx.ui.project_manager.other.EditHandler;
import ijfx.ui.project_manager.project.ProjectViewModel;
import ijfx.ui.project_manager.project.ProjectViewModelService;
import ijfx.ui.canvas.FxImageCanvas;
import ijfx.ui.main.ImageJFX;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import org.scijava.plugin.Parameter;
import mongis.utils.FXUtilities;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class SingleImageViewPane extends BorderPane implements EditHandler {

    /* UI Elements */
    @FXML
    ListView<String> tagsListView;

    @FXML
    TableView metaDataTableView;

    @FXML
    TextField newTagTextField;

    @FXML
    TextField addKeyTextField;

    @FXML
    TextField addValueTextField;

    @FXML
    Button addTagButton;

    @FXML
    Button addMetaDataButton;

    @FXML
    BorderPane centerBorderPane;

    @FXML
    AnchorPane imageStackPane;

    @FXML
    ListView suggestionsListView;
    
    ResourceBundle rb = rb = FXUtilities.getResourceBundle();

    /* 
     Image Canvas
     */
    FxImageCanvas canvas = new FxImageCanvas();

    /* ImageJFX Services */
    @Parameter
    ProjectViewModelService viewModelService;

    @Parameter
    ProjectManagerService projectService;

    @Parameter
    ThumbService thumbService;

    @Parameter
    PlaneDBModifierService planeModifierService;

    @Parameter
            ProjectToImageJService projectToImageJService;
    
    
    
    /* Observable list */
    ObservableList<String> sortedTagList = FXCollections.observableArrayList();

    ObservableList<MetaData> metaList = FXCollections.observableArrayList();

    ProjectViewModel viewModel;

    /* Plane related */
    PlaneDB currentPlane;

    MapChangeListener<String, MetaData> metaDataListener = (MapChangeListener.Change<? extends String, ? extends MetaData> change) -> {
        if (change.wasAdded()) {
            metaList.add(change.getValueAdded());
        }
        if (change.wasRemoved()) {
            metaList.remove(change.getValueRemoved());
        }
    };
    
    ListChangeListener<String> tagListener = new ListChangeListener<String>() {

        @Override
        public void onChanged(ListChangeListener.Change<? extends String> c) {
            
            while(c.next()) {
                
                //suggestionsListView.getItems().add(c.getAddedSubList());
                updateSuggestions();
            }
            
        }
    };

    public SingleImageViewPane(ProjectViewModel viewModel) {
        super();

        try {
            FXUtilities.injectFXML(this);
        } catch (IOException ex) {
            ImageJFX.getLogger();
        }

        tagsListView.setOrientation(Orientation.HORIZONTAL);
        // tagsListView.setCellFactory((ListView<String> param) -> new ListTagCell(this));
        //tagsListView.setItems(sortedTagList);

        TableColumn<MetaData, String> keyCol = new TableColumn(rb.getString("key"));
        keyCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<MetaData, String> valueCol = new TableColumn(rb.getString("value"));
        valueCol.setCellValueFactory(new PropertyValueFactory<>("stringValue"));
        metaDataTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        metaDataTableView.setItems(metaList);
        metaDataTableView.getColumns().clear();
        metaDataTableView.getColumns().addAll(keyCol, valueCol);
        tagsListView.setCellFactory((ListView<String> param) -> new ListTagCell(this));
        newTagTextField.promptTextProperty().set(rb.getString("addTagDirection"));
        addKeyTextField.promptTextProperty().set(rb.getString("addKeyDirection"));
        addValueTextField.promptTextProperty().set(rb.getString("addValueDirection"));

        addTagButton.setTooltip(new Tooltip(rb.getString("addTag")));
        addMetaDataButton.setTooltip(new Tooltip(rb.getString("addMetaData")));

        this.viewModel = viewModel;
        viewModel.nodeProperty().addListener(this::onTreeItemChanged);
        //canvas.setWidth(Double.MAX_VALUE);
        //canvas.setHeight(Double.MAX_VALUE);

        canvas.widthProperty().bind(imageStackPane.widthProperty());
        canvas.heightProperty().bind(imageStackPane.heightProperty());
        //canvas.maxWidth(Double.MAX_VALUE);
        //canvas.maxHeight(Double.MAX_VALUE);
        imageStackPane.getChildren().add(canvas);
        centerBorderPane.widthProperty().addListener((obs, o, n) -> {

        });

        imageStackPane.widthProperty().addListener((obs, o, n) -> {

        });

        //widthProperty().addListener((obj, old, nw) -> canvas.repaint());
    }

    public Project getProject() {
        return projectService.getCurrentProject();
    }

    public void listenPlaneDB(PlaneDB planeDB) {
        planeDB.getMetaDataSet().addListener(metaDataListener);
        tagsListView.setItems(planeDB.getTags());
        planeDB.getTags().addListener(tagListener);
        
        
    }

    public void stopListeningPlaneDB(PlaneDB planeDB) {
        if (planeDB == null) {
            return;
        }

        planeDB.getMetaDataSet().removeListener(metaDataListener);
        planeDB.getTags().removeListener(tagListener);
    }

    @Override
    public void modify(String oldValue, String newValue) {
        planeModifierService.replaceTag(getCurrentProject(), currentPlane, oldValue, newValue);
    }

    @Override
    public void remove(String value) {
        planeModifierService.removeTag(getCurrentProject(), currentPlane, value);
    }
    
    public Project getCurrentProject() {
        return projectService.getCurrentProject();
    }
    
    
    public void onTreeItemChanged(Observable observable, TreeItem oldValue, TreeItem newValue) {

        if (newValue == null) {
            return;
        }



        if (ProjectViewModelService.containsPlane(newValue)) {

            stopListeningPlaneDB(currentPlane);
            currentPlane = (PlaneDB) newValue.getValue();
            listenPlaneDB(currentPlane);
            updateData();
        }
    }

    private void updateData() {
        List<MetaData> metaDataList = new ArrayList<>();
        for (String key : currentPlane.getMetaDataSet().keySet()) {
            metaDataList.add(currentPlane.getMetaDataSet().get(key));
        }

        metaList.clear();
        metaList.addAll(metaDataList);

        Task<Image> task = thumbService.getThumbTask(currentPlane.getFile(), (int) currentPlane.getPlaneIndex(), 500, 500);

        task.setOnSucceeded(image -> {
            canvas.setImage(task.getValue());

            canvas.repaint();

        });

        //LoadingScreen.submit(task, imageStackPane);
        ImageJFX.getThreadPool().submit(task);

    }

    @FXML
    public void nextImage() {
        viewModel.nextImage();
    }

    @FXML
    public void previousImage() {
        viewModel.previousImage();
    }

     @FXML
    public void openSinglePlane() {
        projectToImageJService.convert(projectService.getCurrentProject(), viewModel.nodeProperty().getValue());
    }
    
    public void addMetaData() {
        planeModifierService.addMetaData(projectService.getCurrentProject(), currentPlane, new GenericMetaData(addKeyTextField.getText(), addValueTextField.getText()));
        addValueTextField.setText("");
        addKeyTextField.setText("");
        
    }
    public void addTag() {
        planeModifierService.addTag(projectService.getCurrentProject(), currentPlane, newTagTextField.getText());
        newTagTextField.setText("");
    }
    
    
    public void updateSuggestions() {
        Set<String> tags = projectService.getAllPossibleTag(getCurrentProject());
        
        suggestionsListView.getItems().clear();
        suggestionsListView.getItems().addAll(tags);
        
    }
    
    
   

}
