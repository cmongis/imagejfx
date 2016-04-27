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
import ijfx.service.ui.LoadingScreenService;
import ijfx.service.workflow.DefaultWorkflow;
import ijfx.ui.UiConfiguration;
import ijfx.ui.UiPlugin;
import ijfx.ui.batch.WorkflowPanel;
import ijfx.ui.main.Localization;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import mongis.utils.AsyncCallback;
import mongis.utils.FXUtilities;
import mongis.utils.TaskButtonBinding;
import net.imagej.Dataset;
import net.imagej.display.DefaultImageDisplay;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.OverlayService;
import net.imagej.overlay.Overlay;
import net.imagej.plugins.commands.binary.Binarize;
import net.imagej.plugins.commands.imglib.GaussianBlur;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

/**
 *
 * @author cyril
 */
@Plugin(type=UiPlugin.class)
@UiConfiguration(id = "segmentation-panel",context = "segmentation",localization = Localization.RIGHT)
public class SegmentationPanel extends BorderPane implements UiPlugin{
    
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
       return runTask();
    }
    
    public Task<Boolean> runTask() {
        
        List<BatchSingleInput> inputs = new ArrayList<>();
        BatchSingleInput input = new ImageDisplayBatchInput(imageDisplayService.getActiveImageDisplay(), true);
        input = new BatchInputWrapper(input)
                .then(this::onTestFinished);
        inputs.add(input);
        Task<Boolean> task = batchService.applyWorkflow(inputs, new DefaultWorkflow(workflowPanel.stepListProperty()));
        loadingScreenService.frontEndTask(task, false);
        return task;
    }
    
    public void onTestFinished(BatchSingleInput output) {
        System.out.println("test is finished !");
       
        Task task = new AsyncCallback<Dataset,ImageDisplay>()
                .setInput(output.getDataset())
                .run(dataset->{
                    Overlay[] overlay = BinaryToOverlay.transform(context, dataset, false);
                    ImageDisplay imageDisplay = new DefaultImageDisplay();
                    
                    context.inject(imageDisplay);
                    
                    imageDisplay.display(dataset);
                    overlayService.addOverlays(imageDisplay, Arrays.asList(overlay));
                    
                    return imageDisplay;
                })
                .then(uiService::show)
                .setName("Converting mask to overlay...")
                .start();
        
        loadingScreenService.frontEndTask(task, false);
        
    }
    
}
