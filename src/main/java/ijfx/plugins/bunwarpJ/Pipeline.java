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
import ijfx.core.imagedb.ImageLoaderService;
import ijfx.plugins.adapter.AbstractImageJ1PluginAdapter;
import static ijfx.plugins.bunwarpJ.bUnwarpJ_.sMaxScaleDeformationChoices;
import static ijfx.plugins.bunwarpJ.bUnwarpJ_.sMinScaleDeformationChoices;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.display.DisplayService;
import org.scijava.module.ModuleInfo;
import org.scijava.module.ModuleService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Tuan anh TRINH
 */
@Plugin(type = Command.class, menuPath = "Plugins>Pipeline")
public class Pipeline extends AbstractImageJ1PluginAdapter implements Command {

    public static String[] modesArray = {"Fast", "Accurate", "Mono"};
    public static String[] sMinScaleDeformationChoices = {"Very Coarse", "Coarse", "Fine", "Very Fine"};
    public static String[] sMaxScaleDeformationChoices = {"Very Coarse", "Coarse", "Fine", "Very Fine", "Super Fine"};
    @Parameter
    CommandService commandService;

    @Parameter
    ModuleService moduleService;

    @Parameter
    ImageLoaderService imageLoaderService;

    @Parameter
    DisplayService displayService;

    @Parameter
    DatasetService datasetService;

    @Parameter
    Dataset sourceDataset;
    @Parameter
    Dataset targetDataset;

    ImagePlus targetImp;
    ImagePlus sourceImp;
    ImageProcessor targetMskIP = null;
    ImageProcessor sourceMskIP = null;

    @Parameter(choices = {"Fast", "Accurate", "Mono"})
    String modeChoice;
    @Parameter(choices = {"0", "1", "2", "3", "4", "5", "6", "7"})
    String img_subsamp_fact;
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
    @Parameter
    double divWeight;
    @Parameter
    double curlWeight;
    @Parameter
    double landmarkWeight;
    @Parameter
    double imageWeight = 1.0;
    @Parameter
    double consistencyWeight = 10.0;
    @Parameter
    double stopThreshold = 0.01;

    public int min_scale_image = 0;

    public int outputLevel = -1;

    final boolean showMarquardtOptim = false;

    @Parameter
    File file;

    @Override
    public void run() {
//        Runtime.getRuntime().gc();
//
//        //Set parameters for transformation
//        this.sourceImp = getInput(sourceDataset);
//        this.targetImp = getInput(targetDataset);
//        ImagePlus[] imageList = new ImagePlus[2];//loadImagePlus();;
        int mode = Arrays.asList(modesArray).indexOf(modeChoice);
        int max_scale_deformation = Arrays.asList(sMaxScaleDeformationChoices).indexOf(max_scale_deformation_choice);
        int min_scale_deformation = Arrays.asList(sMinScaleDeformationChoices).indexOf(min_scale_deformation_choice);
//        //Calcul transformation
////        Transformation transformation = bUnwarpJ_.computeTransformationBatch(targetImp, sourceImp, targetMskIP, sourceMskIP, mode, Integer.parseInt(img_subsamp_fact), min_scale_deformation, max_scale_deformation, divWeight, curlWeight, landmarkWeight, imageWeight, consistencyWeight, stopThreshold);
////        transformation.getDirectResults().show();
////        //Apply transformation to imageList
////        sourceImp = imageList[0];
////        targetImp = imageList[1];
//        sourceImp.show();
//        targetImp.show();
//        imageList[0] = sourceImp;
//        imageList[1] = targetImp;
//        MainDialog dialog = new MainDialog(imageList, mode, max_scale_deformation, min_scale_deformation, max_scale_deformation, divWeight, curlWeight, landmarkWeight, imageWeight, consistencyWeight, stopThreshold, false, false);
//        loadTransformation(dialog);
////        FinalAction finalAction
////                = new FinalAction(dialog);
////        finalAction.setup(imageList[0], imageList[0],
////                dialog.getSource(), dialog.getTarget(), dialog.getSourcePh(), dialog.getTargetPh(),
////                dialog.getSourceMsk(), dialog.getTargetMsk(),
////                dialog.getSourceAffineMatrix(), dialog.getTargetAffineMatrix(),
////                Arrays.asList(sMinScaleDeformationChoices).indexOf(min_scale_deformation), Arrays.asList(sMaxScaleDeformationChoices).indexOf(max_scale_deformation),
////                min_scale_image, divWeight, curlWeight, landmarkWeight, imageWeight,
////                consistencyWeight, stopThreshold, outputLevel, showMarquardtOptim, mode);
////        
////        finalAction.setWarp(transformation);
////            Thread fa = finalAction.getThread();
////        fa.start();
////        try {
////            // We join the thread to the main plugin thread
////            fa.join();
////        } catch (InterruptedException e) {
////            e.printStackTrace();
////        }
////        Img img = ImageJFunctions.wrap(finalAction.getWarp().getDirectResults());
////        
////        displayService.createDisplay(datasetService.create(img));
//
//    }
//
//    @Override
//    public ImagePlus processImagePlus(ImagePlus input) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    private ImagePlus[] createImageList() {
//
//        final ImagePlus[] imageList = new ImagePlus[2];
//
//        imageList[0] = sourceImp;
//        imageList[1] = targetImp;
//        return (imageList);
//    }
//
//    public ImagePlus[] loadImagePlus() {
//        Opener opener = new Opener();
//        List<File> listFiles = (List<File>) imageLoaderService.getAllImagesFromDirectory(file.getParentFile());
//        List<ImagePlus> listImagePlus = new ArrayList();
//        listFiles.stream()
//                .forEach((file) -> {
//                    ImagePlus imagePlus = opener.openImage(file.getAbsolutePath());
//                    listImagePlus.add(imagePlus);
//                    imagePlus.show();
//                });
////        return new ImagePlus[]{sourceImp, targetImp};
//        return listImagePlus.toArray(new ImagePlus[listImagePlus.size()]);
//    }
//
//    private void loadTransformation(MainDialog dialog) {
//        final OpenDialog od = new OpenDialog("Load Elastic Transformation", "");
//        final String path = od.getDirectory();
//        final String filename = od.getFileName();
//
//        if ((path == null) || (filename == null)) {
//            return;
//        }
//
//        String fn_tnf = path + filename;
//
//        int intervals = MiscTools.numberOfIntervalsOfTransformation(fn_tnf);
//
//        double[][] cx = new double[intervals + 3][intervals + 3];
//        double[][] cy = new double[intervals + 3][intervals + 3];
//
//        MiscTools.loadTransformation(fn_tnf, cx, cy);
//
//        // Apply transformation
//        dialog.applyTransformationToSource(intervals, cx, cy);
//    }
    
    	Runtime.getRuntime().gc();
    	final ImagePlus[] imageList = createImageList();
    	if (imageList.length < 2) 
    	{
    		IJ.error("At least two (8, 16, 32-bit or RGB Color) images are required");
    		return;
    	}
	
            MainDialog dialog = new MainDialog(imageList, mode, max_scale_deformation, min_scale_deformation, max_scale_deformation, divWeight, curlWeight, landmarkWeight, imageWeight, consistencyWeight, stopThreshold, false, false,"");

//	 	dialog.showDialog();
	 	
//	 	// If canceled
//	 	if (dialog.wasCanceled())
//	 	{
//	 		dialog.dispose();
//    		dialog.restoreAll();
//    		return;
//    	}
//    	
	 	// If OK
//     	dialog.dispose();    	       
        
        // Collect input values
		// Source and target image plus
		this.sourceImp = imageList[0];
		this.targetImp = imageList[1];
                loadTransformation(dialog);
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

    } /* end run */

    // end alignImagesCommandLine

    //------------------------------------------------------------------
    /**
     * Create a list with the open images in ImageJ that bUnwarpJ can
     * process.
     *
     * @return array of references to the open images in bUnwarpJ
     */
    private ImagePlus[] createImageList () 
    {
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
        imageList[0] = getInput(sourceDataset);
        imageList[1] = getInput(targetDataset);
        imageList[0].show();
        imageList[1].show();
       return(imageList);
    }

    @Override
    public ImagePlus processImagePlus(ImagePlus input) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
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

}
