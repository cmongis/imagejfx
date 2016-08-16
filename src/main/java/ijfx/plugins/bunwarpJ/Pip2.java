/*
    This imagesFolder is part of ImageJ FX.

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
package ijfx.plugins.bunwarpJ;

import bunwarpj.MainDialog;
import bunwarpj.MiscTools;
import bunwarpj.Param;
import bunwarpj.Transformation;
import ij.ImagePlus;
import ij.ImageStack;
import ij.io.Opener;
import ij.process.ImageProcessor;
import ijfx.core.imagedb.ImageLoaderService;
import ijfx.plugins.adapter.IJ1Service;
import ijfx.plugins.flatfield.FlatFieldCorrection;
import ijfx.plugins.stack.ImagesToStack;
import ijfx.service.dataset.DatasetUtillsService;
import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.axis.Axes;
import net.imagej.display.ImageDisplayService;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.display.DisplayService;
import org.scijava.module.MethodCallException;
import org.scijava.module.Module;
import org.scijava.module.ModuleService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Tuan anh TRINH
 */
@Plugin(type = Command.class, menuPath = "Plugins>Pip2")
public class Pip2 implements Command {

    public static String[] modesArray = {"Fast", "Accurate", "Mono"};
    public static String[] sMinScaleDeformationChoices = {"Very Coarse", "Coarse", "Fine", "Very Fine"};
    public static String[] sMaxScaleDeformationChoices = {"Very Coarse", "Coarse", "Fine", "Very Fine", "Super Fine"};

    @Parameter
    IJ1Service iJ1Service;

    @Parameter
    DatasetService datasetService;

    @Parameter
    ImageLoaderService imageLoaderService;

    @Parameter
    DisplayService displayService;

    @Parameter
    Dataset datasetSource;

    @Parameter
    Dataset datasetTarget;

    @Parameter
    ImageDisplayService imageDisplayService;

    @Parameter
    DatasetUtillsService datasetUtillsService;

    @Parameter
    ModuleService moduleService;

    @Parameter
    CommandService commandService;
    /*....................................................................
    	Private variables
 	....................................................................*/

    /**
     * Image representation for source image
     */
    private ImagePlus sourceImp;
    /**
     * Image representation for target image
     */
    private ImagePlus targetImp;

    /**
     * minimum scale deformation
     */
    @Parameter(choices = {"Very Coarse", "Coarse", "Fine", "Very Fine"})
    private String min_scale_deformation_choice;

    /**
     * maximum scale deformation
     */
    @Parameter(choices = {"Very Coarse", "Coarse", "Fine", "Very Fine", "Super Fine"})
    private String max_scale_deformation_choice;

    /**
     * algorithm mode (fast, accurate or mono)
     */
    @Parameter(choices = {"Fast", "Accurate", "Mono"})
    private String modeChoice = "Mono";
    /**
     * image subsampling factor at the highest pyramid level
     */
    @Parameter
    private int maxImageSubsamplingFactor = 0;

    // Transformation parameters
    /**
     * divergence weight
     */
    @Parameter
    private double divWeight = 0;
    /**
     * curl weight
     */
    @Parameter
    private double curlWeight = 0;
    /**
     * landmarks weight
     */
    @Parameter
    private double landmarkWeight = 1.0;
    /**
     * image similarity weight
     */
    @Parameter
    private double imageWeight = 0.0;
    /**
     * consistency weight
     */
    @Parameter
    private double consistencyWeight = 10;
    /**
     * flag for rich output (verbose option)
     */
    //@Parameter
    private boolean richOutput = true;
    /**
     * flag for save transformation option
     */
    @Parameter
    private boolean saveTransformation = false;

    /**
     * minimum image scale
     */
    @Parameter
    private int min_scale_image = 0;
    /**
     * stopping threshold
     */
    @Parameter
    private static double stopThreshold = 1e-2;

    @Parameter(choices = {"0", "1", "2", "3", "4", "5", "6", "7"})
    String img_subsamp_fact;

    @Parameter(label = "Images Folder")
    File imagesFolder;

    @Parameter(label = "Landmarks File")
    File landmarksFile;

    @Parameter(label = "Flatfield Image", required = false)
    Dataset flatfield;

    ImageProcessor targetMskIP = null;
    ImageProcessor sourceMskIP = null;
    int max_scale_deformation;
    int min_scale_deformation;
    int mode;

    /*....................................................................
       Public methods
    ....................................................................*/
    //------------------------------------------------------------------
    /**
     * Method to lunch the plugin.
     *
     */
    @Override
    public void run() {
        Runtime.getRuntime().gc();
        init();

        //Load landmarks
        Stack<Point> sourcePoints = new Stack<>();
        Stack<Point> targetPoints = new Stack<>();
        MiscTools.loadPoints(landmarksFile.getAbsolutePath(), sourcePoints, targetPoints);
        Param param = new Param(mode, maxImageSubsamplingFactor, min_scale_deformation, max_scale_deformation, divWeight, curlWeight, landmarkWeight, imageWeight, consistencyWeight, stopThreshold);

        //Calcul transformation which have to be applied after
        Transformation transformation =null;// bUnwarpJ_.computeTransformationBatch(targetImp, sourceImp, targetMskIP, sourceMskIP, sourcePoints, targetPoints, param);;
        //transformation.saveDirectTransformation("/home/tuananh/Desktop/trans_direct2.txt");
        //Apply transformation
        loadImagePlus().parallelStream().forEach((imagePlus) -> {
            Dataset afterTransformation = iJ1Service.wrapDataset(applyTransformation(sourceImp, transformation));
            if (flatfield != null) {
                afterTransformation = applyFlatFieldCorrection(afterTransformation, flatfield);
            }
            displayService.createDisplay(afterTransformation.getName(), afterTransformation);
        });

        getMergedFeedback(transformation);
        if (richOutput) {
            Img img = ImageJFunctions.wrap(getFeedback(transformation));
            displayService.createDisplay("Feedback", img);
        }

    }

    /**
     * Init parameters
     */
    public void init() {
        //Set parameters
        mode = Arrays.asList(this.modesArray).indexOf(modeChoice);
        max_scale_deformation = Arrays.asList(sMaxScaleDeformationChoices).indexOf(max_scale_deformation_choice);
        min_scale_deformation = Arrays.asList(sMinScaleDeformationChoices).indexOf(min_scale_deformation_choice);
        this.sourceImp = iJ1Service.getInput(datasetUtillsService.extractPlane(datasetUtillsService.getImageDisplay(datasetSource)));
        this.targetImp = iJ1Service.getInput(datasetUtillsService.extractPlane(datasetUtillsService.getImageDisplay(datasetSource)));
    }

    /**
     * Load ImagePlus from Folder
     *
     * @return
     */
    public List<ImagePlus> loadImagePlus() {
        Opener opener = new Opener();

        List<File> listFiles;
        listFiles = (List<File>) imageLoaderService.getAllImagesFromDirectory(imagesFolder.getParentFile());
        List<ImagePlus> listImagePlus = new ArrayList();
        listFiles.stream()
                .forEach((f) -> {
                    ImagePlus imagePlus = opener.openImage(f.getAbsolutePath());
                    listImagePlus.add(imagePlus);
                });
        return listImagePlus;
    }

    /**
     * Return the matrix transformation
     *
     * @param transformation
     * @return
     */
    private ImagePlus getFeedback(Transformation transformation) {
        ImagePlus deformation = new ImagePlus();
        ImageStack imageStack = new ImageStack(sourceImp.getWidth(), sourceImp.getHeight());
//        transformation.computeDeformationVectors(transformation.getIntervals(), transformation.getDirectDeformationCoefficientsX(), transformation.getDirectDeformationCoefficientsY(), imageStack, richOutput);
//        transformation.computeDeformationGrid(transformation.getIntervals(), transformation.getDirectDeformationCoefficientsX(), transformation.getDirectDeformationCoefficientsY(), imageStack, richOutput);
        deformation.setStack(imageStack);
        return deformation;
    }

    private String transformedTitle(ImagePlus imagePlus) {
        String title = imagePlus.getTitle();
//        int index = title.indexOf(".");
//        title = title.substring(0, index) + "-aligned" + title.substring(index);
        return title;
    }

    /**
     * Display the source and transformed target
     *
     * @param transformation
     */
    public void getMergedFeedback(Transformation transformation) {
        ImagePlus sourceTransformed = applyTransformation(sourceImp, transformation);
        Img img = ImageJFunctions.wrap(sourceTransformed);
        Dataset datasetPostTransformation = datasetService.create(img);
        Dataset[] datasetArray = {datasetPostTransformation, datasetTarget};
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("title", "Tuan anh est magique");
        parameters.put("datasetArray", datasetArray);
        parameters.put("axisType", Axes.CHANNEL);
        Module module = executeCommand(ImagesToStack.class, parameters);
        Dataset dataset = (Dataset) module.getOutput("outputDataset");
        displayService.createDisplay(dataset);
    }

    private Dataset applyFlatFieldCorrection(Dataset dataset, Dataset flatField) {

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("inputDataset", dataset);
        parameters.put("flatFieldDataset", flatField);
        Module module = executeCommand(FlatFieldCorrection.class, parameters);
        dataset = (Dataset) module.getOutput("outputDataset");
        return dataset;

    }

    /**
     *
     * @param imagePlus
     * @param transformation
     * @return
     */
    private ImagePlus applyTransformation(ImagePlus imagePlus, Transformation transformation) {
        ImagePlus[] imArray = new ImagePlus[2];
        imArray[0] = imagePlus;
        imArray[1] = imagePlus;
        final MainDialog dialog = new MainDialog(null,imArray, mode,
                this.maxImageSubsamplingFactor, this.min_scale_deformation,
                this.max_scale_deformation, this.divWeight, this.curlWeight,
                this.landmarkWeight, this.imageWeight, this.consistencyWeight,
                this.stopThreshold, false, this.saveTransformation);

        dialog.applyTransformationToSource(transformation.getIntervals(), transformation.getDirectDeformationCoefficientsX(), transformation.getDirectDeformationCoefficientsY());
        imagePlus.setTitle(transformedTitle(imagePlus));
        return imagePlus;

    }

    /**
     *
     * @param <C>
     * @param type
     * @param parameters
     * @return
     */
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
        } catch (InterruptedException ex) {
            Logger.getLogger(FlatFieldCorrection.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(FlatFieldCorrection.class.getName()).log(Level.SEVERE, null, ex);
        }
        return module;
    }

}
