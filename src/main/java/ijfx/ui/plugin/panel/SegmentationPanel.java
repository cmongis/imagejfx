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
import ijfx.bridge.ImageJContainer;
import ijfx.plugins.commands.Binarize;
import ijfx.plugins.commands.BinaryToOverlay;
import ijfx.service.batch.BatchService;
import ijfx.service.batch.BatchSingleInput;
import ijfx.service.batch.ImageDisplayBatchInput;
import ijfx.service.batch.SegmentationService;
import ijfx.service.batch.SegmentedObject;
import ijfx.service.batch.input.BatchInputBuilder;
import ijfx.service.overlay.OverlayStatService;
import ijfx.service.ui.LoadingScreenService;
import ijfx.service.ui.MeasurementService;
import ijfx.service.uicontext.UiContextService;
import ijfx.service.workflow.DefaultWorkflow;
import ijfx.service.workflow.WorkflowBuilder;
import ijfx.ui.UiConfiguration;
import ijfx.ui.UiContexts;
import ijfx.ui.UiPlugin;
import ijfx.ui.activity.ActivityService;
import ijfx.ui.batch.WorkflowPanel;
import ijfx.ui.context.UiContextProperty;
import ijfx.ui.explorer.Explorable;
import ijfx.ui.explorer.ExplorationMode;
import ijfx.ui.explorer.ExplorerActivity;
import ijfx.ui.explorer.ExplorerService;
import ijfx.ui.explorer.FolderManagerService;
import ijfx.ui.main.ImageJFX;
import ijfx.ui.main.Localization;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mongis.utils.CallbackTask;
import mongis.utils.FXUtilities;
import mongis.utils.ProgressHandler;
import mongis.utils.SilentProgressHandler;
import mongis.utils.TaskButtonBinding;
import net.imagej.Dataset;
import net.imagej.display.ColorMode;
import net.imagej.display.DefaultImageDisplay;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.OverlayService;
import net.imagej.display.OverlayView;
import net.imagej.event.OverlayCreatedEvent;

import net.imagej.overlay.Overlay;
import net.imagej.plugins.commands.imglib.GaussianBlur;
import net.imglib2.RandomAccessibleInterval;
import org.controlsfx.control.PopOver;
import org.scijava.Context;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.ui.DialogPrompt;
import org.scijava.ui.UIService;

/**
 *
 * @author Cyril MONGIS, 2016
 */
//@Plugin(type = UiPlugin.class)
@UiConfiguration(id = "segment-panel", context = "segment -overlay-selected", localization = Localization.RIGHT)
public class SegmentationPanel extends BorderPane implements UiPlugin {

    /*
        FXML components
     */
    @FXML
    private VBox workflowVBox;

    @FXML
    private Button testButton;

    @FXML
    private Button startButton;

    @FXML
    private Label titleLabel;

    @FXML
    private VBox resultVBox;

    @FXML
    private VBox paramVBox;

    @Parameter
    private Context context;

    CheckBox whiteObjectCheckbox = new CheckBox("Ojects are white");

    PreviewImageView imagePreview;

    /*
            Context related components
     */
    @Parameter
    private ExplorerService explorerService;

    @Parameter
    private ImageDisplayService imageDisplayService;

    @Parameter
    private BatchService batchService;

    @Parameter
    private UIService uiService;

    @Parameter
    private UiContextService uiContextService;

    @Parameter
    private OverlayService overlayService;

    @Parameter
    private OverlayStatService overlayStatsService;

    @Parameter
    private LoadingScreenService loadingScreenService;

    @Parameter
    private FolderManagerService folderManagerService;

    @Parameter
    EventService eventService;

    @Parameter
    ActivityService activityService;

    
    
    
    @Parameter
    SegmentationService segmentationService;

    @Parameter
    MeasurementService measureService;
    
    
    Logger logger = ImageJFX.getLogger();

    TaskButtonBinding taskButtonBinding;

    WorkflowPanel workflowPanel;

    BooleanProperty replaceFile;

    ObjectProperty<File> ouputFolderProperty;

    ReadOnlyBooleanProperty isExplorer;

    ReadOnlyBooleanProperty isMultidimensional;
    
    ReadOnlyBooleanProperty isSegmentationContext;
    
    private static final Boolean USE_ALL_PLANE = Boolean.TRUE;

    PopOver previewPopOver = new PopOver();

    public SegmentationPanel() {
        try {
            FXUtilities.injectFXML(this);

        } catch (IOException ex) {
            Logger.getLogger(SegmentationPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public UiPlugin init() {

        workflowPanel = new WorkflowPanel(context);
        workflowPanel.setMinHeight(300);
        workflowVBox.getChildren().add(workflowPanel);

        workflowPanel.addStep(GaussianBlur.class);
        workflowPanel.addStep(Binarize.class);

        isExplorer = new UiContextProperty(context, UiContexts.EXPLORE);
        
        isSegmentationContext = new UiContextProperty(context, UiContexts.SEGMENT);
        isSegmentationContext.addListener(this::onSegmetationContextChanged);
        isMultidimensional = new UiContextProperty(context, "multi-n-img");
        //startButton.visibleProperty().bind(isExplorer);
        // Bindings buttons
        new TaskButtonBinding(startButton)
                .setTaskFactory(this::generateTask)
                .setBaseIcon(FontAwesomeIcon.LIST_ALT)
                .textBeforeTaskProperty().bind(Bindings.createStringBinding(this::getStartButtonText, isExplorer));

        new TaskButtonBinding(testButton)
                .setTaskFactory(this::createTaskButtonBinding)
                .setBaseIcon(FontAwesomeIcon.CHECK_SQUARE)
                .textBeforeTaskProperty().bind(Bindings.createStringBinding(this::getTestButtonText, isExplorer,isMultidimensional));

        testButton.visibleProperty().bind(isExplorer.or(isExplorer.not().and(isMultidimensional)));
        
        
        Button button = new Button("Segment more");
        button.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.COMPASS));
        button.setOnAction(this::segmentMore);
        button.setMaxWidth(Double.POSITIVE_INFINITY);
        button.visibleProperty().bind(isExplorer.not());
        resultVBox.getChildren().add(2, button);

        whiteObjectCheckbox.setId("black-objects-checkbox");
        whiteObjectCheckbox.setTooltip(new Tooltip("The workflows generates a black/white mask. Tick this option if the objects generated by the mask are white."));

        imagePreview = new PreviewImageView(context);

        previewPopOver.setContentNode(imagePreview);

        Button previewMask = new Button("Preview Mask", new FontAwesomeIconView(FontAwesomeIcon.EYE));

        HBox hbox = new HBox(20);

        hbox.getChildren().addAll(whiteObjectCheckbox, previewMask);
        whiteObjectCheckbox.setStyle("-fx-padding-top:10px;-fx-padding-bottom:10px;");
        whiteObjectCheckbox.getStyleClass().add("width-padding");
        previewMask.setOnAction(this::onPreviewMaskButtonClicked);

        paramVBox.getChildren().add(hbox);

        return this;
    }

    @Override
    public Node getUiElement() {
        return this;
    }

    public Task createTaskButtonBinding(TaskButtonBinding b) {
        System.out.println("Generating task");
        return generateTestTask();
    }

    protected Task<Boolean> generateTestTask() {

        Task<Boolean> task = new CallbackTask<ImageDisplay, Boolean>()
                .run(this::runTestProcessing)
                .then(this::whenTestFinished);

        loadingScreenService.frontEndTask(task, false);
        return task;
    }

    public void whenTestFinished(Boolean b) {
        System.out.println("End result = " + b);
    }

    protected Boolean runTestProcessing(ProgressHandler handler, ImageDisplay imageDisplay) {

        logger.info("Executing segmentation test processing");

        if (handler == null) {
            handler = new SilentProgressHandler();
        }

        // generating the batch service input from an image display
        BatchSingleInput input;
        ImageDisplay inputDisplay;

        if (isExplorer()) {
            Explorable explorable;
            logger.info("isExplorer = true");
            if (explorerService.getSelectedItems().size() > 0) {
                explorable = explorerService.getSelectedItems().get(0);
            } else {
                explorable = explorerService.getItems().get(0);
            }

            input = new BatchInputBuilder(context)
                    .from(explorable)
                    .getInput();

            inputDisplay = new DefaultImageDisplay();
            context.inject(inputDisplay);
            inputDisplay.display(explorable.getDataset());
            uiService.show(inputDisplay);

        } else {
            logger.info("isExplorer = false");
            input = new ImageDisplayBatchInput(imageDisplayService.getActiveImageDisplay(), true);
            inputDisplay = imageDisplayService.getActiveImageDisplay();
        }

        logger.info("Executing segmentation workflow");

        // applying the workflow
        boolean batchResult = batchService.applyWorkflow(handler, input, new DefaultWorkflow(workflowPanel.stepListProperty()));

        if (!batchResult) {
            return false;
        }

        detectObjectsAndDisplay(handler, inputDisplay, input);

        return true;
    }

    protected void detectObjectsAndDisplay(ProgressHandler handler, ImageDisplay inputDisplay, BatchSingleInput input) {
        // detecting objects
        handler.setStatus("Detecting objects...");

        Overlay[] overlay = BinaryToOverlay.transform(context, (RandomAccessibleInterval)input.getDataset(), whiteObjectCheckbox.isSelected());
        // giving a random color to each overlay
        overlayStatsService.setRandomColor(Arrays.asList(overlay));
        
        List<Overlay> toRemove = inputDisplay
                .stream()
                .filter(o -> o instanceof OverlayView)
                .map(o -> (OverlayView) o)
                .map(o -> o.getData())
                .collect(Collectors.toList());
        
        for(Overlay o : toRemove) overlayService.removeOverlay(inputDisplay, o);
        
        
        // deleting the overlay owned previously by the input
        inputDisplay.addAll(Stream.of(overlay).map(o -> imageDisplayService.createDataView(o)).collect(Collectors.toList()));

        Stream.of(overlay).map(o -> new OverlayCreatedEvent(o)).forEach(eventService::publish);
        inputDisplay.update();
        
        
        
        // creating a display for the mask
        ImageDisplay outputDisplay = new DefaultImageDisplay();
        context.inject(outputDisplay);
        outputDisplay.display(input.getDataset());

        // adding the overlay to the input dipslay

        handler.setStatus("Gathering statistics...");

        logger.info("Gathering statistics");

        handler.setProgress(-1);
       
        measureService.measureAllOverlay(inputDisplay);
        
        if (isExplorer()) {
            activityService.openByType(ImageJContainer.class);
        }
       
        uiService.show(outputDisplay);

    }

    protected Task<Boolean> generateTask(TaskButtonBinding binding) {

        if (activityService.getCurrentActivity() instanceof ExplorerActivity) {

            final DefaultWorkflow workflow = new DefaultWorkflow(workflowPanel.stepListProperty());
            final List<SegmentedObject> objectFound = new ArrayList<>();
            List<BatchSingleInput> inputList = explorerService.getSelectedItems()
                    .stream()
                    .map(explorable -> new BatchInputBuilder(context)
                            .from(explorable)
                            //.saveNextToSourceWithPrefix("_mask","png")
                            .onFinished(segmentationService.addObjectToList(objectFound))
                            //.onFinished(segmentationService::saveOverlaysNextToSourceFile)
                            .getInput()
                    )
                    .collect(Collectors.toList());

            Task<Boolean> task = new CallbackTask<List<BatchSingleInput>, Boolean>(inputList)
                    .run((progress, input) -> {
                        boolean result = batchService.applyWorkflow(progress, input, workflow);
                        if (result == true) {

                            uiService.showDialog(String.format("%d objects where segmented", objectFound.size()), "Segmentation over");
                            folderManagerService.getCurrentFolder().addObjects(objectFound);
                            folderManagerService.setExplorationMode(ExplorationMode.OBJECT);
                            return result;
                        } else {
                            uiService.showDialog(String.format("Error when segmenting the objects"));
                            return false;
                        }
                    });

            loadingScreenService.frontEndTask(task);

            return task;

        } else {
            
            Task<Boolean> task = new WorkflowBuilder(context)
                    .addInput(imageDisplayService.getActiveDataset().duplicate())
                    .execute(workflowPanel.stepListProperty())
                    .then(input -> detectObjectsAndDisplay(new SilentProgressHandler(), imageDisplayService.getActiveImageDisplay(), input))
                    .start();

            loadingScreenService.frontEndTask(
                    task, true);

            return task;

        }

    }

    private boolean isExplorer() {
        return isExplorer.get();
    }

    private String getTestButtonText() {
        if (!isExplorer()) {
             
                return "Segment the plane";
            
        } else {
            return "Test on one image";
        }
    }
    private boolean isMultidimensional() {
        return isMultidimensional.getValue();
    }
    private String getStartButtonText() {
        if (!isExplorer()) {
            if(!isMultidimensional()) {
                return "Segment this image";
            }
            else
            return "Use whole image as input";
        } else {
            return "Process all images";
        }
    }

    @FXML
    public void close() {
        uiContextService.leave("segment");
        uiContextService.leave("segmentation");
        if (!isExplorer()) {
            uiContextService.enter("imagej");
        }
        uiContextService.update();
    }

    private void segmentMore(ActionEvent event) {

        

    }

    protected Dataset getExampleDataset() {

        if (isExplorer()) {

            if (explorerService.getItems().size() == 0) {
                uiService.showDialog("No item selected or displayed.", DialogPrompt.MessageType.ERROR_MESSAGE);
                return null;
            } else if (explorerService.getSelectedItems().size() == 0) {
                return explorerService.getItems().get(0).getDataset();
            } else {
                return explorerService.getSelectedItems().get(0).getDataset();
            }
        } else {
            return imageDisplayService.getActiveDataset();
        }

    }

    protected void onPreviewMaskButtonClicked(ActionEvent event) {
        Dataset exampleDataset;

        exampleDataset = getExampleDataset().duplicate();
        imagePreview.refresh(exampleDataset, workflowPanel.stepListProperty());

        previewPopOver.setAutoHide(true);
        previewPopOver.setArrowLocation(PopOver.ArrowLocation.RIGHT_CENTER);
        if (previewPopOver.isShowing() == false) {
            previewPopOver.show(whiteObjectCheckbox);
        }

    }
    
    protected void onSegmetationContextChanged(Observable obs, Boolean oldValue, Boolean newValue) {
        if(!newValue) {
            
            
            imageDisplayService
                    .getImageDisplays()
                    .stream()
                    .map(imageDisplayService::getActiveDatasetView)
                    .filter(view->view.getColorMode() == ColorMode.GRAYSCALE)
                    .forEach(view-> {
                        view.setColorMode(ColorMode.COLOR);
                    });
            
        }
    }
}
