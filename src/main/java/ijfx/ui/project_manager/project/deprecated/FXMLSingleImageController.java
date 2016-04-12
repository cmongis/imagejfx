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

import ijfx.core.project.imageDBService.PlaneDB;
import mongis.utils.FXUtilities;
import ijfx.core.listenableSystem.Listening;
import ijfx.core.metadata.GenericMetaData;
import ijfx.core.metadata.MetaData;
import ijfx.core.project.PlaneDBModifierService;
import ijfx.core.project.Project;
import ijfx.ui.project_manager.ImageDisplayerJavaFXService;
import ijfx.ui.project_manager.singleimageview.ImageContextMenu;
import ijfx.ui.project_manager.singleimageview.ImageLoadedController;
import ijfx.ui.project_manager.singleimageview.ImageLoadedControllerAbs;
import ijfx.ui.project_manager.singleimageview.ListTagCell;
import ijfx.ui.project_manager.other.EditHandler;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.Pane;
import org.scijava.Context;
import org.scijava.plugin.Parameter;

/**
 *
 * @author Cyril Quinton
 */
public class FXMLSingleImageController extends ImageLoadedControllerAbs implements EditHandler, Initializable, Listening, ImageLoadedController {

    @FXML
    private ListView<String> tagsListView;
    @FXML
    private TextField newTagTextField;
    @FXML
    private Button addTagButton;
    @FXML
    private TableView metaDataTableView;
    @FXML
    private TextField addKeyTextField;
    @FXML
    private TextField addValueTextField;
    @FXML
    private Button addMetaDataButton;
    @FXML
    private Pane imagePane;

    private final BooleanProperty loadingProperty;
    private final BooleanProperty loadedProperty;
    private final PlaneDB image;
    private final ObservableList<String> tagList;
    private final SortedList<String> sortedTagList;
    private final MapChangeListener<String, MetaData> metaDataListener;
    
    @Parameter
    private PlaneDBModifierService planeDBModifier;
    private final ObservableList<MetaData> metaList;
    private final CheckBoxTreeItem<PlaneDB> imageItem;
    private ResourceBundle rb;


    private final Project project;
    private final ContextMenu contextMenu;
    
    
    private final ImageDisplayerJavaFXService displayerService;

    public FXMLSingleImageController(CheckBoxTreeItem<PlaneDB> imageItem, Project project, Context context) {
        super(imageItem.getValue(), project, context);
        this.image = imageItem.getValue();
        this.imageItem = imageItem;
       
        this.project = project;
        this.displayerService = new ImageDisplayerJavaFXService(project, this, context);
        this.loadedProperty = new SimpleBooleanProperty(false);
        this.loadingProperty = new SimpleBooleanProperty(false);
        List<MetaData> metaDataList = new ArrayList<>();
        for (String key : image.metaDataSetProperty().keySet()) {
            metaDataList.add(image.metaDataSetProperty().get(key));
        }
        metaList = FXCollections.observableArrayList(metaDataList);
        tagList = image.getTags();
        sortedTagList = new SortedList<>(tagList);
        
        metaDataListener = (MapChangeListener.Change<? extends String, ? extends MetaData> change) -> {
            if (change.wasAdded()) {
                metaList.add(change.getValueAdded());
            }
            if (change.wasRemoved()) {
                metaList.remove(change.getValueRemoved());
            }
        };
        image.metaDataSetProperty().addListener(metaDataListener);
        
        rb = FXUtilities.getResourceBundle();
        contextMenu = new ImageContextMenu(image, project, context);
        this.setOnContextMenuRequested((ContextMenuEvent event) -> {
            contextMenu.show(this, Side.LEFT, 0, 0);
        });
        prefHeightProperty.set(300);
        prefWidthProperty.set(300);
        FXUtilities.loadView(getClass().getResource("FXMLSingleImage.fxml"), this, true);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tagsListView.setOrientation(Orientation.HORIZONTAL);
        tagsListView.setCellFactory((ListView<String> param) -> new ListTagCell(this));
        tagsListView.setItems(sortedTagList);

        TableColumn<MetaData, String> keyCol = new TableColumn(rb.getString("key"));
        keyCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<MetaData, String> valueCol = new TableColumn(rb.getString("value"));
        valueCol.setCellValueFactory(new PropertyValueFactory<>("stringValue"));
        metaDataTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        metaDataTableView.setItems(metaList);
        metaDataTableView.getColumns().clear();
        metaDataTableView.getColumns().addAll(keyCol, valueCol);
        newTagTextField.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                addTagAction();
            }
        });
        newTagTextField.promptTextProperty().set(rb.getString("addTagDirection"));
        addKeyTextField.promptTextProperty().set(rb.getString("addKeyDirection"));
        addValueTextField.promptTextProperty().set(rb.getString("addValueDirection"));

        addTagButton.setTooltip(new Tooltip(rb.getString("addTag")));
        addMetaDataButton.setTooltip(new Tooltip(rb.getString("addMetaData")));
        addMetaDataButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                addMetaDataAction();
            }
        });
        displayerService.start();

    }

    public void addTagAction() {
        String newTagName = newTagTextField.getText();
        if (!newTagName.isEmpty()) {
            planeDBModifier.addTag(project, image, newTagName);
        }

    }

    public void addMetaDataAction() {
        String key = addKeyTextField.getText();
        String value = addValueTextField.getText();
        if (!(key.isEmpty() || value.isEmpty())) {
            planeDBModifier.addMetaData(project, plane, new GenericMetaData(key, value));
        }
    }

    @Override
    public void modify(String oldValue, String newValue) {
        planeDBModifier.replaceTag(project, image, oldValue, newValue);
    }

    @Override
    public void remove(String value) {
        planeDBModifier.removeTag(project, image, value);
    }

    @Override
    public void stopListening() {
        image.metaDataSetProperty().removeListener(metaDataListener);
    }

    @Override
    public void setImage(Image image) {
        ImageView imageView = new ImageView(image);
        FXUtilities.modifyUiThreadSafe(new Runnable() {

            @Override
            public void run() {
                imagePane.getChildren().clear();
                imagePane.getChildren().add(imageView);
            }
        });
    }

    @Override
    public BooleanProperty loadingProperty() {
        return loadingProperty;
    }

    @Override
    public BooleanProperty loadedProperty() {
        return loadedProperty;
    }

    @Override
    public void setLoadingFailed() {

    }

    @Override
    public PlaneDB getPlane() {
        return image;
    }

    @Override
    protected Pane getImagePane() {
        return imagePane;
    }

}
