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
import ijfx.service.batch.SilentImageDisplay;
import ijfx.ui.datadisplay.image.ImageDisplayPane;
import ijfx.ui.main.ImageJFX;
import io.datafx.controller.injection.scopes.FlowScoped;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import net.imagej.Dataset;
import net.imagej.display.ImageDisplay;
import net.imagej.table.DefaultResultsTable;
import net.imagej.table.DefaultTableDisplay;
import net.imagej.table.ResultsTable;
import net.imagej.table.TableDisplay;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.StringUtils;
import org.scijava.Context;
import org.scijava.io.IOService;
import org.scijava.plugin.Parameter;

/**
 *
 * @author Tuan anh TRINH
 */
@FlowScoped
public class WorkflowModel {

    protected ImageDisplay flatFieldImageDisplay1;

    protected ObjectProperty<ImageDisplayPane> imageDisplayPaneLeftProperty = new SimpleObjectProperty<>();

    protected ObjectProperty<ImageDisplayPane> imageDisplayPaneRightProperty = new SimpleObjectProperty<>();

    protected ObjectProperty<ImageDisplayPane> imageDisplayPaneBottomProperty = new SimpleObjectProperty<>();

    @Parameter
    IOService iOService;

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
    private BooleanProperty richOutput = new SimpleBooleanProperty(true);
    /**
     * flag for save transformation option
     */
    private BooleanProperty saveTransformation = new SimpleBooleanProperty(true);

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

    File landmarksFile;

    Dataset flatfield;

    ImageProcessor targetMskIP = null;
    ImageProcessor sourceMskIP = null;
    int max_scale_deformation;
    int min_scale_deformation;
    int mode;

    @Parameter
    Context context;

    private Dataset flatField;

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }

    private List<File> files;

    protected final static Logger LOGGER = ImageJFX.getLogger();

    public WorkflowModel() {
        LOGGER.info("Init WorkflowModel");
    }

    public void setContext(Context context) {
        if (context!=this.context){
        this.context = context;
            context.inject(this);
            
        }
    }

    public Optional<ImageDisplay> getFlatFieldImageDisplay() {
        return Optional.ofNullable(flatFieldImageDisplay1);
    }

    public void setFlatFieldImageDisplay1(ImageDisplay flatFieldImageDisplay1) {
        this.flatFieldImageDisplay1 = flatFieldImageDisplay1;
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

        Reader in;
        List<double[]> points = new ArrayList<>();
        File file = fileChooser.showOpenDialog(null);
        fileLabel.setText(file.getName());
        in = new FileReader(file.getAbsolutePath());
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
     * 
     * @param imageDisplayPaneProperty
     * @return 
     */
    public ImageDisplay openImage(ObjectProperty<ImageDisplayPane> imageDisplayPaneProperty) {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);

        Dataset dataset = null;
        try {
            dataset = (Dataset) iOService.open(file.getAbsolutePath());
        } catch (IOException ex) {
            Logger.getLogger(BUnwarpJWorkflow.class.getName()).log(Level.SEVERE, null, ex);
        }
        return displayDataset(dataset, imageDisplayPaneProperty.get());

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
        }
        SilentImageDisplay imageDisplay = new SilentImageDisplay(context, dataset);
        imageDisplay.display(dataset);
        imageDisplayPane.display(imageDisplay);
        return imageDisplay;
    }

    /**
     * 
     * @param bUnwarpJWorkflow 
     */
    public void bindView(BUnwarpJWorkflow bUnwarpJWorkflow) {
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

        bUnwarpJWorkflow.imageDisplayPaneLeftProperty.bindBidirectional(imageDisplayPaneLeftProperty);
        bUnwarpJWorkflow.imageDisplayPaneRightProperty.bindBidirectional(imageDisplayPaneRightProperty);
//        bUnwarpJWorkflow.imageDisplayPaneLeftProperty.bindBidirectional(imageDisplayPaneLeftBo);

    }
}
