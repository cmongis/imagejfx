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
package ijfx.ui.project_manager.hierarchy.creator;

import mongis.utils.FXUtilities;
import java.net.URL;
import java.util.Comparator;
import java.util.ResourceBundle;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

/**
 *
 * @author Cyril Quinton
 */
public class HierarchyKey extends AnchorPane implements Initializable {

    public static Comparator<HierarchyKey> getComparator() {
        return (HierarchyKey o1, HierarchyKey o2) -> {
            if (o1.getIndex() < o2.getIndex()) {
                return -1;
            } else if (o1.getIndex() == o2.getIndex()) {
                return 0;
            } else {
                return 1;
            }
        };
    }
    private String key;
    private int index;
    private final FXMLHierarchyCreatorController controller;
    @FXML
    private ComboBox comboBox;
    @FXML
    private Button removeButton;
    @FXML
    private HBox hbox;
    private ObservableList<String> possibleNewKeys;

    public HierarchyKey(FXMLHierarchyCreatorController controller) {
        possibleNewKeys = controller.getPossibleNewKey();
        this.controller = controller;
        FXUtilities.loadView(getClass().getResource("FXMLHierarchyItem.fxml"),
                this, true);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        comboBox.setValue("");
        comboBox.setEditable(true);
        comboBox.setItems(possibleNewKeys);
        comboBox.valueProperty().addListener((ObservableValue observable, Object oldValue, Object newValue) -> {
            if (newValue instanceof String) {
                key = (String) newValue;
                //possibleNewKeys.remove(newValue);
                //comboBox.setItems(possibleNewKeys);
            }
        });
        comboBox.getEditor().setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                comboBox.getEditor().editableProperty().set(false);
            }
        });
        comboBox.getEditor().setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                comboBox.getEditor().editableProperty().set(true);
            }
        });
        removeButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                remove();
            }
        });
        comboBox.setOnDragDetected((MouseEvent event) -> {

            if (key != null) {
                Dragboard db = comboBox.startDragAndDrop(TransferMode.ANY);

                ClipboardContent content = new ClipboardContent();
                content.put(DataFormat.PLAIN_TEXT, new String(key + "\n" + index));
                db.setContent(content);
                WritableImage snapshot = comboBox.getEditor().snapshot(null, null);
                double xmiddle = snapshot.getWidth()/2;
                double ymiddle = snapshot.getHeight()/2;


                db.setDragView(snapshot,xmiddle ,ymiddle);
                event.consume();
            }
        });

    }

    public String getKey() {
        return key;
    }

    public HierarchyKey setKey(String key) {
        this.key = key;
        comboBox.setValue(key);
        return this;
    }

    public int getIndex() {
        return index;
    }

    public HierarchyKey setIndex(int index) {
        this.index = index;
        return this;
    }

    private void remove() {
        controller.getHieararchyKeyList().remove(this);
    }

}
