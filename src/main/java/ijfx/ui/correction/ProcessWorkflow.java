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
import ijfx.ui.main.ImageJFX;
import io.datafx.controller.ViewController;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import mongis.utils.CallbackTask;
import net.imagej.Dataset;
import net.imagej.display.ColorMode;
import net.imagej.display.DefaultImageDisplay;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import org.scijava.Context;
import org.scijava.display.DisplayService;
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
    DisplayService displayService;
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
    ListView<File> listViewItems;

    @FXML
    Button processButton;

    @FXML
    HBox hBox;

    @FXML
    GridPane gridPane;

    private File directory;

    ImageDisplayPane imageDisplayPaneLeft;

    ImageDisplayPane imageDisplayPaneRight;

    ExecutorService executor = ImageJFX.getThreadPool();

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
        setCellFactory(listViewItems);
        gridPane.add(imageDisplayPaneLeft, 1, 1);
        gridPane.add(imageDisplayPaneRight, 2, 1);
        ImageDisplayPane[] imageDisplayPanes = new ImageDisplayPane[]{imageDisplayPaneLeft, imageDisplayPaneRight};
        bindPaneProperty(Arrays.asList(imageDisplayPanes));

        processButton.setOnMouseClicked(e -> {
            saveDirectory();
            if (directory != null) {
                process();
            }
        });

    }

    /**
     * Compute the dataset
     */
    public void process() {
        List<File> files = workflowModel.getFiles();
        listViewItems.getItems().clear();
        workflowModel.getMapImages().clear();
        System.gc();
        workflowModel.transformeImages(files, directory.getAbsolutePath()).thenRunnable(() -> {
            files.sort((File o1, File o2) -> {
                return o1.getName().compareTo(o2.getName());
            });
            listViewItems.getItems().addAll(files);
        })
                .start();

    }

    /**
     * Add the items and set the listener on change
     */
    public void bindListView() {
        listViewItems.getItems().addAll(workflowModel.getMapImages().keySet());
        listViewItems.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends File> observable, File oldValue, File newValue) -> {
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
    public void selectPosition(ImageDisplay imageDisplay, long[] firstPosition, long[] secondPosition, ImageDisplayPane imageDisplayPane) {
        workflowModel.setPosition(firstPosition, imageDisplay);
        imageDisplayService.getActiveDatasetView(imageDisplay).setColorMode(ColorMode.COLOR);
        Dataset datasetFirstSlide = datasetUtillsService.extractPlane(imageDisplay);
        workflowModel.setPosition(secondPosition, imageDisplay);
        Dataset datasetSecondSlide = datasetUtillsService.extractPlane(imageDisplay);
        workflowModel.extractAndMerge(new Dataset[]{datasetFirstSlide, datasetSecondSlide}, imageDisplayPane);
    }

    /**
     * Extract the plane at the different positions and concatenate the Dataset
     *
     * @param newValue
     * @return
     */
    private CallbackTask<Void, Void> createSampleImage(File newValue) {
        return new CallbackTask<Void, Void>().run(() -> {

            try {
                File fileTarget = workflowModel.getMapImages().get(newValue);
                Dataset datasetTarget = (Dataset) iOService.open(fileTarget.getAbsolutePath());
                ImageDisplay imageDisplayTarget = new SilentImageDisplay();
                context.inject(imageDisplayTarget);
                imageDisplayTarget.display(datasetTarget);
                selectPosition(imageDisplayTarget, workflowModel.getPositionLeft(), workflowModel.getPositionRight(), imageDisplayPaneRight);

                Dataset datasetSource = (Dataset) iOService.open(newValue.getAbsolutePath());
                ImageDisplay imageDisplaySource = new SilentImageDisplay();
                context.inject(imageDisplaySource);
                imageDisplaySource.display(datasetSource);
                selectPosition(imageDisplaySource, workflowModel.getPositionLeft(), workflowModel.getPositionRight(), imageDisplayPaneLeft);

            } catch (IOException ex) {
                Logger.getLogger(ProcessWorkflow.class.getName()).log(Level.SEVERE, null, ex);
            }
        })
                .submit(loadingScreenService);
    }

    @FXML
    public void saveDirectory() {
        directory = null;
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File file = directoryChooser.showDialog(null);
        directory = file;

    }

}
