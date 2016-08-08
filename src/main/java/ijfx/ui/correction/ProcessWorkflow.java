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

import ijfx.service.batch.SilentImageDisplay;
import ijfx.service.dataset.DatasetUtillsService;
import ijfx.ui.datadisplay.image.ImageDisplayPane;
import io.datafx.controller.ViewController;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import net.imagej.Dataset;
import net.imagej.display.ImageDisplay;
import org.scijava.Context;
import org.scijava.io.IOService;
import org.scijava.object.ObjectService;
import org.scijava.plugin.Parameter;

/**
 *
 * @author Tuan anh TRINH
 */
@ViewController(value = "ProcessWorkflow.fxml")
public class ProcessWorkflow extends CorrectionFlow {

    @Parameter
    DatasetUtillsService datasetUtillsService;

    @Parameter
    Context context;

    @Parameter
    IOService iOService;

    @FXML
    ListView<String> listView;

    @FXML
    Button processButton;

    @FXML
    HBox hBox;

    @Inject
    WorkflowModel workflowModel;

    ImageDisplayPane imageDisplayPaneLeft;

    ImageDisplayPane imageDisplayPaneRight;

    public ProcessWorkflow() {
        CorrectionActivity.getStaticContext().inject(this);

        try {
            imageDisplayPaneLeft = new ImageDisplayPane(context);
            imageDisplayPaneRight = new ImageDisplayPane(context);
        } catch (IOException ex) {
            Logger.getLogger(ProcessWorkflow.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @PostConstruct
    public void init() {
        bindListView();
        hBox.getChildren().add(imageDisplayPaneLeft);
        hBox.getChildren().add(imageDisplayPaneRight);

    }

    @FXML
    public void process() {
        List<File> files = workflowModel.getFiles();
        workflowModel.getMapImages().clear();
        files.stream().forEach((File file) -> {
            try {
                Dataset inputDataset = (Dataset) iOService.open(file.getAbsolutePath());
                ImageDisplay imageDisplay = new SilentImageDisplay(context, inputDataset);
                imageDisplay.setPosition(workflowModel.positionRightProperty.get());
                Dataset outputDataset = workflowModel.getTransformedImage(imageDisplay);
                workflowModel.getMapImages().put(file.getAbsolutePath(), outputDataset);
            } catch (IOException ex) {
                Logger.getLogger(ProcessWorkflow.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        bindListView();
    }

    public void bindListView() {
        listView.getItems().addAll(workflowModel.getMapImages().keySet());
        listView.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            try {
                Dataset datasetTarget = workflowModel.getMapImages().get(newValue);
                ImageDisplay imageDisplayTarget = new SilentImageDisplay(context, datasetTarget);
//                imageDisplayTarget.setPosition(workflowModel.positionLeftProperty.get());
//                imageDisplayPaneLeft.display(imageDisplayTarget);
                selectPosition(imageDisplayTarget, workflowModel.positionLeftProperty.get(), workflowModel.positionRightProperty.get(), imageDisplayPaneRight);

                Dataset datasetSource = (Dataset) iOService.open(newValue);
                ImageDisplay imageDisplaySource = new SilentImageDisplay(context, datasetSource);
//                imageDisplaySource.setPosition(workflowModel.positionLeftProperty.get());
//                imageDisplayPaneRight.display(imageDisplaySource);
                selectPosition(imageDisplaySource, workflowModel.positionLeftProperty.get(), workflowModel.positionRightProperty.get(), imageDisplayPaneLeft);

            } catch (IOException ex) {
                Logger.getLogger(ProcessWorkflow.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

    }

    public void selectPosition(ImageDisplay imageDisplay, int[] firstPosition, int[] secondPosition, ImageDisplayPane imageDisplayPane) {
        imageDisplay.setPosition(firstPosition);
        Dataset datasetFirstSlide = datasetUtillsService.extractPlane(imageDisplay);

        imageDisplay.setPosition(secondPosition);
        Dataset datasetSecondSlide = datasetUtillsService.extractPlane(imageDisplay);
        workflowModel.extractAndMerge(new Dataset[]{datasetFirstSlide, datasetSecondSlide}, imageDisplayPane);
    }

}
