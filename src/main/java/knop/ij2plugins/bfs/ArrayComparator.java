package knop.ij2plugins.bfs;

import ij.ImagePlus;
import ij.process.ImageProcessor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.util.ArrayList;

public class ArrayComparator {

	protected String path1;
	protected String path2;
	protected double[][] A1;
	protected double[][] A2;
	
	public ArrayComparator(String path1, String path2) {
		this.path1 = path1;
		this.path2 = path2;
		this.A1 = readArray(this.path1);
		this.A2 = readArray(this.path2);
	}
	
	////////////////////////
	public void launchComparison() {
		System.out.println("==================================\n"
				+ " COMPARE " + this.path1 + " --vs-- " + this.path2
				+ "\n======================================\n");
		compare(this.A1, this.A2);
	}
	
	
	/////////////////////////////
	public static void compare(double[][] A1, double[][] A2) {
		
		if(A1.length != A2.length ) {
			System.out.println("Your arrays don't have the same dimension:");
			System.out.println("  A1.length: " + A1.length + "\tA2.length: " + A2.length);
			System.out.println("[/compare] : done\n");
			return;
		}
		System.out.println("[compare] : beginning...");
		
		double mean1=0.0, mean2=0.0;
		double sum1=0.0, sum2=0.0;
		double d1, d2;
		//BigDecimal bd1, bd2; 
		
		ArrayList<String> differences = new ArrayList<String>();
		String dif;
		for(int h=0; h<A1.length; h++) {
			if(A1[h].length != A2[h].length) {
				System.out.println("Your arrays don't have the same dimension");
				System.out.println("  A1["+h+"].length: " + A1[h].length 
						+ "\t A2["+h+"].length: " + A2[h].length);
				return;
			}
			for(int w=0; w<A1[h].length; w++) {
				//bd1 = new BigDecimal(A1[h][w]).setScale(precision, RoundingMode.HALF_EVEN);
				//bd2 = new BigDecimal(A2[h][w]).setScale(precision, RoundingMode.HALF_EVEN);
				d1 = A1[h][w];
				d2 = A2[h][w];
				//D1 = new Double(String.format("%.20g", A1[h][w]));
				//D2 = new Double(String.format("%.20g", A2[h][w]));
				//if( A1[h][w] != A2[h][w]) {
				//if( bd1.doubleValue() != bd2.doubleValue() ) {
					//if(Math.abs( bd1.doubleValue() - bd2.doubleValue() ) > 0.1 ) {
					//}
				//if(D1.doubleValue() != D2.doubleValue() ) {
				if( !DoubleComparator.equals(d1, d2) ) {
						dif = "x A1["+h+"]["+w+"]="+A1[h][w]
								+ "\tA2["+h+"]["+w+"]="+A2[h][w];
						differences.add(dif);
				}
				sum1 += A1[h][w];
				sum2 += A2[h][w];
				
			}  // for width
		} // for height
		
		mean1 = sum1 / ((double)A1.length * (double) A1[0].length );
		mean2 = sum2 / ((double)A1.length * (double) A1[0].length );
		
		System.out.println("  sum(A1)= " + sum1 + "  ---  sum(A2)= " + sum2);
		System.out.println("  mean(A1)= " + mean1 + "  ---  mean(A2)= " + mean2);
		System.out.println("  Nb of differences: " + differences.size() );

		if(! differences.isEmpty() ) { //&& differences.size() < 20) {
			for(int i=0; i<differences.size(); i++) {
				System.out.println("    " + differences.get(i));
			}
		}
		System.out.println("  sum(A1)= " + sum1 + "  ---  sum(A2)= " + sum2);
		System.out.println("  mean(A1)= " + mean1 + "  ---  mean(A2)= " + mean2);
		System.out.println("  Nb of differences: " + differences.size() );


		System.out.println("[/compare] : done\n");
	}
	
	
	public static void writeArray(double[] a, String path) {
		double[][] A = new double[1][a.length];
		A[0] = a;
		writeArray(A, path);
	}
	
	//////////////////////////////////
	public static double[][] readArray(String path) {
		System.out.println("[readArray] " + path + " : beginning...");
		double[][] A;
		int nb_NaN=0;
		
		BufferedReader br = null;
		 
		try {
 
			String[] elements;
			RandomAccessFile raf = new RandomAccessFile(path, "r");

			int nbLines = 0;
			while(raf.readLine() != null)
				nbLines ++;
			
			A = new double[nbLines][];
			
			raf.seek(0);
			String line;
			
			for(int i=0; i<nbLines; i++) {
				elements = raf.readLine().split(" ");
				A[i] = new double[elements.length];
				for(int j=0; j<elements.length; j++) {
					if( elements[j].equals("NaN") || elements[j].equals("null")) {
						A[i][j] = 0.0 ;
						nb_NaN++;
					}
					else {
						A[i][j] = new Double(elements[j]).doubleValue() ;
					}
				}
			}
			raf.close();
 
		} catch (IOException e) {
			e.printStackTrace();
			A = null;
		} 
		System.out.println("  size of array: " + A.length + "x" + A[0].length);
		System.out.println("  NaN: " + nb_NaN + " / " + Integer.toString(A.length*A[0].length));
		System.out.println("[/readArray] " + path + " : done\n");
		return A;
	}
	
	
	//////////////////////////////////
	public static Double[][] read_Double_Array(String path, String nullValue) {
		Double ifNull;
		if( nullValue.equals("0")) {
			ifNull = 0.0;
		}
		else if( nullValue.equals("1")) {
			ifNull = 0.0;
		}
		else {
			ifNull = null;
		}
		System.out.println("[readArray] " + path + " : beginning...");
		Double[][] A;
		int nb_NaN=0;
		
		BufferedReader br = null;
		 
		try {
			String[] elements;
			RandomAccessFile raf = new RandomAccessFile(path, "r");

			int nbLines = 0;
			while(raf.readLine() != null)
				nbLines ++;
			
			A = new Double[nbLines][];
			
			raf.seek(0);
			String line;
			
			for(int i=0; i<nbLines; i++) {
				elements = raf.readLine().split(" ");
				A[i] = new Double[elements.length];
				for(int j=0; j<elements.length; j++) {
					if( elements[j].equals("NaN") || elements[j].equals("null")) {
						A[i][j] = ifNull ;
						nb_NaN++;
					}
					else {
						A[i][j] = new Double(elements[j]).doubleValue() ;
					}
				}
			}
			raf.close();
 
		} catch (IOException e) {
			e.printStackTrace();
			A = null;
		} 
		System.out.println("  size of array: " + A.length + "x" + A[0].length);
		System.out.println("  NaN: " + nb_NaN + " / " + Integer.toString(A.length*A[0].length));
		System.out.println("[/readArray] " + path + " : done\n");
		return A;
	}
		
	
	//////////////////////////////////
	public static void writeArray(ImagePlus ip, String path) {
		Writer writer = null;
		System.out.println("[writeArray] " + path + " : beginning...");
		try {
		    writer = new BufferedWriter(new OutputStreamWriter(
		          new FileOutputStream(path), "utf-8"));

		    ImageProcessor iproc = ip.getProcessor(); 
		    for(int h=0; h< ip.getHeight(); h++) {
				for(int w=0; w<ip.getWidth(); w++) {
					writer.write( Float.toString( iproc.getPixelValue(w, h) ) + " " );
				}
				if(h<ip.getHeight()-1) 
					writer.write("\n");
			}
		    
		} catch (IOException ex) {
		  // report
			ex.printStackTrace();
		} finally {
		   try {writer.close();} catch (Exception ex) {}
		}
		
		System.out.println("  " + ip.getHeight() + "x" + ip.getWidth());
		System.out.println("[/writeArray] " + path + " : done\n");

	}
	
	//////////////////////////////////
	public static void writeArray(double [][] A, String path) {
		Writer writer = null;
		System.out.println("[writeArray] " + path + " : beginning...");

		try {
		    writer = new BufferedWriter(new OutputStreamWriter(
		          new FileOutputStream(path), "utf-8"));
		    for(int i=0; i<A.length;i++) {
				for(int j=0; j<A[0].length; j++) {
				    writer.write(Double.toString( A[i][j] ) + " ");
				}
				if( i<A.length-1) {
				    writer.write("\n");
				}
			}
		} catch (IOException ex) {
		  // report
			ex.printStackTrace();
		} finally {
		   try {writer.close();} catch (Exception ex) {}
		}
		
		System.out.println("  " + A.length + "x" + A[0].length);
		System.out.println("[/writeArray] " + path + " : done\n");

	}
	
	//////////////////////////////////
	public static void writeArray(Double [][] A, String path) {
		Writer writer = null;
		System.out.println("[writeArray] " + path + " : beginning...");

		try {
		    writer = new BufferedWriter(new OutputStreamWriter(
		          new FileOutputStream(path), "utf-8"));
		    for(int i=0; i<A.length;i++) {
				for(int j=0; j<A[0].length; j++) {
					if( A[i][j] == null) 
						writer.write( "NaN" + " ");
					else
						writer.write( A[i][j].toString() + " ");
				}
				if( i<A.length-1) {
				    writer.write("\n");
				}
			}
		} catch (IOException ex) {
		  // report
			ex.printStackTrace();
		} finally {
		   try {writer.close();} catch (Exception ex) {}
		}
		System.out.println("  " + A.length + "x" + A[0].length);
		System.out.println("[/writeArray] " + path + " : done\n");

	}
	
	
	////////////////////////
	public static void Compare(double[][] a1, String path_a1) {
		
	}
	
	///////////////////////////
	public static void Compare(String p1, String p2) {
		
	}
	
	
	////////////////////////////
	public static void main(String [] args) {
		String FOLDER = "compare_tile=30/";

		System.out.println("-------------------------\n" 
				+ "    Matlab \t\tJava\n-------------------------------");
		
		ArrayComparator ac;
		
		/*
		ac = new ArrayComparator(FOLDER + "Iorg_Matlab.txt", FOLDER + "/Iorg_Java.txt");
		ac.launchComparison();
		MyUtils.waitInput();
		
		System.out.println("---------");
		
		ac = new ArrayComparator(FOLDER + "IorgDouble_Matlab.txt", FOLDER + "IorgDouble_Java.txt");
		ac.launchComparison();
		MyUtils.waitInput();
		System.out.println("---------");
		*/
		/*
		ac = new ArrayComparator(FOLDER + "featuremat_Matlab.txt", FOLDER + "featuremat_Java.txt");
		ac.launchComparison();
		MyUtils.waitInput();

		ac = new ArrayComparator(FOLDER + "type_Matlab.txt", FOLDER + "type_Java.txt");
		ac.launchComparison();
		MyUtils.waitInput();

		ac = new ArrayComparator(FOLDER + "classes_Matlab.txt", FOLDER + "classes_Java.txt");
		ac.launchComparison();
		MyUtils.waitInput();

		
		ac = new ArrayComparator(FOLDER + "xDel_Matlab.txt", FOLDER + "xDel_Java.txt");
		ac.launchComparison();
		MyUtils.waitInput();

		ac = new ArrayComparator(FOLDER + "yDel_Matlab.txt", FOLDER + "yDel_Java.txt");
		ac.launchComparison();
		MyUtils.waitInput();

		
		ac = new ArrayComparator(FOLDER + "zDel_Matlab.txt", FOLDER + "zDel_Java.txt");
		ac.launchComparison();
		MyUtils.waitInput();
		
		ac = new ArrayComparator(FOLDER + "ZI_Matlab.txt", FOLDER + "ZI_Java.txt");
		ac.launchComparison();
		MyUtils.waitInput();

		ac = new ArrayComparator(FOLDER + "ZI_interp1_Matlab.txt", FOLDER + "ZI_interp1_Java.txt");
		ac.launchComparison();
		MyUtils.waitInput();

		ac = new ArrayComparator(FOLDER + "ZI_interp2_Matlab.txt", FOLDER + "ZI_interp2_Java.txt");
		ac.launchComparison();
		MyUtils.waitInput();

		ac = new ArrayComparator(FOLDER + "ZI_fixed_Matlab.txt", FOLDER + "ZI_fixed_Java.txt");
		ac.launchComparison();
		MyUtils.waitInput();
*/
		
		/*
		ac = new ArrayComparator(FOLDER + "ZI_interp1_Matlab.txt", 
				"/home/alex/workspace/Bright_Field_Segmentation/compare_tile=30_loadMatlabZI/" + "ZI_interp1_Java.txt");
		ac.launchComparison();
		//MyUtils.waitInput();
*/
		
		/*
		ac = new ArrayComparator(FOLDER + "ZI_interp2_Matlab.txt", 
				"/home/alex/workspace/Bright_Field_Segmentation/compare_tile=30_loadMatlabZI/" + "ZI_interp2_Java.txt");
		ac.launchComparison();
		MyUtils.waitInput();

		ac = new ArrayComparator(FOLDER + "ZI_fixed_Matlab.txt", 
				"/home/alex/workspace/Bright_Field_Segmentation/compare_tile=30_loadMatlabZI/" + "ZI_fixed_Java.txt");
		ac.launchComparison();
		MyUtils.waitInput();
		*/
		/*
		ac = new ArrayComparator("/home/alex/workspace/Bright_Field_Segmentation/compare_tile=30_loadMatlabZI/lowInterp_Matlab.txt", 
				"/home/alex/workspace/Bright_Field_Segmentation/compare_tile=30_loadMatlabZI/" + "lowInterp_Java.txt");
		ac.launchComparison();
		*/
		ac = new ArrayComparator("/home/alex/workspace/Bright_Field_Segmentation/compare_tile=30_loadMatlabZI/ZI_lowInterp1_Matlab.txt", 
				"/home/alex/workspace/Bright_Field_Segmentation/compare_tile=30_loadMatlabZI/" + "ZI_lowInterp1_Java.txt");
		ac.launchComparison();
		System.out.println("---------");
		
	}
}
