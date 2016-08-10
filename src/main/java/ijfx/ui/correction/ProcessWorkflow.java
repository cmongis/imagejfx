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
import ijfx.service.ui.LoadingScreenService;
import ijfx.ui.datadisplay.image.ImageDisplayPane;
import io.datafx.controller.ViewController;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import mongis.utils.CallbackTask;
import net.imagej.Dataset;
import net.imagej.display.ColorMode;
import net.imagej.display.DefaultImageDisplay;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import org.scijava.Context;
import org.scijava.io.IOService;
import org.scijava.plugin.Parameter;

/**
 *
 * @author Tuan anh TRINH
 */
@ViewController(value = "ProcessWorkflow.fxml")
public class ProcessWorkflow extends CorrectionFlow {

    @Inject
    WorkflowModel workflowModel;

    @Parameter
    ImageDisplayService imageDisplayService;
    @Parameter
    DatasetUtillsService datasetUtillsService;

    @Parameter
    LoadingScreenService loadingScreenService;
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

    @FXML
    GridPane gridPane;

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
        gridPane.add(imageDisplayPaneLeft, 1, 0);
        gridPane.add(imageDisplayPaneRight, 2, 0);
        ImageDisplayPane[] imageDisplayPanes = new ImageDisplayPane[]{imageDisplayPaneLeft, imageDisplayPaneRight};
        bindPaneProperty(Arrays.asList(imageDisplayPanes));

        processButton.setOnMouseClicked(e -> process());

    }


    /**
     * Compute the dataset
     */
    public void process() {
        List<File> files = workflowModel.getFiles();
        listView.getItems().clear();
        workflowModel.getMapImages().clear();
        files.parallelStream().forEach((File file) -> {
            new CallbackTask<Void, Void>().run(() -> {
                try {
                    Dataset inputDataset = (Dataset) iOService.open(file.getAbsolutePath());
                    ImageDisplay imageDisplay = new SilentImageDisplay(context, inputDataset);
                    workflowModel.setPosition(workflowModel.getPositionRight(), imageDisplay);
                    Dataset targetDataset = imageDisplayService.getActiveDataset(imageDisplay);
                    workflowModel.setPosition(workflowModel.getPositionLeft(), imageDisplay);
                    Dataset sourceDataset = imageDisplayService.getActiveDataset(imageDisplay);
                    Dataset outputDataset = workflowModel.getTransformedImage(sourceDataset, targetDataset);

                    String key = file.getAbsolutePath();
                    workflowModel.getMapImages().put(key, outputDataset);
                    listView.getItems().add(key);
                } catch (IOException ex) {
                    Logger.getLogger(ProcessWorkflow.class.getName()).log(Level.SEVERE, null, ex);
                }
            })
                    .submit(loadingScreenService)
                    .start();

        });

    }

    /**
     * Add the items and set the listener on change
     */
    public void bindListView() {
        listView.getItems().addAll(workflowModel.getMapImages().keySet());
        listView.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            createSampleImage(newValue).start();
        });

    }
/**
 * 
 * @param imageDisplay
 * @param firstPosition
 * @param secondPosition
 * @param imageDisplayPane 
 */
    public void selectPosition(ImageDisplay imageDisplay, int[] firstPosition, int[] secondPosition, ImageDisplayPane imageDisplayPane) {
        workflowModel.setPosition(firstPosition, imageDisplay);
        imageDisplayService.getActiveDatasetView(imageDisplay).setColorMode(ColorMode.COLOR);
        Dataset datasetFirstSlide = datasetUtillsService.extractPlane(imageDisplay);
        workflowModel.setPosition(secondPosition, imageDisplay);
        Dataset datasetSecondSlide = datasetUtillsService.extractPlane(imageDisplay);
        workflowModel.extractAndMerge(new Dataset[]{datasetFirstSlide, datasetSecondSlide}, imageDisplayPane);
    }

    
    /**
     * Extract the plane at the different positions and concatenate the Dataset
     * @param newValue
     * @return 
     */
    private CallbackTask<Void, Void> createSampleImage(String newValue) {
        return new CallbackTask<Void, Void>().run(() -> {

            try {
                Dataset datasetTarget = workflowModel.getMapImages().get(newValue);
                ImageDisplay imageDisplayTarget = new DefaultImageDisplay();
                context.inject(imageDisplayTarget);
                imageDisplayTarget.display(datasetTarget);
                selectPosition(imageDisplayTarget, workflowModel.getPositionLeft(), workflowModel.getPositionRight(), imageDisplayPaneRight);

                Dataset datasetSource = (Dataset) iOService.open(newValue);
                ImageDisplay imageDisplaySource = new DefaultImageDisplay();
                context.inject(imageDisplaySource);
                imageDisplaySource.display(datasetSource);
                selectPosition(imageDisplaySource, workflowModel.getPositionLeft(), workflowModel.getPositionRight(), imageDisplayPaneLeft);

            } catch (IOException ex) {
                Logger.getLogger(ProcessWorkflow.class.getName()).log(Level.SEVERE, null, ex);
            }
        })
                .submit(loadingScreenService);
    }

}
