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

import ijfx.plugins.commands.BinaryToOverlay;
import ijfx.service.batch.BatchInputWrapper;
import ijfx.service.batch.BatchService;
import ijfx.service.batch.BatchSingleInput;
import ijfx.service.batch.ImageDisplayBatchInput;
import ijfx.service.overlay.OverlayStatService;
import ijfx.service.ui.LoadingScreenService;
import ijfx.service.workflow.DefaultWorkflow;
import ijfx.ui.UiConfiguration;
import ijfx.ui.UiPlugin;
import ijfx.ui.batch.WorkflowPanel;
import ijfx.ui.main.Localization;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import mongis.utils.AsyncCallback;
import mongis.utils.FXUtilities;
import mongis.utils.ProgressHandler;
import mongis.utils.SilentProgressHandler;
import mongis.utils.TaskButtonBinding;
import net.imagej.Dataset;
import net.imagej.display.DefaultImageDisplay;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.OverlayService;
import net.imagej.overlay.Overlay;
import net.imagej.plugins.commands.binary.Binarize;
import net.imagej.plugins.commands.imglib.GaussianBlur;
import net.imagej.table.DefaultGenericTable;
import net.imagej.table.Table;
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

    @Parameter
    private Context context;

    @FXML
    private VBox workflowVBox;

    @FXML
    private Button testButton;

    @Parameter
    ImageDisplayService imageDisplayService;

    @Parameter
    BatchService batchService;

    @Parameter
    UIService uiService;

    @Parameter
    OverlayService overlayService;

    @Parameter
    OverlayStatService overlayStatsService;

    @Parameter
    LoadingScreenService loadingScreenService;

    TaskButtonBinding taskButtonBinding;

    WorkflowPanel workflowPanel;

    public SegmentationPanel() {
        try {
            FXUtilities.injectFXML(this);

            taskButtonBinding = new TaskButtonBinding(testButton);
            taskButtonBinding.runTaskOnClick(this::createTaskButtonBinding);

        } catch (IOException ex) {
            Logger.getLogger(SegmentationPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public UiPlugin init() {

        workflowPanel = new WorkflowPanel(context);

        workflowVBox.getChildren().add(workflowPanel);

        workflowPanel.addStep(GaussianBlur.class);
        workflowPanel.addStep(Binarize.class);

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

        Task<Boolean> task = new AsyncCallback<ImageDisplay, Boolean>(imageDisplayService.getActiveImageDisplay())
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
        BatchSingleInput input = new ImageDisplayBatchInput(imageDisplayService.getActiveImageDisplay(), true);
        System.out.println("Executing");

        // applying the workflow
        boolean batchResult = batchService.applyWorkflow(handler, input, new DefaultWorkflow(workflowPanel.stepListProperty()));

        if (!batchResult) {
            return false;
        }

        // detecting objects
        handler.setStatus("Detecting objects...");
        Overlay[] overlay = BinaryToOverlay.transform(context, input.getDataset(), false);

        // adding the objects to the display
        ImageDisplay outputDisplay = new DefaultImageDisplay();
        context.inject(outputDisplay);
        outputDisplay.display(input.getDataset());
        overlayService.addOverlays(outputDisplay, Arrays.asList(overlay));

        handler.setStatus("Gathering statistics...");

        handler.setProgress(-1);

        // gathering statistics about the object from the input display
        List<HashMap<String, Double>> map = Arrays.stream(overlay)
                .map(o -> {
                    try {
                        return overlayStatsService.getStat(imageDisplay, o);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(o -> o != null)
                .collect(Collectors.toList());

        DefaultGenericTable resultTable = new DefaultGenericTable();
        if (map.size() > 0) {
            int headerNumber = map.get(0).keySet().size();

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

        uiService.show(resultTable);
        uiService.show(outputDisplay);
        return true;

    }

}
