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
import ijfx.service.batch.BatchService;
import ijfx.service.batch.BatchSingleInput;
import ijfx.service.batch.input.BatchInputBuilder;
import ijfx.service.ui.LoadingScreenService;
import ijfx.service.uicontext.UiContextService;
import ijfx.service.workflow.DefaultWorkflow;
import ijfx.service.workflow.Workflow;
import ijfx.ui.UiConfiguration;
import ijfx.ui.UiPlugin;
import ijfx.ui.batch.WorkflowPanel;
import ijfx.ui.explorer.Explorable;
import ijfx.ui.explorer.ExplorerService;
import ijfx.ui.main.ImageJFX;
import ijfx.ui.main.Localization;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
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
import mongis.utils.TaskButtonBinding;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

/**
 *
 * @author cyril
 */
@Plugin(type=UiPlugin.class)
@UiConfiguration(id="batch-processing-panel",context="batch",localization = Localization.RIGHT)
public class BatchProcessingPanel extends BorderPane implements UiPlugin{
    
    @FXML
    Button testButton;
    
    @FXML
    Button startButton;
    
    @FXML
    Label titleLabel;
    
    @FXML
    VBox workflowVBox;
    
   @Parameter
   Context context;
    
    @Parameter
    BatchService batchService;

    @Parameter
    ExplorerService explorerService;
    
    @Parameter
    UIService uiService;
    
    @Parameter
    LoadingScreenService loadingScreenService;
    
     @Parameter
    private UiContextService uiContextService;
    
    
    WorkflowPanel workflowPanel;
    
    
    
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
        } catch (IOException ex) {
           ImageJFX.getLogger().log(Level.SEVERE,"Error when creating the BatchProcessingPanel",ex);
        }
        
    }
    @Override
    public UiPlugin init() {
        workflowPanel = new WorkflowPanel(context);
          workflowVBox.getChildren().add(workflowPanel);
        return this;
    }
    
   
    public Task<Boolean> generateBatchTask(TaskButtonBinding binding) {
        return new CallbackTask<Void,Boolean>()
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
        return new BatchInputBuilder(context).from(explorable).saveNextToSourceWithPrefix("_processed").getInput();
    }
    
     public Task<Boolean> generateTestBatchTask(TaskButtonBinding binding) {
        return new CallbackTask<Void,Boolean>()
                .runLongCallable(this::runTest)
                .submit(loadingScreenService);
                
    }
    
    public Boolean runTest(ProgressHandler handler) {
       
        
       
         if(getItems().size() > 0) {
             
             Explorable explorable = getItems().get(0);
             
             BatchSingleInput input =
                     new BatchInputBuilder(context)
                             .from(explorable)
                             .onFinished(this::onTestFinished)
                             .getInput();
             
             
             return batchService.applyWorkflow(handler, input, new DefaultWorkflow(workflowPanel.stepListProperty()));
             
             
             
         }
         else {
             uiService.showDialog("There is no item to batch process. Select or filter some planes", "Batch processing");
         }
        
        return false;
    }
    
    
    private List<? extends Explorable> getItems() {
        if(explorerService.getSelectedItems().size() == 0) {
            return explorerService.getFilteredItems();
        }
        else {
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
        uiContextService.leave("segment segmentation");
        uiContextService.update();
    }
    
      
}
