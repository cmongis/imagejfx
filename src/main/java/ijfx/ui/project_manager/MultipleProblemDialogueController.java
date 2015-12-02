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
import java.util.ResourceBundle;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 *
 * @author Cyril Quinton
 */
public abstract class MultipleProblemDialogueController<T> extends BorderPane {

    protected ListProperty<T> listOfProblems;
    protected BooleanProperty validateUnableProperty;
    protected BooleanProperty multipleAvailableProperty;
    protected BooleanProperty multipleUnableProperty;
    protected StringProperty statusMessageProperty;
    protected StringProperty mainMessageProperty;
    protected StringProperty detailMessageProperty;
    protected ResourceBundle rb;

    public MultipleProblemDialogueController(ObservableList<T> listOfProblems) {
        this.listOfProblems = new SimpleListProperty<>(listOfProblems);
        validateUnableProperty = new SimpleBooleanProperty();
        multipleAvailableProperty = new SimpleBooleanProperty();
        multipleUnableProperty = new SimpleBooleanProperty();
        statusMessageProperty = new SimpleStringProperty();
        mainMessageProperty = new SimpleStringProperty();
        detailMessageProperty = new SimpleStringProperty();
        rb = FXUtilities.getResourceBundle();
        listOfProblems.addListener((Observable observable) -> {
            if (listOfProblems.size() < 2) {
                if (listOfProblems.isEmpty()) {
                    onClose(true);
                }
                multipleAvailableProperty.set(false);

            } else {
                multipleAvailableProperty.set(true);
            }
        });
        validateUnableProperty.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (newValue && multipleAvailableProperty.get()) {
                suggestMultiple();
            }
        });
        multipleAvailableProperty.set(listOfProblems.size() > 1);

    }

    private void initProperties() {
        validateUnableProperty.set(false);
        statusMessageProperty.set("");
        mainMessageProperty.set("");
        detailMessageProperty.set("");
    }

    protected void createView() {
        initProperties();
    }

    protected void onClose(boolean wait) {
        if (wait) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        Stage stage = (Stage) this.getScene().getWindow();
        stage.close();
    }

    protected abstract void suggestMultiple();

}
