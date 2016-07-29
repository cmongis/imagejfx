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

    protected ImageDisplayPane imageDisplayPaneLeft;

    protected ImageDisplayPane imageDisplayPaneRight;

    protected ImageDisplayPane imageDisplayPaneBottom;

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
    


//    @FXML
//    Button bottomLeftButton;
//    protected final List<ImageDisplayPane> imageDisplayPaneList;
    public BUnwarpJWorkflow() {
        CorrectionActivity.getStaticContext().inject(this);

//        imageDisplayPaneList = Arrays.asList(imageDisplayPaneLeft, imageDisplayPaneRight, imageDisplayPaneBottom);
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
        foo();
            imagesContainer.add(imageDisplayPaneLeft, 0, 1);
            imagesContainer.add(imageDisplayPaneBottom, 0, 2);
            imagesContainer.add(imageDisplayPaneRight, 1, 1);
        loadPointsButton.setOnAction(e -> loadPointsOverlay());

        rightButton.setOnAction(e -> {
            openImage(imageDisplayPaneRight);
        });
        leftButton.setOnAction(e -> {
            openImage(imageDisplayPaneLeft);
        });
        bindProperty();

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

        new CallbackTask<Void, Void>().run(() -> {
            bindProperty();
            Map<String, Object> inputMap = new HashMap();
            inputMap.put("datasetArray", datasets);
            inputMap.put("axisType", Axes.CHANNEL);
            Module module = executeCommand(ImagesToStack.class, inputMap);
            Dataset result = (Dataset) module.getOutput("outputDataset");
//                ChannelMerger<? extends RealType<?>> merger = new ChannelMerger(context);
//                merger.setInput(result);
//                merger.run();
//                Dataset output = merger.getOutput();
            copyLUT(Arrays.asList(imageDisplayLeft, imageDisplayRight), result);;
            displayDataset(result, imageDisplayPaneBottom);
        }).submit(loadingScreenService).start();

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
//
//    public void unBindProperty() {
//        imageDisplayPaneBottom.getCanvas().getCamera().zoomProperty().unbindBidirectional(imageDisplayPaneRight.getCanvas().getCamera().zoomProperty());
//        imageDisplayPaneRight.getCanvas().getCamera().zoomProperty().unbindBidirectional(imageDisplayPaneLeft.getCanvas().getCamera().zoomProperty());
//
//        imageDisplayPaneLeft.getCanvas().getCamera().xProperty().unbindBidirectional(imageDisplayPaneRight.getCanvas().getCamera().xProperty());
//        imageDisplayPaneBottom.getCanvas().getCamera().xProperty().unbindBidirectional(imageDisplayPaneLeft.getCanvas().getCamera().xProperty());
//
//        imageDisplayPaneLeft.getCanvas().getCamera().yProperty().unbindBidirectional(imageDisplayPaneRight.getCanvas().getCamera().yProperty());
//        imageDisplayPaneBottom.getCanvas().getCamera().yProperty().unbindBidirectional(imageDisplayPaneLeft.getCanvas().getCamera().yProperty());
//    }

    protected void loadPointsOverlay() {
////        double[] points1 = {100.0, 100.0};
//        double[] points2 = {200.0, 100.0};
//        double[] points3 = {150.0, 150.0};
//        List<double[]> overlays = new ArrayList<>();
////        overlays.add(points1);
//        overlays.add(points2);
//        overlays.add(points3);
//
//        PointOverlay pointOverlay = new PointOverlay(context, overlays);
//        List<Overlay> listOverlays = new ArrayList<>();
//        listOverlays.add(pointOverlay);
//        overlayService.addOverlays(imageDisplayPaneLeft.getImageDisplay(), listOverlays);

        FileChooser fileChooser = new FileChooser();
        List<Overlay> listOverlays = new ArrayList<>();

        Reader in;
        List<double[]> points = new ArrayList<>();
        try {
            in = new FileReader(fileChooser.showOpenDialog(null).getAbsolutePath());
            Iterable<CSVRecord> records = CSVFormat.EXCEL.withHeader("Index", "xSource", "ySource", "xTarget", "yTarget").parse(in);
            for (CSVRecord record : records) {
                try {
                    double xSource = Double.valueOf(record.get("xSource"));
                    double ySource = Double.valueOf(record.get("ySource"));
                    double[] sourceArray = {xSource, ySource};
                    points.add(sourceArray);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(BUnwarpJWorkflow.class.getName()).log(Level.SEVERE, null, ex);
        }
        PointOverlay pointOverlay = new PointOverlay(context, points);
        listOverlays.add(pointOverlay);
        overlayService.addOverlays(imageDisplayPaneLeft.getImageDisplay(), listOverlays);

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
//        System.gc();
        return module;
    }

    public void copyLUT(List<ImageDisplay> list, Dataset output) {

        if (list.stream().allMatch(imageDisplay -> imageDisplayService.getActiveDatasetView(imageDisplay).getColorTables().isEmpty())) {
            return;
        }
//        if (list.stream().allMatch(imageDisplay -> imageDisplayService.getActiveDatasetView(imageDisplay).getColorTables().si)) {
        IntStream.range(0, imageDisplayService.getActiveDatasetView(imageDisplayPaneBottom.getImageDisplay()).getColorTables().size() + 1).forEach(i -> {
            DatasetView datasetView = imageDisplayService.getActiveDatasetView(list.get(i));
            imageDisplayService.getActiveDatasetView(imageDisplayPaneBottom.getImageDisplay()).setColorTable(datasetView.getColorTables().get(0), i);
//                output.setColorTable(datasetView.getColorTables().get(0), i);
        });
        System.out.println("ijfx.ui.correction.BUnwarpJWorkflow.copyLUT()");
//        }
//        if (first.getColorTableCount() == 0 || second.getColorTableCount() == 0) return;
//        if (firs)
    }
    
    public void foo(){
        final double[][] data = {
			{1978, 21, .273},
			{1979, 22, .322},
			{1980, 23, .304},
			{1981, 24, .267},
			{1982, 25, .302},
			{1983, 26, .270},
			{1984, 27, .217},
			{1985, 28, .297},
			{1986, 29, .281},
			{1987, 30, .353},
			{1988, 31, .312},
			{1989, 32, .315},
			{1990, 33, .285},
			{1991, 34, .325},
			{1992, 35, .320},
			{1993, 36, .332},
			{1994, 37, .341},
			{1995, 38, .270},
			{1996, 39, .341},
			{1997, 40, .305},
			{1998, 41, .281},
		};
        ResultsTable resultsTable = new DefaultResultsTable(1,1);
//        resultsTable.setColumnHeader(0,"r");
        DefaultTableDisplay defaultTableDisplay = new DefaultTableDisplay();
        defaultTableDisplay.add(resultsTable);
//        tableDisplayView = new TableDisplayView();
        tableDisplayView.display(defaultTableDisplay);
//        borderPaneTableView.setCenter(tableDisplayView);
    }

}
