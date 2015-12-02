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
import ijfx.core.project.AnnotationRule;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;

/**
 *
 * @author Cyril Quinton
 */
public class RulePickerController extends BorderPane implements Initializable {

    @FXML
    private Button validateButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Label instructionLabel;
    @FXML
    private TableView tableView;
    private final ObservableList<AnnotationRule> rules;
    private TableView.TableViewSelectionModel selectionModel;

    public RulePickerController(List<AnnotationRule> rules) {
        this.rules = FXCollections.observableArrayList(rules);
        FXUtilities.loadView(getClass().getResource("RulePicker.fxml"), this, true);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        selectionModel = tableView.getSelectionModel();
        TableColumn<AnnotationRule, String> selectorCol = new TableColumn<>(resources.getString("selector"));
        selectorCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<AnnotationRule, String>, ObservableValue<String>>() {

            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<AnnotationRule, String> param) {
                return new ReadOnlyObjectWrapper<>(param.getValue().getSelector().getQueryString());
            }
        });
        TableColumn<AnnotationRule, String> modifierCol = new TableColumn<>(resources.getString("modifier"));
        modifierCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<AnnotationRule, String>, ObservableValue<String>>() {

            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<AnnotationRule, String> param) {
                return new ReadOnlyObjectWrapper<>(param.getValue().getModifier().getNonParsedString());
            }
        });
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        tableView.setItems(rules);
        tableView.getColumns().addAll(selectorCol, modifierCol);
        /*stage.setOnCloseRequest(new EventHandler<WindowEvent>() {

         @Override
         public void handle(WindowEvent event) {
         selectionModel.clearSelection();
         }
         });
         */
        validateButton.setOnAction((ActionEvent event) -> {
            close();
        });
        cancelButton.setOnAction((ActionEvent event) -> {
            selectionModel.clearSelection();
            close();
        });

    }

    public List<AnnotationRule> getPickedRules() {
        return selectionModel.getSelectedItems();
    }

    private void close() {
        FXUtilities.close(this);
    }

}
