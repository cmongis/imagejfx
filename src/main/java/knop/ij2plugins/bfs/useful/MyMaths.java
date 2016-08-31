package knop.ij2plugins.bfs.useful;

import java.util.ArrayList;

import org.apache.commons.math3.stat.descriptive.moment.Kurtosis;
import org.apache.commons.math3.stat.descriptive.moment.Skewness;

public class MyMaths {


	public static void distance_Row_In_Matrix(double[][] matrix, int nbPoints, int nbStatistics,
			int currentRow, ArrayList<Integer> indexes, double inferiorBorn) {
		/*
		 * This method computes the distance between 1 row of a matrix and this matrix.
		 * It adds the indexes of the computed distances in the input  arrayList.
		 */
	
		double[] ob = matrix[currentRow];
		double dist; // the euclidean distance between 2 rows of same dimension
		for(int i=0; i<nbPoints; i++) {
			dist = euclideanDistance(ob, matrix[i]);
			if(dist <= inferiorBorn) {
				indexes.add(new Integer(i));
			}
		}

		/*// Used for debugging with matlab code
		for(int j=0; j<indexes.size(); j++) {
			System.out.println(indexes.get(j));
		}
		System.out.println("size: " + indexes.size());
		System.exit(0);
		 */
	}
	
	///////////////////////////
	public static double euclideanDistance(double[] p1, double[] p2) {
		/*
		 * Computes the euclidean distance between 2 arrays of double
		 */
		if(p1.length != p2.length) {
			System.out.println("Warning, your arrays have different dimensions");
			return 0.0;
		}
		double d=0;
		double sum=0;
		for(int i=0; i<p1.length; i++) {
			d = p1[i] - p2[i];
			sum += d * d;
		}
		return Math.sqrt(sum);
	}
	
	//////////////////////////////////
	public static double matlabSkewness(double[] x) {
		/*
		 * Return the skewness computed by matlab 
		 * with the flag 1 instead of flag 0
		 */
		
		double n = (double) x.length;
		double s0 = new Skewness().evaluate(x);
		double s1 = s0 / ( Math.sqrt( n * ( n-1 ) ) / (n-2) ); 

		return s1;
	}
	
	///////////////////////////////////////
	public static double matlabKurtosis(double[] x) {
		/*
		 * Return the Matlab kurtosis computed  with the flag 1 instead of flag 0
		 */
		
		// Mathwork documentation:
		// n= length(x); k0=kurt.evaluate(x)+3; k1 = (6*k0 + 9*n - 5*k0*n + k0*n^2 - 15)/(n^2 - 1) 
		double n = (double) x.length;
		
		double kurt0 = new Kurtosis().evaluate(x) +3;

		//double kurt1 =  1d/(n-1d) * ( ( kurt0 * (n-2d) * (n-3d)) / (n-1d) + 3d*(n-1d));
		double kurt1 =  (6*kurt0 + 9*n - 5*kurt0*n + kurt0*Math.pow(n, 2) - 15)/(Math.pow(n, 2) - 1); 

		return kurt1;
	}

	
	
}
