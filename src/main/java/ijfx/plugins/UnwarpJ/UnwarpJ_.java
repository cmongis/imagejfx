/*
 * /*
 *     This file is part of ImageJ FX.
 *
 *     ImageJ FX is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     ImageJ FX is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with ImageJ FX.  If not, see <http://www.gnu.org/licenses/>. 
 *
 * 	Copyright 2015,2016 Cyril MONGIS, Michael Knop
 *
 */
/*====================================================================
| Version: July 8, 2006 by Steve Bryson and Carlos Oscar
| modified to replace Tiff image outputDataset with high-quality Jpeg outputDataset
\===================================================================*/

/*====================================================================
| Carlos Oscar Sanchez Sorzano
| Unidad de Bioinformatica
| Centro Nacional de Biotecnologia (CSIC)
| Campus Universidad Autonoma (Cantoblanco)
| E-28049 Madrid
| Spain
|
| phone (CET): +34(91)585.45.10
| fax: +34(91)585.45.06
| RFC-822: coss@cnb.uam.es
| URL: http://biocomp.cnb.uam.es/
\===================================================================*/

/*====================================================================
| This work is based on the following paper:
|
| C.O.S. Sorzano, P. Thevenaz, M. Unser
| Elastic Registration of Biological Images Using Vector-Spline Regularization
| IEEE Transactions on Biomedical Imaging, 52: 652-663 (2005)
|
| This paper is available on-line at
| http://bigwww.epfl.ch/publications/sorzano0501.html
|
| Other relevant on-line publications are available at
| http://bigwww.epfl.ch/publications/
\===================================================================*/

/*====================================================================
| Additional help available at http://bigwww.epfl.ch/thevenaz/UnwarpJ/
|
| You'll be free to use this software for research purposes, but you
| should not redistribute it without our consent. In addition, we expect
| you to include a citation or acknowledgment whenever you present or
| publish results that are based on it.
\===================================================================*/

package ijfx.plugins.UnwarpJ;

import ij.IJ;
import ij.ImagePlus;
import ij.Macro;
import ij.WindowManager;
import ij.gui.GUI;
import ij.io.FileSaver;
import ij.plugin.JpegWriter;
import ij.io.Opener;
import ij.process.ImageConverter;
import ijfx.plugins.AbstractImageJ1PluginAdapter;
import java.awt.Point;
import java.util.Stack;
import java.util.StringTokenizer;
import net.imagej.Dataset;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/*====================================================================
|   UnwarpJ_
\===================================================================*/
/* Main class.
   This is the one called by ImageJ */

/*------------------------------------------------------------------*/

@Plugin(type = Command.class, menuPath = "Plugins>UnwarpJ_")
public class UnwarpJ_ extends AbstractImageJ1PluginAdapter {
   @Parameter(type = ItemIO.OUTPUT)
   Dataset outputDataset;
    @Parameter(label = "Source")
    Dataset sourceDataset;
    
    @Parameter(label = "Target")
    Dataset targetDataset;
 /* begin class UnwarpJ_ */

/*....................................................................
   Public methods
....................................................................*/

/*------------------------------------------------------------------*/
   @Override
    public void run() 
 {
     setWholeWrap(true);
   final String commandLine ="-align target.tif NULL source.tif NULL 0 4 0 0 1 output.tif";
   String options = Macro.getOptions();
   if (!commandLine.equals("")) options = commandLine;
   if (options == null) {
      Runtime.getRuntime().gc();
      final ImagePlus[] imageList = createImageList();
      if (imageList.length < 2) {
         IJ.error("At least two images are required (stack of color images disallowed)");
         return;
      }

      final unwarpJDialog dialog = new unwarpJDialog(IJ.getInstance(), imageList);
      GUI.center(dialog);
      dialog.setVisible(true);
   } else {
      final String[] args = getTokens(options);
      if (args.length<1) {
         dumpSyntax();
         return;
      } else {
         if      (args[0].equals("-help"))      dumpSyntax();
         else if (args[0].equals("-align"))     alignImagesMacro(args);
         else if (args[0].equals("-transform")) transformImageMacro(args);
      }
      return;
   }
//} /* end run */
//
///*------------------------------------------------------------------*/
//public static void main(String args[]) {
String []args= commandLine.split(" ");

   if (args.length<1) {
      dumpSyntax();
      System.exit(1);
   } else {      
      if      (args[0].equals("-help"))      dumpSyntax();
      else if (args[0].equals("-align"))     alignImagesMacro(args);
      else if (args[0].equals("-transform")) transformImageMacro(args);
   }
   System.exit(0);
}

/*....................................................................
   Private methods
....................................................................*/

/*------------------------------------------------------------------*/
private void alignImagesMacro(String args[]) {

   if(args.length < 11)
   {
       dumpSyntax();
       System.exit(0);
   }

   // Check if -output_jpg at the end
   int args_len=args.length;
   String last_argument=args[args_len-1];
   boolean jpeg_output=last_argument.equals("-jpg_output");
   if (jpeg_output) args_len--;

   // Read input parameters
   String fn_target=args[1];
   String fn_target_mask=args[2];
   String fn_source=args[3];
   String fn_source_mask=args[4];
   int min_scale_deformation=((Integer) new Integer(args[5])).intValue();
   int max_scale_deformation=((Integer) new Integer(args[6])).intValue();
   double  divWeight=((Double) new Double(args[7])).doubleValue();
   double  curlWeight=((Double) new Double(args[8])).doubleValue();
   double  imageWeight=((Double) new Double(args[9])).doubleValue();
   String fn_out=args[10];
   double  landmarkWeight=0;
   String fn_landmark="";
   if (args_len>=13) {
      landmarkWeight=((Double) new Double(args[11])).doubleValue();
      fn_landmark=args[12];
   }
   double  stopThreshold=1e-2;
   if (args_len>=14)
      stopThreshold=((Double) new Double(args[13])).doubleValue();

   // Show parameters
   IJ.write("Target image           : "+fn_target);
   IJ.write("Target mask            : "+fn_target_mask);
   IJ.write("Source image           : "+fn_source);
   IJ.write("Source mask            : "+fn_source_mask);
   IJ.write("Min. Scale Deformation : "+min_scale_deformation);
   IJ.write("Max. Scale Deformation : "+max_scale_deformation);
   IJ.write("Div. Weight            : "+divWeight);
   IJ.write("Curl Weight            : "+curlWeight);
   IJ.write("Image Weight           : "+imageWeight);
   IJ.write("Output:                : "+fn_out);
   IJ.write("Landmark Weight        : "+landmarkWeight);
   IJ.write("Landmark file          : "+fn_landmark);
   IJ.write("JPEG Output            : "+jpeg_output);

   // Produce side information
   int     imagePyramidDepth=max_scale_deformation-min_scale_deformation+1;
   int     min_scale_image=0;
   int     outputLevel=2;
   boolean showMarquardtOptim=false;
   int     accurate_mode=1;
   boolean saveTransf=true;

   String fn_tnf="";
   int dot = fn_out.lastIndexOf('.');
   if (dot == -1) fn_tnf=fn_out + "_transf.txt";
   else           fn_tnf=fn_out.substring(0, dot)+"_transf.txt";

   // Open targetDataset
   Opener opener=new Opener();
   ImagePlus targetImp = this.getInput(this.targetDataset);
//   targetImp=opener.openImage(fn_target);
   unwarpJImageModel target =
      new unwarpJImageModel(targetImp.getProcessor(), true);
   target.setPyramidDepth(imagePyramidDepth+min_scale_image);
   target.getThread().start();
   unwarpJMask targetMsk =
      new unwarpJMask(targetImp.getProcessor(),false);
   if (fn_target_mask.equalsIgnoreCase(new String("NULL")) == false) 
       targetMsk.readFile(fn_target_mask);
   unwarpJPointHandler targetPh=null;

   // Open sourceDataset
   ImagePlus sourceImp = getInput(sourceDataset);
   sourceImp.show();
//   sourceImp=opener.openImage(fn_source);
   unwarpJImageModel source =
      new unwarpJImageModel(sourceImp.getProcessor(), false);
   source.setPyramidDepth(imagePyramidDepth+min_scale_image);
   source.getThread().start();
   unwarpJMask sourceMsk =
       new unwarpJMask(sourceImp.getProcessor(),false);   
   if (fn_source_mask.equalsIgnoreCase(new String("NULL")) == false)
       sourceMsk.readFile(fn_source_mask);   
   unwarpJPointHandler sourcePh=null;

   // Load landmarks
   if (fn_landmark!="") {
      Stack sourceStack = new Stack();
      Stack targetStack = new Stack();
      unwarpJMiscTools.loadPoints(fn_landmark,sourceStack,targetStack);

      sourcePh  = new unwarpJPointHandler(sourceImp);
      targetPh  = new unwarpJPointHandler(targetImp);
      while ((!sourceStack.empty()) && (!targetStack.empty())) {
         Point sourcePoint = (Point)sourceStack.pop();
         Point targetPoint = (Point)targetStack.pop();
         sourcePh.addPoint(sourcePoint.x, sourcePoint.y);
         targetPh.addPoint(targetPoint.x, targetPoint.y);
      }
   }

   // Join threads
   try {
    source.getThread().join();
    target.getThread().join();
  } catch (InterruptedException e) {
    IJ.error("Unexpected interruption exception " + e);
  }

   // Perform registration
   ImagePlus output_ip=new ImagePlus();
   unwarpJDialog dialog=null;
   final unwarpJTransformation warp = new unwarpJTransformation(
     sourceImp, targetImp, source, target, sourcePh, targetPh,
     sourceMsk, targetMsk, min_scale_deformation, max_scale_deformation,
     min_scale_image, divWeight, curlWeight, landmarkWeight, imageWeight,
     stopThreshold, outputLevel, showMarquardtOptim, accurate_mode,
     saveTransf, fn_tnf, output_ip, dialog);
   warp.doRegistration();

   // Save results
   ImageConverter converter=new ImageConverter(output_ip);
   converter.convertToGray16();
   FileSaver fs=new FileSaver(output_ip);
   if (jpeg_output) {
      JpegWriter js = new JpegWriter();
      js.setQuality(100);
      WindowManager.setTempCurrentImage(output_ip);	
      js.run(fn_out);
   } else
      //fs.saveAsTiff(fn_out);
       output_ip.show();
    Img img = ImageJFunctions.wrap(output_ip);
    ImageJFunctions.show((RandomAccessibleInterval) img);
   outputDataset= service.create(img);//setOutput(output_ip, outputDataset);;
   outputDataset.setName(fn_out);
}

/*------------------------------------------------------------------*/
private ImagePlus[] createImageList (
) {
   final int[] windowList = WindowManager.getIDList();
   final Stack stack = new Stack();
   for (int k = 0; ((windowList != null) && (k < windowList.length)); k++) {
      final ImagePlus imp = WindowManager.getImage(windowList[k]);
      final int inputType = imp.getType();
      if ((imp.getStackSize() == 1) || (inputType == imp.GRAY8) || (inputType == imp.GRAY16)
         || (inputType == imp.GRAY32)) {
         stack.push(imp);
      }
   }
   final ImagePlus[] imageList = new ImagePlus[stack.size()];
   int k = 0;
   while (!stack.isEmpty()) {
      imageList[k++] = (ImagePlus)stack.pop();
   }
   return(imageList);
} /* end createImageList */

/*------------------------------------------------------------------*/
private static void dumpSyntax (
) {
   IJ.write("Purpose: Elastic registration of two images.");
   IJ.write(" ");
   IJ.write("Usage: unwarpj ");
   IJ.write("  -help                       : SHOWS THIS MESSAGE");
   IJ.write("");
   IJ.write("  -align                      : ALIGN TWO IMAGES");
   IJ.write("          target_image        : In any image format");
   IJ.write("          target_mask         : In any image format");
   IJ.write("          source_image        : In any image format");
   IJ.write("          source_mask         : In any image format");
   IJ.write("          min_scale_def       : Scale of the coarsest deformation");
   IJ.write("                                0 is the coarsest possible");
   IJ.write("          max_scale_def       : Scale of the finest deformation");
   IJ.write("          Div_weight          : Weight of the divergence term");
   IJ.write("          Curl_weight         : Weight of the curl term");
   IJ.write("          Image_weight        : Weight of the image term");
   IJ.write("          Output image        : Output result in JPG (100%)");
   IJ.write("          Optional parameters :");
   IJ.write("             Landmark_weight  : Weight of the landmarks");
   IJ.write("             Landmark_file    : Landmark file");
   IJ.write("             stopThreshold    : By default 1e-2");
   IJ.write("");
   IJ.write("  -transform                  : TRANSFORM AN IMAGE WITH A GIVEN DEFORMATION");
   IJ.write("          target_image        : In any image format");
   IJ.write("          source_image        : In any image format");
   IJ.write("          transformation_file : As saved by UnwarpJ");
   IJ.write("          Output image        : Output result in JPG (100%)");
   IJ.write("");
   IJ.write("Examples:");
   IJ.write("Align two images without landmarks and without mask");
   IJ.write("   unwarpj -align target.jpg NULL source.jpg NULL 0 2 1 1 1 output.tif");
   IJ.write("Align two images with landmarks and mask");
   IJ.write("   unwarpj -align target.tif target_mask.tif source.tif source_mask.tif 0 2 1 1 1 1 landmarks.txt output.tif");
   IJ.write("Align two images using only landmarks");
   IJ.write("   unwarpj -align target.jpg NULL source.jpg NULL 0 2 1 1 0 output.tif 1 landmarks.txt");
   IJ.write("Transform the source image with a previously computed transformation");
   IJ.write("   unwarpj -transform target.jpg source.jpg transformation.txt output.tif");
   IJ.write("");
   IJ.write("JPEG Output:");
   IJ.write("   If you want to produce JPEG output simply add -jpg_output as the last argument");
   IJ.write("   of the alignment or transformation command. For instance:");
   IJ.write("   unwarpj -align target.jpg NULL source.jpg NULL 0 2 1 1 1 output.jpg -jpg_output");
   IJ.write("   unwarpj -align target.tif target_mask.tif source.tif source_mask.tif 0 2 1 1 1 1 landmarks.txt output.jpg -jpg_output");
   IJ.write("   unwarpj -align target.jpg NULL source.jpg NULL 0 2 1 1 0 output.jpg 1 landmarks.txt -jpg_output");
   IJ.write("   unwarpj -transform target.jpg source.jpg transformation.txt output.jpg -jpg_output");
} /* end dumpSyntax */

/*------------------------------------------------------------------*/
private String[] getTokens (
	final String options
) {
	StringTokenizer t = new StringTokenizer(options);
	String[] token = new String[t.countTokens()];
	for (int k = 0; (k < token.length); k++) {
		token[k] = t.nextToken();
	}
	return(token);
} /* end getTokens */

/*------------------------------------------------------------------*/
private static void transformImageMacro(String args[]) {
   // Read input parameters
   String fn_target=args[1];
   String fn_source=args[2];
   String fn_tnf   =args[3];
   String fn_out   =args[4];

   // Jpeg outputDataset
   String last_argument=args[args.length-1];
   boolean jpeg_output=last_argument.equals("-jpg_output");

   // Show parameters
   IJ.write("Target image           : "+fn_target);
   IJ.write("Source image           : "+fn_source);
   IJ.write("Transformation file    : "+fn_tnf);
   IJ.write("Output                 : "+fn_out);
   IJ.write("JPEG output            : "+jpeg_output);

   // Open targetDataset
   Opener opener=new Opener();
   ImagePlus targetImp;
   targetImp=opener.openImage(fn_target);

   // Open sourceDataset
   ImagePlus sourceImp;
   sourceImp=opener.openImage(fn_source);
   unwarpJImageModel source =
      new unwarpJImageModel(sourceImp.getProcessor(), false);
   source.setPyramidDepth(0);
   source.getThread().start();

   // Load transformation
   int intervals=unwarpJMiscTools.
      numberOfIntervalsOfTransformation(fn_tnf);
   double [][]cx=new double[intervals+3][intervals+3];
   double [][]cy=new double[intervals+3][intervals+3];
   unwarpJMiscTools.loadTransformation(fn_tnf, cx, cy);

   // Join threads
   try {
      source.getThread().join();
   } catch (InterruptedException e) {
      IJ.error("Unexpected interruption exception " + e);
   }

   // Apply transformation to sourceDataset
   unwarpJMiscTools.applyTransformationToSource(
      sourceImp,targetImp,source,intervals,cx,cy);

   // Save results
   FileSaver fs=new FileSaver(sourceImp);
   if (jpeg_output) {
      JpegWriter js = new JpegWriter();
      js.setQuality(100);
      WindowManager.setTempCurrentImage(sourceImp);	
      js.run(fn_out);
   } else
      fs.saveAsTiff(fn_out);
}

    @Override
    public ImagePlus processImagePlus(ImagePlus input) {
        return input;
    }

 

} /* end class UnwarpJ_ */

/*====================================================================
|   unwarpJClearAll
\===================================================================*/


/*====================================================================
|   unwarpJCredits
\===================================================================*/



/*====================================================================
|   unwarpJDialog
\===================================================================*/


/*====================================================================
|   unwarpJFile
\===================================================================*/



/*====================================================================
|   unwarpJFinalAction
\===================================================================*/


/*====================================================================
|   unwarpJImageModel
\===================================================================*/


/*====================================================================
|   unwarpJMask
\===================================================================*/




/*====================================================================
|   unwarpJPointAction
\===================================================================*/


/*====================================================================
|   unwarpJPointHandler
\===================================================================*/


/*====================================================================
|   unwarpJPointToolbar
\===================================================================*/


/*====================================================================
|   unwarpJProgressBar
\===================================================================*/


/*====================================================================
|   unwarpJTransformation
\===================================================================*/

