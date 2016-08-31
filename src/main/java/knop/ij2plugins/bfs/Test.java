package knop.ij2plugins.bfs;

//package bfs;
//
//import ij.IJ;
//import ij.ImagePlus;
//
//import java.io.File;
//import java.util.Arrays;
//
//import mser.MSER_;
//
//
//public class Test {
//
//	protected double tileDim = 30.0;
//	protected double minSizeSplit = 30.0;
//	protected double maxSizeSplit = 1000.0;
//	// MSER parameters
//	protected int lambda = 5;
//	protected int minSizeMSER = 30;
//	protected int maxSizeMSER = 4000;
//	protected double maxVariation = 1.0;
//	protected double maxEcc = 0.7;
//	protected boolean darkToBright = true;
//	protected boolean brightToDark = true;
//	
//	protected final String IMAGE_PATH = "/home/alex/bfs/java_demo_bgCorr.png";
//	protected final String SAVE_FOLDER = "/home/alex/bfs/java_code/java1/";
//	protected final String END_EXTENSION = "_weird_bg.png";
//	
//	public static void main(String [] args) {
//		Test t = new Test();
//		int max = 10000000;
//		int real = 9000000;
//		int count = 0;
//		long time1, time2, time3;
//		double[] x = new double[max];
//		double[] x1=null, x2=null, xclone=null;
//		for(int i=0; i<real; i++) {
//			x[count++] = 3*i;
//		}
//		
//		time1 = System.currentTimeMillis();
//		for(int i=0; i<100; i++) {
//			x1 = Arrays.copyOf(x, count);
//		}
//		time1 = System.currentTimeMillis() - time1;
//
//		time2 = System.currentTimeMillis();
//		for(int i=0; i<100; i++) {
//			x2 = resizeArray(x, count);
//		}
//		time2 = System.currentTimeMillis() - time2;
//		
//		time3 = System.currentTimeMillis();
//		//for(int i=0; i<100; i++) {
//			xclone =x2.clone();
//		//}
//		time3 = System.currentTimeMillis() - time3;
//
//
//		System.out.println("time1: " + time1);
//		System.out.println("time2: " + time2);
//		System.out.println("time cloning: " + time3);
//
//		
//		
//	}
//	
//	public static double[] resizeArray(double[] d, int dim) {
//		if(dim > d.length) {
//			System.out.println("Error: new dimension > previous");
//			System.exit(0);
//		}
//		double[] res = new double[dim];
//		for(int i=0; i<dim; i++) 
//			res[i] = d[i];
//	
//		return res;
//	}
//	
//	public void runBG() {
//		ImagePlus bg;
//		Debug deb;
//		int i=20;
//		//for(int i=10; i!=60; i+=10) {
//			System.out.println("\n====================================\n\tTileDime= " + i + "\n======================================\n");
//			deb = new Debug(IMAGE_PATH
//					, (double)i);
//			
//			bg = deb.startSegmentation();
//			IJ.save(bg, "/home/alex/bfs/bg_debugging/tileDim_variation/bg_tileDim_" + Integer.toString(i) + ".png");
//		//}
//	}
//	
//	public void runBG_tileDim(double beg, double end, double incr) {
//		ImagePlus bg;
//		Debug deb;
//		String folder = SAVE_FOLDER + "tileDime_variation/";
//		File parent = new File(folder);
//		if(! parent.exists())
//			parent.mkdir();
//		for(double i=beg; i<=end; i+=incr) {
//			deb = new Debug(IMAGE_PATH, i);
//			bg = deb.startSegmentation();
//			IJ.save(bg, folder + i + END_EXTENSION);
//		}
//
//	}
//	
//	public void runMSER() {
//		runMSERs_lamdba(0, 10, 1);
//		runMSERs_minSize(10, 50, 10);
//		runMSERs_maxSize(1000, 10000, 1000);
//		runMSERs_maxVariation(0.0, 5.0, 1.0);
//		runMSERs_maxEcc(0.0, 1.0, 0.10);
//		System.out.println("test is over");
//	}
//	
//	
//	public void runMSERs_maxEcc(double beg, double end, double incr) {
//		ImagePlus bg = IJ.openImage(IMAGE_PATH);
//		ImagePlus seg;
//		String folder = SAVE_FOLDER + "maxEcc_variation/";
//		File parent = new File(folder);
//		if(! parent.exists())
//			parent.mkdir();
//		
//		for(double i=beg; i<=end; i+=incr) {
//			MSER_ mser = new MSER_(this.lambda, this.minSizeMSER, 
//					this.maxSizeMSER, this.maxVariation, i, 
//					this.darkToBright, this.brightToDark);
//			seg = new ImagePlus();
//			seg = mser.runMser(bg);
//			
//			IJ.save(seg,  folder + i + END_EXTENSION);
//		}
//	}
//	
//	public void runMSERs_maxVariation(double beg, double end, double incr) {
//		ImagePlus bg = IJ.openImage(IMAGE_PATH);
//		ImagePlus seg;
//		String folder = SAVE_FOLDER + "maxVariation_variation/";
//		File parent = new File(folder);
//		if(! parent.exists())
//			parent.mkdir();
//		
//		for(double i=beg; i<=end; i+=incr) {
//			MSER_ mser = new MSER_(this.lambda, this.minSizeMSER, 
//					this.maxSizeMSER, i, this.maxEcc, 
//					this.darkToBright, this.brightToDark);
//			seg = new ImagePlus();
//			seg = mser.runMser(bg);
//			
//			IJ.save(seg,  folder + i + END_EXTENSION);
//		}
//	}
//	
//	public void runMSERs_maxSize(int beg, int end, int incr) {
//		ImagePlus bg = IJ.openImage(IMAGE_PATH);
//		ImagePlus seg;
//		String folder = SAVE_FOLDER + "maxSize_variation/";
//		File parent = new File(folder);
//		if(! parent.exists())
//			parent.mkdir();
//		
//		for(int i=beg; i<=end; i+=incr) {
//			MSER_ mser = new MSER_(this.lambda, this.minSizeMSER, 
//					i, this.maxVariation, this.maxEcc, 
//					this.darkToBright, this.brightToDark);
//			seg = new ImagePlus();
//			seg = mser.runMser(bg);
//			IJ.save(seg,  folder + i + END_EXTENSION);
//		}
//	}
//	
//	public void runMSERs_minSize(int beg, int end, int incr) {
//		ImagePlus bg = IJ.openImage(IMAGE_PATH);
//		ImagePlus seg;
//		String folder = SAVE_FOLDER + "minSize_variation/";
//		File parent = new File(folder);
//		if(! parent.exists())
//			parent.mkdir();
//		
//		for(int i=beg; i<=end; i+=incr) {
//			MSER_ mser = new MSER_(this.lambda, i, 
//					this.maxSizeMSER, this.maxVariation, this.maxEcc, 
//					this.darkToBright, this.brightToDark);
//			seg = new ImagePlus();
//			seg = mser.runMser(bg);
//			IJ.save(seg,  folder + i + END_EXTENSION);
//		}
//	}
//	
//	public void runMSERs_lamdba(int beg, int end, int incr) {
//		ImagePlus bg = IJ.openImage(IMAGE_PATH);
//		ImagePlus seg;
//		String folder = SAVE_FOLDER + "lambda_variation/";
//		File parent = new File(folder);
//		if(! parent.exists())
//			parent.mkdir();
//		
//		for(int i=beg; i<=end; i+=incr) {
//			MSER_ mser = new MSER_(i, this.minSizeMSER, 
//					this.maxSizeMSER, this.maxVariation, this.maxEcc, 
//					this.darkToBright, this.brightToDark);
//			seg = new ImagePlus();
//			seg = mser.runMser(bg);
//			
//			IJ.save(seg,  folder + i + END_EXTENSION);
//		}
//	}
//	
//	public static void sleep(long t) {
//		try{Thread.sleep(t);}catch(InterruptedException e) {}
//	}
//	
//}
