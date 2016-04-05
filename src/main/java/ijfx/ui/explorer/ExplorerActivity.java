/*
    This file is part of ImageJ FX.

    ImageJ FX is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    ImageJ FX is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with ImageJ FX.  If not, see <http://www.gnu.org/licenses/>. 
    
     Copyright 2015,2016 Cyril MONGIS, Michael Knop
	
 */
package ijfx.ui.explorer;

import ijfx.ui.activity.Activity;
import ijfx.ui.explorer.cell.FolderListCellCtrl;
import ijfx.ui.explorer.event.FolderAddedEvent;
import ijfx.ui.explorer.event.FolderDeletedEvent;
import ijfx.ui.explorer.event.ExploreredListChanged;
import ijfx.ui.explorer.view.IconView;
import ijfx.ui.main.SideMenuBinding;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import mongis.utils.AsyncCallback;
import mongis.utils.FXUtilities;
import mongis.utils.FileButtonBinding;
import org.scijava.event.EventHandler;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = Activity.class)
public class ExplorerActivity extends AnchorPane implements Activity {

    @FXML
    ListView<Folder> folderListView;

    @FXML
    BorderPane contentBorderPane;

    @FXML
    ToggleButton filterToggleButton;

    @FXML
    VBox filterVBox;

    @FXML
    TextField filterTextField;

    @Parameter
    FolderManagerService folderManagerService;

    @Parameter
    ExplorerService explorerService;

    ExplorerView view = new IconView();

    public ExplorerActivity() {
        try {
            FXUtilities.injectFXML(this);

            contentBorderPane.setCenter(view.getNode());
            folderListView.setCellFactory(listview -> new FolderListCell());
            folderListView.getSelectionModel().selectedItemProperty().addListener(this::onFolderSelectionChanged);

            SideMenuBinding binding = new SideMenuBinding(filterVBox);

            binding.showProperty().bind(filterToggleButton.selectedProperty());
            filterVBox.setTranslateX(-250);

        } catch (IOException ex) {
            Logger.getLogger(ExplorerActivity.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public Node getContent() {
        return this;
    }

    @Override
    public Task updateOnShow() {

        return new AsyncCallback<Void, List<Explorable>>()
                .run(this::update)
                .then(this::updateUi)
                .start();
    }

    // get the list of items to show from the explorer service
    public List<Explorable> update(Void v) {
        if (folderManagerService.getCurrentFolder() == null) {
            return new ArrayList<Explorable>();
        } else {
            return explorerService.getFilteredItems();
        }
    }

    public void onFolderSelectionChanged(Observable obs, Folder oldValue, Folder newValue) {
        folderManagerService.setCurrentFolder(newValue);
    }

    public void updateFolderList() {
        folderListView
                .getItems()
                .addAll(
                        folderManagerService
                        .getFolderList()
                        .stream()
                        .filter(this::isNotDisplayed)
                        .collect(Collectors.toList()));
    }

    public void updateUi(List<? extends Explorable> explorable) {

        updateFolderList();
        System.out.println(explorable.size());
        if (explorable == null) {
            contentBorderPane.setCenter(new Label("Drag and drop a folder containing your image to explore it"));

        } else if (explorable.size() == 0) {
            contentBorderPane.setCenter(new Label("This folder doesn't contain any images... for now"));
        } else {
            contentBorderPane.setCenter(view.getNode());
            view.setItem(explorable);
        }

    }

    // returns true if the folder is not displayed yet
    private boolean isNotDisplayed(Folder folder) {
        return !folderListView.getItems().contains(folder);
    }

    @FXML
    public void addFolder() {
        File f = FXUtilities.openFolder("Open a folder", null);

        if (f != null) {
            folderManagerService.addFolder(f);
        }
    }

    @EventHandler
    public void onFolderAdded(FolderAddedEvent event) {
        Platform.runLater(this::updateFolderList);
    }

    @EventHandler
    public void onFolderDeleted(FolderDeletedEvent event) {
        folderListView.getItems().remove(event.getObject());
    }

    @EventHandler
    public void onDisplayedItemListChanged(ExploreredListChanged event) {
        Platform.runLater(() -> updateUi(event.getObject()));
    }

    private class FolderListCell extends ListCell<Folder> {

        FolderListCellCtrl ctrl = new FolderListCellCtrl();

        public FolderListCell() {
            super();
            getStyleClass().add("selectable");
            itemProperty().addListener(this::onItemChanged);
        }

        public void onItemChanged(Observable obs, Folder oldValue, Folder newValue) {
            if (newValue == null) {
                setGraphic(null);
            } else {

                setGraphic(ctrl);

                if (newValue != null) {
                    ctrl.setItem(newValue);
                }
            }
        }

    }

}
