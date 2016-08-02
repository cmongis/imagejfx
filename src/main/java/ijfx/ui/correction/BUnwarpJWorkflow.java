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
import ijfx.service.batch.SilentImageDisplay;
import ijfx.service.dataset.DatasetUtillsService;
import ijfx.service.ui.LoadingScreenService;
import ijfx.ui.datadisplay.image.ImageDisplayPane;
import ijfx.ui.datadisplay.table.TableDisplayView;
import io.datafx.controller.ViewController;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.util.converter.DoubleStringConverter;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import mongis.utils.CallbackTask;
import net.imagej.Dataset;
import net.imagej.axis.Axes;
import net.imagej.display.DatasetView;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.OverlayService;
import net.imagej.display.event.DataViewUpdatedEvent;
import net.imagej.overlay.Overlay;
import net.imagej.overlay.PointOverlay;
import net.imagej.table.DefaultResultsTable;
import net.imagej.table.DefaultTableDisplay;
import net.imagej.table.ResultsTable;
import net.imglib2.type.numeric.RealType;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.StringUtils;
import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.display.DisplayService;
import org.scijava.event.EventHandler;
import org.scijava.io.IOService;
import org.scijava.module.MethodCallException;
import org.scijava.module.Module;
import org.scijava.module.ModuleService;
import org.scijava.plugin.Parameter;

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

    @Parameter
    OverlayService overlayService;

    @Parameter
    LoadingScreenService loadingScreenService;

    ObjectProperty<ImageDisplayPane> imageDisplayPaneLeftProperty = new SimpleObjectProperty<>();

    ObjectProperty<ImageDisplayPane> imageDisplayPaneRightProperty = new SimpleObjectProperty<>();

    ObjectProperty<ImageDisplayPane> imageDisplayPaneBottomProperty = new SimpleObjectProperty<>();

    public final static String[] HEADER = {"Index", "xSource", "ySource", "xTarget", "yTarget"};

    @FXML
    TableDisplayView tableDisplayView;

    @FXML
    GridPane imagesContainer;

    @FXML
    Button leftButton;

    @FXML
    Button rightButton;

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

    public BUnwarpJWorkflow() {
        CorrectionActivity.getStaticContext().inject(this);

        try {
            imageDisplayPaneBottomProperty = initDisplayPane();
            imageDisplayPaneLeftProperty = initDisplayPane();
            imageDisplayPaneRightProperty = initDisplayPane();

        } catch (IOException ex) {
            Logger.getLogger(BUnwarpJWorkflow.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @PostConstruct
    public void init() {
//        foo();

workflowModel.setContext(context);
        workflowModel.bindView(this);
        imagesContainer.add(imageDisplayPaneLeftProperty.get(), 0, 1);
        imagesContainer.add(imageDisplayPaneBottomProperty.get(), 0, 2);
        imagesContainer.add(imageDisplayPaneRightProperty.get(), 1, 1);
        loadPointsButton.setOnAction(e -> {
            try {
                tableDisplayView.display(workflowModel.loadTable(HEADER, fileLabel));
            } catch (IOException ex) {
                Logger.getLogger(BUnwarpJWorkflow.class.getName()).log(Level.SEVERE, null, ex);
            }

        });

        rightButton.setOnAction(e -> {
            workflowModel.openImage(this.imageDisplayPaneRightProperty);
        });
        leftButton.setOnAction(e -> {
            workflowModel.openImage(this.imageDisplayPaneLeftProperty);
        });
        bindProperty();

    }

    protected ObjectProperty<ImageDisplayPane> initDisplayPane() throws IOException {
        ImageDisplayPane imageDisplayPane = new ImageDisplayPane(context);

        imageDisplayPane.setOnMouseClicked(e -> displayService.setActiveDisplay(imageDisplayPane.getImageDisplay()));

        ObjectProperty<ImageDisplayPane> objectProperty = new SimpleObjectProperty<>();
        objectProperty.set(imageDisplayPane);
        return objectProperty;
    }

    protected void extractAndMerge() {
        Dataset[] datasets = new Dataset[2];
        ImageDisplay imageDisplayLeft = imageDisplayPaneLeftProperty.get().getImageDisplay();
        datasets[0] = datasetUtillsService.extractPlane(imageDisplayLeft);

        ImageDisplay imageDisplayRight = imageDisplayPaneRightProperty.get().getImageDisplay();
        datasets[1] = datasetUtillsService.extractPlane(imageDisplayRight);

        new CallbackTask<Void, Void>().run(() -> {
            bindProperty();
            Map<String, Object> inputMap = new HashMap();
            inputMap.put("datasetArray", datasets);
            inputMap.put("axisType", Axes.CHANNEL);
            Module module = null;
            try {
                module = executeCommand(ImagesToStack.class, inputMap);
            } catch (Exception e) {
            }
            Dataset result = (Dataset) module.getOutput("outputDataset");
//                ChannelMerger<? extends RealType<?>> merger = new ChannelMerger(context);
//                merger.setInput(result);
//                merger.run();
//                Dataset output = merger.getOutput();
//            copyLUT(Arrays.asList(imageDisplayLeft, imageDisplayRight), result);;
            workflowModel.displayDataset(result, imageDisplayPaneBottomProperty.get());
        }).submit(loadingScreenService).start();

    }

    @EventHandler
    public void handleEvent(DataViewUpdatedEvent event) {
        if (imageDisplayPaneLeftProperty.get().getImageDisplay().contains(event.getView()) || imageDisplayPaneRightProperty.get().getImageDisplay().contains(event.getView())) {
            try {
                extractAndMerge();
            } catch (Exception ex) {
                Logger.getLogger(BUnwarpJWorkflow.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

    }

    public void bindProperty() {
        imageDisplayPaneBottomProperty.get().getCanvas().getCamera().zoomProperty().bindBidirectional(imageDisplayPaneRightProperty.get().getCanvas().getCamera().zoomProperty());
        imageDisplayPaneRightProperty.get().getCanvas().getCamera().zoomProperty().bindBidirectional(imageDisplayPaneLeftProperty.get().getCanvas().getCamera().zoomProperty());

        imageDisplayPaneLeftProperty.get().getCanvas().getCamera().xProperty().bindBidirectional(imageDisplayPaneRightProperty.get().getCanvas().getCamera().xProperty());
        imageDisplayPaneBottomProperty.get().getCanvas().getCamera().xProperty().bindBidirectional(imageDisplayPaneLeftProperty.get().getCanvas().getCamera().xProperty());

        imageDisplayPaneLeftProperty.get().getCanvas().getCamera().yProperty().bindBidirectional(imageDisplayPaneRightProperty.get().getCanvas().getCamera().yProperty());
        imageDisplayPaneBottomProperty.get().getCanvas().getCamera().yProperty().bindBidirectional(imageDisplayPaneLeftProperty.get().getCanvas().getCamera().yProperty());
    }

    private <C extends Command> Module executeCommand(Class<C> type, Map<String, Object> parameters) throws MethodCallException, InterruptedException, ExecutionException {
        Module module = moduleService.createModule(commandService.getCommand(type));
        module.initialize();

        parameters.forEach((k, v) -> {
            module.setInput(k, v);
            module.setResolved(k, true);
        });
        Future run = moduleService.run(module, false, parameters);
        run.get();

        return module;
    }

    public void copyLUT(List<ImageDisplay> list, Dataset output) {

        if (list.stream().allMatch(imageDisplay -> imageDisplayService.getActiveDatasetView(imageDisplay).getColorTables().isEmpty())) {
            return;
        }
        IntStream.range(0, imageDisplayService.getActiveDatasetView(imageDisplayPaneBottomProperty.get().getImageDisplay()).getColorTables().size() + 1).forEach(i -> {
            DatasetView datasetView = imageDisplayService.getActiveDatasetView(list.get(i));
            imageDisplayService.getActiveDatasetView(imageDisplayPaneBottomProperty.get().getImageDisplay()).setColorTable(datasetView.getColorTables().get(0), i);
        });

    }

//    public void foo() {
//        Stream.of(stopThreshold, divWeight, curlWeight, landmarkWeight, imageWeight, consistencyWeight)
//                .forEach((TextField e) -> e.setTextFormatter(new TextFormatter(new DoubleStringConverter())));
////                .forEach(e -> e.textFormatterProperty().setValue(new TextFormatter(new DoubleStringConverter())));
//
////);
//
//    }
}
