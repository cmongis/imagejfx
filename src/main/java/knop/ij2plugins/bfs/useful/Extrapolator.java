package knop.ij2plugins.bfs.useful;


public class Extrapolator {

	
	////////////////////////////
	public static void extrapolate(int firstDim, int secondDim, Double[][] I,
			boolean invertMatrix) {
		/*
		 * This method extrapolates a matrix from :
		 * 	- the rows if invertZI = true (inverted)
		 *  - the columns h if invertZI = false
		 * 
		 *  If non-inverted, this method sums the non-null values for each columns,
		 *  checks if this sum is higher than the maximal pixel intensity (1),  then
		 *  interpolates/extrapolates each value from this column.
		 *  If inverted, this method does the same to the matrix checking each rows instead
		 * 
		 *  To extrapolate your regular matrix M :
		 *    extrapolate(height, width, M, false);
		 *   
		 *  To extrapolate your inverted matrix M :
		 *    extrapolate(width, height, M, true);
		 *    
		 */
	
		//TIntArrayList indexRow = new TIntArrayList();
		//TDoubleArrayList yExtrap = new TDoubleArrayList();

		double sumCol;
		double[] xVector = new double[firstDim]; // contains the abscissa values
		double[] yVector = new double[firstDim]; // contains the actual pixel value
		int nbPoints;
		int interpCount = 0; 
		MyInterpolation myInterp;
		long interpTime = 0, time=0;;
		
		for(int w=0; w<secondDim; w++) {
			sumCol = 0.0; // New column, initialize to 0.
			
			//xVector = new double[firstDim]; // No need to re-instanciate: nbPoints sets
			//yVector = new double[firstDim];  // the real size limit of x/yVector
												// firstDim is always the maximal size in that case
			
			nbPoints = 0; // number of points per column. Will be passed to the
					// overloaded method MyInterpolation.linearInterpolation();
			
			for(int h=0; h<firstDim; h++) {
				if(! invertMatrix) {
					// Extrapolation for each column (width)
					if(I[h][w] != null ) {
						xVector[nbPoints] = h;
						yVector[nbPoints] = I[h][w];
						nbPoints ++;
						sumCol+= I[h][w];
					}
				}
				else {
					// Extrapolation for each row (height)
					if(I[w][h] != null ) {
						xVector[nbPoints] = h;
						yVector[nbPoints] = I[w][h];
						nbPoints ++;
						sumCol+=I[w][h];
					}
				}
			}
			if(sumCol > 1.0) {
				time = System.currentTimeMillis();
				myInterp = new MyInterpolation(xVector, yVector, null, true);
						// Instanciating this class seems to save ~2seconds
				interpCount++;
				for(int h=0; h<firstDim; h++) {
					// iterates through each non-null ZI[h][w]
					if( ! invertMatrix) {
						I[h][w] = myInterp.linearInterpolation(h, nbPoints);
						/*ZI[h][w] = MyInterpolation.linearInterpolation(
								xVector, yVector, nbPoints, h, true
								);*/
					}
					else {
						I[w][h] = myInterp.linearInterpolation(h, nbPoints);

						/*ZI[w][h] = MyInterpolation.linearInterpolation(
								xVector, yVector, nbPoints, h, true
								);*/
					}
				}
				interpTime += System.currentTimeMillis() - time;
				
			} // if sumCol > 1.0
		}
	
		System.out.println("interpCount: " + interpCount);
		System.out.println(" => interpolation time: " + interpTime);

	}
}
