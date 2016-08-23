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

import ijfx.service.ImagePlaneService;
import ijfx.service.batch.BatchService;
import ijfx.service.dataset.DatasetUtillsService;
import ijfx.service.ui.LoadingScreenService;
import ijfx.ui.datadisplay.image.ImageDisplayPane;
import ijfx.ui.datadisplay.image.ImageWindowEventBus;
import ijfx.ui.datadisplay.table.TableDisplayView;
import io.datafx.controller.ViewController;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import net.imagej.Dataset;
import net.imagej.display.DatasetView;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.OverlayService;
import net.imagej.display.event.DataViewEvent;
import net.imagej.display.event.LUTsChangedEvent;
import org.scijava.Context;
import org.scijava.command.CommandService;
import org.scijava.display.DisplayService;
import org.scijava.event.EventHandler;
import org.scijava.io.IOService;
import org.scijava.module.ModuleService;
import org.scijava.plugin.Parameter;

/**
 *
 * @author Tuan anh TRINH
 */
@ViewController(value = "BUnwarpJWorkflow.fxml")
public class BUnwarpJWorkflow extends CorrectionFlow {

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

    @Parameter
    OverlayService overlayService;

    @Parameter
    LoadingScreenService loadingScreenService;

    ImageDisplayPane imageDisplayPaneTopLeft;

    ImageDisplayPane imageDisplayPaneTopRight;

    ImageDisplayPane imageDisplayPaneBottomLeft;

    ObjectProperty<File> landmarksFile = new SimpleObjectProperty<>(new File(""));

    public final static String[] HEADER = {"Index", "xSource", "ySource", "xTarget", "yTarget"};

    @FXML
    Tab landmarksTab, deformationTab, weightTab, outputTab, imagesTab;
    @FXML
    TableDisplayView tableDisplayView;

    @FXML
    GridPane imagesContainer;

    @FXML
    Button loadPointsButton;

    @FXML
    BorderPane borderPaneTableView;

    @FXML
    Label fileLabel;

    @FXML
    TextField stopThresholdTextField;

    @FXML
    TextField divWeightTextField;

    @FXML
    TextField curlWeightTextField;

    @FXML
    TextField landmarkWeightTextField;

    @FXML
    TextField imageWeightTextField;

    @FXML
    TextField consistencyWeightTextField;

    @FXML
    CheckBox richOutput;

    @FXML
    CheckBox saveTransformation;

    @FXML
    ComboBox modeChoiceComboBox, min_scale_deformation_choiceComboBox, max_scale_deformation_choiceComboBox, img_subsamp_factComboBox;

    @FXML
    Button mergeButton;

    @FXML
    ListView<File> listView;

    private final ImageWindowEventBus bus = new ImageWindowEventBus();

    public BUnwarpJWorkflow() {
        CorrectionActivity.getStaticContext().inject(this);
//        initImageDisplayPane();
    }

    @PostConstruct
    public void init() {
        try {
            imageDisplayPaneTopLeft = new ImageDisplayPane(context);
            imageDisplayPaneBottomLeft = new ImageDisplayPane(context);
            imageDisplayPaneTopRight = new ImageDisplayPane(context);
        } catch (IOException ex) {
            Logger.getLogger(BUnwarpJWorkflow.class.getName()).log(Level.SEVERE, null, ex);
        }
        workflowModel.setContext(context);
        workflowModel.bindBunwarpJ(this);
        imagesContainer.add(imageDisplayPaneTopLeft, 0, 0);
        imagesContainer.add(imageDisplayPaneBottomLeft, 0, 1);
        imagesContainer.add(imageDisplayPaneTopRight, 1, 0);
        loadPointsButton.setOnAction(e -> {
            try {
                tableDisplayView.display(workflowModel.loadTable(HEADER, fileLabel));
            } catch (IOException ex) {
                Logger.getLogger(BUnwarpJWorkflow.class.getName()).log(Level.SEVERE, null, ex);
            }

        });

        ImageDisplayPane[] imageDisplayPanes = new ImageDisplayPane[]{imageDisplayPaneTopLeft, imageDisplayPaneTopRight, imageDisplayPaneBottomLeft};
        bindPaneProperty(Arrays.asList(imageDisplayPanes));

//Try to get the last one!
        bus.getStream(DataViewEvent.class)
                //                .filter(bus::doesDisplayRequireRefresh)
                .buffer(5000 / 15, TimeUnit.MILLISECONDS)
                .filter(list -> !list.isEmpty())
                //                .first()
                //                .replay(1)
                //                .doOnTerminate(() -> System.out.println("ijfx.ui.correction.BUnwarpJWorkflow.init()"))
                .subscribe(onCompleted -> {
                    Dataset[] datasets = new Dataset[2];
                    ImageDisplay imageDisplayLeft = imageDisplayPaneTopLeft.getImageDisplay();
                    datasets[0] = datasetUtillsService.extractPlane(imageDisplayLeft);

                    ImageDisplay imageDisplayRight = imageDisplayPaneTopRight.getImageDisplay();
                    datasets[1] = datasetUtillsService.extractPlane(imageDisplayRight);
                    workflowModel.extractAndMerge(datasets, imageDisplayPaneBottomLeft);
                });
        

        setCellFactory(listView);
        listView.getItems().addAll(workflowModel.getFiles());
        listView.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends File> observable, File oldValue, File newValue) -> {
            workflowModel.openImage(this.imageDisplayPaneTopRight, this.imageDisplayPaneTopLeft, newValue)
                    .thenRunnable(() -> {
                        workflowModel.setPosition(workflowModel.getPositionLeft(), imageDisplayPaneTopLeft.getImageDisplay());
                        workflowModel.setPosition(workflowModel.getPositionRight(), imageDisplayPaneTopRight.getImageDisplay());
                    })
                    .start();

        });
        if (workflowModel.getLandMarksFile() != null) {
            try {
                tableDisplayView.display(workflowModel.loadTable(HEADER, fileLabel, workflowModel.getLandMarksFile()));
            } catch (IOException ex) {
                Logger.getLogger(BUnwarpJWorkflow.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @EventHandler
    public void handleEvent(DataViewEvent event) {
        //To be changed
        if (imageDisplayPaneTopLeft.getImageDisplay() != null && imageDisplayPaneTopRight.getImageDisplay() != null) {
            if (imageDisplayPaneTopLeft.getImageDisplay().contains(event.getView()) || imageDisplayPaneTopRight.getImageDisplay().contains(event.getView())) {
                bus.channel(event);
            }

        }

    }

    @EventHandler
    public void handleEvent(LUTsChangedEvent event) {
//        if (imageDisplayPaneTopLeft.getImageDisplay().contains(event.getView()) || imageDisplayPaneTopRight.getImageDisplay().contains(event.getView())) {
//            bus.channel(event);
//        }

    }

    public void copyLUT(List<ImageDisplay> list, Dataset output) {

        if (list.stream().allMatch(imageDisplay -> imageDisplayService.getActiveDatasetView(imageDisplay).getColorTables().isEmpty())) {
            return;
        }
        IntStream.range(0, imageDisplayService.getActiveDatasetView(imageDisplayPaneBottomLeft.getImageDisplay()).getColorTables().size() + 1).forEach(i -> {
            DatasetView datasetView = imageDisplayService.getActiveDatasetView(list.get(i));
            imageDisplayService.getActiveDatasetView(imageDisplayPaneBottomLeft.getImageDisplay()).setColorTable(datasetView.getColorTables().get(0), i);
        });

    }

}
