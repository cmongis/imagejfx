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

import ij.IJ;
import ij.ImagePlus;
import ij.io.OpenDialog;
import ij.io.Opener;
import ij.process.ImageProcessor;
import ijfx.core.project.ImageLoaderService;
import ijfx.plugins.adapter.AbstractImageJ1PluginAdapter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.stage.FileChooser;
import net.imagej.Dataset;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Tuan anh TRINH
 */
@Plugin(type = Command.class, menuPath = "Plugins>Pipeline2")

public class Pip2 extends AbstractImageJ1PluginAdapter implements Command {

    /* begin class this */
  @Parameter
    ImageLoaderService imageLoaderService;
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
    private static int min_scale_deformation = 3;
    /**
     * maximum scale deformation
     */
    private static int max_scale_deformation = 4;
    /**
     * algorithm mode (fast, accurate or mono)
     */
    private static int mode = MainDialog.MONO_MODE;
    /**
     * image subsampling factor at the highest pyramid level
     */
    private static int maxImageSubsamplingFactor = 0;

    // Transformation parameters
    /**
     * divergence weight
     */
    private static double divWeight = 0;
    /**
     * curl weight
     */
    private static double curlWeight = 0;
    /**
     * landmarks weight
     */
    private static double landmarkWeight = 0;
    /**
     * image similarity weight
     */
    private static double imageWeight = 1;
    /**
     * consistency weight
     */
    private static double consistencyWeight = 10;
    /**
     * flag for rich output (verbose option)
     */
    private static boolean richOutput = false;
    /**
     * flag for save transformation option
     */
    private static boolean saveTransformation = false;

    /**
     * minimum image scale
     */
    private int min_scale_image = 0;
    /**
     * stopping threshold
     */
    private static double stopThreshold = 1e-2;
    /**
     * debug flag
     */
    private static boolean debug = false;
    @Parameter(choices = {"0", "1", "2", "3", "4", "5", "6", "7"})
    String img_subsamp_fact;
    @Parameter
    Dataset dataset1;

    @Parameter
    Dataset dataset2;

    @Parameter
            File file;
        ImageProcessor targetMskIP = null;
    ImageProcessor sourceMskIP = null;
    /*....................................................................
       Public methods
    ....................................................................*/
    private void loadTransformation(MainDialog dialog) {
        final OpenDialog od = new OpenDialog("Load Elastic Transformation", "");
        final String path = od.getDirectory();
        final String filename = od.getFileName();

        if ((path == null) || (filename == null)) {
            return;
        }

        String fn_tnf = path + filename;

        int intervals = MiscTools.numberOfIntervalsOfTransformation(fn_tnf);

        double[][] cx = new double[intervals + 3][intervals + 3];
        double[][] cy = new double[intervals + 3][intervals + 3];

        MiscTools.loadTransformation(fn_tnf, cx, cy);

        // Apply transformation
        dialog.applyTransformationToSource(intervals, cx, cy);
    }

    //------------------------------------------------------------------
    /**
     * Method to lunch the plugin.
     *
     * @param commandLine command to determine the action
     */
    @Override
    public void run() {
        Runtime.getRuntime().gc();
        final ImagePlus[] imageList = createImageList();
        if (imageList.length < 2) {
            IJ.error("At least two (8, 16, 32-bit or RGB Color) images are required");
            return;
        }
        this.sourceImp = imageList[0];
        this.targetImp = imageList[1];
        this.sourceImp.show();
        this.targetImp.show();
        Transformation transformation = bUnwarpJ_.computeTransformationBatch(targetImp, sourceImp, targetMskIP, sourceMskIP, mode, Integer.parseInt(img_subsamp_fact), min_scale_deformation, max_scale_deformation, divWeight, curlWeight, landmarkWeight, imageWeight, consistencyWeight, stopThreshold);

        loadImagePlus().stream().forEach((imagePlus) ->{
            ImagePlus[] imArray = new ImagePlus[2];
            imArray[0] = imagePlus;
            imArray[1] = imagePlus;
        final MainDialog dialog = new MainDialog(imArray, this.mode,
                this.maxImageSubsamplingFactor, this.min_scale_deformation,
                this.max_scale_deformation, this.divWeight, this.curlWeight,
                this.landmarkWeight, this.imageWeight, this.consistencyWeight,
                this.stopThreshold, this.richOutput, this.saveTransformation);

        dialog.applyTransformationToSource(transformation.getIntervals(), transformation.getDirectDeformationCoefficientsX(), transformation.getDirectDeformationCoefficientsY());
            imagePlus.show();
        });
//		  
//		// Fast or accurate mode
//		this.mode = dialog.getNextChoiceIndex();
//		// Image subsampling factor at highest resolution level		
//		this.maxImageSubsamplingFactor = (int) dialog.getNextNumber();
//		  
//		// Min and max scale deformation level
//		this.min_scale_deformation = dialog.getNextChoiceIndex();
//		this.max_scale_deformation = dialog.getNextChoiceIndex();
//				  
//		// Weights
//		this.divWeight  			= dialog.getNextNumber();
//		this.curlWeight 			= dialog.getNextNumber();
//		this.landmarkWeight 		= dialog.getNextNumber();
//		this.imageWeight			= dialog.getNextNumber();
//		this.consistencyWeight		= dialog.getNextNumber();
//		this.stopThreshold			= dialog.getNextNumber();
//		  
//		// Verbose and save transformation options
//		this.richOutput 		   	= dialog.getNextBoolean();
//		this.saveTransformation 	= dialog.getNextBoolean();
//        dialog.setSaveTransformation(this.saveTransformation);
//
//        int outputLevel = 1;
//
//        boolean showMarquardtOptim = false;
//
//        if (richOutput)
//        {
//           outputLevel++;
//           showMarquardtOptim = true;
//        }                                 
//        
//        FinalAction finalAction =
//           new FinalAction(dialog);
//
//        finalAction.setup(sourceImp, targetImp,
//           dialog.getSource(), dialog.getTarget(), dialog.getSourcePh(), dialog.getTargetPh(),
//           dialog.getSourceMsk(), dialog.getTargetMsk(), 
//           dialog.getSourceAffineMatrix(), dialog.getTargetAffineMatrix(),
//           min_scale_deformation, max_scale_deformation,
//           min_scale_image, divWeight, curlWeight, landmarkWeight, imageWeight,
//           consistencyWeight, stopThreshold, outputLevel, showMarquardtOptim, mode);
//
//        dialog.setFinalActionLaunched(true);
//        dialog.setToolbarAllUp();
//        dialog.repaintToolbar();                
//        
//        // Throw final action thread
//        Thread fa = finalAction.getThread();
//        fa.start();
//        try {
//        	// We join the thread to the main plugin thread
//			fa.join();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		
//		

    }

    /* end run */

    // end alignImagesCommandLine

    //------------------------------------------------------------------
    /**
     * Create a list with the open images in ImageJ that bUnwarpJ can process.
     *
     * @return array of references to the open images in bUnwarpJ
     */
    private ImagePlus[] createImageList() {
//       final int[] windowList = WindowManager.getIDList();
//       final Stack <ImagePlus> stack = new Stack <ImagePlus>();
//       for (int k = 0; ((windowList != null) && (k < windowList.length)); k++) 
//       {
//          final ImagePlus imp = WindowManager.getImage(windowList[k]);
//          final int inputType = imp.getType();
//
//          // Since October 6th, 2008, bUnwarpJ can deal with 8, 16, 32-bit grayscale 
//          // and RGB Color images.
//          if ((imp.getStackSize() == 1) || (inputType == ImagePlus.GRAY8) || (inputType == ImagePlus.GRAY16)
//             || (inputType == ImagePlus.GRAY32) || (inputType == ImagePlus.COLOR_RGB)) 
//          {
//             stack.push(imp);
//          }
//       }
//       final ImagePlus[] imageList = new ImagePlus[stack.size()];
//       int k = 0;
//       while (!stack.isEmpty()) {
//          imageList[k++] = (ImagePlus)stack.pop();
//       }
        ImagePlus[] imageList = new ImagePlus[2];
        imageList[0] = getInput(dataset1);
        imageList[1] = getInput(dataset2);
        return (imageList);
    }

    /* end createImageList */

    @Override
    public ImagePlus processImagePlus(ImagePlus input) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    public List<ImagePlus> loadImagePlus() {
        Opener opener = new Opener();


        List<File> listFiles;
        listFiles = (List<File>) imageLoaderService.getAllImagesFromDirectory(file.getParentFile());
        List<ImagePlus> listImagePlus = new ArrayList();
        listFiles.stream()
                .forEach((file) -> {
                    ImagePlus imagePlus = opener.openImage(file.getAbsolutePath());
                    listImagePlus.add(imagePlus);
                    imagePlus.show();
                });
//        return new ImagePlus[]{sourceImp, targetImp};
        return listImagePlus;
    }
}
