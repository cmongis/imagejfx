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
import ijfx.core.project.imageDBService.FindFileFXService;
import ijfx.core.project.imageDBService.ImageReference;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.stage.DirectoryChooser;
import org.scijava.Context;

/**
 *
 * @author Cyril Quinton
 */
public class FindImageDialogController extends MultipleProblemDialogueController<ImageReference> implements Initializable {

    @FXML
    private TextArea messageTextArea;
    @FXML
    private TextArea detailTextArea;
    @FXML
    private Label statusLabel;
    @FXML
    private Button cancelButton;
    @FXML
    private Button selectDirectoryButton;
    @FXML
    private Button validateButton;
    @FXML
    private Button skipButton;
    @FXML
    private Button stopButton;
    @FXML
    private CheckBox applyOnOtherCheckBox;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private CheckBox searchRecursivelyCheckBox;
    private Task<List<ImageReference>> searchingTask;

    private final List<ImageReference> imagesLost;
    private File rootDirectory;
    private FindFileFXService findFileService;
    private int currentIndex;
    private int numberOfFound;
    private final BooleanProperty onSearchProperty;
    private boolean searchWithName;
    private int numberOfTrial;
    private final Context context;

    public FindImageDialogController(List<ImageReference> imagesLost, Context context) {
        super(FXCollections.observableList(imagesLost));
        this.context = context;
        searchWithName = true;
        numberOfTrial = 0;
        this.imagesLost = imagesLost;
        onSearchProperty = new SimpleBooleanProperty(false);
        FXUtilities.loadView(getClass().getResource("FindImageDialog.fxml"), this, true);

        createView();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rb = resources;
        stopButton.visibleProperty().bind(onSearchProperty);
        multipleUnableProperty.bindBidirectional(applyOnOtherCheckBox.selectedProperty());
        validateButton.visibleProperty().bind(validateUnableProperty);
        searchRecursivelyCheckBox.visibleProperty().bind(validateUnableProperty);
        applyOnOtherCheckBox.visibleProperty().bind(multipleAvailableProperty);
        messageTextArea.setWrapText(true);
        detailTextArea.setWrapText(true);
        for (Labeled labeled : Arrays.asList(statusLabel, cancelButton, selectDirectoryButton, validateButton, skipButton, stopButton, applyOnOtherCheckBox, searchRecursivelyCheckBox)) {
            ProjectManagerUtils.tooltipLabeled(labeled);
        }
        skipButton.visibleProperty().bind(multipleAvailableProperty);
    }

    @Override
    protected void createView() {
        FXUtilities.modifyUiThreadSafe(() -> {
            numberOfTrial = 0;
            selectDirectoryButton.setVisible(true);
            onSearchProperty.set(false);
            super.createView();
            createMessage();
        });

    }

    private void createMessage() {
        if (!listOfProblems.isEmpty()) {
            String message = rb.getString("imageNotFoundSentence") + "\n"
                    + listOfProblems.get(0).getPath() + "\n\n"
                    + rb.getString("findImageInstruction");
            messageTextArea.setText(message);
            detailTextArea.setText(rb.getString("findImageInstructionDetails"));
        }
    }

    @Override
    protected void suggestMultiple() {
        int numberOffOtherImage = imagesLost.size() - 1;
        String imageWord = numberOffOtherImage > 1 ? rb.getString("images") : rb.getString("image");
        String text = String.format("%s%d%s%s", rb.getString("applyFor"), numberOffOtherImage, rb.getString("other"), imageWord);
        applyOnOtherCheckBox.setText(text);
    }

    @FXML
    public void specifyDirectoryAction() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        rootDirectory = directoryChooser.showDialog(this.getScene().getWindow());
        if (rootDirectory != null) {
            selectDirectoryButton.setVisible(false);
            validateUnableProperty.set(true);
        }

    }

    @FXML
    public void searchImageAction() {
        onSearchProperty.set(true);
        currentIndex = 0;
        numberOfFound = 0;
        searchSingleImage();

    }

    private void doOver() {
        if (numberOfTrial <= 1 && findFileService.getValue() == null) {
            searchSingleImage();
        } else if (multipleUnableProperty.get()) {

            numberOfTrial = 0;
            if (findFileService.getValue() != null) {
                numberOfFound++;
                listOfProblems.remove(currentIndex);
            } else {
                currentIndex++;
            }
            searchSingleImage();

        } else {
            displayResultStatus(numberOfFound);
            createView();
        }
    }

    private void searchSingleImage() {
        if (currentIndex < listOfProblems.size()) {
            numberOfTrial++;
            searchWithName = numberOfTrial == 1;
            ImageReference imageReference = listOfProblems.get(currentIndex);
            findFileService = new FindFileFXService(imageReference, context);
            findFileService.setRootDirectory(rootDirectory);
            findFileService.searchRecursively(searchRecursivelyCheckBox.isSelected());
            if (searchWithName) {
                findFileService.setFileName(new File(listOfProblems.get(currentIndex).getPath()).getName());
            }
            findFileService.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

                @Override
                public void handle(WorkerStateEvent event) {
                    if (findFileService.getValue() != null) {
                        File newFile = findFileService.getValue();
                        imageReference.setPath(newFile.getAbsolutePath());
                    }
                    doOver();
                }
            });
            statusLabel.textProperty().bind(findFileService.messageProperty());
            progressBar.progressProperty().bind(findFileService.progressProperty());
            findFileService.start();

        } else {
            currentIndex = 0;
            createView();
        }
    }

    private void displayResultStatus(int numberOfFound) {
        String nbImageMessage;
        String foundMessage;
        if (numberOfFound == 0) {
            nbImageMessage = rb.getString("noImage");
            foundMessage = rb.getString("noFound");
        } else if (numberOfFound == 1) {
            nbImageMessage = rb.getString("oneImage");
            foundMessage = rb.getString("oneFound");
        } else {
            nbImageMessage = String.format("%d%s", numberOfFound, rb.getString("images"));
            foundMessage = rb.getString("severalFound");
        }
        //statusLabel.setText(String.format("%s %s", nbImageMessage, foundMessage));

    }

    @FXML
    public void closeAction() {
        onClose(false);
    }

    @FXML
    public void skipAction() {
        listOfProblems.remove(0);
        createView();
    }

    @FXML
    public void onStopSearching() {
        findFileService.cancel();
        createView();
    }
}
