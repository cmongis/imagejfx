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

import ijfx.plugins.stack.ImagesToStack;
import ijfx.service.ImagePlaneService;
import ijfx.service.dataset.DatasetUtillsService;
import ijfx.ui.datadisplay.image.ImageDisplayPane;
import io.datafx.controller.ViewController;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import net.imagej.Dataset;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.display.DefaultImageDisplay;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.event.DataViewUpdatedEvent;
import org.scijava.Context;
import org.scijava.command.CommandModule;
import org.scijava.command.CommandService;
import org.scijava.display.DisplayService;
import org.scijava.event.EventHandler;
import org.scijava.plugin.Parameter;
import org.scijava.plugins.commands.io.OpenFile;

/**
 *
 * @author Tuan anh TRINH
 */
@ViewController(value = "BUnwarpJWorkflow.fxml")
public class BUnwarpJWorkflow extends AbstractCorrectionActivity {

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
//        imageDisplayPaneList.stream().forEach((ImageDisplayPane e) -> {
//            try {
//                e = initDisplayPane();
//            } catch (IOException ex) {
//                Logger.getLogger(BUnwarpJWorkflow.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        });
        imageDisplayPaneRight.getCanvas().getCamera().zoomProperty().bindBidirectional(imageDisplayPaneBottom.getCanvas().getCamera().zoomProperty());
        imageDisplayPaneRight.getCanvas().getCamera().zoomProperty().bindBidirectional(imageDisplayPaneLeft.getCanvas().getCamera().zoomProperty());

        imageDisplayPaneLeft.getCanvas().getCamera().xProperty().bindBidirectional(imageDisplayPaneRight.getCanvas().getCamera().xProperty());
        imageDisplayPaneLeft.getCanvas().getCamera().xProperty().bindBidirectional(imageDisplayPaneBottom.getCanvas().getCamera().xProperty());

        imageDisplayPaneLeft.getCanvas().getCamera().yProperty().bindBidirectional(imageDisplayPaneRight.getCanvas().getCamera().yProperty());
        imageDisplayPaneLeft.getCanvas().getCamera().yProperty().bindBidirectional(imageDisplayPaneBottom.getCanvas().getCamera().yProperty());

        rightButton.setOnAction(e -> {
            try {
                imagesContainer.add(imageDisplayPaneRight, 1, 0);
                openImage(imageDisplayPaneRight);
                extractAndMerge();
            } catch (InterruptedException | ExecutionException ex) {
                Logger.getLogger(BUnwarpJWorkflow.class.getName()).log(Level.SEVERE, null, ex);
            }

        });
        leftButton.setOnAction(e -> {
            imagesContainer.add(imageDisplayPaneLeft, 0, 0);
            openImage(imageDisplayPaneLeft);
//            imageDisplayPaneLeft.getCanvas().getCamera().zoomProperty().bindBidirectional(imageDisplayPaneRight.getCanvas().getCamera().zoomProperty());

        });

    }

    protected ImageDisplay displayDataset(Dataset dataset, ImageDisplayPane imageDisplayPane) {
        ImageDisplay imageDisplay = (ImageDisplay) displayService.createDisplay(dataset);
        imageDisplay.display(dataset);
        imageDisplayPane.display(imageDisplay);
        return imageDisplay;
    }

    protected ImageDisplay openImage(ImageDisplayPane imageDisplayPane) {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);

        Dataset flatFieldDataset = null;
        try {
            flatFieldDataset = imagePlaneService.openVirtualDataset(file);
        } catch (IOException ex) {
            Logger.getLogger(BUnwarpJWorkflow.class.getName()).log(Level.SEVERE, null, ex);
        }
        return displayDataset(flatFieldDataset, imageDisplayPane);

    }

    protected ImageDisplayPane initDisplayPane() throws IOException {
        ImageDisplayPane imageDisplayPane = new ImageDisplayPane(context);
        imageDisplayPane.setOnMouseClicked(e -> displayService.setActiveDisplay(imageDisplayPane.getImageDisplay()));
        return imageDisplayPane;
    }

    protected void extractAndMerge() throws InterruptedException, ExecutionException {
        Dataset[] datasets = new Dataset[2];
        datasets[0] = datasetUtillsService.extractPlane(imageDisplayPaneLeft.getImageDisplay());
        datasets[1] = datasetUtillsService.extractPlane(imageDisplayPaneRight.getImageDisplay());

        Future<CommandModule> run = commandService.run(ImagesToStack.class, true, "datasetArray", datasets, "axisType", Axes.CHANNEL);
        CommandModule commandModule = run.get();
        Dataset dataset = (Dataset) commandModule.getOutput("outputDataset");
        displayDataset(dataset, imageDisplayPaneBottom);
    }
    
        @EventHandler
    public void handleEvent(DataViewUpdatedEvent event) {
        try {
            extractAndMerge();
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(BUnwarpJWorkflow.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
