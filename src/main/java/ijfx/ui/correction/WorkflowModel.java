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
import ijfx.plugins.adapter.IJ1Service;
import ijfx.plugins.bunwarpJ.ChromaticCorrection;
import ijfx.plugins.commands.AutoContrast;
import ijfx.plugins.flatfield.FlatFieldCorrection;
import ijfx.plugins.stack.ImagesToStack;
import ijfx.service.batch.SilentImageDisplay;
import ijfx.service.ui.LoadingScreenService;
import ijfx.ui.datadisplay.image.ImageDisplayPane;
import ijfx.ui.main.ImageJFX;
import io.datafx.controller.injection.scopes.ApplicationScoped;
import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
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
import net.imagej.DatasetService;
import net.imagej.axis.Axes;
import net.imagej.display.DefaultImageDisplay;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.table.DefaultResultsTable;
import net.imagej.table.DefaultTableDisplay;
import net.imagej.table.ResultsTable;
import net.imagej.table.TableDisplay;
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

    protected Map<File, File> mapImages;

    @Parameter
    IJ1Service iJ1Service;

    @Parameter
    DatasetService datasetService;

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

    static String[] CHOICES_DEFORMATION = {"Very Coarse", "Coarse", "Fine", "Very Fine", "Super Fine"};

    static String[] CHOICES_MODE = {"Fast", "Accurate", "Mono"};
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

    @Parameter
    Context context;

    private ObjectProperty<List<File>> listProperty = new SimpleObjectProperty<>(new ArrayList<File>());
    protected ObjectProperty<long[]> positionLeftProperty = new SimpleObjectProperty<>(new long[]{-1});
    protected ObjectProperty<long[]> positionRightProperty = new SimpleObjectProperty<>(new long[]{-1});

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
//        List<double[]> points = new ArrayList<>();
        landmarksFile.set(file);
        fileLabel.setText(landmarksFile.getName());
        Stack<Point> sourcePoints = new Stack<>();
        Stack<Point> targetPoints = new Stack<>();
        bunwarpj.MiscTools.loadPoints(file.getAbsolutePath(), sourcePoints, targetPoints);
        ResultsTable resultsTable = new DefaultResultsTable(header.length, sourcePoints.size());
        for (int col = 0; col < header.length; col++) {
            resultsTable.setColumnHeader(col, header[col]);
        }
        for (int row = 0; row < sourcePoints.size(); row++) {
            resultsTable.setValue(0, row, row);
            resultsTable.setValue(1, row, sourcePoints.get(0).x);
            resultsTable.setValue(2, row, sourcePoints.get(0).y);
            resultsTable.setValue(3, row, targetPoints.get(0).x);
            resultsTable.setValue(4, row, targetPoints.get(0).y);
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
     * @param sourcePosition
     * @param targetDataset
     * @param targetPosition
     * @return
     */
    public Dataset applyTransformation(Dataset sourceDataset, long[] sourcePosition, Dataset targetDataset, long[] targetPosition, bunwarpj.Transformation transformation) {
        int max_scale_deformation = Arrays.asList(CHOICES_DEFORMATION).indexOf(max_scale_deformation_choice.get());
        int min_scale_deformation = Arrays.asList(CHOICES_DEFORMATION).indexOf(min_scale_deformation_choice.get());
        int mode = Arrays.asList(CHOICES_MODE).indexOf(modeChoice.get());
        Map<String, Object> map = new HashMap<>();
        map.put("sourceDataset", sourceDataset);
        map.put("transformation", transformation);
        bunwarpj.Param parameter = new bunwarpj.Param(mode, maxImageSubsamplingFactor.get(), min_scale_deformation, max_scale_deformation, divWeight.get(), curlWeight.get(), landmarkWeight.get(), imageWeight.get(), consistencyWeight.get(), stopThreshold.get());
        map.put("parameter", parameter);
        map.put("sourcePosition", sourcePosition);
        map.put("targetPosition", targetPosition);

        map.put("landmarksFile", landmarksFile.get());

        Module module = executeCommand(ChromaticCorrection.class, map).orElseThrow(NullPointerException::new);

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

    public void setPosition(long[] position, ImageDisplay imageDisplay) {
        imageDisplay.setPosition(position);
        imageDisplay.update();
    }

    public long[] getPositionLeft() {
        return positionLeftProperty.get();
    }

    public long[] getPositionRight() {
        return positionRightProperty.get();
    }

    public File getLandMarksFile() {
        return landmarksFile.get();
    }

    public Map<File, File> getMapImages() {
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

    public void transformeImages(List<File> files, String destinationPath) {

        bunwarpj.Transformation transformation = getTransformation(files.get(0));
        files.parallelStream().forEach((File file) -> {
            CallbackTask<Void, Void> start = new CallbackTask<Void, Void>().run(() -> {
                transformeImage(file, destinationPath, transformation);
            })
                    .submit(loadingScreenService)
                    .start();

        });
    }

    public void transformeImage(File file, String destinationPath, bunwarpj.Transformation transformation) {
        try {
            Dataset inputDataset = (Dataset) iOService.open(file.getAbsolutePath());
            ImageDisplay imageDisplay = new SilentImageDisplay(context, inputDataset);

            //Set position, get dataset and apply flatField correction
            setPosition(getPositionRight(), imageDisplay);
            Dataset targetDatasetCorrected = applyFlatField(imageDisplay, getFlatfieldRight());

            setPosition(getPositionLeft(), imageDisplay);
            Dataset sourceDatasetCorrected = applyFlatField(imageDisplay, getFlatfieldLeft());
            Dataset outputDataset = applyTransformation(sourceDatasetCorrected, getPositionLeft(), targetDatasetCorrected, getPositionRight(), transformation);//applyTransformation(sourceDatasetCorrected, targetDatasetCorrected, transformation);;
            StringBuilder path = new StringBuilder(destinationPath);
            path.append("/").append(file.getName());
            iOService.save(outputDataset, path.toString());
            mapImages.put(file, new File(path.toString()));

        } catch (Exception ex) {
            Logger.getLogger(ProcessWorkflow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public bunwarpj.Transformation getTransformation(File file) {
        bunwarpj.Transformation transformation = null;
        try {
            Dataset inputDataset = (Dataset) iOService.open(file.getAbsolutePath());
            Stack<Point> sourcePoints = new Stack<>();
            Stack<Point> targetPoints = new Stack<>();
            bunwarpj.MiscTools.loadPoints(landmarksFile.get().getAbsolutePath(), sourcePoints, targetPoints);
            int max_scale_deformation = Arrays.asList(CHOICES_DEFORMATION).indexOf(max_scale_deformation_choice.get());
            int min_scale_deformation = Arrays.asList(CHOICES_DEFORMATION).indexOf(min_scale_deformation_choice.get());
            int mode = Arrays.asList(CHOICES_MODE).indexOf(modeChoice.get());
            bunwarpj.Param parameter = new bunwarpj.Param(mode, maxImageSubsamplingFactor.get(), min_scale_deformation, max_scale_deformation, divWeight.get(), curlWeight.get(), landmarkWeight.get(), imageWeight.get(), consistencyWeight.get(), stopThreshold.get());

            transformation = bunwarpj.bUnwarpJ_.computeTransformationBatch((int) inputDataset.dimension(0), (int) inputDataset.dimension(1), (int) inputDataset.dimension(0), (int) inputDataset.dimension(1), sourcePoints, targetPoints, parameter);
        } catch (IOException ex) {
            Logger.getLogger(WorkflowModel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return transformation;

    }

}
