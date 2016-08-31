package knop.ij2plugins.bfs.useful;

//import gnu.trove.list.array.TDoubleArrayList;
//import gnu.trove.list.array.TIntArrayList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MyUtils {

	
//	//////////////////////////////
//	public static double[] ArrayListInt_to_doubleArray(TIntArrayList I) {
//		double[] d = new double[I.size()];
//		for(int i=0; i<I.size(); i++) {
//			d[i] = (double) I.get(i);
//		}
//		return d;
//	}
//	
//	///////////////////////////////
//	public static double[] ArrayListDouble_to_doubleArray(TDoubleArrayList D) {
//		double[] d = new double[D.size()];
//		for(int i=0; i<D.size(); i++) {
//			d[i] = D.get(i);
//		}
//		return d;
//	}
	
	//////////////////////////////
	public static double[] ArrayListInt_to_doubleArray(ArrayList<Integer> I) {
		double[] d = new double[I.size()];
		for(int i=0; i<I.size(); i++) {
			d[i] = (double) I.get(i);
		}
		return d;
	}
	
	/////////////////////////////////
	public static double[] ArrayListDouble_to_doubleArray(ArrayList<Double> D) {
		double[] d = new double[D.size()];
		for(int i=0; i<D.size(); i++) {
			d[i] = D.get(i).doubleValue();
		}
		return d;
	}
	
	/////////////////////////
	public static double[] Double_to_double(Double[] D) {
		double[] d = new double[D.length];
		for(int i=0; i<D.length; i++) {
			d[i] = D[i].doubleValue();
		}
		return d;
	}
	
	//////////////////////////
	public static double[] unique(double[] d) {
		/*
		 * This method removes identical values from an array
		 * of double[].
		 * 
		 * Estimated cpu time: 11
		 */
		Double[] D = new Double[d.length];

		for(int i=0; i<d.length; i++) {
			D[i] = new Double(d[i]);
		}

		Set<Double> set = new HashSet<Double>(Arrays.asList(D));
		set.remove(null); // removes null values
		D = set.toArray(new Double[set.size()]);
		
		double[] unique = new double[D.length];
		
		for(int i=0; i<unique.length; i++) {
			unique[i] = D[i].doubleValue();
		}
		Arrays.sort(unique);

		return unique;
	}
	
	///////////////////
	public static double mean(double[] d) {
		double mean=0.0;
		for(int i=0; i<d.length; i++) {
			mean += (double) d[i];
		}
		return mean/(double)d.length;
	}
	
	//////////////////
	public static double var(double[] d) {
		double mean = mean(d);
        double temp = 0;
        for(double a :d)
            temp += (a-mean)*(a-mean);
        return temp/(double) (d.length-1.0);
	}
	
	/////////////////////////////////////////
	public static void waitInput() {
		System.out.println("\n[waiting input]");
		try{System.in.read();}catch(IOException e) {}
		System.out.println("\n");
	}
	
	public static void sleep(long t) {
		try{Thread.sleep(t);}catch(InterruptedException e) {}
	}
	
}
