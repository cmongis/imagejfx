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
package ijfx.ui.segmentation;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import ijfx.plugins.commands.Binarize;
import ijfx.service.batch.BatchService;
import ijfx.service.ui.LoadingScreenService;
import ijfx.service.ui.MeasurementService;
import ijfx.service.workflow.DefaultWorkflow;
import ijfx.service.workflow.Workflow;
import ijfx.service.workflow.WorkflowBuilder;
import ijfx.ui.batch.WorkflowPanel;
import ijfx.ui.context.UiContextProperty;
import ijfx.ui.widgets.PopoverToggleButton;
import ijfx.ui.widgets.PrettyStats;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import mongis.utils.transition.OpacityTransitionBinding;
import net.imagej.Dataset;
import net.imagej.display.ImageDisplay;
import net.imagej.plugins.commands.assign.InvertDataValues;
import net.imagej.plugins.commands.binary.DilateBinaryImage;
import net.imagej.plugins.commands.binary.ErodeBinaryImage;

import net.imagej.plugins.commands.imglib.GaussianBlur;
import net.imglib2.img.Img;
import net.imglib2.type.logic.BitType;
import org.controlsfx.control.PopOver;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

/**
 *
 * @author Cyril MONGIS, 2016
 */
@Plugin(type = SegmentationUiPlugin.class, label = "Process workflow", priority = 0.09)
public class WorkflowSegmentationUiPanel extends VBox implements SegmentationUiPlugin<WorkflowSegmentation> {

    @Parameter
    private Context context;

    @Parameter
    private BatchService batchService;

    @Parameter
    UIService uiService;

    @Parameter
    LoadingScreenService loadingService;

    @Parameter
    MeasurementService measurementSrv;

    @FXML
    protected ToggleButton toggleButton;

    Property<Img<BitType>> maskProperty = new SimpleObjectProperty();

    protected WorkflowPanel workflowPanel;

    protected PrettyStats stepCount = new PrettyStats("Steps");

    protected Button testButton = new Button("Test", new FontAwesomeIconView(FontAwesomeIcon.CHECK_CIRCLE_ALT));

    protected ImageDisplay imageDisplay;

    private final BooleanProperty activatedProperty = new SimpleBooleanProperty();

    protected UiContextProperty isExplorer;

    
    WorkflowSegmentation currentSegmentation; 
    
    public WorkflowSegmentationUiPanel() {

        getStyleClass().add("vbox");

        // creating the toggle button displaying the workflow panel
        toggleButton = new ToggleButton("Edit workflow");
        toggleButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.GEARS));
        toggleButton.setMaxWidth(Double.POSITIVE_INFINITY);

        getChildren().addAll(stepCount, toggleButton, testButton);

        testButton.setOnAction(this::onTestClicked);
        testButton.setMaxWidth(Double.POSITIVE_INFINITY);
        testButton.getStyleClass().add("success");

    }

   
   

    @Override
    public Node getContentNode() {

        if (workflowPanel == null) {

            workflowPanel = new WorkflowPanel(context);
            workflowPanel.addStep(GaussianBlur.class);
            workflowPanel.addStep(Binarize.class);
            workflowPanel.addStep(InvertDataValues.class);
            workflowPanel.addStep(ErodeBinaryImage.class);
            workflowPanel.addStep(DilateBinaryImage.class);
            workflowPanel.setPrefHeight(500);
            workflowPanel.setPrefWidth(600);

            isExplorer = new UiContextProperty(context, "explorer");
            
            new OpacityTransitionBinding(testButton, isExplorer.not());
            
            stepCount.valueProperty().bind(Bindings.createIntegerBinding(() -> workflowPanel.stepListProperty().size(), workflowPanel.stepListProperty()));

            // binding the toggle button to the workflow panel
            PopoverToggleButton.bind(toggleButton, workflowPanel, PopOver.ArrowLocation.RIGHT_CENTER);
        }

        return this;
    }

    
    private void onTestClicked(ActionEvent event) {
        
        currentSegmentation.reprocess(workflowPanel.stepListProperty());
        
    }

  
    @Override
    public WorkflowSegmentation createSegmentation(ImageDisplay imageDisplay) {
        WorkflowSegmentation segmentation =  new WorkflowSegmentation(imageDisplay);
        segmentation.reprocess(workflowPanel.stepListProperty());
        currentSegmentation = segmentation;
        return segmentation;
        
    }

    
    
    
    @Override
    public void bind(WorkflowSegmentation t) {
        currentSegmentation = t;
    }

}
