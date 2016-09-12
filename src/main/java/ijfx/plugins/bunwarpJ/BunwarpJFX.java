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
    private String min_scale_deformation_choice;

    /**
     * maximum scale deformation
     */
    private String max_scale_deformation_choice;

    /**
     * algorithm mode (fast, accurate or mono)
     */
    private String modeChoice = "Mono";
    /**
     * image subsampling factor at the highest pyramid level
     */
    private int maxImageSubsamplingFactor = 0;

    // Transformation parameters
    /**
     * divergence weight
     */
    private double divWeight = 0;
    /**
     * curl weight
     */
    private double curlWeight = 0;
    /**
     * landmarks weight
     */
    private double landmarkWeight = 1.0;
    /**
     * image similarity weight
     */
    private double imageWeight = 0.0;
    /**
     * consistency weight
     */
    private double consistencyWeight = 10;
    /**
     * flag for rich output (verbose option)
     */
    /**
     * flag for save transformation option
     */
    private boolean saveTransformation = false;

    /**
     * minimum image scale
     */
    private int min_scale_image = 0;
    /**
     * stopping threshold
     */

    private static double stopThreshold = 1e-2;


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
        Stack<Point> sourcePoints = new Stack<>();
        Stack<Point> targetPoints = new Stack<>();

        if (transformation == null) {
            bunwarpj.MiscTools.loadPoints(landmarksFile.getAbsolutePath(), sourcePoints, targetPoints);
            transformation = bunwarpj.bUnwarpJ_.computeTransformationBatch(sourceImp.getWidth(), sourceImp.getHeight(), targetImp.getWidth(), targetImp.getHeight(), sourcePoints, targetPoints, parameter);
        }
        bunwarpj.MiscTools.applyTransformationToSourceMT(sourceImp, targetImp, transformation.getIntervals(), transformation.getDirectDeformationCoefficientsX(), transformation.getDirectDeformationCoefficientsY());
        Img img = ImagePlusAdapter.wrapImgPlus(sourceImp);
        outputDataset = datasetService.create(img);

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
