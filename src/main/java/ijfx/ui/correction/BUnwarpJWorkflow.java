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
package ijfx.ui.correction;

import ijfx.plugins.commands.ChannelMerger;
import ijfx.plugins.flatfield.FlatFieldCorrection;
import ijfx.plugins.stack.ImagesToStack;
import ijfx.service.ImagePlaneService;
import ijfx.service.batch.BatchService;
import ijfx.service.batch.BatchSingleInput;
import ijfx.service.batch.DisplayBatchInput;
import ijfx.service.batch.SilentImageDisplay;
import ijfx.service.dataset.DatasetUtillsService;
import ijfx.ui.datadisplay.image.ImageDisplayPane;
import io.datafx.controller.ViewController;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import mongis.utils.CallbackTask;
import net.imagej.Dataset;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.display.DefaultImageDisplay;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.event.DataViewUpdatedEvent;
import net.imglib2.type.numeric.RealType;
import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.command.CommandModule;
import org.scijava.command.CommandService;
import org.scijava.display.DisplayService;
import org.scijava.event.EventHandler;
import org.scijava.event.SciJavaEvent;
import org.scijava.io.IOService;
import org.scijava.module.MethodCallException;
import org.scijava.module.Module;
import org.scijava.module.ModuleService;
import org.scijava.plugin.Parameter;
import org.scijava.plugins.commands.io.OpenFile;

/**
 *
 * @author Tuan anh TRINH
 */
@ViewController(value = "BUnwarpJWorkflow.fxml")
public class BUnwarpJWorkflow extends AbstractCorrectionActivity {

    @Parameter
    IOService iOService;

    @Parameter
    DatasetUtillsService datasetUtillsService;

    @Parameter
    ImageDisplayService imageDisplayService;

    @Parameter
    CommandService commandService;

    @Parameter
    DisplayService displayService;

    @Inject
    WorkflowModel workflowModel;

    @Parameter
    Context context;

    @Parameter
    ImagePlaneService imagePlaneService;

    @Parameter
    BatchService batchService;

    @Parameter
    ModuleService moduleService;

    protected ImageDisplayPane imageDisplayPaneLeft;

    protected ImageDisplayPane imageDisplayPaneRight;

    protected ImageDisplayPane imageDisplayPaneBottom;

    @FXML
    GridPane imagesContainer;

    @FXML
    Button leftButton;

    @FXML
    Button rightButton;

//    @FXML
//    Button bottomLeftButton;
    protected final List<ImageDisplayPane> imageDisplayPaneList;

    public BUnwarpJWorkflow() {
        CorrectionActivity.getStaticContext().inject(this);

        imageDisplayPaneList = Arrays.asList(imageDisplayPaneLeft, imageDisplayPaneRight, imageDisplayPaneBottom);
        try {
            imageDisplayPaneBottom = initDisplayPane();
            imageDisplayPaneLeft = initDisplayPane();
            imageDisplayPaneRight = initDisplayPane();

        } catch (IOException ex) {
            Logger.getLogger(BUnwarpJWorkflow.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @PostConstruct
    public void init() {
        imagesContainer.add(imageDisplayPaneBottom, 0, 1);
        bindProperty();

        rightButton.setOnAction(e -> {
            imagesContainer.add(imageDisplayPaneRight, 1, 0);
            openImage(imageDisplayPaneRight);
        });
        leftButton.setOnAction(e -> {
            imagesContainer.add(imageDisplayPaneLeft, 0, 0);
            openImage(imageDisplayPaneLeft);
        });

    }

    protected ImageDisplay displayDataset(Dataset dataset, ImageDisplayPane imageDisplayPane) {
        try {

            imageDisplayPane.getImageDisplay().clear();
        } catch (Exception e) {
        }
        SilentImageDisplay imageDisplay = new SilentImageDisplay(context, dataset);
        imageDisplay.display(dataset);
        imageDisplayPane.display(imageDisplay);
        return imageDisplay;
    }

    protected ImageDisplay openImage(ImageDisplayPane imageDisplayPane) {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);

        Dataset dataset = null;
        try {
            dataset = (Dataset) iOService.open(file.getAbsolutePath());
        } catch (IOException ex) {
            Logger.getLogger(BUnwarpJWorkflow.class.getName()).log(Level.SEVERE, null, ex);
        }
        return displayDataset(dataset, imageDisplayPane);

    }

    protected ImageDisplayPane initDisplayPane() throws IOException {
        ImageDisplayPane imageDisplayPane = new ImageDisplayPane(context);
        imageDisplayPane.setOnMouseClicked(e -> displayService.setActiveDisplay(imageDisplayPane.getImageDisplay()));
        return imageDisplayPane;
    }

    protected void extractAndMerge() {
        Dataset[] datasets = new Dataset[2];
        ImageDisplay imageDisplayLeft = imageDisplayPaneLeft.getImageDisplay();
        datasets[0] = datasetUtillsService.extractPlane(imageDisplayLeft);

        ImageDisplay imageDisplayRight = imageDisplayPaneRight.getImageDisplay();
        datasets[1] = datasetUtillsService.extractPlane(imageDisplayRight);

//        Future<CommandModule> run = commandService.run(ImagesToStack.class, true, "datasetArray", datasets, "axisType", Axes.CHANNEL);
        new CallbackTask<Object, Object>().run(() -> {
            try {
                CommandModule commandModule;
                Platform.runLater(() -> imagesContainer.getChildren().remove(imageDisplayPaneBottom));
                imageDisplayPaneBottom = initDisplayPane();
                bindProperty();
                BatchSingleInput batchSingleInput = new DisplayBatchInput();
                this.context.inject(batchSingleInput);
                batchSingleInput.setDataset(datasets[1]);

//                Module module = moduleService.createModule(commandService.getCommand(ImagesToStack.class));
                try {
//                    this.context.inject(module.getDelegateObject());

                } catch (Exception e) {
                    e.printStackTrace();
                }
                Map<String, Object> inputMap = new HashMap();
                inputMap.put("datasetArray", datasets);
                inputMap.put("axisType", Axes.CHANNEL);
                Module module = executeCommand(ImagesToStack.class, inputMap);
                Dataset result = (Dataset) module.getOutput("outputDataset");
//                batchService.executeModule(batchSingleInput, module, inputMap);
//                Dataset result = batchSingleInput.getDataset();
                Platform.runLater(() -> imagesContainer.add(imageDisplayPaneBottom, 0, 1));
                
//                ChannelMerger<? extends RealType<?>> merger = new ChannelMerger(context);
//   merger.setInput(result);
//   merger.run();
//   Dataset output = merger.getOutput();
                displayDataset(result, imageDisplayPaneBottom);
            } catch (IOException ex) {
                Logger.getLogger(BUnwarpJWorkflow.class.getName()).log(Level.SEVERE, null, ex);
            }
        }).start();

    }

    @EventHandler
    public void handleEvent(DataViewUpdatedEvent event) {
        if (imageDisplayPaneLeft.getImageDisplay().contains(event.getView()) || imageDisplayPaneRight.getImageDisplay().contains(event.getView())) {
            try {
                extractAndMerge();
            } catch (Exception ex) {
                Logger.getLogger(BUnwarpJWorkflow.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

    }

    public void bindProperty() {
        imageDisplayPaneBottom.getCanvas().getCamera().zoomProperty().bindBidirectional(imageDisplayPaneRight.getCanvas().getCamera().zoomProperty());
        imageDisplayPaneRight.getCanvas().getCamera().zoomProperty().bindBidirectional(imageDisplayPaneLeft.getCanvas().getCamera().zoomProperty());

        imageDisplayPaneLeft.getCanvas().getCamera().xProperty().bindBidirectional(imageDisplayPaneRight.getCanvas().getCamera().xProperty());
        imageDisplayPaneBottom.getCanvas().getCamera().xProperty().bindBidirectional(imageDisplayPaneLeft.getCanvas().getCamera().xProperty());

        imageDisplayPaneLeft.getCanvas().getCamera().yProperty().bindBidirectional(imageDisplayPaneRight.getCanvas().getCamera().yProperty());
        imageDisplayPaneBottom.getCanvas().getCamera().yProperty().bindBidirectional(imageDisplayPaneLeft.getCanvas().getCamera().yProperty());
    }

    private <C extends Command> Module executeCommand(Class<C> type, Map<String, Object> parameters) {
        Module module = moduleService.createModule(commandService.getCommand(type));
        try {
            module.initialize();
        } catch (MethodCallException ex) {
            Logger.getLogger(FlatFieldCorrection.class.getName()).log(Level.SEVERE, null, ex);
        }
        parameters.forEach((k, v) -> {
            module.setInput(k, v);
            module.setResolved(k, true);
        });

        Future run = moduleService.run(module, false, parameters);

        try {
            run.get();
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(FlatFieldCorrection.class.getName()).log(Level.SEVERE, null, ex);
        }
        return module;
    }

}
