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
import ijfx.bridge.ImageJContainer;
import ijfx.plugins.commands.BinaryToOverlay;
import ijfx.service.batch.BatchService;
import ijfx.service.batch.BatchSingleInput;
import ijfx.service.batch.ImageDisplayBatchInput;
import ijfx.service.batch.SegmentationService;
import ijfx.service.batch.input.BatchInputBuilder;
import ijfx.service.overlay.OverlayStatService;
import ijfx.service.ui.LoadingScreenService;
import ijfx.service.uicontext.UiContextService;
import ijfx.service.workflow.DefaultWorkflow;
import ijfx.ui.UiConfiguration;
import ijfx.ui.UiPlugin;
import ijfx.ui.activity.ActivityService;
import ijfx.ui.batch.WorkflowPanel;
import ijfx.ui.context.UiContextProperty;
import ijfx.ui.explorer.Explorable;
import ijfx.ui.explorer.ExplorerActivity;
import ijfx.ui.explorer.ExplorerService;
import ijfx.ui.main.Localization;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import mongis.utils.CallbackTask;
import mongis.utils.FXUtilities;
import mongis.utils.ProgressHandler;
import mongis.utils.SilentProgressHandler;
import mongis.utils.TaskButtonBinding;
import net.imagej.display.DefaultImageDisplay;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.OverlayService;
import net.imagej.overlay.Overlay;
import net.imagej.plugins.commands.binary.Binarize;
import net.imagej.plugins.commands.imglib.GaussianBlur;
import net.imagej.table.DefaultGenericTable;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

/**
 *
 * @author cyril
 */
@Plugin(type = UiPlugin.class)
@UiConfiguration(id = "segmentation-panel", context = "segmentation", localization = Localization.RIGHT)
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

    @Parameter
    private Context context;

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
    ActivityService activityService;

    @Parameter
    SegmentationService segmentationService;

    TaskButtonBinding taskButtonBinding;

    WorkflowPanel workflowPanel;

    BooleanProperty replaceFile;

    ObjectProperty<File> ouputFolderProperty;

    ReadOnlyBooleanProperty isExplorer;

    private static final Boolean USE_ALL_PLANE = Boolean.TRUE;

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
        workflowPanel.setMinHeight(400);
        workflowVBox.getChildren().add(workflowPanel);

        workflowPanel.addStep(GaussianBlur.class);
        workflowPanel.addStep(Binarize.class);

        isExplorer = new UiContextProperty(context, "explorerActivity");

        //startButton.visibleProperty().bind(isExplorer);
        // Bindings buttons
        new TaskButtonBinding(startButton)
                .setTaskFactory(this::generateTask)
                .setBaseIcon(FontAwesomeIcon.LIST_ALT)
                .textBeforeTaskProperty().bind(Bindings.createStringBinding(this::getStartButtonText, isExplorer));

        new TaskButtonBinding(testButton)
                .setTaskFactory(this::createTaskButtonBinding)
                .setBaseIcon(FontAwesomeIcon.CHECK_SQUARE)
                .textBeforeTaskProperty().bind(Bindings.createStringBinding(this::getTestButtonText, isExplorer));

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

        System.out.println("Executing");

        if (handler == null) {
            handler = new SilentProgressHandler();
        }

        // generating the batch service input from an image display
        BatchSingleInput input;
        ImageDisplay inputDisplay;


        if (isExplorer()) {
            Explorable explorable;
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
            input = new ImageDisplayBatchInput(imageDisplayService.getActiveImageDisplay(), true);
            inputDisplay = imageDisplayService.getActiveImageDisplay();
        }

        System.out.println("Executing");

        // applying the workflow
        boolean batchResult = batchService.applyWorkflow(handler, input, new DefaultWorkflow(workflowPanel.stepListProperty()));

        if (!batchResult) {
            return false;
        }

        // detecting objects
        handler.setStatus("Detecting objects...");
        Overlay[] overlay = BinaryToOverlay.transform(context, input.getDataset(), false);
        // giving a random color to each overlay
        overlayStatsService.setRandomColor(Arrays.asList(overlay));

        // deleting the overlay owned previously by the input
        overlayService.getOverlays(inputDisplay).forEach(o -> overlayService.removeOverlay(o));
        // creating a display for the mask
        ImageDisplay outputDisplay = new DefaultImageDisplay();
        context.inject(outputDisplay);
        outputDisplay.display(input.getDataset());

        // adding the overlay to the input dipslay
        overlayService.addOverlays(inputDisplay, Arrays.asList(overlay));

        handler.setStatus("Gathering statistics...");

        handler.setProgress(-1);

        // gathering statistics about the object from the input display
        List<HashMap<String, Double>> map = Arrays.stream(overlay)
                .map(o -> {
                    try {

                        return overlayStatsService.getStatisticsAsMap(inputDisplay, o);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(o -> o != null)
                .collect(Collectors.toList());

        DefaultGenericTable resultTable = new DefaultGenericTable();
        if (map.size() > 0) {
            int headerNumber = map.get(0).keySet().size() + 1;

            String[] headers = map.get(0).keySet().toArray(new String[headerNumber]);
            resultTable.insertColumns(0, headers);

            for (int rowNumber = 0; rowNumber != map.size(); rowNumber++) {

                final int finalRowNumber = rowNumber;
                resultTable.insertRow(finalRowNumber);
                map.get(rowNumber).forEach((key, value) -> {
                    System.out.println(String.format("Setting the value %s to %.3f (%d)", key, value, finalRowNumber));

                    resultTable.set(key, finalRowNumber, value);
                });
            }

        }

        if (isExplorer()) {
            activityService.openByType(ImageJContainer.class);
        }
        uiService.show(resultTable);
        uiService.show(outputDisplay);
        return true;

    }

    
    
    
    protected Task<Boolean> generateTask(TaskButtonBinding binding) {

        if (activityService.getCurrentActivity() instanceof ExplorerActivity) {

            final DefaultWorkflow workflow = new DefaultWorkflow(workflowPanel.stepListProperty());

            List<BatchSingleInput> inputList = explorerService.getSelectedItems()
                    .stream()
                    .map(explorable -> new BatchInputBuilder(context)
                            .from(explorable)
                            //.saveNextToSourceWithPrefix("_mask","png")
                            .onFinished(segmentationService::saveOverlaysNextToSourceFile)
                            .getInput()
                    )
                    .collect(Collectors.toList());

            Task<Boolean> task = new CallbackTask<List<BatchSingleInput>, Boolean>(inputList)
                    .run((progress, input) -> {
                        return batchService.applyWorkflow(progress, input, workflow);
                    });

            loadingScreenService.frontEndTask(task);

            return task;

        }

        return null;

    }
    
     
    
    

    private boolean isExplorer() {
        return isExplorer.getValue();
    }

    private String getTestButtonText() {
        if (!isExplorer()) {
            return "Test on the current plane";
        } else {
            return "Test on one image";
        }
    }

    private String getStartButtonText() {
        if (!isExplorer()) {
            return "Use whole image as input";
        } else {
            return "Process all images";
        }
    }

    @FXML
    public void close() {
        uiContextService.leave("segment");
        uiContextService.leave("segmentation");
        uiContextService.update();
    }

}
