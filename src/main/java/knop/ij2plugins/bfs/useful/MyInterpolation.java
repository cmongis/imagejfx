/*
 * This file is part of JPARSEC library.
 * 
 * (C) Copyright 2006-2009 by T. Alonso Albi - OAN (Spain).
 *  
 * Project Info:  http://conga.oan.es/~alonso/jparsec/jparsec.html
 * 
 * JPARSEC library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * JPARSEC library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
//package org.asterope.util;
package knop.ij2plugins.bfs.useful;


import java.io.Serializable;
import java.util.ArrayList;


/**
 * Tools for linear and spline interpolation.
 * 
 * @author T. Alonso Albi - OAN (Spain)
 * @version 1.0
 */
public class MyInterpolation implements Serializable
{


	private static final long serialVersionUID = -4332820361309204455L;
	
	private double x_val[], y_val[], z_val[];
	private boolean allowExtrapolation;
	
	/**
	 * Constructor for a 2d interpolation. Points are sorted
	 * in abscisa crescent order, and repeated points are eliminated.
	 * @param x X values.
	 * @param y Y values.
	 * @param allowExtrapolation True to allow extrapolation.
	 */
	public MyInterpolation(double x[], double y[], boolean allowExtrapolation) {
		if (x != null && y != null) {
			ArrayList<double[]> l = sortInCrescent(x, y, true);
				// You do NOT want to sort: Too much time lost actually
			x_val = l.get(0);
			y_val = l.get(1);
		}
		this.allowExtrapolation = allowExtrapolation;
	}
	/**
	 * Constructor for a 3d interpolation.
	 * @param x X values.
	 * @param y Y values.
	 * @param z Z values.
	 * @param allowExtrapolation True to allow extrapolation.
	 */
	public MyInterpolation(double x[], double y[], double z[], boolean allowExtrapolation) {
		if (x != null) x_val = x;//x.clone();  // No need to clone because we don't modify the arrays 
		if (y != null) y_val = y;//y.clone();
		if (z != null) z_val = z.clone();
		this.allowExtrapolation = allowExtrapolation;		
	}
	
	
	/*
	 * Overloaded method.
	 * Specify the number of points 
	 * 	-> avoid the cost at copying/resizing array 
	 */
	public double linearInterpolation(double x_point, int nbPoints) {

		double x_prev, y_prev, x_next, y_next, slope;
		int v = nbPoints;

		// Get number of points
		//v = x_val.length;

		// Obtain previous and next point
		x_prev = x_point;
		x_next = x_point;
		y_prev = 0.0;
		y_next = 0.0;

		int iprev = 0, inext = 0;
		for (int i = 0; i < v; i++)
		{
			if (x_val[i] == x_point)
				return y_val[i];

			if (x_val[i] < x_point && (x_val[i] > x_prev || x_prev == x_point))
			{
				x_prev = x_val[i];
				y_prev = y_val[i];
				iprev = i;

			}

			if (x_val[i] > x_point && (x_val[i] < x_next || x_next == x_point))
			{
				x_next = x_val[i];
				y_next = y_val[i];
				inext = i;
			}

		}

		// Correct values if no previous or next point exist, if extrapolation
		// is allowed
		if (allowExtrapolation)
		{
			if (x_prev == x_point)
			{
				x_prev = x_val[inext + 1];
				y_prev = y_val[inext + 1];
			}
			if (x_next == x_point)
			{
				x_next = x_val[iprev - 1];
				y_next = y_val[iprev - 1];
			}
		} else
		{
			if (x_prev == x_point || x_next == x_point)
			{
//				System.out.println(x_point + "/" + getMinimumValue(x_val) + "/" + DataSet
//						.getMaximumValue(x_val));
				throw new IllegalArgumentException(
						"interpolation point out of range, extrapolation required.");
			}
		}

		// Interpolate
		slope = 0.0;
		if (!(x_next == x_prev))
		{
			slope = (y_next - y_prev) / (x_next - x_prev);
		}
		double y_point = y_prev + slope * (x_point - x_prev);

		return y_point;

	}
	
	/**
	 * Linear interpolation method.
	 * @param x_point Interpolation point.
	 * @return The interpolated value.
	 */
	public double linearInterpolation(double x_point)
			
	{

		double x_prev, y_prev, x_next, y_next, slope;
		int v;

		// Get number of points
		v = x_val.length;

		// Obtain previous and next point
		x_prev = x_point;
		x_next = x_point;
		y_prev = 0.0;
		y_next = 0.0;

		int iprev = 0, inext = 0;
		for (int i = 0; i < v; i++)
		{
			if (x_val[i] == x_point)
				return y_val[i];

			if (x_val[i] < x_point && (x_val[i] > x_prev || x_prev == x_point))
			{
				x_prev = x_val[i];
				y_prev = y_val[i];
				iprev = i;

			}

			if (x_val[i] > x_point && (x_val[i] < x_next || x_next == x_point))
			{
				x_next = x_val[i];
				y_next = y_val[i];
				inext = i;
			}

		}

		// Correct values if no previous or next point exist, if extrapolation
		// is allowed
		if (allowExtrapolation)
		{
			if (x_prev == x_point)
			{
				x_prev = x_val[inext + 1];
				y_prev = y_val[inext + 1];
			}
			if (x_next == x_point)
			{
				x_next = x_val[iprev - 1];
				y_next = y_val[iprev - 1];
			}
		} else
		{
			if (x_prev == x_point || x_next == x_point)
			{
//				System.out.println(x_point + "/" + getMinimumValue(x_val) + "/" + DataSet
//						.getMaximumValue(x_val));
				throw new IllegalArgumentException(
						"interpolation point out of range, extrapolation required.");
			}
		}

		// Interpolate
		slope = 0.0;
		if (!(x_next == x_prev))
		{
			slope = (y_next - y_prev) / (x_next - x_prev);
		}
		double y_point = y_prev + slope * (x_point - x_prev);

		return y_point;

	}

	/**
	 * Spline interpolation method, up to third order of accuracy. This method
	 * requires that the points are sorted in abscisa crescent order.
	 * <P>
	 * Reference:
	 * <P>
	 * <I>Basic Scientific Subroutines</I>, F. R. Ruckdeschel. 1982.
	 * 
	 * @param px Interpolation point. Must be between minimum and maximum value
	 *        of x array, or equal to one of them.
	 * @return The interpolated value.
	 */
	public double splineInterpolation(double px)
	{

		double z[] = new double[x_val.length + 3];
		double mm[] = new double[x_val.length + 3];
		double cqc, a, B, py;
		int i, v;

		v = x_val.length - 1;
		if (px == x_val[v])
			return y_val[v]; // Solve ArrayIndexOutOfBounds when px = max x.
		for (i = 0; i < v; i++)
		{
			cqc = x_val[i + 1] - x_val[i];
			mm[i + 2] = (y_val[i + 1] - y_val[i]) / cqc;
		}

		mm[v + 2] = 2.0 * mm[v + 1] - mm[v];
		mm[v + 3] = 2.0 * mm[v + 2] - mm[v + 1];
		mm[2] = 2.0 * mm[3] - mm[4];
		mm[1] = 2.0 * mm[2] - mm[3];

		for (i = 0; i < v; i++)
		{
			a = Math.abs(mm[i + 3] - mm[i + 2]);
			B = Math.abs(mm[i + 1] - mm[i]);
			if ((a + B) == 0.0)
			{
				z[i] = (mm[i + 2] + mm[i + 1]) / 2.0;
			} else
			{
				cqc = 1.0 + B;
				if (cqc == 0.0)
				{
					cqc = 1.0E-30;
				}
				z[i] = (a * mm[i + 1] + B * mm[i + 2]) / cqc;
			}
		}
		i = 1;

		while (px >= x_val[i] && i < v)
		{
			i = i + 1;
		}

		i = i - 1;
		B = x_val[i + 1] - x_val[i];
		a = px - x_val[i];
		py = y_val[i] + z[i] * a + (3.0 * mm[i + 2] - 2.0 * z[i] - z[i + 1]) * a * a / B;
		py = py + (z[i] + z[i + 1] - 2.0 * mm[i + 2]) * a * a * a / (B * B);

		return py;
	}

	/**
	 * Linear interpolation method.
	 * @param x_val X values.
	 * @param y_val Y values.
	 * @param x_point Interpolation point.
	 * @param allowExtrapolation True to allow extrapolation.
	 * @return The interpolated value.
	 * If allowExtrapolation is false and the point is
	 *         outside the range.
	 */
	
	
	
	public static double linearInterpolation(double x_val[], double y_val[], int nbPoints, double x_point, boolean allowExtrapolation)
	
	{

		double x_prev, y_prev, x_next, y_next, slope;
		int v = nbPoints;

		// Get number of points
		//v = x_val.length;
		

		// Obtain previous and next point
		x_prev = x_point;
		x_next = x_point;
		y_prev = 0.0;
		y_next = 0.0;

		int iprev = 0, inext = 0;
		for (int i = 0; i < v; i++)
		{
			if (x_val[i] == x_point)
				return y_val[i];

			if (x_val[i] < x_point && (x_val[i] > x_prev || x_prev == x_point))
			{
				x_prev = x_val[i];
				y_prev = y_val[i];
				iprev = i;

			}

			if (x_val[i] > x_point && (x_val[i] < x_next || x_next == x_point))
			{
				x_next = x_val[i];
				y_next = y_val[i];
				inext = i;
			}

		}

		// Correct values if no previous or next point exist, if extrapolation
		// is allowed
		if (allowExtrapolation)
		{
			if (x_prev == x_point)
			{
				x_prev = x_val[inext + 1];
				y_prev = y_val[inext + 1];
			}
			if (x_next == x_point)
			{
				x_next = x_val[iprev - 1];
				y_next = y_val[iprev - 1];
			}
		} else
		{
			if (x_prev == x_point || x_next == x_point)
			{
				//System.out.println(x_point + "/" + getMinimumValue(x_val) + "/" + DataSet
				//	.getMaximumValue(x_val));
				throw new IllegalArgumentException(
						"interpolation point out of range, extrapolation required.");
			}
		}

		// Interpolate
		slope = 0.0;
		if (!(x_next == x_prev))
		{
			slope = (y_next - y_prev) / (x_next - x_prev);
		}
		double y_point = y_prev + slope * (x_point - x_prev);

		return y_point;
	}	
	
	/////////////////////////////////////////////////////
	public static double linearInterpolation(double x_val[], double y_val[], double x_point, boolean allowExtrapolation)
	
	{

		double x_prev, y_prev, x_next, y_next, slope;
		int v;

		// Get number of points
		v = x_val.length;

		// Obtain previous and next point
		x_prev = x_point;
		x_next = x_point;
		y_prev = 0.0;
		y_next = 0.0;

		int iprev = 0, inext = 0;
		for (int i = 0; i < v; i++)
		{
			if (x_val[i] == x_point)
				return y_val[i];

			if (x_val[i] < x_point && (x_val[i] > x_prev || x_prev == x_point))
			{
				x_prev = x_val[i];
				y_prev = y_val[i];
				iprev = i;

			}

			if (x_val[i] > x_point && (x_val[i] < x_next || x_next == x_point))
			{
				x_next = x_val[i];
				y_next = y_val[i];
				inext = i;
			}

		}

		// Correct values if no previous or next point exist, if extrapolation
		// is allowed
		if (allowExtrapolation)
		{
			if (x_prev == x_point)
			{
				x_prev = x_val[inext + 1];
				y_prev = y_val[inext + 1];
			}
			if (x_next == x_point)
			{
				x_next = x_val[iprev - 1];
				y_next = y_val[iprev - 1];
			}
		} else
		{
			if (x_prev == x_point || x_next == x_point)
			{
//				System.out.println(x_point + "/" + getMinimumValue(x_val) + "/" + DataSet
//						.getMaximumValue(x_val));
				throw new IllegalArgumentException(
						"interpolation point out of range, extrapolation required.");
			}
		}

		// Interpolate
		slope = 0.0;
		if (!(x_next == x_prev))
		{
			slope = (y_next - y_prev) / (x_next - x_prev);
		}
		double y_point = y_prev + slope * (x_point - x_prev);

		return y_point;
	}
	
	////////////////////////////////////////
	
	public static Double linearInterpolation(Double x_val[], Double y_val[], double x_point, boolean allowExtrapolation)
			
	{

		Double x_prev, y_prev, x_next, y_next, slope;
		int v;

		// Get number of points
		v = x_val.length;

		// Obtain previous and next point
		x_prev = x_point;
		x_next = x_point;
		y_prev = 0.0;
		y_next = 0.0;

		int iprev = 0, inext = 0;
		for (int i = 0; i < v; i++)
		{
			if (x_val[i] == x_point)
				return y_val[i];

			if (x_val[i] < x_point && (x_val[i] > x_prev || x_prev == x_point))
			{
				x_prev = x_val[i];
				y_prev = y_val[i];
				iprev = i;

			}

			if (x_val[i] > x_point && (x_val[i] < x_next || x_next == x_point))
			{
				x_next = x_val[i];
				y_next = y_val[i];
				inext = i;
			}

		}

		// Correct values if no previous or next point exist, if extrapolation
		// is allowed
		if (allowExtrapolation)
		{
			if (x_prev == x_point)
			{
				x_prev = x_val[inext + 1];
				y_prev = y_val[inext + 1];
			}
			if (x_next == x_point)
			{
				x_next = x_val[iprev - 1];
				y_next = y_val[iprev - 1];
			}
		} else
		{
			if (x_prev == x_point || x_next == x_point)
			{
//				System.out.println(x_point + "/" + getMinimumValue(x_val) + "/" + DataSet
//						.getMaximumValue(x_val));
				throw new IllegalArgumentException(
						"interpolation point out of range, extrapolation required.");
			}
		}

		// Interpolate
		slope = 0.0;
		if (!(x_next == x_prev))
		{
			slope = (y_next - y_prev) / (x_next - x_prev);
		}
		double y_point = y_prev + slope * (x_point - x_prev);

		return y_point;
	}

	/**
	 * Linear interpolation method, but applying natural logarithm to x and y
	 * values previous to calculations.
	 * <P>
	 * 
	 * @param x_point Interpolation point.
	 * @return The interpolated value.
	 *  If allowExtrapolation is false and the point is
	 *         outside the range.
	 */
	public double linearInterpolationInLogScale(double x_point) 
	{
		double x[] = new double[x_val.length];
		double y[] = new double[x_val.length];
		for (int i = 0; i < x_val.length; i++)
		{
			x[i] = Math.log(x_val[i]);
			y[i] = Math.log(y_val[i]);
		}
		double px = Math.log(x_point);
		double py = Math.exp(MyInterpolation.linearInterpolation(x, y, px, allowExtrapolation));
		
		return py;
	}

	/**
	 * Linear interpolation method in 3d. It is supposed that the x array contains
	 * several repeated points, each of them with a different value for the y
	 * coordinate, and also with some value for the z coordinates. Obviously, now 
	 * the interpolation 'point' is the plain defined by an (x, z) point.
	 * 
	 * @param x_point X interpolation point.
	 * @param z_point Z interpolation point.
	 * @return The interpolated value.
	 *  If an error occurs.
	 */
	public double linearInterpolation3d(double x_point, double z_point)
			
	{
		// Reduce to simple case if we have no z values
		if (z_val == null) return this.linearInterpolation(x_point);
		
		double min = getMinimumValue(x_val);
		double max = getMaximumValue(x_val);
		
		if (x_point < min || x_point > max) 
			throw new IllegalArgumentException("the interpolation x point is outside the x domain.");
			
		// Obtain inmediatelly previous and later x values from the x interpolation point
		double lowerX = 0.0, greaterX = 0.0;
		boolean lower = false, greater = false;
        for (double aX_val1 : x_val) {
            if (aX_val1 <= x_point && (aX_val1 > lowerX || !lower)) {
                lower = true;
                lowerX = aX_val1;
            }
            if (aX_val1 >= x_point && (aX_val1 < greaterX || !greater)) {
                greater = true;
                greaterX = aX_val1;
            }
        }

		// If both are equal, we reduce it again to the simple case (2d)
		if (greaterX == lowerX) {
			int index = 0;
            for (double aX_val : x_val) {
                if (aX_val == lowerX) index++;
            }
			double newY[] = new double[index];
			double newZ[] = new double[index];
			index = -1;
			for (int i=0; i<x_val.length; i++)
			{
				if (x_val[i] == lowerX) {
					index ++;
					newY[index] = y_val[i];
					newZ[index] = z_val[i];
				}
			}
			return MyInterpolation.linearInterpolation(newZ, newY, z_point, allowExtrapolation);			
		}

		// Obtain number of z (and y) values available for the inmediatelly lower and later x values
		int nlow = 0, nup = 0;
        for (double aX_val : x_val) {
            if (aX_val == lowerX) nlow++;
            if (aX_val == greaterX) nup++;
        }
		
		// Obtain y and z values for those plains x = lowerX, x = greaterX
		double lowy[] = new double[nlow];
		double lowz[] = new double[nlow];
		double upy[] = new double[nup];
		double upz[] = new double[nup];
		nlow = -1;
		nup = -1;
		for (int i=0; i<x_val.length; i++)
		{
			if (x_val[i] == lowerX) {
				nlow ++;
				lowy[nlow] = y_val[i];
				lowz[nlow] = z_val[i];
			}
			if (x_val[i] == greaterX) {
				nup ++;
				upy[nup] = y_val[i];
				upz[nup] = z_val[i];
			}
		}		

		if (nlow < 1 || nup < 1) 
			throw new IllegalArgumentException("the z domain axis contains no points.");
		
		// Obtain the maximum and minimum values of z in those 
		int greatest_z1 = (int) getMaximumValue(lowz);
		int greatest_z2 = (int) getMaximumValue(upz);
		int greatest_z = greatest_z1;
		if (greatest_z2 > greatest_z1) greatest_z = greatest_z2;

		int lowest_z1 = (int) getMinimumValue(lowz);
		int lowest_z2 = (int) getMinimumValue(upz);
		int lowest_z = lowest_z1;
		if (lowest_z2 > lowest_z1) lowest_z = lowest_z2;

		// Sample the z axis using enough points
		int np = 2 * (int) getMaximumValue(new double[] {nlow, nup});
		
		// Reduce the z axis, using the same values for both  (they are
		// initially supposed to be different)
		double zz[] = new double[np];
		double yy[] = new double[np];
		double frac = (x_point - lowerX) / (greaterX - lowerX);
		for (int i=0; i<np; i++)
		{
			double z = lowest_z + (double) i * (greatest_z - lowest_z) / ((double) (np - 1));
			zz[i] = z;

			double yy1 = MyInterpolation.linearInterpolation(lowz, lowy, z, true);			
			double yy2 = MyInterpolation.linearInterpolation(upz, upy, z, true);

			// Interpolate linearly using a weight defined by the distance of the
			// x interpolation point to both lowerX and greaterX
			yy[i] = yy1 + frac * (yy2 - yy1); 
		}

		return MyInterpolation.linearInterpolation(zz, yy, z_point, allowExtrapolation);			
	}

	/**
	 * Linear interpolation method in 3d in log scale. It is supposed that the x array contains
	 * several repeated points, each of them with a different value for the y
	 * coordinate, and also with some value for the z coordinates. Obviously, now 
	 * the interpolation 'point' is the plain defined by an (x, z) point.
	 * 
	 * @param x_point X interpolation point.
	 * @param z_point Z interpolation point.
	 * @return The interpolated value.
	 * If an error occurs.
	 */
	public double linearInterpolation3dInLogScale(double x_point, double z_point)
			
	{
		double bx[] = x_val.clone();
		double by[] = y_val.clone();
		double bz[] = z_val.clone();
		
		for (int i = 0; i < x_val.length; i++)
		{
			x_val[i] = Math.log(x_val[i]);
			y_val[i] = Math.log(y_val[i]);
			z_val[i] = Math.log(z_val[i]);
		}
		double px = Math.log(x_point);
		double pz = Math.log(z_point);
		double py = Math.exp(linearInterpolation3d(px, pz));
		
		x_val = bx;
		y_val = by;
		z_val = bz;
		return py;		
	}
	

	/**
	 * Obtains minimum value of an array.
	 * 
	 * @param v Array.
	 * @return Minimum value.
	 */
	public static double getMinimumValue(double v[]) {
		if (v == null) throw new IllegalArgumentException("no points in the input data.");
		if (v.length < 1) throw new IllegalArgumentException("no points in the input data.");
		
		double min = v[0];
		if (v.length > 1) {
			for (int i = 1; i < v.length; i++)
			{
				if (v[i] < min)
					min = v[i];
			}
		}
		
		return min;
	}

	/**
	 * Obtains maximum value of an array.
	 * 
	 * @param v Array.
	 * @return Maximum value. -1 is returned as an invalid result.
	 */
	public static double getMaximumValue(double v[]){
		if (v == null) throw new IllegalArgumentException("invalid input.");
		if (v.length < 1) throw new IllegalArgumentException("invalid input.");
		double max = v[0];
		if (v.length > 1) {
			for (int i = 1; i < v.length; i++)
			{
				if (v[i] > max)
					max = v[i];
			}
		}
		
		return max;
	}
	
	/**
	 * A method for reordering points in x crescent order. This method can also
	 * eliminates repeated x points.
	 * 
	 * @param x_val X set of values.
	 * @param y_val Y set of values
	 * @param eliminateRepeatedPoints True to eliminate repeated x points.
	 * @return ArrayList with X and Y sets of values.
	 */
	public static ArrayList<double[]> sortInCrescent(double[] x_val, double[] y_val, boolean eliminateRepeatedPoints)
	{
		// Now lets re-order the points in abscisa crescent order
		int size = x_val.length;
		int min_value = -1;
		int flag_x[] = new int[size];
		for (int i = 1; i < size; i++)
		{
			flag_x[i] = 0;
		}
		double ordered_x[] = new double[size];
		double ordered_y[] = new double[size];
		int np = -1;
		for (int j = 0; j < size; j++)
		{
			min_value = -1;
			for (int i = 0; i < size; i++)
			{
				if (flag_x[i] == 0)
				{
					if (0 <= min_value)
					{
						if (x_val[i] < x_val[min_value])
							min_value = i;
					} else
					{
						min_value = i;
					}
				}
			}
			flag_x[min_value] = 1;

			// Check for repeated points
			if (np == -1)
			{
				np++;
				ordered_x[np] = x_val[min_value];
				ordered_y[np] = y_val[min_value];
			} else
			{
				if (ordered_x[np] != x_val[min_value] || !eliminateRepeatedPoints)
				{
					np++;
					ordered_x[np] = x_val[min_value];
					ordered_y[np] = y_val[min_value];
				}
			}
		}

		double new_x[] = new double[np + 1];
		double new_y[] = new double[np + 1];
		for (int i = 0; i <= np; i++)
		{
			new_x[i] = ordered_x[i];
			new_y[i] = ordered_y[i];
		}

		ArrayList<double[]> v = new ArrayList<double[]>();
		v.add(new_x);
		v.add(new_y);

		return v;
	}

}
