package knop.ij2plugins.bfs;


import java.util.ArrayList;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Min;

import knop.ij2plugins.bfs.delaunay_triangulation.Delaunay_Triangulation;
import knop.ij2plugins.bfs.delaunay_triangulation.Point_dt;
import knop.ij2plugins.bfs.useful.Extrapolator;
import knop.ij2plugins.bfs.useful.MyMaths;
import knop.ij2plugins.bfs.useful.MyUtils;
import knop.ij2plugins.bfs.useful.Timer;
import org.apache.commons.math3.special.Gamma;


public class BackgroundEstimator {

	private double[][] I; // original image matrix
	private Double[][] bg; // estimated background image
	private int height;
	private int width;
	private int tileDim; // dimension of each tile
	private boolean DEBUGGING = false;
        Timer t = new Timer();
	
	public BackgroundEstimator(double[][] I, int height, int width, int tileDim) {
              
		this.I = I;
		this.height = height;
		this.width = width;
		this.tileDim = tileDim;
		this.bg = new Double[this.height][this.width];
		
	}
	
	//////////////////////////////////////
	public Double[][] estimate_Background() {
		/*
		 * Estimate the background bg from the image matrix I
		 */
		
		  // the number of tiles of dimension tileDim
		double nbTiles = Math.ceil( (double) (this.height-this.tileDim) / (double) (this.tileDim/2) )
				* Math.ceil( (double) (this.width-this.tileDim) /  (double) (this.tileDim/2) );

		
		int nbStatistics = 5; // the number of statistics computed
		double[] x = new double[(int) nbTiles];
		double[] y = new double[(int) nbTiles];
		double[] z = new double[(int) nbTiles];

		  // Matrix containing all 5 statistics for each tile
		double[][] features_matrix = new double[(int) nbTiles][(int)nbStatistics];
		
		  // Keep track of the current tile
		int tileCounter = 0;
		
		  // tile frame
		double[] subImg;
		
		  // statistics
		double max, min, var, std, mean, kurt, skew;
		
		  // min/max values for each statistics
		Double[] maxValues = {null, null, null, null, null};
		Double[] minValues = {null, null, null, null, null};
		
		int currentIndex;
		//IJ.log("Getting tiling features...\n");
                t.start();
		for(int i=0; i<(this.height-this.tileDim); i+= (int) this.tileDim/2) {

			for(int j=0 ; j<(width-this.tileDim) ; j+= (int) this.tileDim/2) {
				
				subImg = new double[ (int) Math.pow(this.tileDim+1, 2) ];
				currentIndex = 0;
				
				/*
				 * Here we get a tileDim*tileDim square window and compute
				 * statistics from it
				 */
                                
                               
				for(int i2=i; i2<=i+this.tileDim ; i2++) { // height
					for(int j2=j ; j2<=j+this.tileDim ; j2++) {  // width
						subImg[currentIndex++] = (double) this.I[i2][j2];
					}
				}
                                
				max = new Max().evaluate(subImg);
				min = new Min().evaluate(subImg);
				var = new Variance().evaluate(subImg);
				std = new StandardDeviation().evaluate(subImg);
				mean = new Mean().evaluate(subImg);
				kurt = MyMaths.matlabKurtosis(subImg);
				skew = MyMaths.matlabSkewness(subImg);
				
				x[tileCounter] = (j+1) + tileDim/2;
				y[tileCounter] = (i+1) + tileDim/2;
				z[tileCounter] = mean;
				
				features_matrix[tileCounter][0] = std;
				features_matrix[tileCounter][1] = skew;
				features_matrix[tileCounter][2] = max/min;
				features_matrix[tileCounter][3] = kurt;
				features_matrix[tileCounter][4] = var/mean;

				update_MinMax_Statistics(features_matrix, tileCounter, maxValues, minValues); 
				tileCounter ++;

				// 1 cycle here takes about 1 cpu time
			}

		}
                t.elapsed("1 cycle");
		// 1 cycle here takes about 585 cpu time (1388x1040 pixels image, tildDim = 30);

		//features_matrix = ArrayComparator.readArray("compare/featuremat_Matlab.txt");
		//waitInput();
		
		
		if(this.DEBUGGING) {
			/*
			 * Print the array of max and min values
			 */
			System.out.println("\n\tmin -- max -- max-min");
			for(int check=0; check<minValues.length; check++) {
				
				double maxLessMin = maxValues[check]-minValues[check];
				
				System.out.println(Integer.toString(check+1) + ": " + minValues[check] + " -- " + maxValues[check]
						+ " -- " + maxLessMin);
			}
		}
		
		//this.t1 = System.currentTimeMillis();
		//System.out.println("[TIME] ->clustering= " + MyTime.elapsedTime(this.t0, this.t1) + "s.\n");
		//this.t0 = System.currentTimeMillis();

		//ArrayComparator.writeArray(featuremat, "compare_tile=30/featuremat_Java.txt");
		//waitInput();
		
		
		/*
		 * CLUSTERING 
		 */
		//IJ.log("-------------\nClustering " + tileCounter + " points...");

		/*
		 * Get Epsilon.
		 * Aim: Analytical way of estimating neighborhood radius for DBSCAN
		 */
		double epsilon = prod_A_Minus_B(maxValues, minValues) * (maxValues.length+1) 
				* Gamma.gamma(0.5*maxValues.length + 1);
		
		epsilon /= tileCounter *  Math.sqrt( Math.pow(Math.PI, maxValues.length));
		epsilon = Math.pow(epsilon, 1d/maxValues.length);
		
		if(this.DEBUGGING)
			System.out.println("  EPSILON: " + epsilon);
		
		/*
		 * DBSCAN main loop
		 */

		  // Each tile is either from the background or not.
		  // sClass = vector specifying assignment of the i-th object to certain  cluster/tile
		  // sType = vector specifying type of the i-th object 
		double[] sType = new double[(int) nbTiles];
		double[] sClass = new double[(int) nbTiles];
		

		//dbscan(features_matrix, (int) nbTiles, (int) nbStatistics, (int) nbStatistics+1, sType, sClass, Eps);
		
		DBSCAN.run_DBSCAN(features_matrix, (int) nbTiles, nbStatistics, nbStatistics+1, sType, sClass, epsilon);
		//ArrayComparator.writeArray(sType, "compare_tile=30/type_Java.txt");
		//ArrayComparator.writeArray(sClass, "compare_tile=30/classes_Java.txt");
		
		  // remove identical values from sClass
		double[] uniqueClass = MyUtils.unique(sClass); 
		double daclass = 1;
		
		if(this.DEBUGGING) {
			double sumClass=0.0, sumType=0.0;
			for(int i=0; i<sType.length; i++) { 
				sumClass += sClass[i];
				sumType += sType[i];
			}
			System.out.println("size classes: " + sClass.length);
			System.out.println("  sum classes: " + sumClass);
			System.out.println("  mean classes: " + MyUtils.mean(sClass));
			System.out.println("  var classes: " + MyUtils.var(sClass));

			System.out.println("size type: " + sType.length);
			System.out.println("  sum type: " + sumType);
			System.out.println("  mean type: " + MyUtils.mean(sType));
			System.out.println("  var type: " + MyUtils.var(sType));

			double sumUniq=0.0;
			for(int i=0; i<uniqueClass.length; i++) {
				sumUniq += uniqueClass[i];
			}
			System.out.println("size unique: " + uniqueClass.length);
			System.out.println("  sum unique: " + sumUniq);
			System.out.println("  mean unique: " + MyUtils.mean(uniqueClass));
			System.out.println("  var unique: " + MyUtils.var(uniqueClass) );
			System.out.println("\n\n");

		}

		if(uniqueClass.length == 1) {
			daclass = uniqueClass[0];
		}
		else {
			Double classstd; // No need of any array, faster cpu time
			int classstdCounter = 0; 
			double sumClassstd = 0.0;
			int sClassCounter; 
			double minClassstd = 0.0;
			int minIndexClassstd = 0; 
			int nbSuperior;
			double meanFeatures_matrix; 
			
			for(int i=0; i<uniqueClass.length; i++) {
				 nbSuperior = 0;
				 sClassCounter = 0;
				 meanFeatures_matrix = 0;
				 for(int j=0; j<sClass.length; j++) {
					 if(sClass[j] == (double) uniqueClass[i] ) {
						 sClassCounter ++;
						 meanFeatures_matrix += features_matrix[j][0];
						 if( sType[j] == 1) {
							 nbSuperior ++;
						 }
					 }
				 }
				 if(nbSuperior > 200) {
					 classstd = (double) meanFeatures_matrix/(double) sClassCounter;
					 sumClassstd += classstd;
					 classstdCounter++;
					 if(classstdCounter == 2) {
						 minClassstd = classstd;
						 minIndexClassstd = classstdCounter-1;
					 }
					 else if(classstd >= 2) {
						 if( minClassstd > classstd ) {
							 minClassstd = classstd.doubleValue() ;
							 minIndexClassstd = classstdCounter-1;
						 }
					 }
				 }
				 else {
					 classstdCounter++;
					 classstd = null;
				 }
			} // end for each uniqueClass
			
			daclass = uniqueClass[minIndexClassstd] ;
			//daclass = 1;

			if(this.DEBUGGING) {
				System.out.println("length(classstd): " + classstdCounter);
				System.out.println("sum(classstd): " + sumClassstd);
				System.out.println("minClassStd: " + minClassstd);
				System.out.println("  at: " + minIndexClassstd);
				System.out.println("daclass= " + daclass);
			}

		
		} // else
		t.elapsed("clusturing ?");
		
		/*
		 * INTERPOLATION : Compute number of interpolation points
		 */
		
		int interp=0;
		
		  // prepare the set of points for the Delaunay triangulation
		ArrayList<Point_dt> pointsRef = new ArrayList<Point_dt>(); 

		
		for(int i=0; i<sClass.length; i++) {
			if( sClass[i] == daclass && sType[i] == 1) {
				interp ++;
				pointsRef.add(new Point_dt(x[i], y[i], z[i]) );
				  // debug: content checked 
			}
		}
		
                
                t.elapsed("interpolation");
		//this.t1 = System.currentTimeMillis();
		//System.out.println("[TIME] ->interpolation= " + MyTime.elapsedTime(this.t0, this.t1) + "s.\n");
		//this.t0 = System.currentTimeMillis();
		
		//IJ.log("using " + interp + " interpolation points...");

		/*
		 * Estimate bg: value of each pixel depending on their position
		 */
		Delaunay_Triangulation dt = 
				new Delaunay_Triangulation( pointsRef.toArray(new Point_dt[pointsRef.size()] ) );

		int nanCounter = 0; // null values (or Matlab "Not-A-Number" values)
		double nanSum=0d;
		
		for(int h=0; h<this.height; h++) {
			for(int w=0; w<this.width; w++) {
				  // dt: delaunay triangulation
				this.bg[h][w] = dt.zDouble((double) w+1d, (double) h+1d);
				if (this.DEBUGGING && this.bg[h][w] != null ) {
					nanCounter ++;
					nanSum += this.bg[h][w];
				}
			}
		}
		//this.bg = null;
		//this.bg = ArrayComparator.read_Double_Array("compare_tile=30_loadMatlabZI/ZI_Matlab.txt", "null");
		
		//ArrayComparator.writeArray(this.bg, "compare_tile=30/ZI_Java.txt"); // ZI = bg
		
                t.elapsed("build delaunay");
		//this.t1 = System.currentTimeMillis();
		//System.out.println("[TIME] build delaunay= " + MyTime.elapsedTime(this.t0, this.t1) + "s.\n");
		//this.t0 = System.currentTimeMillis();
		
		/*
		 * Extrapolate based on columns (width)
		 * then on rows
		 */
		  // First extrapolation on each column
//                final ForkJoinPool forkJoinPool = new ForkJoinPool();
//                Long counter = forkJoinPool.invoke(new Extrapolator(this.height,
//                        this.width, this.bg, false)
//                ); 
               
		Extrapolator.extrapolate(this.height, this.width, this.bg, false); // for each column
               // this.t1 = System.currentTimeMillis();
                t.elapsed("first interpolation");
		//System.out.println("[TIME] 1st interpolation= " + MyTime.elapsedTime(this.t0, this.t1) + "s.");
		//this.t0 = System.currentTimeMillis();
		
		//ArrayComparator.writeArray(this.bg, "compare_tile=30_loadMatlabZI/ZI_interp1_Java.txt");
		
		  // Second extrapolation
		//Extrapolator.extrapolate(this.width, this.height, this.bg, true); // for each line
//                counter = forkJoinPool.invoke(new Extrapolator(this.width,
//                        this.height, this.bg, false)
//                );
                
                t.elapsed("second interpolation");
		//this.t1 = System.currentTimeMillis();
		//System.out.println("[TIME] 2nd interpolation= " + MyTime.elapsedTime(this.t0, this.t1) + "s.");
		//this.t0 = System.currentTimeMillis();
		
		//ArrayComparator.writeArray(ZI, "compare_tile=30_loadMatlabZI/ZI_interp2_Java.txt");
		//ArrayComparator.writeArray(ZI, "compare_tile=30_loadMatlabZI/ZI_fixed_Java.txt");
		
		//IJ.log("background estimation done");
		
		return this.bg;
	}

	
	////////////////////////////////
	public double prod_A_Minus_B(Double[] x1, Double[]x2) {
		/*
		 * Return the product of the values of x1 subtracted by 
		 * those of x2. The two arrays should have the same dimension.
		 * -> return prod( x1[] - x2[] )
		 */
		if(x1.length != x2.length)
			return 0d;
		
		double prod = 1d;
		for(int i=0; i<x1.length; i++) {
			prod *= x1[i]-x2[i];
		}
		
		return prod;
		
	}	
	
	/////////////////////////////////
	public void update_MinMax_Statistics(double[][] features_matrix, int currentIndex, 
			Double[] maxValues, Double[] minValues) {
		/*
		 *  Update the maximal and minimal values for each of the 5 statistics 
		 *  contained in double[i][5] features_matrix
		 */
		
		for(int i=0; i<features_matrix[currentIndex].length; i++) {
			if(maxValues[i] == null)
				maxValues[i] = features_matrix[currentIndex][i];
			else if (maxValues[i].doubleValue() < features_matrix[currentIndex][i]) {
					maxValues[i] = Double.valueOf(features_matrix[currentIndex][i]);
			}
			
			if(minValues[i] == null)
				minValues[i] = features_matrix[currentIndex][i];
			else {
				if(minValues[i].doubleValue() > features_matrix[currentIndex][i])
					minValues[i] = Double.valueOf(features_matrix[currentIndex][i]);
			}
		}
	}

	

}
