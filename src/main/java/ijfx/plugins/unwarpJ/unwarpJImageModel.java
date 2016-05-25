/*
    This file is part of ImageJ FX.

    ImageJ FX is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    ImageJ FX is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with ImageJ FX.  If not, see <http://www.gnu.org/licenses/>. 
    
     Copyright 2015,2016 Cyril MONGIS, Michael Knop
	
 */
package ijfx.plugins.unwarpJ;

import ij.process.ImageProcessor;
import java.util.Stack;

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
/*------------------------------------------------------------------*/
class unwarpJImageModel implements Runnable {

    /* begin class unwarpJImageModel */
    // Some constants
    private static int min_image_size = 4;
    /*....................................................................
    Private variables
    ....................................................................*/
    // Thread
    private Thread t;
    // Stack for the pyramid of images/coefficients
    private final Stack cpyramid = new Stack();
    private final Stack imgpyramid = new Stack();
    // Original image, image spline coefficients, and gradient
    private double[] image;
    private double[] coefficient;
    // Current image (the size might be different from the original)
    private double[] currentImage;
    private double[] currentCoefficient;
    private int currentWidth;
    private int currentHeight;
    private int twiceCurrentWidth;
    private int twiceCurrentHeight;
    // Size and other information
    private int width;
    private int height;
    private int twiceWidth;
    private int twiceHeight;
    private int pyramidDepth;
    private int currentDepth;
    private int smallestWidth;
    private int smallestHeight;
    private boolean isTarget;
    private boolean coefficientsAreMirrored;
    // Some variables to speedup interpolation
    // All these information is set through prepareForInterpolation()
    private double x; // Point to interpolate
    private double y;
    public int[] xIndex; // Indexes related
    public int[] yIndex;
    private double[] xWeight; // Weights of the splines related
    private double[] yWeight;
    private double[] dxWeight; // Weights of the derivatives splines related
    private double[] dyWeight;
    private double[] d2xWeight; // Weights of the second derivatives splines related
    private double[] d2yWeight;
    private boolean fromCurrent; // Interpolation source (current or original)
    private int widthToUse; // Size of the image used for the interpolation
    private int heightToUse;
    // Some variables to speedup interpolation (precomputed)
    // All these information is set through prepareForInterpolation()
    public int[][] prec_xIndex; // Indexes related
    public int[][] prec_yIndex;
    private double[][] prec_xWeight; // Weights of the splines related
    private double[][] prec_yWeight;
    private double[][] prec_dxWeight; // Weights of the derivatives splines related
    private double[][] prec_dyWeight;
    private double[][] prec_d2xWeight; // Weights of the second derivatives splines related
    private double[][] prec_d2yWeight;

    /*....................................................................
    Public methods
    ....................................................................*/
    /* Clear the pyramid. */
    public void clearPyramids() {
        cpyramid.removeAllElements();
        imgpyramid.removeAllElements();
    } /* end clearPyramid */

    /*------------------------------------------------------------------*/
    /* Return the full-size B-spline coefficients. */
    public double[] getCoefficient() {
        return coefficient;
    }

    /*------------------------------------------------------------------*/
    /* Return the current height of the image/coefficients. */
    public int getCurrentHeight() {
        return currentHeight;
    }

    /*------------------------------------------------------------------*/
    /* Return the current image of the image/coefficients. */
    public double[] getCurrentImage() {
        return currentImage;
    }

    /*------------------------------------------------------------------*/
    /* Return the current width of the image/coefficients. */
    public int getCurrentWidth() {
        return currentWidth;
    }

    /*------------------------------------------------------------------*/
    /* Return the relationship between the current size of the image
    and the original size. */
    public double getFactorHeight() {
        return (double) currentHeight / height;
    }

    /*------------------------------------------------------------------*/
    /* Return the relationship between the current size of the image
    and the original size. */
    public double getFactorWidth() {
        return (double) currentWidth / width;
    }

    /*------------------------------------------------------------------*/
    /* Return the current depth of the image/coefficients. */
    public int getCurrentDepth() {
        return currentDepth;
    }

    /*------------------------------------------------------------------*/
    /* Return the full-size image height. */
    public int getHeight() {
        return height;
    }

    /*------------------------------------------------------------------*/
    /* Return the full-size image. */
    public double[] getImage() {
        return image;
    }

    /*------------------------------------------------------------------*/
    public double getPixelValFromPyramid(int x, // Pixel location
    int y) {
        return currentImage[y * currentWidth + x];
    }

    /*------------------------------------------------------------------*/
    /* Return the depth of the image pyramid. A depth 1 means
    that one coarse resolution level is present in the stack. The
    full-size level is not placed on the stack. */
    public int getPyramidDepth() {
        return pyramidDepth;
    }

    /*------------------------------------------------------------------*/
    /* Return the height of the smallest image in the pyramid. */
    public int getSmallestHeight() {
        return smallestHeight;
    }

    /*------------------------------------------------------------------*/
    /* Return the width of the smallest image in the pyramid. */
    public int getSmallestWidth() {
        return smallestWidth;
    }

    /*------------------------------------------------------------------*/
    /* Return the thread associated. */
    public Thread getThread() {
        return t;
    }

    /*------------------------------------------------------------------*/
    /* Return the full-size image width. */
    public int getWidth() {
        return width;
    }

    /*------------------------------------------------------------------*/
    /* Return the weight of the coefficient l,m (yIndex, xIndex) in the
    image interpolation */
    public double getWeightDx(int l, int m) {
        return yWeight[l] * dxWeight[m];
    }

    /*------------------------------------------------------------------*/
    /* Return the weight of the coefficient l,m (yIndex, xIndex) in the
    image interpolation */
    public double getWeightDxDx(int l, int m) {
        return yWeight[l] * d2xWeight[m];
    }

    /*------------------------------------------------------------------*/
    /* Return the weight of the coefficient l,m (yIndex, xIndex) in the
    image interpolation */
    public double getWeightDxDy(int l, int m) {
        return dyWeight[l] * dxWeight[m];
    }

    /*------------------------------------------------------------------*/
    /* Return the weight of the coefficient l,m (yIndex, xIndex) in the
    image interpolation */
    public double getWeightDy(int l, int m) {
        return dyWeight[l] * xWeight[m];
    }

    /*------------------------------------------------------------------*/
    /* Return the weight of the coefficient l,m (yIndex, xIndex) in the
    image interpolation */
    public double getWeightDyDy(int l, int m) {
        return d2yWeight[l] * xWeight[m];
    }

    /*------------------------------------------------------------------*/
    /* Return the weight of the coefficient l,m (yIndex, xIndex) in the
    image interpolation */
    public double getWeightI(int l, int m) {
        return yWeight[l] * xWeight[m];
    }

    /*------------------------------------------------------------------*/
    /*
    There are two types of interpolation routines. Those that use
    precomputed weights and those that don't.
    An example of use of the ones without precomputation is the
    following:
    // Set of B-spline coefficients
    double [][]c;
    // Set these coefficients to an interpolator
    unwarpJImageModel sw = new unwarpJImageModel(c);
    // Compute the transformation mapping
    for (int v=0; v<ImageHeight; v++) {
    final double tv = (double)(v * intervals) / (double)(ImageHeight - 1) + 1.0F;
    for (int u = 0; u<ImageeWidth; u++) {
    final double tu = (double)(u * intervals) / (double)(ImageWidth - 1) + 1.0F;
    sw.prepareForInterpolation(tu, tv, ORIGINAL);
    interpolated_val[v][u] = sw.interpolateI();
     */
    /*------------------------------------------------------------------*/
    /*------------------------------------------------------------------*/
    /* Interpolate the X and Y derivatives of the image at a
    given point. */
    public void interpolateD(double[] D) {
        // Only SplineDegree=3 is implemented
        D[0] = D[1] = 0.0F;
        for (int j = 0; j < 4; j++) {
            double sx = 0.0F;
            double sy = 0.0F;
            int iy = yIndex[j];
            if (iy != -1) {
                int p = iy * widthToUse;
                for (int i = 0; i < 4; i++) {
                    int ix = xIndex[i];
                    if (ix != -1) {
                        double c;
                        if (fromCurrent) {
                            c = currentCoefficient[p + ix];
                        } else {
                            c = coefficient[p + ix];
                        }
                        sx += dxWeight[i] * c;
                        sy += xWeight[i] * c;
                    }
                }
                D[0] += yWeight[j] * sx;
                D[1] += dyWeight[j] * sy;
            }
        }
    } /* end Interpolate D */

    /*------------------------------------------------------------------*/
    /* Interpolate the XY, XX and YY derivatives of the image at a
    given point. */
    public void interpolateD2(double[] D2) {
        // Only SplineDegree=3 is implemented
        D2[0] = D2[1] = D2[2] = 0.0F;
        for (int j = 0; j < 4; j++) {
            double sxy = 0.0F;
            double sxx = 0.0F;
            double syy = 0.0F;
            int iy = yIndex[j];
            if (iy != -1) {
                int p = iy * widthToUse;
                for (int i = 0; i < 4; i++) {
                    int ix = xIndex[i];
                    if (ix != -1) {
                        double c;
                        if (fromCurrent) {
                            c = currentCoefficient[p + ix];
                        } else {
                            c = coefficient[p + ix];
                        }
                        sxy += dxWeight[i] * c;
                        sxx += d2xWeight[i] * c;
                        syy += xWeight[i] * c;
                    }
                }
                D2[0] += dyWeight[j] * sxy;
                D2[1] += yWeight[j] * sxx;
                D2[2] += d2yWeight[j] * syy;
            }
        }
    } /* end Interpolate dxdy, dxdx and dydy */

    /*------------------------------------------------------------------*/
    /* Interpolate the X derivative of the image at a given point. */
    public double interpolateDx() {
        // Only SplineDegree=3 is implemented
        double ival = 0.0F;
        for (int j = 0; j < 4; j++) {
            double s = 0.0F;
            int iy = yIndex[j];
            if (iy != -1) {
                int p = iy * widthToUse;
                for (int i = 0; i < 4; i++) {
                    int ix = xIndex[i];
                    if (ix != -1) {
                        if (fromCurrent) {
                            s += dxWeight[i] * currentCoefficient[p + ix];
                        } else {
                            s += dxWeight[i] * coefficient[p + ix];
                        }
                    }
                }
                ival += yWeight[j] * s;
            }
        }
        return ival;
    } /* end Interpolate Dx */

    /*------------------------------------------------------------------*/
    /* Interpolate the X derivative of the image at a given point. */
    public double interpolateDxDx() {
        // Only SplineDegree=3 is implemented
        double ival = 0.0F;
        for (int j = 0; j < 4; j++) {
            double s = 0.0F;
            int iy = yIndex[j];
            if (iy != -1) {
                int p = iy * widthToUse;
                for (int i = 0; i < 4; i++) {
                    int ix = xIndex[i];
                    if (ix != -1) {
                        if (fromCurrent) {
                            s += d2xWeight[i] * currentCoefficient[p + ix];
                        } else {
                            s += d2xWeight[i] * coefficient[p + ix];
                        }
                    }
                }
                ival += yWeight[j] * s;
            }
        }
        return ival;
    } /* end Interpolate DxDx */

    /*------------------------------------------------------------------*/
    /* Interpolate the X derivative of the image at a given point. */
    public double interpolateDxDy() {
        // Only SplineDegree=3 is implemented
        double ival = 0.0F;
        for (int j = 0; j < 4; j++) {
            double s = 0.0F;
            int iy = yIndex[j];
            if (iy != -1) {
                int p = iy * widthToUse;
                for (int i = 0; i < 4; i++) {
                    int ix = xIndex[i];
                    if (ix != -1) {
                        if (fromCurrent) {
                            s += dxWeight[i] * currentCoefficient[p + ix];
                        } else {
                            s += dxWeight[i] * coefficient[p + ix];
                        }
                    }
                }
                ival += dyWeight[j] * s;
            }
        }
        return ival;
    } /* end Interpolate DxDy */

    /*------------------------------------------------------------------*/
    /* Interpolate the Y derivative of the image at a given point. */
    public double interpolateDy() {
        // Only SplineDegree=3 is implemented
        double ival = 0.0F;
        for (int j = 0; j < 4; j++) {
            double s = 0.0F;
            int iy = yIndex[j];
            if (iy != -1) {
                int p = iy * widthToUse;
                for (int i = 0; i < 4; i++) {
                    int ix = xIndex[i];
                    if (ix != -1) {
                        if (fromCurrent) {
                            s += xWeight[i] * currentCoefficient[p + ix];
                        } else {
                            s += xWeight[i] * coefficient[p + ix];
                        }
                    }
                }
                ival += dyWeight[j] * s;
            }
        }
        return ival;
    } /* end Interpolate Dy */

    /*------------------------------------------------------------------*/
    /* Interpolate the X derivative of the image at a given point. */
    public double interpolateDyDy() {
        // Only SplineDegree=3 is implemented
        double ival = 0.0F;
        for (int j = 0; j < 4; j++) {
            double s = 0.0F;
            int iy = yIndex[j];
            if (iy != -1) {
                int p = iy * widthToUse;
                for (int i = 0; i < 4; i++) {
                    int ix = xIndex[i];
                    if (ix != -1) {
                        if (fromCurrent) {
                            s += xWeight[i] * currentCoefficient[p + ix];
                        } else {
                            s += xWeight[i] * coefficient[p + ix];
                        }
                    }
                }
                ival += d2yWeight[j] * s;
            }
        }
        return ival;
    } /* end Interpolate DyDy */

    /*------------------------------------------------------------------*/
    /* Interpolate the image at a given point. */
    public double interpolateI() {
        // Only SplineDegree=3 is implemented
        double ival = 0.0F;
        for (int j = 0; j < 4; j++) {
            double s = 0.0F;
            int iy = yIndex[j];
            if (iy != -1) {
                int p = iy * widthToUse;
                for (int i = 0; i < 4; i++) {
                    int ix = xIndex[i];
                    if (ix != -1) {
                        if (fromCurrent) {
                            s += xWeight[i] * currentCoefficient[p + ix];
                        } else {
                            s += xWeight[i] * coefficient[p + ix];
                        }
                    }
                }
                ival += yWeight[j] * s;
            }
        }
        return ival;
    } /* end Interpolate Image */

    /*------------------------------------------------------------------*/
    public boolean isFinest() {
        return cpyramid.isEmpty();
    }

    /*------------------------------------------------------------------*/
    public void popFromPyramid() {
        // Pop coefficients
        if (cpyramid.isEmpty()) {
            currentWidth = width;
            currentHeight = height;
            currentCoefficient = coefficient;
        } else {
            currentWidth = ((Integer) cpyramid.pop()).intValue();
            currentHeight = ((Integer) cpyramid.pop()).intValue();
            currentCoefficient = (double[]) cpyramid.pop();
        }
        twiceCurrentWidth = 2 * currentWidth;
        twiceCurrentHeight = 2 * currentHeight;
        if (currentDepth > 0) {
            currentDepth--;
        }
        // Pop image
        if (isTarget && !imgpyramid.isEmpty()) {
            if (currentWidth != ((Integer) imgpyramid.pop()).intValue()) {
                System.out.println("I cannot understand");
            }
            if (currentHeight != ((Integer) imgpyramid.pop()).intValue()) {
                System.out.println("I cannot understand");
            }
            currentImage = (double[]) imgpyramid.pop();
        } else {
            currentImage = image;
        }
    }

    /*------------------------------------------------------------------*/
    /* fromCurrent=true  --> The interpolation is prepared to be done
    from the current image in the pyramid.
    fromCurrent=false --> The interpolation is prepared to be done
    from the original image. */
    public void prepareForInterpolation(double x, double y, boolean fromCurrent) {
        // Remind this point for interpolation
        this.x = x;
        this.y = y;
        this.fromCurrent = fromCurrent;
        if (fromCurrent) {
            widthToUse = currentWidth;
            heightToUse = currentHeight;
        } else {
            widthToUse = width;
            heightToUse = height;
        }
        int ix = (int) x;
        int iy = (int) y;
        int twiceWidthToUse = 2 * widthToUse;
        int twiceHeightToUse = 2 * heightToUse;
        // Set X indexes
        // p is the index of the rightmost influencing spline
        int p = (0.0 <= x) ? (ix + 2) : (ix + 1);
        for (int k = 0; k < 4; p--, k++) {
            if (coefficientsAreMirrored) {
                int q = (p < 0) ? (-1 - p) : (p);
                if (twiceWidthToUse <= q) {
                    q -= twiceWidthToUse * (q / twiceWidthToUse);
                }
                xIndex[k] = (widthToUse <= q) ? (twiceWidthToUse - 1 - q) : (q);
            } else {
                xIndex[k] = (p < 0 || p >= widthToUse) ? (-1) : (p);
            }
        }
        // Set Y indexes
        p = (0.0 <= y) ? (iy + 2) : (iy + 1);
        for (int k = 0; k < 4; p--, k++) {
            if (coefficientsAreMirrored) {
                int q = (p < 0) ? (-1 - p) : (p);
                if (twiceHeightToUse <= q) {
                    q -= twiceHeightToUse * (q / twiceHeightToUse);
                }
                yIndex[k] = (heightToUse <= q) ? (twiceHeightToUse - 1 - q) : (q);
            } else {
                yIndex[k] = (p < 0 || p >= heightToUse) ? (-1) : (p);
            }
        }
        // Compute how much the sample depart from an integer position
        double ex = x - ((0.0 <= x) ? (ix) : (ix - 1));
        double ey = y - ((0.0 <= y) ? (iy) : (iy - 1));
        // Set X weights for the image and derivative interpolation
        double s = 1.0F - ex;
        dxWeight[0] = 0.5F * ex * ex;
        xWeight[0] = ex * dxWeight[0] / 3.0F; // Bspline03(x-ix-2)
        dxWeight[3] = -0.5F * s * s;
        xWeight[3] = s * dxWeight[3] / -3.0F; // Bspline03(x-ix+1)
        dxWeight[1] = 1.0F - 2.0F * dxWeight[0] + dxWeight[3];
        //xWeight[1]  = 2.0F / 3.0F + (1.0F + ex) * dxWeight[3]; // Bspline03(x-ix-1);
        xWeight[1] = unwarpJMathTools.Bspline03(x - ix - 1);
        dxWeight[2] = 1.5F * ex * (ex - 4.0F / 3.0F);
        xWeight[2] = 2.0F / 3.0F - (2.0F - ex) * dxWeight[0]; // Bspline03(x-ix)
        d2xWeight[0] = ex;
        d2xWeight[1] = s - 2 * ex;
        d2xWeight[2] = ex - 2 * s;
        d2xWeight[3] = s;
        // Set Y weights for the image and derivative interpolation
        double t = 1.0F - ey;
        dyWeight[0] = 0.5F * ey * ey;
        yWeight[0] = ey * dyWeight[0] / 3.0F;
        dyWeight[3] = -0.5F * t * t;
        yWeight[3] = t * dyWeight[3] / -3.0F;
        dyWeight[1] = 1.0F - 2.0F * dyWeight[0] + dyWeight[3];
        yWeight[1] = 2.0F / 3.0F + (1.0F + ey) * dyWeight[3];
        dyWeight[2] = 1.5F * ey * (ey - 4.0F / 3.0F);
        yWeight[2] = 2.0F / 3.0F - (2.0F - ey) * dyWeight[0];
        d2yWeight[0] = ey;
        d2yWeight[1] = t - 2 * ey;
        d2yWeight[2] = ey - 2 * t;
        d2yWeight[3] = t;
    } /* prepareForInterpolation */

    /*------------------------------------------------------------------*/
    /* Return the width of the precomputed vectors */
    public int precomputed_getWidth() {
        return prec_yWeight.length;
    }

    /*------------------------------------------------------------------*/
    /* Return the weight of the coefficient l,m (yIndex, xIndex) in the
    image interpolation */
    public double precomputed_getWeightDx(int l, int m, int u, int v) {
        return prec_yWeight[v][l] * prec_dxWeight[u][m];
    }

    /*------------------------------------------------------------------*/
    /* Return the weight of the coefficient l,m (yIndex, xIndex) in the
    image interpolation */
    public double precomputed_getWeightDxDx(int l, int m, int u, int v) {
        return prec_yWeight[v][l] * prec_d2xWeight[u][m];
    }

    /*------------------------------------------------------------------*/
    /* Return the weight of the coefficient l,m (yIndex, xIndex) in the
    image interpolation */
    public double precomputed_getWeightDxDy(int l, int m, int u, int v) {
        return prec_dyWeight[v][l] * prec_dxWeight[u][m];
    }

    /*------------------------------------------------------------------*/
    /* Return the weight of the coefficient l,m (yIndex, xIndex) in the
    image interpolation */
    public double precomputed_getWeightDy(int l, int m, int u, int v) {
        return prec_dyWeight[v][l] * prec_xWeight[u][m];
    }

    /*------------------------------------------------------------------*/
    /* Return the weight of the coefficient l,m (yIndex, xIndex) in the
    image interpolation */
    public double precomputed_getWeightDyDy(int l, int m, int u, int v) {
        return prec_d2yWeight[v][l] * prec_xWeight[u][m];
    }

    /*------------------------------------------------------------------*/
    /* Return the weight of the coefficient l,m (yIndex, xIndex) in the
    image interpolation */
    public double precomputed_getWeightI(int l, int m, int u, int v) {
        return prec_yWeight[v][l] * prec_xWeight[u][m];
    }

    /*------------------------------------------------------------------*/
    /* Interpolate the X and Y derivatives of the image at a
    given point. */
    public void precomputed_interpolateD(double[] D, int u, int v) {
        // Only SplineDegree=3 is implemented
        D[0] = D[1] = 0.0F;
        for (int j = 0; j < 4; j++) {
            double sx = 0.0F;
            double sy = 0.0F;
            int iy = prec_yIndex[v][j];
            if (iy != -1) {
                int p = iy * widthToUse;
                for (int i = 0; i < 4; i++) {
                    int ix = prec_xIndex[u][i];
                    if (ix != -1) {
                        double c;
                        if (fromCurrent) {
                            c = currentCoefficient[p + ix];
                        } else {
                            c = coefficient[p + ix];
                        }
                        sx += prec_dxWeight[u][i] * c;
                        sy += prec_xWeight[u][i] * c;
                    }
                }
                D[0] += prec_yWeight[v][j] * sx;
                D[1] += prec_dyWeight[v][j] * sy;
            }
        }
    } /* end Interpolate D */

    /*------------------------------------------------------------------*/
    /* Interpolate the XY, XX and YY derivatives of the image at a
    given point. */
    public void precomputed_interpolateD2(double[] D2, int u, int v) {
        // Only SplineDegree=3 is implemented
        D2[0] = D2[1] = D2[2] = 0.0F;
        for (int j = 0; j < 4; j++) {
            double sxy = 0.0F;
            double sxx = 0.0F;
            double syy = 0.0F;
            int iy = prec_yIndex[v][j];
            if (iy != -1) {
                int p = iy * widthToUse;
                for (int i = 0; i < 4; i++) {
                    int ix = prec_xIndex[u][i];
                    if (ix != -1) {
                        double c;
                        if (fromCurrent) {
                            c = currentCoefficient[p + ix];
                        } else {
                            c = coefficient[p + ix];
                        }
                        sxy += prec_dxWeight[u][i] * c;
                        sxx += prec_d2xWeight[u][i] * c;
                        syy += prec_xWeight[u][i] * c;
                    }
                }
                D2[0] += prec_dyWeight[v][j] * sxy;
                D2[1] += prec_yWeight[v][j] * sxx;
                D2[2] += prec_d2yWeight[v][j] * syy;
            }
        }
    } /* end Interpolate dxdy, dxdx and dydy */

    /*------------------------------------------------------------------*/
    /* Interpolate the image at a given point. */
    public double precomputed_interpolateI(int u, int v) {
        // Only SplineDegree=3 is implemented
        double ival = 0.0F;
        for (int j = 0; j < 4; j++) {
            double s = 0.0F;
            int iy = prec_yIndex[v][j];
            if (iy != -1) {
                int p = iy * widthToUse;
                for (int i = 0; i < 4; i++) {
                    int ix = prec_xIndex[u][i];
                    if (ix != -1) {
                        if (fromCurrent) {
                            s += prec_xWeight[u][i] * currentCoefficient[p + ix];
                        } else {
                            s += prec_xWeight[u][i] * coefficient[p + ix];
                        }
                    }
                }
                ival += prec_yWeight[v][j] * s;
            }
        }
        return ival;
    } /* end Interpolate Image */

    /*------------------------------------------------------------------*/
    /* Prepare precomputations for a given image size. */
    public void precomputed_prepareForInterpolation(int Ydim, int Xdim, int intervals) {
        // Ask for memory
        prec_xIndex = new int[Xdim][4];
        prec_yIndex = new int[Ydim][4];
        prec_xWeight = new double[Xdim][4];
        prec_yWeight = new double[Ydim][4];
        prec_dxWeight = new double[Xdim][4];
        prec_dyWeight = new double[Ydim][4];
        prec_d2xWeight = new double[Xdim][4];
        prec_d2yWeight = new double[Ydim][4];
        boolean ORIGINAL = false;
        // Fill the precomputed weights and indexes for the Y axis
        for (int v = 0; v < Ydim; v++) {
            // Express the current point in Spline units
            final double tv = (double) (v * intervals) / (double) (Ydim - 1) + 1.0F;
            final double tu = 1.0F;
            // Compute all weights and indexes
            prepareForInterpolation(tu, tv, ORIGINAL);
            // Copy all values
            for (int k = 0; k < 4; k++) {
                prec_yIndex[v][k] = yIndex[k];
                prec_yWeight[v][k] = yWeight[k];
                prec_dyWeight[v][k] = dyWeight[k];
                prec_d2yWeight[v][k] = d2yWeight[k];
            }
        }
        // Fill the precomputed weights and indexes for the X axis
        for (int u = 0; u < Xdim; u++) {
            // Express the current point in Spline units
            final double tv = 1.0F;
            final double tu = (double) (u * intervals) / (double) (Xdim - 1) + 1.0F;
            // Compute all weights and indexes
            prepareForInterpolation(tu, tv, ORIGINAL);
            // Copy all values
            for (int k = 0; k < 4; k++) {
                prec_xIndex[u][k] = xIndex[k];
                prec_xWeight[u][k] = xWeight[k];
                prec_dxWeight[u][k] = dxWeight[k];
                prec_d2xWeight[u][k] = d2xWeight[k];
            }
        }
    }

    /*------------------------------------------------------------------*/
    /* Start the image precomputations. The computation of the B-spline
    coefficients of the full-size image is not interruptible; all other
    methods are. */
    public void run() {
        coefficient = getBasicFromCardinal2D();
        buildCoefficientPyramid();
        if (isTarget) {
            buildImagePyramid();
        }
    } /* end run */

    /*------------------------------------------------------------------*/
    /* Set spline coefficients */
    public void setCoefficients(final double[] c, // Set of B-spline coefficients
    final int Ydim, // Dimensions of the set of coefficients
    final int Xdim, final int offset // Offset of the beginning of the array
    ) {
        // Copy the array of coefficients
        System.arraycopy(c, offset, coefficient, 0, Ydim * Xdim);
    }

    /*------------------------------------------------------------------*/
    /* Sets the depth up to which the pyramids should be computed. */
    public void setPyramidDepth(final int pyramidDepth) {
        int proposedPyramidDepth = pyramidDepth;
        // Check what is the maximum depth allowed by the image
        int currentWidth = width;
        int currentHeight = height;
        int scale = 0;
        while (currentWidth >= min_image_size && currentHeight >= min_image_size) {
            currentWidth /= 2;
            currentHeight /= 2;
            scale++;
        }
        scale--;
        if (proposedPyramidDepth > scale) {
            proposedPyramidDepth = scale;
        }
        this.pyramidDepth = proposedPyramidDepth;
    } /* end setPyramidDepth */

    /*------------------------------------------------------------------*/
    /* Converts the pixel array of the incoming ImageProcessor
    object into a local double array. The flag is target enables the
    computation of the derivative or not. */
    public unwarpJImageModel(final ImageProcessor ip, final boolean isTarget) {
        // Initialize thread
        t = new Thread(this);
        t.setDaemon(true);
        // Get image information
        this.isTarget = isTarget;
        width = ip.getWidth();
        height = ip.getHeight();
        twiceWidth = 2 * width;
        twiceHeight = 2 * height;
        coefficientsAreMirrored = true;
        // Copy the pixel array
        int k = 0;
        image = new double[width * height];
        unwarpJMiscTools.extractImage(ip, image);
        // Resize the speedup arrays
        xIndex = new int[4];
        yIndex = new int[4];
        xWeight = new double[4];
        yWeight = new double[4];
        dxWeight = new double[4];
        dyWeight = new double[4];
        d2xWeight = new double[4];
        d2yWeight = new double[4];
    } /* end unwarpJImage */

    /* The same as before, but take the image from an array */
    public unwarpJImageModel(final double[][] img, final boolean isTarget) {
        // Initialize thread
        t = new Thread(this);
        t.setDaemon(true);
        // Get image information
        this.isTarget = isTarget;
        width = img[0].length;
        height = img.length;
        twiceWidth = 2 * width;
        twiceHeight = 2 * height;
        coefficientsAreMirrored = true;
        // Copy the pixel array
        int k = 0;
        image = new double[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++, k++) {
                image[k] = img[y][x];
            }
        }
        // Resize the speedup arrays
        xIndex = new int[4];
        yIndex = new int[4];
        xWeight = new double[4];
        yWeight = new double[4];
        dxWeight = new double[4];
        dyWeight = new double[4];
        d2xWeight = new double[4];
        d2yWeight = new double[4];
    } /* end unwarpJImage */

    /*------------------------------------------------------------------*/
    /* Initialize the model from a set of coefficients */
    public unwarpJImageModel(final double[][] c // Set of B-spline coefficients
    ) {
        // Get the size of the input array
        currentHeight = height = c.length;
        currentWidth = width = c[0].length;
        twiceCurrentHeight = twiceHeight = 2 * height;
        twiceCurrentWidth = twiceWidth = 2 * width;
        coefficientsAreMirrored = false;
        // Copy the array of coefficients
        coefficient = new double[height * width];
        int k = 0;
        for (int y = 0; y < height; y++, k += width) {
            System.arraycopy(c[y], 0, coefficient, k, width);
        }
        // Resize the speedup arrays
        xIndex = new int[4];
        yIndex = new int[4];
        xWeight = new double[4];
        yWeight = new double[4];
        dxWeight = new double[4];
        dyWeight = new double[4];
        d2xWeight = new double[4];
        d2yWeight = new double[4];
    }

    /*------------------------------------------------------------------*/
    /* Initialize the model from a set of coefficients.
    The same as the previous function but now the coefficients
    are in a single row. */
    public unwarpJImageModel(final double[] c, // Set of B-spline coefficients
    final int Ydim, // Dimensions of the set of coefficients
    final int Xdim, final int offset // Offset of the beginning of the array
    ) {
        // Get the size of the input array
        currentHeight = height = Ydim;
        currentWidth = width = Xdim;
        twiceCurrentHeight = twiceHeight = 2 * height;
        twiceCurrentWidth = twiceWidth = 2 * width;
        coefficientsAreMirrored = false;
        // Copy the array of coefficients
        coefficient = new double[height * width];
        System.arraycopy(c, offset, coefficient, 0, height * width);
        // Resize the speedup arrays
        xIndex = new int[4];
        yIndex = new int[4];
        xWeight = new double[4];
        yWeight = new double[4];
        dxWeight = new double[4];
        dyWeight = new double[4];
        d2xWeight = new double[4];
        d2yWeight = new double[4];
    }
    /*....................................................................
    Private methods
    ....................................................................*/

    /*------------------------------------------------------------------*/
    private void antiSymmetricFirMirrorOffBounds1D(final double[] h, final double[] c, final double[] s) {
        if (2 <= c.length) {
            s[0] = h[1] * (c[1] - c[0]);
            for (int i = 1; i < (s.length - 1); i++) {
                s[i] = h[1] * (c[i + 1] - c[i - 1]);
            }
            s[s.length - 1] = h[1] * (c[c.length - 1] - c[c.length - 2]);
        } else {
            s[0] = 0.0;
        }
    } /* end antiSymmetricFirMirrorOffBounds1D */

    /*------------------------------------------------------------------*/
    private void basicToCardinal2D(final double[] basic, final double[] cardinal, final int width, final int height, final int degree) {
        final double[] hLine = new double[width];
        final double[] vLine = new double[height];
        final double[] hData = new double[width];
        final double[] vData = new double[height];
        double[] h = null;
        switch (degree) {
            case 3:
                h = new double[2];
                h[0] = 2.0 / 3.0;
                h[1] = 1.0 / 6.0;
                break;
            case 7:
                h = new double[4];
                h[0] = 151.0 / 315.0;
                h[1] = 397.0 / 1680.0;
                h[2] = 1.0 / 42.0;
                h[3] = 1.0 / 5040.0;
                break;
            default:
                h = new double[1];
                h[0] = 1.0;
        }
        for (int y = 0; (y < height) && (!t.isInterrupted()); y++) {
            extractRow(basic, y, hLine);
            symmetricFirMirrorOffBounds1D(h, hLine, hData);
            putRow(cardinal, y, hData);
        }
        for (int x = 0; (x < width) && (!t.isInterrupted()); x++) {
            extractColumn(cardinal, width, x, vLine);
            symmetricFirMirrorOffBounds1D(h, vLine, vData);
            putColumn(cardinal, width, x, vData);
        }
    } /* end basicToCardinal2D */

    /*------------------------------------------------------------------*/
    private void buildCoefficientPyramid() {
        int fullWidth;
        int fullHeight;
        double[] fullDual = new double[width * height];
        int halfWidth = width;
        int halfHeight = height;
        basicToCardinal2D(coefficient, fullDual, width, height, 7);
        for (int depth = 1; (depth <= pyramidDepth) && (!t.isInterrupted()); depth++) {
            fullWidth = halfWidth;
            fullHeight = halfHeight;
            halfWidth /= 2;
            halfHeight /= 2;
            final double[] halfDual = getHalfDual2D(fullDual, fullWidth, fullHeight);
            final double[] halfCoefficient = getBasicFromCardinal2D(halfDual, halfWidth, halfHeight, 7);
            cpyramid.push(halfCoefficient);
            cpyramid.push(new Integer(halfHeight));
            cpyramid.push(new Integer(halfWidth));
            fullDual = halfDual;
        }
        smallestWidth = halfWidth;
        smallestHeight = halfHeight;
        currentDepth = pyramidDepth + 1;
    } /* end buildCoefficientPyramid */

    /*------------------------------------------------------------------*/
    private void buildImagePyramid() {
        int fullWidth;
        int fullHeight;
        double[] fullDual = new double[width * height];
        int halfWidth = width;
        int halfHeight = height;
        cardinalToDual2D(image, fullDual, width, height, 3);
        for (int depth = 1; (depth <= pyramidDepth) && (!t.isInterrupted()); depth++) {
            fullWidth = halfWidth;
            fullHeight = halfHeight;
            halfWidth /= 2;
            halfHeight /= 2;
            final double[] halfDual = getHalfDual2D(fullDual, fullWidth, fullHeight);
            final double[] halfImage = new double[halfWidth * halfHeight];
            dualToCardinal2D(halfDual, halfImage, halfWidth, halfHeight, 3);
            imgpyramid.push(halfImage);
            imgpyramid.push(new Integer(halfHeight));
            imgpyramid.push(new Integer(halfWidth));
            fullDual = halfDual;
        }
    } /* end buildImagePyramid */

    /*------------------------------------------------------------------*/
    private void cardinalToDual2D(final double[] cardinal, final double[] dual, final int width, final int height, final int degree) {
        basicToCardinal2D(getBasicFromCardinal2D(cardinal, width, height, degree), dual, width, height, 2 * degree + 1);
    } /* end cardinalToDual2D */

    /*------------------------------------------------------------------*/
    private void coefficientToGradient1D(final double[] c) {
        final double[] h = {0.0, 1.0 / 2.0};
        final double[] s = new double[c.length];
        antiSymmetricFirMirrorOffBounds1D(h, c, s);
        System.arraycopy(s, 0, c, 0, s.length);
    } /* end coefficientToGradient1D */

    /*------------------------------------------------------------------*/
    private void coefficientToSamples1D(final double[] c) {
        final double[] h = {2.0 / 3.0, 1.0 / 6.0};
        final double[] s = new double[c.length];
        symmetricFirMirrorOffBounds1D(h, c, s);
        System.arraycopy(s, 0, c, 0, s.length);
    } /* end coefficientToSamples1D */

    /*------------------------------------------------------------------*/
    private void coefficientToXYGradient2D(final double[] basic, final double[] xGradient, final double[] yGradient, final int width, final int height) {
        final double[] hLine = new double[width];
        final double[] hData = new double[width];
        final double[] vLine = new double[height];
        for (int y = 0; (y < height) && (!t.isInterrupted()); y++) {
            extractRow(basic, y, hLine);
            System.arraycopy(hLine, 0, hData, 0, width);
            coefficientToGradient1D(hLine);
            coefficientToSamples1D(hData);
            putRow(xGradient, y, hLine);
            putRow(yGradient, y, hData);
        }
        for (int x = 0; (x < width) && (!t.isInterrupted()); x++) {
            extractColumn(xGradient, width, x, vLine);
            coefficientToSamples1D(vLine);
            putColumn(xGradient, width, x, vLine);
            extractColumn(yGradient, width, x, vLine);
            coefficientToGradient1D(vLine);
            putColumn(yGradient, width, x, vLine);
        }
    } /* end coefficientToXYGradient2D */

    /*------------------------------------------------------------------*/
    private void dualToCardinal2D(final double[] dual, final double[] cardinal, final int width, final int height, final int degree) {
        basicToCardinal2D(getBasicFromCardinal2D(dual, width, height, 2 * degree + 1), cardinal, width, height, degree);
    } /* end dualToCardinal2D */

    /*------------------------------------------------------------------*/
    private void extractColumn(final double[] array, final int width, int x, final double[] column) {
        for (int i = 0; i < column.length; i++, x += width) {
            column[i] = (double) array[x];
        }
    } /* end extractColumn */

    /*------------------------------------------------------------------*/
    private void extractRow(final double[] array, int y, final double[] row) {
        y *= row.length;
        for (int i = 0; i < row.length; i++) {
            row[i] = (double) array[y++];
        }
    } /* end extractRow */

    /*------------------------------------------------------------------*/
    private double[] getBasicFromCardinal2D() {
        final double[] basic = new double[width * height];
        final double[] hLine = new double[width];
        final double[] vLine = new double[height];
        for (int y = 0; y < height; y++) {
            extractRow(image, y, hLine);
            samplesToInterpolationCoefficient1D(hLine, 3, 0.0);
            putRow(basic, y, hLine);
        }
        for (int x = 0; x < width; x++) {
            extractColumn(basic, width, x, vLine);
            samplesToInterpolationCoefficient1D(vLine, 3, 0.0);
            putColumn(basic, width, x, vLine);
        }
        return basic;
    } /* end getBasicFromCardinal2D */

    /*------------------------------------------------------------------*/
    private double[] getBasicFromCardinal2D(final double[] cardinal, final int width, final int height, final int degree) {
        final double[] basic = new double[width * height];
        final double[] hLine = new double[width];
        final double[] vLine = new double[height];
        for (int y = 0; (y < height) && (!t.isInterrupted()); y++) {
            extractRow(cardinal, y, hLine);
            samplesToInterpolationCoefficient1D(hLine, degree, 0.0);
            putRow(basic, y, hLine);
        }
        for (int x = 0; (x < width) && (!t.isInterrupted()); x++) {
            extractColumn(basic, width, x, vLine);
            samplesToInterpolationCoefficient1D(vLine, degree, 0.0);
            putColumn(basic, width, x, vLine);
        }
        return basic;
    } /* end getBasicFromCardinal2D */

    /*------------------------------------------------------------------*/
    private double[] getHalfDual2D(final double[] fullDual, final int fullWidth, final int fullHeight) {
        final int halfWidth = fullWidth / 2;
        final int halfHeight = fullHeight / 2;
        final double[] hLine = new double[fullWidth];
        final double[] hData = new double[halfWidth];
        final double[] vLine = new double[fullHeight];
        final double[] vData = new double[halfHeight];
        final double[] demiDual = new double[halfWidth * fullHeight];
        final double[] halfDual = new double[halfWidth * halfHeight];
        for (int y = 0; (y < fullHeight) && (!t.isInterrupted()); y++) {
            extractRow(fullDual, y, hLine);
            reduceDual1D(hLine, hData);
            putRow(demiDual, y, hData);
        }
        for (int x = 0; (x < halfWidth) && (!t.isInterrupted()); x++) {
            extractColumn(demiDual, halfWidth, x, vLine);
            reduceDual1D(vLine, vData);
            putColumn(halfDual, halfWidth, x, vData);
        }
        return halfDual;
    } /* end getHalfDual2D */

    /*------------------------------------------------------------------*/
    private double getInitialAntiCausalCoefficientMirrorOffBounds(final double[] c, final double z, final double tolerance) {
        return z * c[c.length - 1] / (z - 1.0);
    } /* end getInitialAntiCausalCoefficientMirrorOffBounds */

    /*------------------------------------------------------------------*/
    private double getInitialCausalCoefficientMirrorOffBounds(final double[] c, final double z, final double tolerance) {
        double z1 = z;
        double zn = Math.pow(z, c.length);
        double sum = (1.0 + z) * (c[0] + zn * c[c.length - 1]);
        int horizon = c.length;
        if (0.0 < tolerance) {
            horizon = 2 + (int) (Math.log(tolerance) / Math.log(Math.abs(z)));
            horizon = (horizon < c.length) ? (horizon) : (c.length);
        }
        zn = zn * zn;
        for (int n = 1; n < (horizon - 1); n++) {
            z1 = z1 * z;
            zn = zn / z;
            sum = sum + (z1 + zn) * c[n];
        }
        return sum / (1.0 - Math.pow(z, 2 * c.length));
    } /* end getInitialCausalCoefficientMirrorOffBounds */

    /*------------------------------------------------------------------*/
    private void putColumn(final double[] array, final int width, int x, final double[] column) {
        for (int i = 0; i < column.length; i++, x += width) {
            array[x] = (double) column[i];
        }
    } /* end putColumn */

    /*------------------------------------------------------------------*/
    private void putRow(final double[] array, int y, final double[] row) {
        y *= row.length;
        for (int i = 0; i < row.length; i++) {
            array[y++] = (double) row[i];
        }
    } /* end putRow */

    /*------------------------------------------------------------------*/
    private void reduceDual1D(final double[] c, final double[] s) {
        final double[] h = {6.0 / 16.0, 4.0 / 16.0, 1.0 / 16.0};
        if (2 <= s.length) {
            s[0] = h[0] * c[0] + h[1] * (c[0] + c[1]) + h[2] * (c[1] + c[2]);
            for (int i = 2, j = 1; j < (s.length - 1); i += 2, j++) {
                s[j] = h[0] * c[i] + h[1] * (c[i - 1] + c[i + 1]) + h[2] * (c[i - 2] + c[i + 2]);
            }
            if (c.length == (2 * s.length)) {
                s[s.length - 1] = h[0] * c[c.length - 2] + h[1] * (c[c.length - 3] + c[c.length - 1]) + h[2] * (c[c.length - 4] + c[c.length - 1]);
            } else {
                s[s.length - 1] = h[0] * c[c.length - 3] + h[1] * (c[c.length - 4] + c[c.length - 2]) + h[2] * (c[c.length - 5] + c[c.length - 1]);
            }
        } else {
            switch (c.length) {
                case 3:
                    s[0] = h[0] * c[0] + h[1] * (c[0] + c[1]) + h[2] * (c[1] + c[2]);
                    break;
                case 2:
                    s[0] = h[0] * c[0] + h[1] * (c[0] + c[1]) + 2.0 * h[2] * c[1];
                    break;
                default:
            }
        }
    } /* end reduceDual1D */

    /*------------------------------------------------------------------*/
    private void samplesToInterpolationCoefficient1D(final double[] c, final int degree, final double tolerance) {
        double[] z = new double[0];
        double lambda = 1.0;
        switch (degree) {
            case 3:
                z = new double[1];
                z[0] = Math.sqrt(3.0) - 2.0;
                break;
            case 7:
                z = new double[3];
                z[0] = -0.5352804307964381655424037816816460718339231523426924148812;
                z[1] = -0.122554615192326690515272264359357343605486549427295558490763;
                z[2] = -0.0091486948096082769285930216516478534156925639545994482648003;
                break;
            default:
        }
        if (c.length == 1) {
            return;
        }
        for (int k = 0; k < z.length; k++) {
            lambda *= (1.0 - z[k]) * (1.0 - 1.0 / z[k]);
        }
        for (int n = 0; n < c.length; n++) {
            c[n] = c[n] * lambda;
        }
        for (int k = 0; k < z.length; k++) {
            c[0] = getInitialCausalCoefficientMirrorOffBounds(c, z[k], tolerance);
            for (int n = 1; n < c.length; n++) {
                c[n] = c[n] + z[k] * c[n - 1];
            }
            c[c.length - 1] = getInitialAntiCausalCoefficientMirrorOffBounds(c, z[k], tolerance);
            for (int n = c.length - 2; 0 <= n; n--) {
                c[n] = z[k] * (c[n + 1] - c[n]);
            }
        }
    } /* end samplesToInterpolationCoefficient1D */

    /*------------------------------------------------------------------*/
    private void symmetricFirMirrorOffBounds1D(final double[] h, final double[] c, final double[] s) {
        switch (h.length) {
            case 2:
                if (2 <= c.length) {
                    s[0] = h[0] * c[0] + h[1] * (c[0] + c[1]);
                    for (int i = 1; i < (s.length - 1); i++) {
                        s[i] = h[0] * c[i] + h[1] * (c[i - 1] + c[i + 1]);
                    }
                    s[s.length - 1] = h[0] * c[c.length - 1] + h[1] * (c[c.length - 2] + c[c.length - 1]);
                } else {
                    s[0] = (h[0] + 2.0 * h[1]) * c[0];
                }
                break;
            case 4:
                if (6 <= c.length) {
                    s[0] = h[0] * c[0] + h[1] * (c[0] + c[1]) + h[2] * (c[1] + c[2]) + h[3] * (c[2] + c[3]);
                    s[1] = h[0] * c[1] + h[1] * (c[0] + c[2]) + h[2] * (c[0] + c[3]) + h[3] * (c[1] + c[4]);
                    s[2] = h[0] * c[2] + h[1] * (c[1] + c[3]) + h[2] * (c[0] + c[4]) + h[3] * (c[0] + c[5]);
                    for (int i = 3; i < (s.length - 3); i++) {
                        s[i] = h[0] * c[i] + h[1] * (c[i - 1] + c[i + 1]) + h[2] * (c[i - 2] + c[i + 2]) + h[3] * (c[i - 3] + c[i + 3]);
                    }
                    s[s.length - 3] = h[0] * c[c.length - 3] + h[1] * (c[c.length - 4] + c[c.length - 2]) + h[2] * (c[c.length - 5] + c[c.length - 1]) + h[3] * (c[c.length - 6] + c[c.length - 1]);
                    s[s.length - 2] = h[0] * c[c.length - 2] + h[1] * (c[c.length - 3] + c[c.length - 1]) + h[2] * (c[c.length - 4] + c[c.length - 1]) + h[3] * (c[c.length - 5] + c[c.length - 2]);
                    s[s.length - 1] = h[0] * c[c.length - 1] + h[1] * (c[c.length - 2] + c[c.length - 1]) + h[2] * (c[c.length - 3] + c[c.length - 2]) + h[3] * (c[c.length - 4] + c[c.length - 3]);
                } else {
                    switch (c.length) {
                        case 5:
                            s[0] = h[0] * c[0] + h[1] * (c[0] + c[1]) + h[2] * (c[1] + c[2]) + h[3] * (c[2] + c[3]);
                            s[1] = h[0] * c[1] + h[1] * (c[0] + c[2]) + h[2] * (c[0] + c[3]) + h[3] * (c[1] + c[4]);
                            s[2] = h[0] * c[2] + h[1] * (c[1] + c[3]) + (h[2] + h[3]) * (c[0] + c[4]);
                            s[3] = h[0] * c[3] + h[1] * (c[2] + c[4]) + h[2] * (c[1] + c[4]) + h[3] * (c[0] + c[3]);
                            s[4] = h[0] * c[4] + h[1] * (c[3] + c[4]) + h[2] * (c[2] + c[3]) + h[3] * (c[1] + c[2]);
                            break;
                        case 4:
                            s[0] = h[0] * c[0] + h[1] * (c[0] + c[1]) + h[2] * (c[1] + c[2]) + h[3] * (c[2] + c[3]);
                            s[1] = h[0] * c[1] + h[1] * (c[0] + c[2]) + h[2] * (c[0] + c[3]) + h[3] * (c[1] + c[3]);
                            s[2] = h[0] * c[2] + h[1] * (c[1] + c[3]) + h[2] * (c[0] + c[3]) + h[3] * (c[0] + c[2]);
                            s[3] = h[0] * c[3] + h[1] * (c[2] + c[3]) + h[2] * (c[1] + c[2]) + h[3] * (c[0] + c[1]);
                            break;
                        case 3:
                            s[0] = h[0] * c[0] + h[1] * (c[0] + c[1]) + h[2] * (c[1] + c[2]) + 2.0 * h[3] * c[2];
                            s[1] = h[0] * c[1] + (h[1] + h[2]) * (c[0] + c[2]) + 2.0 * h[3] * c[1];
                            s[2] = h[0] * c[2] + h[1] * (c[1] + c[2]) + h[2] * (c[0] + c[1]) + 2.0 * h[3] * c[0];
                            break;
                        case 2:
                            s[0] = (h[0] + h[1] + h[3]) * c[0] + (h[1] + 2.0 * h[2] + h[3]) * c[1];
                            s[1] = (h[0] + h[1] + h[3]) * c[1] + (h[1] + 2.0 * h[2] + h[3]) * c[0];
                            break;
                        case 1:
                            s[0] = (h[0] + 2.0 * (h[1] + h[2] + h[3])) * c[0];
                            break;
                        default:
                    }
                }
                break;
            default:
        }
    } /* end symmetricFirMirrorOffBounds1D */
    
} /* end class unwarpJImageModel */