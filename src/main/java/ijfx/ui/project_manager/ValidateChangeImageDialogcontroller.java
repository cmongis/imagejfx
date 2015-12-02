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
import ijfx.core.hash.HashService;
import ijfx.core.project.imageDBService.ImageReference;
import ijfx.ui.main.ImageJFX;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import org.scijava.Context;

/**
 *
 * @author Cyril Quinton
 */
public class ValidateChangeImageDialogcontroller extends MultipleProblemDialogueController<ImageReference> implements Initializable {

    @FXML
    private TextArea messageTextArea;
    @FXML
    private Button cancelButton;
    @FXML
    private Button validateButton;
    @FXML
    private Button selectAnotherImageButton;
    @FXML
    private Button removeButton;
    @FXML
    private CheckBox applyOnOtherCheckBox;
    @FXML
    private ImageView imageView;
    
    private final HashService hashService;

    private final Context context;

    public ValidateChangeImageDialogcontroller(List<ImageReference> listOfChangedImage, Context context) {
        super(FXCollections.observableArrayList(listOfChangedImage));
        hashService = context.getService(HashService.class);
        this.context = context;
        FXUtilities.loadView(getClass().getResource("ValidateChangeImage.fxml"), this, true);
        validateUnableProperty.set(true);
        FXUtilities.modifyUiThreadSafe(this::createView);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rb = resources;
        applyOnOtherCheckBox.visibleProperty().bind(multipleAvailableProperty);
        messageTextArea.textProperty().bind(mainMessageProperty);
        messageTextArea.setWrapText(true);
        removeButton.setText(String.format("%s %s", rb.getString("no"), rb.getString("removeFromImageSet")));
        validateButton.setText(rb.getString("yes"));
        selectAnotherImageButton.setText(String.format("%s %s", rb.getString("no"), rb.getString("selectAnotherImage")));
        multipleUnableProperty.bind(applyOnOtherCheckBox.selectedProperty());
    }

    @Override
    protected void suggestMultiple() {

    }

    @Override
    protected void createView() {
        super.createView();
        mainMessageProperty.set(rb.getString("imageChangedSentence"));

    }

    @FXML
    public void onValidateAction() {
        boolean again = true;
        List<ImageReference> updateIDList = new ArrayList<>();
        while (again && !listOfProblems.isEmpty()) {
            if (!multipleUnableProperty.get()) {
                again = false;
            }
            
            updateIDList.add(listOfProblems.get(0));
            listOfProblems.remove(0);
        }
        // the user has validate image changes,
        //new ID are calculated in a separated thread.
        Task<Void> task = new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                for (ImageReference ir: updateIDList) {
                    String newID = hashService.getHash(new File(ir.getPath()));
                    ir.setID(newID);
                }
                return null;
            }
        };
        ImageJFX.getThreadPool().submit(task);
        FXUtilities.modifyUiThreadSafe(this::createView);
    }

    @FXML
    public void onRemoveAction() {
        /*
         projectManager.getCurrentProject().removeImageUndoable(listOfProblems.get(0));
         listOfProblems.remove(0);
         */
        FXUtilities.modifyUiThreadSafe(this::createView);
    }

    @FXML
    public void onSelectAnotherImageAction() {
        /*
        ObservableList<ImageReference> list = FXCollections.observableArrayList();
        list.add(listOfProblems.get(0));
        Stage finderStage = BrowserController.createDialogWindow(getScene().getWindow(), new FXMLFindImageDialogController(list,context),rb.getString("selectAnotherImage"));
        finderStage.showAndWait();
        if (listOfProblems.get(0).getStatus() == ImageReference.Status.OK) {
            listOfProblems.remove(0);
        }
        FXUtilities.modifyUiThreadSafe(this::createView);
                */

    }  

}
