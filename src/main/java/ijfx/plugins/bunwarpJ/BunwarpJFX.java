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
package ijfx.plugins.bunwarpJ;

import ij.ImagePlus;
import ij.measure.Calibration;
import ij.process.ImageProcessor;
import ijfx.core.imagedb.ImageLoaderService;
import ijfx.plugins.adapter.IJ1Service;
import ijfx.service.dataset.DatasetUtillsService;
import java.awt.Point;
import java.io.File;
import java.util.Arrays;
import java.util.Stack;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.display.ImageDisplayService;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.display.DisplayService;
import org.scijava.module.ModuleService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Tuan anh TRINH
 */
@Plugin(type = Command.class, menuPath = "Plugins>BUnwarpJCommand")
public class BunwarpJFX implements Command {

    public static String[] modesArray = {"Fast", "Accurate", "Mono"};
    public static String[] sMinScaleDeformationChoices = {"Very Coarse", "Coarse", "Fine", "Very Fine"};
    public static String[] sMaxScaleDeformationChoices = {"Very Coarse", "Coarse", "Fine", "Very Fine", "Super Fine"};

    /**
     * Image representation for source image
     */
    private ImagePlus sourceImp;
    /**
     * Image representation for target image
     */
    private ImagePlus targetImp;

    @Parameter
    IJ1Service iJ1Service;

    @Parameter
    DatasetService datasetService;

    @Parameter
    ImageLoaderService imageLoaderService;

    @Parameter
    DisplayService displayService;

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
//    private ImagePlus imagePlus;
    /**
     * minimum scale deformation
     */
//    @Parameter(choices = {"Very Coarse", "Coarse", "Fine", "Very Fine"})
    private String min_scale_deformation_choice;

    /**
     * maximum scale deformation
     */
//    @Parameter(choices = {"Very Coarse", "Coarse", "Fine", "Very Fine", "Super Fine"})
    private String max_scale_deformation_choice;

    /**
     * algorithm mode (fast, accurate or mono)
     */
//    @Parameter(choices = {"Fast", "Accurate", "Mono"})
    private String modeChoice = "Mono";
    /**
     * image subsampling factor at the highest pyramid level
     */
//    @Parameter
    private int maxImageSubsamplingFactor = 0;

    // Transformation parameters
    /**
     * divergence weight
     */
//    @Parameter
    private double divWeight = 0;
    /**
     * curl weight
     */
//    @Parameter
    private double curlWeight = 0;
    /**
     * landmarks weight
     */
//    @Parameter
    private double landmarkWeight = 1.0;
    /**
     * image similarity weight
     */
//    @Parameter
    private double imageWeight = 0.0;
    /**
     * consistency weight
     */
//    @Parameter
    private double consistencyWeight = 10;
    /**
     * flag for rich output (verbose option)
     */
//    @Parameter
//    private boolean richOutput = false;
    /**
     * flag for save transformation option
     */
//    @Parameter
    private boolean saveTransformation = false;

    /**
     * minimum image scale
     */
//    @Parameter
    private int min_scale_image = 0;
    /**
     * stopping threshold
     */
//    @Parameter
    private static double stopThreshold = 1e-2;

//    @Parameter(choices = {"0", "1", "2", "3", "4", "5", "6", "7"})
//    String img_subsamp_fact;
    @Parameter(label = "Landmarks File", required = false)
    File landmarksFile = null;

    @Parameter(type = ItemIO.INPUT)
    Dataset sourceDataset;

    @Parameter(type = ItemIO.INPUT)
    Dataset targetDataset;

    @Parameter(type = ItemIO.OUTPUT)
    Dataset outputDataset;

    @Parameter
    bunwarpj.Param parameter;

    @Parameter(required = false)
    bunwarpj.Transformation transformation;

    ImageProcessor targetMskIP = null;
    ImageProcessor sourceMskIP = null;
    int max_scale_deformation;
    int min_scale_deformation;
    int mode;
    String pathFile;

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
        this.sourceImp = iJ1Service.getInput(sourceDataset).duplicate();
        this.targetImp = iJ1Service.getInput(targetDataset);
//        sourceImp.setPosition(0, 0, 0);
        Stack<Point> sourcePoints = new Stack<>();
        Stack<Point> targetPoints = new Stack<>();
//        bunwarpj.Param parameter = new bunwarpj.Param(mode, maxImageSubsamplingFactor, min_scale_deformation, max_scale_deformation, divWeight, curlWeight, landmarkWeight, imageWeight, consistencyWeight, stopThreshold);
//        sourceImp.setProcessor(sourceImp.getProcessor().convertToFloat());
//        targetImp.setProcessor(targetImp.getProcessor().convertToFloat());

//        sourceImp = convertToGray32(sourceImp);
//        targetImp = convertToGray32(targetImp);
        if (transformation == null) {
            bunwarpj.MiscTools.loadPoints(landmarksFile.getAbsolutePath(), sourcePoints, targetPoints);
            transformation = bunwarpj.bUnwarpJ_.computeTransformationBatch(sourceImp.getWidth(), sourceImp.getHeight(), targetImp.getWidth(), targetImp.getHeight(), sourcePoints, targetPoints, parameter);
        }
        bunwarpj.MiscTools.applyTransformationToSourceMT(sourceImp, targetImp, transformation.getIntervals(), transformation.getDirectDeformationCoefficientsX(), transformation.getDirectDeformationCoefficientsY());
        Img img = ImagePlusAdapter.wrapImgPlus(sourceImp);
        outputDataset = datasetService.create(img);//        init();
//        if (landmarksFile != null) {
//            pathFile = landmarksFile.getAbsolutePath();
//        }
//
//        // Collect input values
//        // Source and target image plus
//        this.sourceImp = iJ1Service.getInput(sourceDataset);
//        this.targetImp = iJ1Service.getInput(targetDataset);
//        final ImagePlus[] imageList = new ImagePlus[]{sourceImp, targetImp};
//
//        final MainDialog dialog = new MainDialog(imageList, Arrays.asList(modesArray).indexOf(modeChoice),
//                maxImageSubsamplingFactor, Arrays.asList(sMinScaleDeformationChoices).indexOf(min_scale_deformation),
//                Arrays.asList(sMaxScaleDeformationChoices).indexOf(max_scale_deformation), divWeight, curlWeight,
//                landmarkWeight, imageWeight, consistencyWeight,
//                stopThreshold, richOutput, saveTransformation, pathFile
//        );
//
//        int outputLevel = 1;
//
//        boolean showMarquardtOptim = false;
//
//        if (richOutput) {
//            outputLevel++;
//            showMarquardtOptim = true;
//        }
//
//        FinalAction finalAction
//                = new FinalAction(dialog);
//
//        finalAction.setup(sourceImp, targetImp,
//                dialog.getSource(), dialog.getTarget(), dialog.getSourcePh(), dialog.getTargetPh(),
//                dialog.getSourceMsk(), dialog.getTargetMsk(),
//                dialog.getSourceAffineMatrix(), dialog.getTargetAffineMatrix(),
//                Arrays.asList(sMinScaleDeformationChoices).indexOf(min_scale_deformation), Arrays.asList(sMaxScaleDeformationChoices).indexOf(max_scale_deformation),
//                min_scale_image, divWeight, curlWeight, landmarkWeight, imageWeight,
//                consistencyWeight, stopThreshold, outputLevel, showMarquardtOptim, Arrays.asList(modesArray).indexOf(modeChoice));
//
//        dialog.setFinalActionLaunched(true);
//        dialog.setToolbarAllUp();
//        dialog.repaintToolbar();
//
//        // Throw final action thread
//        Thread fa = finalAction.getThread();
//        fa.start();
//        try {
//            // We join the thread to the main plugin thread
//            fa.join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        transformation = finalAction.getWarp();

    }

    /**
     * Init parameters
     */
    public void init() {
        //Set parameters
        mode = Arrays.asList(this.modesArray).indexOf(modeChoice);
        max_scale_deformation = Arrays.asList(sMaxScaleDeformationChoices).indexOf(max_scale_deformation_choice);
        min_scale_deformation = Arrays.asList(sMinScaleDeformationChoices).indexOf(min_scale_deformation_choice);
        this.sourceImp = iJ1Service.getInput(((sourceDataset)));
        this.targetImp = iJ1Service.getInput(((targetDataset)));
    }

    public ImagePlus convertToGray32(ImagePlus imp) {
        int type = imp.getType();
        if (type == ImagePlus.GRAY32) {
            return imp;
        }
        if (!(type == ImagePlus.GRAY8 || type == ImagePlus.GRAY16 || type == ImagePlus.COLOR_RGB)) {
            throw new IllegalArgumentException("Unsupported conversion");
        }
        ImageProcessor ip = imp.getProcessor();
        imp.trimProcessor();
        Calibration cal = imp.getCalibration();
        imp.setProcessor(null, ip.convertToFloat());
        imp.setCalibration(cal); //update calibration
        return imp;
    }

}
