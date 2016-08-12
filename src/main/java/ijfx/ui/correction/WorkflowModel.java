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

import ij.process.ImageProcessor;
import ijfx.plugins.bunwarpJ.bUnwarpJ_;
import ijfx.plugins.commands.AutoContrast;
import ijfx.plugins.flatfield.FlatFieldCorrection;
import ijfx.plugins.stack.ImagesToStack;
import ijfx.service.ui.LoadingScreenService;
import ijfx.ui.datadisplay.image.ImageDisplayPane;
import ijfx.ui.main.ImageJFX;
import io.datafx.controller.injection.scopes.ApplicationScoped;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.util.converter.NumberStringConverter;
import mongis.utils.CallbackTask;
import net.imagej.Dataset;
import net.imagej.axis.Axes;
import net.imagej.display.DefaultImageDisplay;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.table.DefaultResultsTable;
import net.imagej.table.DefaultTableDisplay;
import net.imagej.table.ResultsTable;
import net.imagej.table.TableDisplay;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.StringUtils;
import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.display.DisplayService;
import org.scijava.io.IOService;
import org.scijava.module.MethodCallException;
import org.scijava.module.Module;
import org.scijava.module.ModuleService;
import org.scijava.plugin.Parameter;

/**
 *
 * @author Tuan anh TRINH
 */
@ApplicationScoped
public class WorkflowModel {

    protected ObjectProperty<ImageDisplayPane> flatFieldImageDisplayProperty1;

    protected ObjectProperty<ImageDisplayPane> flatFieldImageDisplayProperty2;

    protected Map<String, Dataset> mapImages;

    @Parameter
    IOService iOService;

    @Parameter
    DisplayService displayService;

    @Parameter
    CommandService commandService;

    @Parameter
    ImageDisplayService imageDisplayService;

    @Parameter
    ModuleService moduleService;

    @Parameter
    LoadingScreenService loadingScreenService;

    private StringProperty min_scale_deformation_choice = new SimpleStringProperty("Very Fine");

    /**
     * maximum scale deformation
     */
    private StringProperty max_scale_deformation_choice = new SimpleStringProperty("Super Fine");

    /**
     * algorithm mode (fast, accurate or mono)
     */
    private StringProperty modeChoice = new SimpleStringProperty("Mono");
    /**
     * image subsampling factor at the highest pyramid level
     */
    private IntegerProperty maxImageSubsamplingFactor = new SimpleIntegerProperty(0);

    // Transformation parameters
    /**
     * divergence weight
     */
    private DoubleProperty divWeight = new SimpleDoubleProperty(0);
    /**
     * curl weight
     */
    private DoubleProperty curlWeight = new SimpleDoubleProperty(0);
    /**
     * landmarks weight
     */
    private DoubleProperty landmarkWeight = new SimpleDoubleProperty(1.0);
    /**
     * image similarity weight
     */
    private DoubleProperty imageWeight = new SimpleDoubleProperty(0.0);
    /**
     * consistency weight
     */
    private DoubleProperty consistencyWeight = new SimpleDoubleProperty(10.0);
    /**
     * flag for rich output (verbose option)
     */
    private BooleanProperty richOutput = new SimpleBooleanProperty(false);
    /**
     * flag for save transformation option
     */
    private BooleanProperty saveTransformation = new SimpleBooleanProperty(false);

    /**
     * minimum image scale
     */
    private IntegerProperty min_scale_image = new SimpleIntegerProperty(0);
    /**
     * stopping threshold
     */
    private DoubleProperty stopThreshold = new SimpleDoubleProperty(1e-2);

    private StringProperty img_subsamp_fact = new SimpleStringProperty("0");

    File imagesFolder;

    private ObjectProperty<File> landmarksFile = new SimpleObjectProperty<>();

    Dataset flatfield;

    ImageProcessor targetMskIP = null;
    ImageProcessor sourceMskIP = null;
    int max_scale_deformation;
    int min_scale_deformation;
    int mode;

    @Parameter
    Context context;

    private ObjectProperty<List<File>> listProperty = new SimpleObjectProperty<>(new ArrayList<File>());
    protected ObjectProperty<int[]> positionLeftProperty = new SimpleObjectProperty<>(new int[]{-1});
    protected ObjectProperty<int[]> positionRightProperty = new SimpleObjectProperty<>(new int[]{-1});

    protected final static Logger LOGGER = ImageJFX.getLogger();

    public WorkflowModel() {
        LOGGER.info("Init WorkflowModel");
        CorrectionActivity.getStaticContext().inject(this);
        init();
        mapImages = new HashMap<>();
    }

    public void init() {

        flatFieldImageDisplayProperty1 = initDisplayPane();

        flatFieldImageDisplayProperty2 = initDisplayPane();

    }

    public void setContext(Context context) {
        if (context != this.context) {
            this.context = context;
            try {
                context.inject(this);

            } catch (Exception e) {
            }

        }
    }

    /**
     *
     * @param header
     * @param fileLabel
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public TableDisplay loadTable(String[] header, Label fileLabel) throws FileNotFoundException, IOException {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);
        landmarksFile.set(file);
        return loadTable(header, fileLabel, file);
    }

    /**
     * Load a table and diplay it in the TableDisplay
     *
     * @param header
     * @param fileLabel
     * @param file
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public TableDisplay loadTable(String[] header, Label fileLabel, File file) throws FileNotFoundException, IOException {
        Reader in;
        List<double[]> points = new ArrayList<>();
        landmarksFile.set(file);
        fileLabel.setText(landmarksFile.getName());
        in = new FileReader(landmarksFile.get().getAbsolutePath());
        Iterable<CSVRecord> records = CSVFormat.EXCEL.withHeader(header).parse(in);
        for (CSVRecord record : records) {
            double[] sourceArray = new double[header.length];
            if (!StringUtils.isNumeric(record.get(header[0]))) {
                continue;
            }
            points.add(sourceArray);
            for (int i = 0; i < header.length; i++) {
                sourceArray[i] = Double.valueOf(record.get(header[i]));
            }
        }
        ResultsTable resultsTable = new DefaultResultsTable(header.length, points.size());
        for (int col = 0; col < header.length; col++) {
            resultsTable.setColumnHeader(col, header[col]);
            for (int row = 0; row < points.size(); row++) {
                resultsTable.setValue(col, row, points.get(row)[col]);
            }
        }
        TableDisplay tableDisplay = new DefaultTableDisplay();
        tableDisplay.add(resultsTable);
        return tableDisplay;
    }

    /**
     * Open an image in an other thread. And display the image in the different
     * ImageDisplayPane
     *
     * @param imageDisplayPane1
     * @param imageDisplayPane2
     * @param file
     * @return
     */
    public CallbackTask<Void, Void> openImage(ImageDisplayPane imageDisplayPane1, ImageDisplayPane imageDisplayPane2, File file) {

        CallbackTask<Void, Void> task = new CallbackTask<Void, Void>().run(() -> {
            try {
                Dataset dataset;
                dataset = (Dataset) iOService.open(file.getAbsolutePath());
                displayDataset(dataset, imageDisplayPane1);
                displayDataset(dataset, imageDisplayPane2);
            } catch (IOException ex) {
                Logger.getLogger(WorkflowModel.class.getName()).log(Level.SEVERE, null, ex);
            }
        })
                .submit(loadingScreenService);
        return task;
    }

    public CallbackTask<Void, Void> openImage(ImageDisplayPane imageDisplayPane, File file) {

        CallbackTask<Void, Void> task = new CallbackTask<Void, Void>().run(() -> {
            try {
                Dataset dataset;
                dataset = (Dataset) iOService.open(file.getAbsolutePath());
                displayDataset(dataset, imageDisplayPane);
            } catch (IOException ex) {
                Logger.getLogger(WorkflowModel.class.getName()).log(Level.SEVERE, null, ex);
            }
        })
                .submit(loadingScreenService);
        return task;
    }

    /**
     *
     * @param dataset
     * @param imageDisplayPane
     * @return
     */
    public ImageDisplay displayDataset(Dataset dataset, ImageDisplayPane imageDisplayPane) {
        try {
            imageDisplayPane.getImageDisplay().clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ImageDisplay imageDisplay = new DefaultImageDisplay();
        context.inject(imageDisplay);
        imageDisplay.display(dataset);
        try {
            commandService.run(AutoContrast.class, true, "imageDisplay", imageDisplay, "channelDependant", true).get();
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(WorkflowModel.class.getName()).log(Level.SEVERE, null, ex);
        }

        imageDisplayPane.display(imageDisplay);
        return imageDisplay;
    }

    /**
     *
     * @param bUnwarpJWorkflow
     */
    public void bindBunwarpJ(BUnwarpJWorkflow bUnwarpJWorkflow) {
        bUnwarpJWorkflow.consistencyWeightTextField.textProperty().bindBidirectional(consistencyWeight, new NumberStringConverter());
        bUnwarpJWorkflow.curlWeightTextField.textProperty().bindBidirectional(curlWeight, new NumberStringConverter());
        bUnwarpJWorkflow.divWeightTextField.textProperty().bindBidirectional(divWeight, new NumberStringConverter());
        bUnwarpJWorkflow.imageWeightTextField.textProperty().bindBidirectional(imageWeight, new NumberStringConverter());
        bUnwarpJWorkflow.landmarkWeightTextField.textProperty().bindBidirectional(landmarkWeight, new NumberStringConverter());
        bUnwarpJWorkflow.stopThresholdTextField.textProperty().bindBidirectional(stopThreshold, new NumberStringConverter());

        bUnwarpJWorkflow.richOutput.selectedProperty().bindBidirectional(richOutput);

        bUnwarpJWorkflow.saveTransformation.selectedProperty().bindBidirectional(saveTransformation);

        bUnwarpJWorkflow.modeChoiceComboBox.valueProperty().bindBidirectional(modeChoice);
        bUnwarpJWorkflow.img_subsamp_factComboBox.valueProperty().bindBidirectional(img_subsamp_fact);
        bUnwarpJWorkflow.min_scale_deformation_choiceComboBox.valueProperty().bindBidirectional(min_scale_deformation_choice);
        bUnwarpJWorkflow.max_scale_deformation_choiceComboBox.valueProperty().bindBidirectional(max_scale_deformation_choice);

        bUnwarpJWorkflow.landmarksFile.bindBidirectional(landmarksFile);
    }

    public void bindFlatfield(FlatfieldWorkflow flatfieldWorkflow) {
        flatfieldWorkflow.flatFieldProperty1.bindBidirectional(flatFieldImageDisplayProperty1);
        flatfieldWorkflow.flatFieldProperty2.bindBidirectional(flatFieldImageDisplayProperty2);

    }

    protected ObjectProperty<ImageDisplayPane> initDisplayPane() {
        ObjectProperty<ImageDisplayPane> objectProperty = null;
        try {
            ImageDisplayPane imageDisplayPane = new ImageDisplayPane(context);
            objectProperty = new SimpleObjectProperty<>();
            objectProperty.set(imageDisplayPane);
        } catch (IOException ex) {
            Logger.getLogger(WorkflowModel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return objectProperty;
    }

    /**
     * Call the bUnwarpJ_ Command
     *
     * @param sourceDataset
     * @param targetDataset
     * @return
     */
    public Dataset getTransformedImage(Dataset sourceDataset, Dataset targetDataset) {
        Map<String, Object> map = new HashMap<>();
        map.put("sourceDataset", sourceDataset);
        map.put("targetDataset", targetDataset);
        map.put("min_scale_deformation", min_scale_deformation_choice.get());
        map.put("max_scale_deformation", max_scale_deformation_choice.get());
        map.put("modeChoice", modeChoice.get());
        map.put("maxImageSubsamplingFactor", maxImageSubsamplingFactor.get());
        map.put("divWeight", divWeight.get());
        map.put("curlWeight", curlWeight.get());
        map.put("landmarkWeight", landmarkWeight.get());
        map.put("imageWeight", imageWeight.get());
        map.put("consistencyWeight", consistencyWeight.get());
        map.put("richOutput", richOutput.get());
        map.put("saveTransformation", saveTransformation.get());
        map.put("min_scale_image", min_scale_image.get());
        map.put("stopThreshold", stopThreshold.get());
//        map.put("img_subsamp_fact", img_subsamp_fact.get());
        map.put("landmarksFile", landmarksFile.get());

        Module module = executeCommand(bUnwarpJ_.class, map).orElseThrow(NullPointerException::new);
        Dataset outputDataset = (Dataset) module.getOutput("outputDataset");
        return outputDataset;
    }

    public Dataset applyFlatField(ImageDisplay inputImageDisplay, ImageDisplay flatFieldImageDisplay) {
        if (flatFieldImageDisplay == null) {
            return imageDisplayService.getActiveDataset(inputImageDisplay);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("flatFieldImageDisplay", flatFieldImageDisplay);
        map.put("inputImageDisplay", inputImageDisplay);
        Module module = executeCommand(FlatFieldCorrection.class, map).orElseThrow(NullPointerException::new);
        Dataset outputDataset = (Dataset) module.getOutput("outputDataset");
        return outputDataset;
    }

    /**
     *
     * @param <C>
     * @param type
     * @param parameters
     * @return
     */
    public <C extends Command> Optional<Module> executeCommand(Class<C> type, Map<String, Object> parameters) {
        Module module = moduleService.createModule(commandService.getCommand(type));
        try {
            module.initialize();
            parameters.forEach((k, v) -> {
                module.setInput(k, v);
                module.setResolved(k, true);
            });

            Future run = moduleService.run(module, false, parameters);

            run.get();
        } catch (MethodCallException | InterruptedException | ExecutionException ex) {
            Logger.getLogger(WorkflowModel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Optional.of(module);
    }

    public void bindWelcome(FolderWorkflow welcomeWorkflow) {
        welcomeWorkflow.listProperty.bindBidirectional(listProperty);
        welcomeWorkflow.positionLeftProperty.bindBidirectional(positionLeftProperty);
        welcomeWorkflow.positionRightProperty.bindBidirectional(positionRightProperty);
    }

    /**
     * Concatenate the different Dataset and display them in the
     * ImageDisplayPane
     *
     * @param datasets
     * @param imageDisplayPane
     */
    public void extractAndMerge(Dataset[] datasets, ImageDisplayPane imageDisplayPane) {
        new CallbackTask<Void, Void>().run(() -> {
            Map<String, Object> inputMap = new HashMap();
            inputMap.put("datasetArray", datasets);
            inputMap.put("axisType", Axes.CHANNEL);
            Module module = null;
            module = executeCommand(ImagesToStack.class, inputMap).orElseThrow(NullPointerException::new);
            Dataset result = (Dataset) module.getOutput("outputDataset");
//            DatasetView datasetView = imageDisplayService.getActiveDatasetView(imageDisplayPane.getImageDisplay());
//            datasetView.setColorMode(ColorMode.COMPOSITE);
//                ChannelMerger<? extends RealType<?>> merger = new ChannelMerger(context);
//                merger.setInput(result);
//                merger.run();
//                Dataset output = merger.getOutput();
//            copyLUT(Arrays.asList(imageDisplayPaneTopLeftProperty.get().getImageDisplay(), imageDisplayPaneTopRightProperty.get().getImageDisplay()), result);;
            displayDataset(result, imageDisplayPane);
        }).submit(loadingScreenService).start();

    }

    public void setPosition(int[] position, ImageDisplay imageDisplay) {
        imageDisplay.setPosition(position);
        imageDisplay.update();
    }

    public int[] getPositionLeft() {
        return positionLeftProperty.get();
    }

    public int[] getPositionRight() {
        return positionRightProperty.get();
    }

    public File getLandMarksFile() {
        return landmarksFile.get();
    }

    public Map<String, Dataset> getMapImages() {
        return mapImages;
    }

    public List<File> getFiles() {
        return listProperty.get();
    }

    public ImageDisplay getFlatfieldLeft() {
        return flatFieldImageDisplayProperty1.get().getImageDisplay();
    }

    public ImageDisplay getFlatfieldRight() {
        return flatFieldImageDisplayProperty2.get().getImageDisplay();
    }
}
