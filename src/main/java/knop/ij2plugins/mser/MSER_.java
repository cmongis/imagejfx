package knop.ij2plugins.mser;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

import java.awt.Color;
import java.util.Collection;
import java.util.HashSet;

import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.cursor.special.HyperSphereIterator;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.image.ImagePlusAdapter;
import mpicbg.imglib.type.numeric.RealType;


public class MSER_<T extends RealType<T>> implements PlugIn {

	// the image to process
	private Image<T>  image;
	private Image<T>  regions;
	private ImagePlus imp;
	private ImagePlus reg;
	
	private int delta; // tile dimension
	private int minArea;
	private int maxArea;
	private double maxVariation;
	private double minDiversity;
	private boolean darkToBright;
	private boolean brightToDark;

	private LocalizableByDimCursor<T> regionsCursor;

	private int[] dimensions;

	private MSER<T>   mser;
	private String workingDir;
	
	private long time;
	
	public MSER_() {
		this.setDefaultParameters();
	}
	
	public MSER_(int delta, int minArea, int maxArea, double maxVariation, 
			double minDiversity, boolean darkToBright, boolean brightToDark) {
		
		this.setDelta(delta);
		this.setMinArea(minArea);
		this.setMaxArea(maxArea);
		this.setMaxVariation(maxVariation);
		this.setMinDiversity(minDiversity);
		this.setDarkToBright(darkToBright);
		this.setBrightToDark(brightToDark);
	}
	
	
	public void howTo() {
		ImagePlus i1;// = new ImagePlus();
		//i1 = IJ.openImage("/home/alex/workspace/MSER/colony.tif");
		i1 = IJ.openImage("http://imagej.nih.gov/ij/images/Cell_Colony.jpg");
			// returns an ImagePlus
		//i1.show();
		/*
		ImageConverter ic = new ImageConverter(i1);
		ic.convertToGray32();
		*/
		int nbSlices = i1.getSlice();

		ImagePlus i2 = i1.createImagePlus();

		ImageStack stack = new ImageStack(i1.getDimensions()[0], i1.getDimensions()[1]);
		
		for (int s = 1; s <= nbSlices; s++) {
			ImageProcessor duplProcessor = i1.getStack().getProcessor(s).duplicate();
			stack.addSlice(duplProcessor);
		}
		
		i2.setStack(stack);
		i2.setDimensions(1, nbSlices, 1);
		i2.setTitle("Test " );
		i2.show();
		
		ImageProcessor ip = i2.getProcessor();
		
			// get ImageProcessor from ImagePlus
		i2.setColor(Color.red);
		ImageProcessor ipr = i2.getProcessor();
		ipr.setColor(new Color(255, 0, 9));
		ipr.drawOval(50, 50, 75, 30);
		ipr.fillOval(100, 100, 10, 10);
		i2.updateAndDraw();
		
	}
	
	public void uiParameters() {
	
		/*
		/ ask for parameters
		*/
		
		GenericDialog gd = new GenericDialog("Settings");
		gd.addNumericField("delta:", this.delta, 0);
		gd.addNumericField("min_area:", this.minArea, 0);
		gd.addNumericField("max_area:", this.maxArea, 0);
		gd.addNumericField("max_variation:", this.maxVariation, 2);
		gd.addNumericField("min_diversity:", this.minDiversity, 2);
		gd.addCheckbox("dark_to_bright", this.darkToBright);
		gd.addCheckbox("bright_to_dark", this.brightToDark);
		gd.showDialog();
		if (gd.wasCanceled()) {
			IJ.log("MSER aborted");
			System.exit(0);
		}
	
		IJ.log("after dialog");
		this.delta = (int)gd.getNextNumber();
		this.minArea = (int)gd.getNextNumber();
		this.maxArea = (int)gd.getNextNumber();
		this.maxVariation = (double)gd.getNextNumber();
		this.minDiversity = (double)gd.getNextNumber();
		this.darkToBright = gd.getNextBoolean();
		this.brightToDark = gd.getNextBoolean();
	}

	public static void main(String [] args) {
		// main function used as command line
		MSER_ mser = new MSER_();
		//mser.howTo();

		ImagePlus ip = new ImagePlus();
		
		String PATH = new String("/home/alex/workspace/MSER/");
		String file = new String("colony.tif");
		// To use as an external program
		System.out.println("\n**************************\n" +
				"--main: Beginning program" +
				"\n**********************************\n");
		//IJ.log("IJ log: inside main");
		
		
		IJ.run("Cell Colony (31K)");
		ip = WindowManager.getCurrentImage();
		System.out.println("CurrentImage Title: " + ip.getTitle()) ;
		
		//ip = IJ.openImage(PATH + file);
		//ip = IJ.openImage("/home/alex/workspace/MSER/Test/bg.tif");
		ip = IJ.openImage("/home/alex/NetBeansProjects/MSER/IorgCorr.tif");
		ImageProcessor ipP = ip.getProcessor();
		ipP.invert(); // invert -> bright to dark

		//ip.show();	
		//try{System.in.read();}catch(IOException e) {}

		System.out.println("Current Image Title: " + ip.getTitle()) ;

		
		//IJ.run("MSER_", "delta=5 min=30 max=4000 max=1 min=0.70 dark");
		//IJ.selectWindow("Cell Colony.jpg");
		//IJ.saveAs("Tiff", "/home/alex/workspace/IJ/msers of Cell_Colony.tif");
		
		mser.uiParameters(); // asks the user for the parameters
		mser.runMser(ip);
		
		mser.time = System.currentTimeMillis() - mser.time;
		System.out.println("took " + mser.time);
		
		
		//IJ.save(fin, "/home/alex/workspace/MSER/Test/segment_java_Darkbright.tif");
                
		System.out.println("\n*********************\n" +
				"main: program is over" +
				"\n***************************\n");
		System.out.println("Program will quit in 5 seconds");

		
		IJ.wait(5000);


		System.exit(0);
		
	}
	
	public void run(String args) {
		
		// Main function as a PLUGIN
		IJ.log("--Starting plugin MSER");
		IJ.log("args: ." + args + ".");
		/*
		/ read image
		*/
		imp = WindowManager.getCurrentImage();
			// returns an ImagePlus object
		
		if (imp == null) {
			IJ.showMessage("Please open an image first.");
			return;
		}
		MSER_ mser = new MSER_();

		mser.uiParameters();
		mser.runMser(imp);
		
	} // run()
	
	
	//@SuppressWarnings("deprecation")
	public ImagePlus runMser(ImagePlus i0) {
		System.out.println("inside runMser...");

		//IJ.log("------------------\n" +
		//		"Starting MSER algo\n" +
		//		"--------------------------");
		
		//IJ.log("name: " + i0.getTitle() );
		
		this.time = System.currentTimeMillis();
		
		//darkToBright = true;
		//brightToDark = false;
		image = ImagePlusAdapter.wrap(i0);
		
		// Adapter seems necessary to add/get Dimension methods
			// http://trac.imagej.net/browser/ImgLib/imglib2/ij/src/main/java/net/imglib2/img/ImagePlusAdapter.java?rev=364bb88303070fed6be873af44d6062546b110e2
		dimensions = image.getDimensions();
		int width  = dimensions[0];
		int height = dimensions[1];
		int slices = 1;
		if (dimensions.length > 2)
			slices = dimensions[2];
	
		/*
		 * prepare segmentation image
		 */
		reg = i0.createImagePlus();
	
		ImageStack stack = new ImageStack(width, height);

		for (int s = 1; s <= slices; s++) {
			ImageProcessor duplProcessor = i0.getStack().getProcessor(s).duplicate();
			stack.addSlice("", duplProcessor);
		}
		reg.setStack(stack);
		reg.setDimensions(1, slices, 1);
		if (slices > 1)
			reg.setOpenAsHyperStack(true);

		reg.setTitle("msers of " + i0.getTitle());
		//reg.show();

    
		regions = ImagePlusAdapter.wrap(reg);
		regionsCursor = regions.createLocalizableByDimCursor();
		while (regionsCursor.hasNext()) {
			//****** -> 
			regionsCursor.fwd();
		
			regionsCursor.getType().setReal(0.0);
		}
		
		/*
		 * set up algorithm 
		 */
		mser = new MSER<T>(dimensions, delta, minArea, maxArea, maxVariation, minDiversity);
	
		//Thread processThread = new Thread(new Runnable() {
			//public void run() {
				mser.process(image, darkToBright, brightToDark, regions);
			//}
		//});
		//processThread.start();
	
		// wait for the thread to finish
		//try {
		//	processThread.join();
		//} catch (InterruptedException e) {
		//	processThread.interrupt();
		//}
	
		// visualize MSER centers
		HashSet<Region> topMsers = mser.getTopMsers();
	
		regionsCursor = regions.createLocalizableByDimCursor();
		drawRegions(topMsers);
		//drawRegions(topMsers, reg);
		
		this.time = System.currentTimeMillis();

		System.out.println("inverting final image");
		
		ImagePlus finalImage = reg.duplicate();
		ImageProcessor iproc = finalImage.getProcessor();
		
		for(int w=0; w<this.dimensions[0]; w++) {
			for(int h=0; h<this.dimensions[1]; h++) {
				if(iproc.getPixel(w, h) > 0)
					iproc.putPixel(w, h, 255);
				else 
					iproc.putPixel(w, h, 0);
			}
		}
		
		this.time = System.currentTimeMillis() - this.time;
	
		System.out.println("to binary conversion time: " + this.time);
		
		
		return finalImage;
	}

	private void drawRegions(Collection<Region> msers) {
		// original method
		for (Region mser: msers) {
			drawRegions(mser.children);
			drawRegion(mser, (int)(Math.sqrt(mser.size)/10));
		}
		
	}

	private void drawRegion(Region mser, int radius) {
		/*
		 *  Original method
		 */
		int[] center = new int[dimensions.length];
		for (int d = 0; d < dimensions.length; d++) {
			center[d] = (int)mser.center[d];
			if (center[d] < radius || center[d] > dimensions[d] - radius - 1)
				return;
		}
		// draw a filled circle		
		regionsCursor.setPosition(center);
		HyperSphereIterator<T> sphereIterator = new HyperSphereIterator<T>(regions, regionsCursor, radius);
		while (sphereIterator.hasNext()) {
			sphereIterator.fwd();
			sphereIterator.getType().setReal(255);
		} 
	}
		
	private void drawRegions(Collection<Region> msers, ImagePlus img) {
		
		// Modified method
		for (Region mser: msers) {
			drawRegions(mser.children, img);
			//drawRegion(mser, (int)(Math.sqrt(mser.size)/10));
			//drawRegion2(mser, (int)(Math.sqrt(mser.size)/10), img);
			drawRegion2(mser, 5, img);
			//drawRegion(mser, 5, img);
		}
	}
	
	private void drawRegion2(Region mser, int radius, ImagePlus img) {
		/*
		 * Modified method, draw circles directly from image processor
		 */
		ImageProcessor ip = img.getProcessor();
		img.setColor(Color.white);
		ImageProcessor ipr = img.getProcessor();
		//ipr.setColor(new Color(255, 0, 9));
		ipr.setColor(new Color(255, 255, 255));
		
		//ipr.drawOval((int)mser.center[0] , (int)mser.center[1], radius+5, radius+5);
		ipr.drawOval((int)mser.center[0] - radius/2, (int)mser.center[1] - radius/2, radius+5, radius+5);
		img.updateAndDraw();
	}
	
	public void setDefaultParameters() {
		this.delta = 8;
		this.minArea = 200;
		this.maxArea = 4000;
		this.maxVariation = 1.00;
		this.minDiversity = 0.00;
		this.darkToBright = true;
		this.brightToDark = false;
	}
	
	public void setBrightToDark(boolean x) {
		this.brightToDark = x;
	}
	
	public void setDarkToBright(boolean x) {
		this.darkToBright = x;
	}
	
	public void setMinDiversity(double x) {
		this.minDiversity = x;
	}
	
	public void setMaxVariation(double x) {
		this.maxVariation = x;
	}
	
	public void setDelta(int d) {
		this.delta = d;
	}
	
	public void setMinArea(int x) {
		this.minArea = x;
	}
	
	public void setMaxArea(int x) {
		this.maxArea = x;
	}
	
	
}
