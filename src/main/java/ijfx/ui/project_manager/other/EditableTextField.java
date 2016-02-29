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
package ijfx.ui.project_manager.other;

import com.sun.javafx.tk.Toolkit;
import mongis.utils.FXUtilities;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;


/**
 *
 * @author Cyril Quinton
 */
public class EditableTextField extends HBox implements Initializable {

    @FXML
    private TextField textField;
    @FXML
    private Button modifyButton;
    @FXML
    private Button validateButton;
    @FXML
    private Button removeButton;

    private final BooleanProperty onEditProperty;

    private final EditHandler editHandler;

    private String initialValue;

    public EditableTextField(EditHandler editHandler, String initialValue) {
        this.editHandler = editHandler;
        this.initialValue = initialValue;
        onEditProperty = new SimpleBooleanProperty(false);
        FXUtilities.loadView(getClass().getResource("FXMLEditableField.fxml"), this, true);
    }

    @Override
    public void initialize(URL location, ResourceBundle rb) {
        textField.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                updateTextFieldSize();
            }
        });
        textField.setText(initialValue);
        textField.editableProperty().bind(onEditProperty);
        validateButton.visibleProperty().bind(onEditProperty);
        modifyButton.visibleProperty().bind(onEditProperty.not());
        textField.onMouseClickedProperty().set((MouseEvent event) -> {
            if (event.getClickCount() == 2) {
                setEdit(true);
            }
        });
        textField.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                onValidateAction();
            }
        });
        this.setOnKeyReleased(this::handleKeyReleased);
        setEdit(initialValue.isEmpty());
        validateButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                onValidateAction();
            }
        });
        modifyButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                onModifyAction();
            }
        });
        removeButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                onRemoveAction();
            }
        });

        //*****setting tooltips*******
        modifyButton.setTooltip(new Tooltip(rb.getString("edit")));
        removeButton.setTooltip(new Tooltip(rb.getString("remove")));
        validateButton.setTooltip(new Tooltip(rb.getString("validateChange")));

    }

    private void updateTextFieldSize() {
        float newWidth = Toolkit.getToolkit().getFontLoader().computeStringWidth(textField.getText(), textField.getFont());
        textField.setPrefWidth(newWidth + 50);
    }

    private void onValidateAction() {
        editHandler.modify(initialValue, textField.getText());
        initialValue = textField.getText();
        setEdit(false);

    }

    private void setEdit(boolean onEdit) {
        onEditProperty.set(onEdit);
    }

    private void onModifyAction() {
        setEdit(true);
        textField.requestFocus();
        textField.selectAll();
    }

    private void onRemoveAction() {
        editHandler.remove(initialValue);
    }

    private void handleKeyReleased(KeyEvent event) {
        if (event.getCode() == KeyCode.DELETE) {
            if (!onEditProperty.get()) {
                onRemoveAction();
            }
        }
    }

    private void handleOnEditPropertyChange(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if (newValue) {
            textField.requestFocus();
            textField.positionCaret(textField.getLength());
            textField.selectAll();
        }
    }

}
