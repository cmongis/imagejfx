/*
 * Aim: Clustering the data with Density-Based 
 * Scan Algorithm with Noise (DBSCAN)
 */

package knop.ij2plugins.bfs;



import java.util.ArrayList;

import knop.ij2plugins.bfs.useful.MyMaths;
import knop.ij2plugins.bfs.useful.Timer;

public class DBSCAN {
	
	public static void run_DBSCAN(double[][] features_matrix, int nbPoints, int nbStatistics, int nbNewStatistics,
			double[] sType, double[]sClass, double Eps) {

		Timer t = new Timer();
                t.start();

		int no = 1;
		int[] touched = new int[nbPoints];
		ArrayList<Integer> ind;
		ArrayList<Integer> indWhile;

		for(int i=0; i<nbPoints; i++) {
			if(touched[i] == 0) {
				ind =  new ArrayList<Integer>();
				/*
				 * Get/updates the indexes (ind) in which the distance between row number if
				 * featuremat and featuremat is <= Eps
				 */
				MyMaths.distance_Row_In_Matrix(features_matrix, nbPoints, nbStatistics, i, ind, Eps);

				double sum=0;
				for(int ii=0; ii<ind.size(); ii++) {
					sum += ind.get(ii)+1d;
				}

				if(ind.size() > 1 && ind.size() < nbNewStatistics+1 ) {
					sType[i] = 0d;
					sClass[i] = 0d;
				}

				else if( ind.size() == 1) {
					sType[i] = -1d;
					sClass[i] = -1d;
					touched[i] = 1;
				}
				else if( ind.size() >= nbNewStatistics+1 ) {
					sType[i] = 1;
					for(int ii=0; ii<ind.size(); ii++) {
						sClass[ind.get(ii)] = (double) no;
					}

					while( ! ind.isEmpty() ) {

						indWhile = new ArrayList<Integer>();
						int currentRow = ind.get(0);
						MyMaths.distance_Row_In_Matrix(features_matrix, nbPoints, nbStatistics,
								ind.get(0), indWhile, Eps);

						touched[ind.get(0)] = 1;
						ind.remove(0);

						if(indWhile.size() > 1 ) {

							for(int ii=0; ii<indWhile.size(); ii++) {
								sClass[indWhile.get(ii)] = (double) no;
							}	
							//System.out.println("  3.1.1: sum(sClass)=" + sumClass);

							if( indWhile.size() > nbNewStatistics ) {
								sType[ currentRow ] = 1d;
							}
							else {
								sType[ currentRow ] = 0d;
							}

							for( int jj=0; jj<indWhile.size(); jj++) {
								if( touched[indWhile.get(jj)] == 0 ) {
									touched[indWhile.get(jj)] = 1;
									ind.add( indWhile.get(jj) );
									sClass[ indWhile.get(jj) ] = (double) no;
								}
							}

						} 

					} // end while
					no += 1;
				}
				else {
				}
			}
			else {
			}
		} // for nbPoints


		for(int i=0; i<sClass.length; i++) {
			if(sClass[i] == 0) {
				sClass[i] = -1d;
				sType[i] = -1d;
			}
		}

		t.elapsed("dbscan");
			
	} // static run_DBSCAN

}
