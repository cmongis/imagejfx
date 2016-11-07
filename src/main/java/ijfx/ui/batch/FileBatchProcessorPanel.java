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
package ijfx.ui.batch;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import ijfx.service.batch.BatchService;
import ijfx.service.batch.BatchSingleInput;
import ijfx.service.batch.FileBatchInput;
import ijfx.service.workflow.DefaultWorkflow;
import ijfx.ui.UiConfiguration;
import ijfx.ui.UiContexts;
import ijfx.ui.UiPlugin;
import ijfx.ui.activity.Activity;
import ijfx.ui.main.ImageJFX;
import ijfx.ui.main.Localization;
import ijfx.ui.utils.CurrentTaskProperty;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.beans.Observable;
import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import mongis.utils.FXUtilities;
import mongis.utils.FileButtonBinding;
import mongis.utils.ImageFinderTask;
import mongis.utils.TaskButtonBinding;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Cyril MONGIS, 2016
 */
@Plugin(type = Activity.class,name=UiContexts.FILE_BATCH_PROCESSING)
//@UiConfiguration(id = "file-batch-processing-panel", context = UiContexts.FILE_BATCH_PROCESSING, localization = Localization.CENTER)
public class FileBatchProcessorPanel extends SplitPane implements Activity {

    private final static Logger logger = ImageJFX.getLogger();

    @Parameter
    private Context context;

    @Parameter
    BatchService batchService;

    /*
        FXML elements
     */
    @FXML
    private Button markButton;

    @FXML
    private Button unmarkButton;

    @FXML
    private TextField filterTextField;

    @FXML
    private Label fileLabel;

    // Table
    @FXML
    private TableView<FileInputModel> fileTableView;

    @FXML
    private TableColumn<FileInputModel, Boolean> markedColumn;

    @FXML
    private TableColumn<FileInputModel, String> nameColumn;

    @FXML
    private TableColumn<FileInputModel, String> folderColumn;

    @FXML
    private TableColumn<FileInputModel, String> pathColumn;

    @FXML
    private BorderPane rightBorderPane;

    @FXML
    private Button startProcessingButton;

    @FXML
    private Button saveDirectoryButton;

    @FXML
    private Label markedLabel;

    @FXML
    private Label progressLabel;
    
    @FXML
    private ProgressBar progressBar;
    
    private ObjectProperty<File> saveFolderProperty;

    private WorkflowPanel workflowPanel;

    private final static String MARK_SELECTION = "Mark selection";
    private final static String MARK_ALL = "Mark all";
    private final static String MARK_ALL_FILTERED = "Mark all filtered";

    private final static String MARK_LABEL_TEXT = "%d files marked for processing";

    /*
        Properties
     */
    CurrentTaskProperty currentTask = new CurrentTaskProperty();

    ObservableSet<FileInputModel> addedFiles = FXCollections.observableSet();
    ObservableList<FileInputModel> filteredFiles = FXCollections.observableArrayList();

    ListProperty<FileInputModel> filteredFilesListProperty = new SimpleListProperty<>(filteredFiles);

    /*
        Processing marking properties
     */
    // true when the filter is on
    BooleanBinding isFilterOn;

    // list property allowing us to tract the number of selected files
    ListProperty<FileInputModel> selectedFilesListProperty;

    // property equals to true when multiple files are selected in the table
    BooleanBinding isMultipleSelection;

    Binding<String> markButtonText;

    Binding<String> markLabelText;
    
    public FileBatchProcessorPanel() {
        try {
            FXUtilities.injectFXML(this);

            // Initializing columns
            nameColumn.setCellValueFactory(p -> new ReadOnlyObjectWrapper(p.getValue().getFile().getName()));
            pathColumn.setCellValueFactory(p -> new ReadOnlyObjectWrapper(p.getValue().getFile().getAbsolutePath()));
            folderColumn.setCellValueFactory(p -> new ReadOnlyObjectWrapper(p.getValue().getFile().getParentFile().getName()));
            markedColumn.setCellValueFactory(p -> p.getValue().markedProperty());
           
            //markedColumn.setCellValueFactory(new PropertyValueFactory<FileInputModel,Boolean>("active"));
            markedColumn.setCellFactory(this::generateCheckBoxCell);
            markedColumn.setEditable(true);
            fileTableView.setEditable(true);
            fileTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            fileTableView.setItems(filteredFiles);

            filterTextField.setOnKeyTyped(this::onKeyTyped);

            saveFolderProperty = new FileButtonBinding(saveDirectoryButton).fileProperty();

            // defining remaining properties
            // true when the filter has something written inside
            isFilterOn = Bindings.notEqual("", filterTextField.textProperty());

            // property giving access to the number of selected files inside the table view
            selectedFilesListProperty = new SimpleListProperty<>(fileTableView.getSelectionModel().getSelectedItems());

            // true when multiple selection
            isMultipleSelection = Bindings.greaterThan(selectedFilesListProperty.sizeProperty(), 1);

            // creating a binding calling a method deciding for the mark name
            markButtonText = Bindings.createStringBinding(this::getMarkButtonText, isFilterOn, isMultipleSelection);

            // binding the mark button to the property
            markButton.textProperty().bind(markButtonText);
            
            markLabelText = Bindings.createStringBinding(this::updateMarkedLabel, addedFiles);
            
            markedLabel.textProperty().bind(markLabelText);
            
         
            new TaskButtonBinding(startProcessingButton)
                    .setBaseIcon(FontAwesomeIcon.TASKS)
                    .setTaskFactory(this::getBatchProcessingTask)
                    .setTextWhenSucceed("Batch processing finished !")
                    .setTextWhenError("Error while batch processing !");

        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        //
        disableProperty().bind(currentTask.isNotNull());

    }

    /*
    
        FXML UI Methods
    
     */
    private String updateMarkedLabel() {

        long count = addedFiles.stream().filter(this::isMarked).count();

        return String.format(MARK_LABEL_TEXT, count);

    }

    @FXML
    private void addFiles() {

    }

    @FXML
    private void addFolder() {
        File folder = FXUtilities.openFolder("Import images from folder", null);

        if (folder != null) {
            ImageFinderTask task = new ImageFinderTask(folder);
            fileLabel.textProperty().bind(task.messageProperty());
            new Thread(task).start();

            task.setOnSucceeded(event -> {
                addFiles(task.getValue());
                updateFilter();
            });
        }

    }

    @FXML
    private void markSelection() {
        if (isMultipleSelection.getValue()) {
            fileTableView
                    .getSelectionModel()
                    .getSelectedItems()
                    .forEach(this::markFileForSelection);
        } else if (isFilterOn.getValue()) {
            filteredFiles.forEach(this::markFileForSelection);
        } else {
            addedFiles.forEach(this::markFileForSelection);
        }
        onFileMarkedForProcessing(null, null, null);
    }

    @FXML
    private void unmarkSelection() {
        addedFiles.forEach(fileInputModel -> fileInputModel.markedProperty().setValue(false));
        onFileMarkedForProcessing(null, null, null);
    }

    @FXML
    private void deleteSelection() {
        addedFiles.forEach(fileInputModel -> fileInputModel.markedProperty().setValue(false));
    }

    @FXML
    private void deleteAll() {
        
        addedFiles.forEach(finput->{
            finput.markedProperty().removeListener(this::onFileMarkedForProcessing);
        });
        
        addedFiles.clear();
        filteredFiles.clear();
    }

    private void markFileForSelection(FileInputModel fileInputModel) {
        fileInputModel.markedProperty().setValue(true);
       
    }

    private boolean isMarked(FileInputModel finputModel) {
        return finputModel.markedProperty().getValue();
    }

    private String getMarkButtonText() {

        if (isMultipleSelection.getValue()) {
            return MARK_SELECTION;
        } else if (isFilterOn.getValue()) {
            return MARK_ALL_FILTERED;
        } else {
            return MARK_ALL;
        }

    }

    private List<FileInputModel> toFileInputModel(List<File> fileList) {
        List<FileInputModel> fileInputList = new ArrayList<>();

        for (File f : fileList) {
            FileInputModel finputModel = new FileInputModel(f);
            finputModel.markedProperty().addListener(this::onFileMarkedForProcessing);
            fileInputList.add(finputModel);
        }
        
        return fileInputList;
    }

    private void addFiles(List<File> f) {

        addedFiles.addAll(toFileInputModel(f));
        updateFilter();
    }

    private void onKeyTyped(KeyEvent event) {
        updateFilter();
    }

    public void updateFilter() {
        final String filterContent = filterTextField.getText();

        // if nothing is on the filter field
        if (filterContent.trim().equals("")) {
            this.filteredFiles.clear();
            this.filteredFiles.addAll(addedFiles);

        } // filtering the files
        else {
            System.out.println("Filtering");
            List<FileInputModel> filteredFiles = addedFiles.parallelStream().filter(inputFileModel -> {
                File f = inputFileModel.getFile();
                return f.getName().contains(filterContent)
                        || f.getAbsolutePath().contains(filterContent);

            }).collect(Collectors.toList());

            this.filteredFiles.clear();
            this.filteredFiles.addAll(filteredFiles);

        }
        System.out.println("Filtered files : " + filteredFiles.size());
        System.out.println("filter added");
    }

    @Override
    public Node getContent() {
        return this;
    }

    @Override
    public Task updateOnShow() {

        workflowPanel = new WorkflowPanel(context);
        setDividerPositions(0.5);
        //getChildren().add(workflowPanel);
        rightBorderPane.setCenter(workflowPanel);
        return null;
    }
    
    
    public void onFileMarkedForProcessing(Observable obs, Boolean oldValue, Boolean newValue) {
        if(oldValue == null || oldValue.equals(newValue)) return;
        markLabelText.invalidate();
        markLabelText.getValue();
    }

    public Task getBatchProcessingTask(TaskButtonBinding binding) {
        Task task = batchService.applyWorkflow(prepareInputs(), new DefaultWorkflow(workflowPanel.stepListProperty()));
        
        progressBar.progressProperty().bind(task.progressProperty());
        progressLabel.textProperty().bind(task.messageProperty());
        
        return task;

        // return new FakeTask(5000);
    }

    public List<BatchSingleInput> prepareInputs() {
        // creating the list
        List<BatchSingleInput> toProcess = new ArrayList<>();

        addedFiles.stream()
                // filtering the inputs marked for processing
                .filter(finput -> finput.markedProperty().getValue())
                // for each of them, we create a FileBatchInput object destined to the save folder
                .forEach(finput -> {
                    toProcess.add(new FileBatchInput(finput.getFile(), saveFolderProperty.getValue()));
                });

        return toProcess;
    }

   
    public TableCell<FileInputModel, Boolean> generateCheckBoxCell(TableColumn<FileInputModel,Boolean> p) {
        CheckBoxTableCell cell = new CheckBoxTableCell();
       
        
       
        return cell;
    }

    

}
