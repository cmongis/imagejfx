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
package ijfx.ui.plugin.panel;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import ijfx.service.batch.BatchService;
import ijfx.service.batch.BatchSingleInput;
import ijfx.service.batch.input.BatchInputBuilder;
import ijfx.service.ui.LoadingScreenService;
import ijfx.service.uicontext.UiContextService;
import ijfx.service.workflow.DefaultWorkflow;
import ijfx.service.workflow.Workflow;
import ijfx.ui.UiConfiguration;
import ijfx.ui.UiPlugin;
import ijfx.ui.activity.ActivityService;
import ijfx.ui.batch.WorkflowPanel;
import ijfx.ui.context.UiContextProperty;
import ijfx.ui.explorer.Explorable;
import ijfx.ui.explorer.ExplorerSelectionChangedEvent;
import ijfx.ui.explorer.ExplorerService;
import ijfx.ui.main.ImageJFX;
import ijfx.ui.main.Localization;
import ijfx.ui.messageBox.DefaultMessage;
import ijfx.ui.messageBox.DefaultMessageBox;
import ijfx.ui.messageBox.Message;
import ijfx.ui.messageBox.MessageBox;
import ijfx.ui.messageBox.MessageType;
import ijfx.ui.save.DefaultSaveOptions;
import ijfx.ui.save.SaveOptions;
import ijfx.ui.save.SaveType;
import ijfx.ui.widgets.PopoverToggleButton;
import ijfx.ui.widgets.PrettyStats;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import mongis.utils.CallbackTask;
import mongis.utils.FXUtilities;
import mongis.utils.ProgressHandler;
import mongis.utils.TaskButtonBinding;
import org.controlsfx.control.PopOver;
import org.scijava.Context;
import org.scijava.event.EventHandler;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

/**
 *
 * @author cyril
 */
@Plugin(type = UiPlugin.class)
@UiConfiguration(id = "batch-processing-panel", context = "batch", localization = Localization.RIGHT)
public class BatchProcessingPanel extends BorderPane implements UiPlugin {

    @FXML
    private Button testButton;

    @FXML
    private Button startButton;

    @FXML
    private Label titleLabel;

    @FXML
    private VBox paramVBox;

    @FXML
    private VBox resultVBox;

    @FXML
    private VBox workflowVBox;

    private ToggleButton toggleButton;

    @Parameter
    private Context context;

    @Parameter
    private BatchService batchService;

    @Parameter
    private ExplorerService explorerService;

    @Parameter
    private UIService uiService;

    @Parameter
    private LoadingScreenService loadingScreenService;

    @Parameter
    private UiContextService uiContextService;

    @Parameter
    private ActivityService activityService;

    protected WorkflowPanel workflowPanel;

    protected SaveOptions saveOption;

    protected MessageBox messageBox = new DefaultMessageBox();

    protected IntegerProperty selectedItems = new SimpleIntegerProperty();

    protected Label label = new Label();

    protected PrettyStats stepCount = new PrettyStats("Steps");
    protected PrettyStats selectedItemCount = new PrettyStats("Items selected.");

    /**
     * VBox
     */
    protected VBox countVBox = new VBox();

    public BatchProcessingPanel() {

        try {
            FXUtilities.injectFXML(this, "/ijfx/ui/plugin/panel/SegmentationPanel.fxml");

            new TaskButtonBinding(testButton)
                    .setTextBeforeTask("Run a test on a single file")
                    .setBaseIcon(FontAwesomeIcon.CHECK)
                    .setTaskFactory(this::generateTestBatchTask);

            new TaskButtonBinding(startButton)
                    .setBaseIcon(FontAwesomeIcon.LIST_ALT)
                    .setTaskFactory(this::generateBatchTask);

            paramVBox.setPrefHeight(20);

            //paramVBox.setSpacing(40);
        } catch (IOException ex) {
            ImageJFX.getLogger().log(Level.SEVERE, "Error when creating the BatchProcessingPanel", ex);
        }

    }

    @Override
    public UiPlugin init() {

        // creating the workflow panel
        workflowPanel = new WorkflowPanel(context);
        workflowPanel.setPrefHeight(400);

        // creating the toggle button displaying the workflow panel
        toggleButton = new ToggleButton("Edit workflow");
        toggleButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.GEARS));
        toggleButton.setMaxWidth(Double.POSITIVE_INFINITY);

        // binding the toggle button to the workflow panel
        PopoverToggleButton.bind(toggleButton, workflowPanel, PopOver.ArrowLocation.RIGHT_CENTER);

        // changing the title of the workflwo panel
        titleLabel.setText("Batch processing");
      

        // creating a SaveOption element
        saveOption = new DefaultSaveOptions();

        // creating a message box
        messageBox.messageProperty().setValue(getMessage());
        messageBox.messageProperty().bind(Bindings.createObjectBinding(this::getMessage, saveOption.saveType(), workflowPanel.stepListProperty(), saveOption.folder(), saveOption.suffix(), selectedItems));

        // hooking start and test button to workflow states
        startButton.disableProperty().bind(Bindings.createBooleanBinding(this::canStart, saveOption.saveType(), saveOption.folder(), workflowPanel.stepListProperty()).not());
        testButton.disableProperty().setValue(false);
        testButton.disableProperty().bind(Bindings.createBooleanBinding(() -> (getStepCount() > 0 && getItems().size() > 0), workflowPanel.stepListProperty()).not());

        // setting the 
        // binding the counters to the right property
        selectedItemCount.valueProperty().bind(selectedItems);
        stepCount.valueProperty().bind(Bindings.createIntegerBinding(() -> workflowPanel.stepListProperty().size(), workflowPanel.stepListProperty()));
       
        Platform.runLater(this::initUi);
        
        return this;
    }

    public void initUi() {

        // placing
        // assembling the box containing the different count and the toggle button
        countVBox.setSpacing(5);
        countVBox.getChildren().addAll(stepCount, selectedItemCount, toggleButton);

        // adding the box to the workflow panel
        workflowVBox.getChildren().add(countVBox);

        // adding the save option to the VBox containing the parameters
        paramVBox.getChildren().addAll(saveOption.getContent());
        
        // adding the message box just before the buttons
        resultVBox.getChildren().add(0, messageBox.getContent());
    }

    public Task<Boolean> generateBatchTask(TaskButtonBinding binding) {
        return new CallbackTask<Void, Boolean>()
                .runLongCallable(this::runBatchProcessing)
                .submit(loadingScreenService);
    }

    public Boolean runBatchProcessing(ProgressHandler handler) {

        List<BatchSingleInput> inputs = getItems()
                .stream()
                .map(this::buildBatchInput)
                .collect(Collectors.toList());

        return batchService.applyWorkflow(handler, inputs, getWorkflow());
    }

    public Workflow getWorkflow() {
        return new DefaultWorkflow(workflowPanel.stepListProperty());
    }

    public BatchSingleInput buildBatchInput(Explorable explorable) {
        BatchInputBuilder builder = new BatchInputBuilder(context).from(explorable);

        SaveType saveType = saveOption.saveType().getValue();

        File directory = saveOption.folder().getValue();
        String suffix = saveOption.suffix().getValue();

        if (saveType == SaveType.NEW) {

            if (directory == null) {
                builder = builder.saveNextToSourceWithPrefix(suffix, "tif");
            } else if (suffix == null) {
                builder = builder.saveIn(directory);
            } else {
                builder = builder.saveIn(directory, suffix);
            }
        } else {
            builder = builder.overwriteOriginal();
        }

        return builder.getInput();
    }

    public Task<Boolean> generateTestBatchTask(TaskButtonBinding binding) {
        return new CallbackTask<Void, Boolean>()
                .runLongCallable(this::runTest)
                .submit(loadingScreenService);

    }

    public Boolean runTest(ProgressHandler handler) {

        if (getItems().size() > 0) {

            Explorable explorable = getItems().get(0);

            BatchSingleInput input
                    = new BatchInputBuilder(context)
                    .from(explorable)
                    .onFinished(this::onTestFinished)
                    .getInput();
            return batchService.applyWorkflow(handler, input, new DefaultWorkflow(workflowPanel.stepListProperty()));
        } else {
            uiService.showDialog("There is no item to batch process. Select or filter some planes", "Batch processing");
        }

        return false;
    }

    private List<? extends Explorable> getItems() {
        if (explorerService.getSelectedItems().size() == 0) {
            return explorerService.getFilteredItems();
        } else {
            return explorerService.getSelectedItems();
        }

    }

    private void onTestFinished(BatchSingleInput input) {
        uiService.show(input.getDataset());

    }

    @Override
    public Node getUiElement() {
        return this;
    }

    @FXML
    public void close() {
        uiContextService.leave("batch");
        uiContextService.update();
    }

    public boolean canStart() {

        if (getStepCount() <= 0) {
            return false;
        }

        if (getSavetype() == SaveType.REPLACE) {
            return true;
        } else if (getSuffix() == null || getSuffix().trim().equals("")) {
            return false;
        }

        return true;
    }

    private String getSuffix() {
        return saveOption.suffix().getValue();
    }

    private int getStepCount() {
        return workflowPanel.stepListProperty().size();
    }

    private SaveType getSavetype() {
        return saveOption.saveType().getValue();
    }

    private File getSaveFolder() {
        return saveOption.folder().getValue();
    }

    public Message getMessage() {
        System.out.println("called");
        if (workflowPanel != null && workflowPanel.stepListProperty().size() == 0) {

            return new DefaultMessage("Your current workflow is empty.\nPlease add processing steps.", MessageType.WARNING);

        } else if (selectedItems.getValue() <= 0) {
            return new DefaultMessage("Please select files to process", MessageType.DANGER);
        } else if (saveOption.saveType().getValue() == SaveType.REPLACE) {
            return new DefaultMessage("Be careful!\nOverwritten images cannot be recovered.", MessageType.DANGER);
        } else if (getSuffix() == null || getSuffix().trim().equals("")) {
            return new DefaultMessage("Please indicate a valid suffix.", MessageType.DANGER);
        } else if (saveOption.folder().getValue() == null) {
            return new DefaultMessage("If you don't select a\ndirectory, the created images\nwill be saved in their original\ndirectory.", MessageType.WARNING);
        } else {
            return null;
        }
    }

    @EventHandler
    public void onExplorerSelectionChanged(ExplorerSelectionChangedEvent event) {
        Platform.runLater(() -> selectedItems.setValue(event.getObject().size()));
    }

}
