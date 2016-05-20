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
package ijfx.plugins.UnwarpJ;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.FloatProcessor;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Point;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

/*====================================================================
|   unwarpJTransformation
\===================================================================*/
/*------------------------------------------------------------------*/
class unwarpJTransformation {

    /* begin class unwarpJTransformation */
    /*....................................................................
    Private variables
    ....................................................................*/
    private final double FLT_EPSILON = (double) Float.intBitsToFloat((int) 0x33FFFFFF);
    private final boolean PYRAMID = true;
    private final boolean ORIGINAL = false;
    private final int transformationSplineDegree = 3;
    // Some useful references
    private ImagePlus output_ip;
    private unwarpJDialog dialog;
    // Images
    private ImagePlus sourceImp;
    private ImagePlus targetImp;
    private unwarpJImageModel source;
    private unwarpJImageModel target;
    // Landmarks
    private unwarpJPointHandler sourcePh;
    private unwarpJPointHandler targetPh;
    // Masks for the images
    private unwarpJMask sourceMsk;
    private unwarpJMask targetMsk;
    // Image size
    private int sourceHeight;
    private int sourceWidth;
    private int targetHeight;
    private int targetWidth;
    private int targetCurrentHeight;
    private int targetCurrentWidth;
    private double factorHeight;
    private double factorWidth;
    // Transformation parameters
    private int min_scale_deformation;
    private int max_scale_deformation;
    private int min_scale_image;
    private int outputLevel;
    private boolean showMarquardtOptim;
    private double divWeight;
    private double curlWeight;
    private double landmarkWeight;
    private double imageWeight;
    private double stopThreshold;
    private int accurate_mode;
    private boolean saveTransf;
    private String fn_tnf;
    // Transformation estimate
    private int intervals;
    private double[][] cx;
    private double[][] cy;
    private double[][] transformation_x;
    private double[][] transformation_y;
    private unwarpJImageModel swx;
    private unwarpJImageModel swy;
    // Regularization temporary variables
    private double[][] P11;
    private double[][] P22;
    private double[][] P12;

    /*....................................................................
    Public methods
    ....................................................................*/
    /*------------------------------------------------------------------*/
    public void doRegistration() {
        // This function can only be applied with splines of an odd order
        // Bring into consideration the image/coefficients at the smallest scale
        source.popFromPyramid();
        target.popFromPyramid();
        targetCurrentHeight = target.getCurrentHeight();
        targetCurrentWidth = target.getCurrentWidth();
        factorHeight = target.getFactorHeight();
        factorWidth = target.getFactorWidth();
        // Ask memory for the transformation coefficients
        intervals = (int) Math.pow(2, min_scale_deformation);
        cx = new double[intervals + 3][intervals + 3];
        cy = new double[intervals + 3][intervals + 3];
        // Build matrices for computing the regularization
        buildRegularizationTemporary(intervals);
        // Ask for memory for the residues
        final int K;
        if (targetPh != null) {
            K = targetPh.getPoints().size();
        } else {
            K = 0;
        }
        double[] dx = new double[K];
        double[] dy = new double[K];
        computeInitialResidues(dx, dy);
        // Compute the affine transformation from the target to the source coordinates
        // Notice that this matrix is independent of the scale, but the residues are not
        double[][] affineMatrix = null;
        if (landmarkWeight == 0) {
            affineMatrix = computeAffineMatrix();
        } else {
            affineMatrix = new double[2][3];
            affineMatrix[0][0] = affineMatrix[1][1] = 1;
            affineMatrix[0][1] = affineMatrix[0][2] = 0;
            affineMatrix[1][0] = affineMatrix[1][2] = 0;
        }
        // Incorporate the affine transformation into the spline coefficient
        for (int i = 0; i < intervals + 3; i++) {
            final double v = (double) ((i - 1) * (targetCurrentHeight - 1)) / (double) intervals;
            final double xv = affineMatrix[0][2] + affineMatrix[0][1] * v;
            final double yv = affineMatrix[1][2] + affineMatrix[1][1] * v;
            for (int j = 0; j < intervals + 3; j++) {
                final double u = (double) ((j - 1) * (targetCurrentWidth - 1)) / (double) intervals;
                cx[i][j] = xv + affineMatrix[0][0] * u;
                cy[i][j] = yv + affineMatrix[1][0] * u;
            }
        }
        // Now refine with the different scales
        int state; // state=-1 --> Finish
        // state= 0 --> Increase deformation detail
        // state= 1 --> Increase image detail
        // state= 2 --> Do nothing until the finest image scale
        if (min_scale_deformation == max_scale_deformation) {
            state = 1;
        } else {
            state = 0;
        }
        int s = min_scale_deformation;
        int step = 0;
        computeTotalWorkload();
        while (state != -1) {
            int currentDepth = target.getCurrentDepth();
            // Update the deformation coefficients only in states 0 and 1
            if (state == 0 || state == 1) {
                // Update the deformation coefficients with the error of the landmarks
                // The following conditional is now useless but it is there to allow
                // easy changes like applying the landmarks only in the coarsest deformation
                if (s >= min_scale_deformation) {
                    // Number of intervals at this scale and ask for memory
                    intervals = (int) Math.pow(2, s);
                    final double[][] newcx = new double[intervals + 3][intervals + 3];
                    final double[][] newcy = new double[intervals + 3][intervals + 3];
                    // Compute the residues before correcting at this scale
                    computeScaleResidues(intervals, cx, cy, dx, dy);
                    // Compute the coefficients at this scale
                    boolean underconstrained = true;
                    if (divWeight == 0 && curlWeight == 0) {
                        underconstrained = computeCoefficientsScale(intervals, dx, dy, newcx, newcy);
                    } else {
                        underconstrained = computeCoefficientsScaleWithRegularization(intervals, dx, dy, newcx, newcy);
                    }
                    // Incorporate information from the previous scale
                    if (!underconstrained || (step == 0 && landmarkWeight != 0)) {
                        for (int i = 0; i < intervals + 3; i++) {
                            for (int j = 0; j < intervals + 3; j++) {
                                cx[i][j] += newcx[i][j];
                                cy[i][j] += newcy[i][j];
                            }
                        }
                    }
                }
                // Optimize deformation coefficients
                if (imageWeight != 0) {
                    optimizeCoeffs(intervals, stopThreshold, cx, cy);
                }
            }
            // Prepare for next iteration
            step++;
            switch (state) {
                case 0:
                    // Finer details in the deformation
                    if (s < max_scale_deformation) {
                        cx = propagateCoeffsToNextLevel(intervals, cx, 1);
                        cy = propagateCoeffsToNextLevel(intervals, cy, 1);
                        s++;
                        intervals *= 2;
                        // Prepare matrices for the regularization term
                        buildRegularizationTemporary(intervals);
                        if (currentDepth > min_scale_image) {
                            state = 1;
                        } else {
                            state = 0;
                        }
                    } else if (currentDepth > min_scale_image) {
                        state = 1;
                    } else {
                        state = 2;
                    }
                    break;
                case 1:
// Finer details in the image, go on  optimizing
                case 2:
                    // Finer details in the image, do not optimize
                    // Compute next state
                    if (state == 1) {
                        if (s == max_scale_deformation && currentDepth == min_scale_image) {
                            state = 2;
                        } else if (s == max_scale_deformation) {
                            state = 1;
                        } else {
                            state = 0;
                        }
                    } else if (state == 2) {
                        if (currentDepth == 0) {
                            state = -1;
                        } else {
                            state = 2;
                        }
                    }
                    // Pop another image and prepare the deformation
                    if (currentDepth != 0) {
                        double oldTargetCurrentHeight = targetCurrentHeight;
                        double oldTargetCurrentWidth = targetCurrentWidth;
                        source.popFromPyramid();
                        target.popFromPyramid();
                        targetCurrentHeight = target.getCurrentHeight();
                        targetCurrentWidth = target.getCurrentWidth();
                        factorHeight = target.getFactorHeight();
                        factorWidth = target.getFactorWidth();
                        // Adapt the transformation to the new image size
                        double factorY = (targetCurrentHeight - 1) / (oldTargetCurrentHeight - 1);
                        double factorX = (targetCurrentWidth - 1) / (oldTargetCurrentWidth - 1);
                        for (int i = 0; i < intervals + 3; i++) {
                            for (int j = 0; j < intervals + 3; j++) {
                                cx[i][j] *= factorX;
                                cy[i][j] *= factorY;
                            }
                        }
                        // Prepare matrices for the regularization term
                        buildRegularizationTemporary(intervals);
                    }
                    break;
            }
            // In accurate_mode reduce the stopping threshold for the last iteration
            if ((state == 0 || state == 1) && s == max_scale_deformation && currentDepth == min_scale_image + 1 && accurate_mode == 1) {
                stopThreshold /= 10;
            }
        }
        // Show results
        showTransformation(intervals, cx, cy);
        if (saveTransf) {
            saveTransformation(intervals, cx, cy);
        }
    } /* end doMultiresolutionElasticTransformation */

    /*--------------------------------------------------------------------------*/
    public double evaluateImageSimilarity() {
        int int3 = intervals + 3;
        int halfM = int3 * int3;
        int M = halfM * 2;
        double[] x = new double[M];
        double[] grad = new double[M];
        for (int i = 0, p = 0; i < intervals + 3; i++) {
            for (int j = 0; j < intervals + 3; j++, p++) {
                x[p] = cx[i][j];
                x[halfM + p] = cy[i][j];
            }
        }
        if (swx == null) {
            swx = new unwarpJImageModel(cx);
            swy = new unwarpJImageModel(cy);
            swx.precomputed_prepareForInterpolation(target.getCurrentHeight(), target.getCurrentWidth(), intervals);
            swy.precomputed_prepareForInterpolation(target.getCurrentHeight(), target.getCurrentWidth(), intervals);
        }
        if (swx.precomputed_getWidth() != target.getCurrentWidth()) {
            swx.precomputed_prepareForInterpolation(target.getCurrentHeight(), target.getCurrentWidth(), intervals);
            swy.precomputed_prepareForInterpolation(target.getCurrentHeight(), target.getCurrentWidth(), intervals);
        }
        return evaluateSimilarity(x, intervals, grad, true, false);
    }

    /*------------------------------------------------------------------*/
    public void getDeformation(final double[][] transformation_x, final double[][] transformation_y) {
        computeDeformation(intervals, cx, cy, transformation_x, transformation_y);
    }

    /*------------------------------------------------------------------*/
    public unwarpJTransformation(final ImagePlus sourceImp, final ImagePlus targetImp, final unwarpJImageModel source, final unwarpJImageModel target, final unwarpJPointHandler sourcePh, final unwarpJPointHandler targetPh, final unwarpJMask sourceMsk, final unwarpJMask targetMsk, final int min_scale_deformation, final int max_scale_deformation, final int min_scale_image, final double divWeight, final double curlWeight, final double landmarkWeight, final double imageWeight, final double stopThreshold, final int outputLevel, final boolean showMarquardtOptim, final int accurate_mode, final boolean saveTransf, final String fn_tnf, final ImagePlus output_ip, final unwarpJDialog dialog) {
        this.sourceImp = sourceImp;
        this.targetImp = targetImp;
        this.source = source;
        this.target = target;
        this.sourcePh = sourcePh;
        this.targetPh = targetPh;
        this.sourceMsk = sourceMsk;
        this.targetMsk = targetMsk;
        this.min_scale_deformation = min_scale_deformation;
        this.max_scale_deformation = max_scale_deformation;
        this.min_scale_image = min_scale_image;
        this.divWeight = divWeight;
        this.curlWeight = curlWeight;
        this.landmarkWeight = landmarkWeight;
        this.imageWeight = imageWeight;
        this.stopThreshold = stopThreshold;
        this.outputLevel = outputLevel;
        this.showMarquardtOptim = showMarquardtOptim;
        this.accurate_mode = accurate_mode;
        this.saveTransf = saveTransf;
        this.fn_tnf = fn_tnf;
        this.output_ip = output_ip;
        this.dialog = dialog;
        sourceWidth = source.getWidth();
        sourceHeight = source.getHeight();
        targetWidth = target.getWidth();
        targetHeight = target.getHeight();
    } /* end unwarpJTransformation */

    /*------------------------------------------------------------------*/
    public void transform(double u, double v, double[] xyF) {
        final double tu = (u * intervals) / (double) (target.getCurrentWidth() - 1) + 1.0F;
        final double tv = (v * intervals) / (double) (target.getCurrentHeight() - 1) + 1.0F;
        final boolean ORIGINAL = false;
        swx.prepareForInterpolation(tu, tv, ORIGINAL);
        xyF[0] = swx.interpolateI();
        swy.prepareForInterpolation(tu, tv, ORIGINAL);
        xyF[1] = swy.interpolateI();
    }

    /*....................................................................
    Private methods
    ....................................................................*/
    /*------------------------------------------------------------------*/
    private void build_Matrix_B(int intervals, // Intervals in the deformation
    int K, // Number of landmarks
    double[][] B // System matrix of the landmark interpolation
    ) {
        Vector targetVector = null;
        if (targetPh != null) {
            targetVector = targetPh.getPoints();
        }
        for (int k = 0; k < K; k++) {
            final Point targetPoint = (Point) targetVector.elementAt(k);
            double x = factorWidth * (double) targetPoint.x;
            double y = factorHeight * (double) targetPoint.y;
            final double[] bx = xWeight(x, intervals, true);
            final double[] by = yWeight(y, intervals, true);
            for (int i = 0; i < intervals + 3; i++) {
                for (int j = 0; j < intervals + 3; j++) {
                    B[k][(intervals + 3) * i + j] = by[i] * bx[j];
                }
            }
        }
    }

    /*------------------------------------------------------------------*/
    private void build_Matrix_Rq1q2(int intervals, double weight, int q1, int q2, double[][] R) {
        build_Matrix_Rq1q2q3q4(intervals, weight, q1, q2, q1, q2, R);
    }

    private void build_Matrix_Rq1q2q3q4(int intervals, double weight, int q1, int q2, int q3, int q4, double[][] R) {
        /* Let's define alpha_q as the q-th derivative of a B-Spline
        q   n
        d    B (x)
        alpha_q(x)= --------------
        q
        dx
        eta_q1q2(x,s1,s2)=integral_0^Xdim alpha_q1(x/h-s1) alpha_q2(x/h-s2)
         */
        double[][] etaq1q3 = new double[16][16];
        int Ydim = target.getCurrentHeight();
        int Xdim = target.getCurrentWidth();
        build_Matrix_R_geteta(etaq1q3, q1, q3, Xdim, intervals);
        double[][] etaq2q4 = null;
        if (q2 != q1 || q4 != q3 || Ydim != Xdim) {
            etaq2q4 = new double[16][16];
            build_Matrix_R_geteta(etaq2q4, q2, q4, Ydim, intervals);
        } else {
            etaq2q4 = etaq1q3;
        }
        int M = intervals + 1;
        int Mp = intervals + 3;
        for (int l = -1; l <= M; l++) {
            for (int k = -1; k <= M; k++) {
                for (int n = -1; n <= M; n++) {
                    for (int m = -1; m <= M; m++) {
                        int[] ip = new int[2];
                        int[] jp = new int[2];
                        boolean valid_i = build_Matrix_R_getetaindex(l, n, intervals, ip);
                        boolean valid_j = build_Matrix_R_getetaindex(k, m, intervals, jp);
                        if (valid_i && valid_j) {
                            int mn = (n + 1) * Mp + (m + 1);
                            int kl = (l + 1) * Mp + (k + 1);
                            R[kl][mn] += weight * etaq1q3[jp[0]][jp[1]] * etaq2q4[ip[0]][ip[1]];
                        }
                    }
                }
            }
        }
    }

    /*------------------------------------------------------------------*/
    private double build_Matrix_R_computeIntegral_aa(double x0, double xF, double s1, double s2, double h, int q1, int q2) {
        // Computes the following integral
        //
        //           xF d^q1      3  x        d^q2    3  x
        //  integral    -----   B  (--- - s1) ----- B  (--- - s2) dx
        //           x0 dx^q1        h        dx^q2      h
        // Form the spline coefficients
        double[][] C = new double[3][3];
        int[][] d = new int[3][3];
        double[][] s = new double[3][3];
        C[0][0] = 1;
        C[0][1] = 0;
        C[0][2] = 0;
        C[1][0] = 1;
        C[1][1] = -1;
        C[1][2] = 0;
        C[2][0] = 1;
        C[2][1] = -2;
        C[2][2] = 1;
        d[0][0] = 3;
        d[0][1] = 0;
        d[0][2] = 0;
        d[1][0] = 2;
        d[1][1] = 2;
        d[1][2] = 0;
        d[2][0] = 1;
        d[2][1] = 1;
        d[2][2] = 1;
        s[0][0] = 0;
        s[0][1] = 0;
        s[0][2] = 0;
        s[1][0] = -0.5;
        s[1][1] = 0.5;
        s[1][2] = 0;
        s[2][0] = 1;
        s[2][1] = 0;
        s[2][2] = -1;
        // Compute the integral
        double integral = 0;
        for (int k = 0; k < 3; k++) {
            double ck = C[q1][k];
            if (ck == 0) {
                continue;
            }
            for (int l = 0; l < 3; l++) {
                double cl = C[q2][l];
                if (cl == 0) {
                    continue;
                }
                integral += ck * cl * build_matrix_R_computeIntegral_BB(x0, xF, s1 + s[q1][k], s2 + s[q2][l], h, d[q1][k], d[q2][l]);
            }
        }
        return integral;
    }

    /*------------------------------------------------------------------*/
    private double build_matrix_R_computeIntegral_BB(double x0, double xF, double s1, double s2, double h, int n1, int n2) {
        // Computes the following integral
        //
        //           xF   n1  x          n2  x
        //  integral     B  (--- - s1)  B  (--- - s2) dx
        //           x0       h              h
        // Change the variable so that the h disappears
        // X=x/h
        double xFp = xF / h;
        double x0p = x0 / h;
        // Form the spline coefficients
        double[] c1 = new double[n1 + 2];
        double fact_n1 = 1;
        for (int k = 2; k <= n1; k++) {
            fact_n1 *= k;
        }
        double sign = 1;
        for (int k = 0; k <= n1 + 1; k++, sign *= -1) {
            c1[k] = sign * unwarpJMathTools.nchoosek(n1 + 1, k) / fact_n1;
        }
        double[] c2 = new double[n2 + 2];
        double fact_n2 = 1;
        for (int k = 2; k <= n2; k++) {
            fact_n2 *= k;
        }
        sign = 1;
        for (int k = 0; k <= n2 + 1; k++, sign *= -1) {
            c2[k] = sign * unwarpJMathTools.nchoosek(n2 + 1, k) / fact_n2;
        }
        // Compute the integral
        double n1_2 = (double) ((n1 + 1)) / 2.0;
        double n2_2 = (double) ((n2 + 1)) / 2.0;
        double integral = 0;
        for (int k = 0; k <= n1 + 1; k++) {
            for (int l = 0; l <= n2 + 1; l++) {
                integral += c1[k] * c2[l] * build_matrix_R_computeIntegral_xx(x0p, xFp, s1 + k - n1_2, s2 + l - n2_2, n1, n2);
            }
        }
        return integral * h;
    }

    /*------------------------------------------------------------------*/
    private double build_matrix_R_computeIntegral_xx(double x0, double xF, double s1, double s2, int q1, int q2) {
        // Computation of the integral
        //             xF          q1       q2
        //    integral       (x-s1)   (x-s2)     dx
        //             x0          +        +
        // Change of variable so that s1 is 0
        // X=x-s1 => x-s2=X-(s2-s1)
        double s2p = s2 - s1;
        double xFp = xF - s1;
        double x0p = x0 - s1;
        // Now integrate
        if (xFp < 0) {
            return 0;
        }
        // Move x0 to the first point where both integrands
        // are distinct from 0
        x0p = Math.max(x0p, Math.max(s2p, 0));
        if (x0p > xFp) {
            return 0;
        }
        // There is something to integrate
        // Evaluate the primitive at xF and x0
        double IxFp = 0;
        double Ix0p = 0;
        for (int k = 0; k <= q2; k++) {
            double aux = unwarpJMathTools.nchoosek(q2, k) / (q1 + k + 1) * Math.pow(-s2p, q2 - k);
            IxFp += Math.pow(xFp, q1 + k + 1) * aux;
            Ix0p += Math.pow(x0p, q1 + k + 1) * aux;
        }
        return IxFp - Ix0p;
    }

    /*------------------------------------------------------------------*/
    private void build_Matrix_R_geteta(double[][] etaq1q2, int q1, int q2, int dim, int intervals) {
        boolean[][] done = new boolean[16][16];
        // Clear
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                etaq1q2[i][j] = 0;
                done[i][j] = false;
            }
        }
        // Compute each integral needed
        int M = intervals + 1;
        double h = (double) dim / intervals;
        for (int ki1 = -1; ki1 <= M; ki1++) {
            for (int ki2 = -1; ki2 <= M; ki2++) {
                int[] ip = new int[2];
                boolean valid_i = build_Matrix_R_getetaindex(ki1, ki2, intervals, ip);
                if (valid_i && !done[ip[0]][ip[1]]) {
                    etaq1q2[ip[0]][ip[1]] = build_Matrix_R_computeIntegral_aa(0, dim, ki1, ki2, h, q1, q2);
                    done[ip[0]][ip[1]] = true;
                }
            }
        }
    }

    /*------------------------------------------------------------------*/
    private boolean build_Matrix_R_getetaindex(int ki1, int ki2, int intervals, int[] ip) {
        ip[0] = 0;
        ip[1] = 0;
        // Determine the clipped inner limits of the intersection
        int kir = Math.min(intervals, Math.min(ki1, ki2) + 2);
        int kil = Math.max(0, Math.max(ki1, ki2) - 2);
        if (kil >= kir) {
            return false;
        }
        // Determine which are the pieces of the
        // function that lie in the intersection
        int two_i = 1;
        double ki;
        for (int i = 0; i <= 3; i++, two_i *= 2) {
            // First function
            ki = ki1 + i - 1.5; // Middle sample of the piece i
            if (kil <= ki && ki <= kir) {
                ip[0] += two_i;
            }
            // Second function
            ki = ki2 + i - 1.5; // Middle sample of the piece i
            if (kil <= ki && ki <= kir) {
                ip[1] += two_i;
            }
        }
        ip[0]--;
        ip[1]--;
        return true;
    }

    /*------------------------------------------------------------------*/
    private void buildRegularizationTemporary(int intervals) {
        // M is the number of spline coefficients per row
        int M = intervals + 3;
        int M2 = M * M;
        // P11
        P11 = new double[M2][M2];
        for (int i = 0; i < M2; i++) {
            for (int j = 0; j < M2; j++) {
                P11[i][j] = 0.0;
            }
        }
        build_Matrix_Rq1q2(intervals, divWeight, 2, 0, P11);
        build_Matrix_Rq1q2(intervals, divWeight + curlWeight, 1, 1, P11);
        build_Matrix_Rq1q2(intervals, curlWeight, 0, 2, P11);
        // P22
        P22 = new double[M2][M2];
        for (int i = 0; i < M2; i++) {
            for (int j = 0; j < M2; j++) {
                P22[i][j] = 0.0;
            }
        }
        build_Matrix_Rq1q2(intervals, divWeight, 0, 2, P22);
        build_Matrix_Rq1q2(intervals, divWeight + curlWeight, 1, 1, P22);
        build_Matrix_Rq1q2(intervals, curlWeight, 2, 0, P22);
        // P12
        P12 = new double[M2][M2];
        for (int i = 0; i < M2; i++) {
            for (int j = 0; j < M2; j++) {
                P12[i][j] = 0.0;
            }
        }
        build_Matrix_Rq1q2q3q4(intervals, 2 * divWeight, 2, 0, 1, 1, P12);
        build_Matrix_Rq1q2q3q4(intervals, 2 * divWeight, 1, 1, 0, 2, P12);
        build_Matrix_Rq1q2q3q4(intervals, -2 * curlWeight, 0, 2, 1, 1, P12);
        build_Matrix_Rq1q2q3q4(intervals, -2 * curlWeight, 1, 1, 2, 0, P12);
    }

    /*------------------------------------------------------------------*/
    private double[][] computeAffineMatrix() {
        boolean adjust_size = false;
        final double[][] D = new double[3][3];
        final double[][] H = new double[3][3];
        final double[][] U = new double[3][3];
        final double[][] V = new double[3][3];
        final double[][] X = new double[2][3];
        final double[] W = new double[3];
        Vector sourceVector = null;
        if (sourcePh != null) {
            sourceVector = sourcePh.getPoints();
        } else {
            sourceVector = new Vector();
        }
        Vector targetVector = null;
        if (targetPh != null) {
            targetVector = targetPh.getPoints();
        } else {
            targetVector = new Vector();
        }
        int removeLastPoint = 0;
        if (false) {
            removeLastPoint = sourceMsk.numberOfMaskPoints();
            for (int i = 0; i < removeLastPoint; i++) {
                sourceVector.addElement(sourceMsk.getPoint(i));
                targetVector.addElement(targetMsk.getPoint(i));
            }
        }
        int n = targetVector.size();
        switch (n) {
            case 0:
                for (int i = 0; i < 2; i++) {
                    for (int j = 0; j < 3; j++) {
                        X[i][j] = 0.0;
                    }
                }
                if (adjust_size) {
                    // Make both images of the same size
                    X[0][0] = (double) source.getCurrentWidth() / target.getCurrentWidth();
                    X[1][1] = (double) source.getCurrentHeight() / target.getCurrentHeight();
                } else {
                    // Make both images to be centered
                    X[0][0] = X[1][1] = 1;
                    X[0][2] = ((double) source.getCurrentWidth() - target.getCurrentWidth()) / 2;
                    X[1][2] = ((double) source.getCurrentHeight() - target.getCurrentHeight()) / 2;
                }
                break;
            case 1:
                for (int i = 0; i < 2; i++) {
                    for (int j = 0; j < 2; j++) {
                        X[i][j] = (i == j) ? (1.0F) : (0.0F);
                    }
                }
                X[0][2] = factorWidth * (double) (((Point) sourceVector.firstElement()).x - ((Point) targetVector.firstElement()).x);
                X[1][2] = factorHeight * (double) (((Point) sourceVector.firstElement()).y - ((Point) targetVector.firstElement()).y);
                break;
            case 2:
                final double x0 = factorWidth * ((Point) sourceVector.elementAt(0)).x;
                final double y0 = factorHeight * ((Point) sourceVector.elementAt(0)).y;
                final double x1 = factorWidth * ((Point) sourceVector.elementAt(1)).x;
                final double y1 = factorHeight * ((Point) sourceVector.elementAt(1)).y;
                final double u0 = factorWidth * ((Point) targetVector.elementAt(0)).x;
                final double v0 = factorHeight * ((Point) targetVector.elementAt(0)).y;
                final double u1 = factorWidth * ((Point) targetVector.elementAt(1)).x;
                final double v1 = factorHeight * ((Point) targetVector.elementAt(1)).y;
                sourceVector.addElement(new Point((int) (x1 + y0 - y1), (int) (x1 + y1 - x0)));
                targetVector.addElement(new Point((int) (u1 + v0 - v1), (int) (u1 + v1 - u0)));
                removeLastPoint = 1;
                n = 3;
            default:
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        H[i][j] = 0.0F;
                    }
                }
                for (int k = 0; k < n; k++) {
                    final Point sourcePoint = (Point) sourceVector.elementAt(k);
                    final Point targetPoint = (Point) targetVector.elementAt(k);
                    final double sx = factorWidth * (double) sourcePoint.x;
                    final double sy = factorHeight * (double) sourcePoint.y;
                    final double tx = factorWidth * (double) targetPoint.x;
                    final double ty = factorHeight * (double) targetPoint.y;
                    H[0][0] += tx * sx;
                    H[0][1] += tx * sy;
                    H[0][2] += tx;
                    H[1][0] += ty * sx;
                    H[1][1] += ty * sy;
                    H[1][2] += ty;
                    H[2][0] += sx;
                    H[2][1] += sy;
                    H[2][2] += 1.0F;
                    D[0][0] += sx * sx;
                    D[0][1] += sx * sy;
                    D[0][2] += sx;
                    D[1][0] += sy * sx;
                    D[1][1] += sy * sy;
                    D[1][2] += sy;
                    D[2][0] += sx;
                    D[2][1] += sy;
                    D[2][2] += 1.0F;
                }
                unwarpJMathTools.singularValueDecomposition(H, W, V);
                if ((Math.abs(W[0]) < FLT_EPSILON) || (Math.abs(W[1]) < FLT_EPSILON) || (Math.abs(W[2]) < FLT_EPSILON)) {
                    return computeRotationMatrix();
                }
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        V[i][j] /= W[j];
                    }
                }
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        U[i][j] = 0.0F;
                        for (int k = 0; k < 3; k++) {
                            U[i][j] += D[i][k] * V[k][j];
                        }
                    }
                }
                for (int i = 0; i < 2; i++) {
                    for (int j = 0; j < 3; j++) {
                        X[i][j] = 0.0F;
                        for (int k = 0; k < 3; k++) {
                            X[i][j] += U[i][k] * H[j][k];
                        }
                    }
                }
                break;
        }
        if (removeLastPoint != 0) {
            for (int i = 1; i <= removeLastPoint; i++) {
                sourcePh.getPoints().removeElementAt(n - i);
                targetPh.getPoints().removeElementAt(n - i);
            }
        }
        return X;
    } /* end computeAffineMatrix */

    /*------------------------------------------------------------------*/
    private void computeAffineResidues(final double[][] affineMatrix, // Input
    final double[] dx, // output, difference in x for each landmark
    final double[] dy // output, difference in y for each landmark
    ) {
        Vector sourceVector = null;
        if (sourcePh != null) {
            sourceVector = sourcePh.getPoints();
        } else {
            sourceVector = new Vector();
        }
        Vector targetVector = null;
        if (targetPh != null) {
            targetVector = targetPh.getPoints();
        } else {
            targetVector = new Vector();
        }
        final int K = targetPh.getPoints().size();
        for (int k = 0; k < K; k++) {
            final Point sourcePoint = (Point) sourceVector.elementAt(k);
            final Point targetPoint = (Point) targetVector.elementAt(k);
            double u = factorWidth * (double) targetPoint.x;
            double v = factorHeight * (double) targetPoint.y;
            final double x = affineMatrix[0][2] + affineMatrix[0][0] * u + affineMatrix[0][1] * v;
            final double y = affineMatrix[1][2] + affineMatrix[1][0] * u + affineMatrix[1][1] * v;
            dx[k] = factorWidth * (double) sourcePoint.x - x;
            dy[k] = factorHeight * (double) sourcePoint.y - y;
        }
    }

    /*------------------------------------------------------------------*/
    private boolean computeCoefficientsScale(final int intervals, // input, number of intervals at this scale
    final double[] dx, // input, x residue so far
    final double[] dy, // input, y residue so far
    final double[][] cx, // output, x coefficients for splines
    final double[][] cy // output, y coefficients for splines
    ) {
        int K = 0;
        if (targetPh != null) {
            K = targetPh.getPoints().size();
        }
        boolean underconstrained = false;
        if (0 < K) {
            // Form the equation system Bc=d
            final double[][] B = new double[K][(intervals + 3) * (intervals + 3)];
            build_Matrix_B(intervals, K, B);
            // "Invert" the matrix B
            int Nunk = (intervals + 3) * (intervals + 3);
            double[][] iB = new double[Nunk][K];
            underconstrained = unwarpJMathTools.invertMatrixSVD(K, Nunk, B, iB);
            // Now multiply iB times dx and dy respectively
            int ij = 0;
            for (int i = 0; i < intervals + 3; i++) {
                for (int j = 0; j < intervals + 3; j++) {
                    cx[i][j] = cy[i][j] = 0.0F;
                    for (int k = 0; k < K; k++) {
                        cx[i][j] += iB[ij][k] * dx[k];
                        cy[i][j] += iB[ij][k] * dy[k];
                    }
                    ij++;
                }
            }
        }
        return underconstrained;
    }

    /*------------------------------------------------------------------*/
    private boolean computeCoefficientsScaleWithRegularization(final int intervals, // input, number of intervals at this scale
    final double[] dx, // input, x residue so far
    final double[] dy, // input, y residue so far
    final double[][] cx, // output, x coefficients for splines
    final double[][] cy // output, y coefficients for splines
    ) {
        boolean underconstrained = true;
        int K = 0;
        if (targetPh != null) {
            K = targetPh.getPoints().size();
        }
        if (0 < K) {
            // M is the number of spline coefficients per row
            int M = intervals + 3;
            int M2 = M * M;
            // Create A and b for the system Ac=b
            final double[][] A = new double[2 * M2][2 * M2];
            final double[] b = new double[2 * M2];
            for (int i = 0; i < 2 * M2; i++) {
                b[i] = 0.0;
                for (int j = 0; j < 2 * M2; j++) {
                    A[i][j] = 0.0;
                }
            }
            // Get the matrix related to the landmarks
            final double[][] B = new double[K][M2];
            build_Matrix_B(intervals, K, B);
            // Fill the part of the equation system related to the landmarks
            // Compute 2 * B^t * B
            for (int i = 0; i < M2; i++) {
                for (int j = i; j < M2; j++) {
                    double bitbj = 0; // bi^t * bj, i.e., column i x column j
                    for (int l = 0; l < K; l++) {
                        bitbj += B[l][i] * B[l][j];
                    }
                    bitbj *= 2;
                    int ij = i * M2 + j;
                    A[M2 + i][M2 + j] = A[M2 + j][M2 + i] = A[i][j] = A[j][i] = bitbj;
                }
            }
            // Compute 2 * B^t * [dx dy]
            for (int i = 0; i < M2; i++) {
                double bitdx = 0;
                double bitdy = 0;
                for (int l = 0; l < K; l++) {
                    bitdx += B[l][i] * dx[l];
                    bitdy += B[l][i] * dy[l];
                }
                bitdx *= 2;
                bitdy *= 2;
                b[i] = bitdx;
                b[M2 + i] = bitdy;
            }
            // Get the matrices associated to the regularization
            // Copy P11 symmetrized to the equation system
            for (int i = 0; i < M2; i++) {
                for (int j = 0; j < M2; j++) {
                    double aux = P11[i][j];
                    A[i][j] += aux;
                    A[j][i] += aux;
                }
            }
            // Copy P22 symmetrized to the equation system
            for (int i = 0; i < M2; i++) {
                for (int j = 0; j < M2; j++) {
                    double aux = P22[i][j];
                    A[M2 + i][M2 + j] += aux;
                    A[M2 + j][M2 + i] += aux;
                }
            }
            // Copy P12 and P12^t to their respective places
            for (int i = 0; i < M2; i++) {
                for (int j = 0; j < M2; j++) {
                    A[i][M2 + j] = P12[i][j]; // P12
                    A[M2 + i][j] = P12[j][i]; // P12^t
                }
            }
            // Now solve the system
            // Invert the matrix A
            double[][] iA = new double[2 * M2][2 * M2];
            underconstrained = unwarpJMathTools.invertMatrixSVD(2 * M2, 2 * M2, A, iA);
            // Now multiply iB times b and distribute in cx and cy
            int ij = 0;
            for (int i = 0; i < intervals + 3; i++) {
                for (int j = 0; j < intervals + 3; j++) {
                    cx[i][j] = cy[i][j] = 0.0F;
                    for (int l = 0; l < 2 * M2; l++) {
                        cx[i][j] += iA[ij][l] * b[l];
                        cy[i][j] += iA[M2 + ij][l] * b[l];
                    }
                    ij++;
                }
            }
        }
        return underconstrained;
    }

    /*------------------------------------------------------------------*/
    private void computeInitialResidues(final double[] dx, // output, difference in x for each landmark
    final double[] dy // output, difference in y for each landmark
    ) {
        Vector sourceVector = null;
        if (sourcePh != null) {
            sourceVector = sourcePh.getPoints();
        } else {
            sourceVector = new Vector();
        }
        Vector targetVector = null;
        if (targetPh != null) {
            targetVector = targetPh.getPoints();
        } else {
            targetVector = new Vector();
        }
        int K = 0;
        if (targetPh != null) {
            targetPh.getPoints().size();
        }
        for (int k = 0; k < K; k++) {
            final Point sourcePoint = (Point) sourceVector.elementAt(k);
            final Point targetPoint = (Point) targetVector.elementAt(k);
            dx[k] = factorWidth * (sourcePoint.x - targetPoint.x);
            dy[k] = factorHeight * (sourcePoint.y - targetPoint.y);
        }
    }

    /*------------------------------------------------------------------*/
    private void computeDeformation(final int intervals, final double[][] cx, final double[][] cy, final double[][] transformation_x, final double[][] transformation_y) {
        // Set these coefficients to an interpolator
        unwarpJImageModel swx = new unwarpJImageModel(cx);
        unwarpJImageModel swy = new unwarpJImageModel(cy);
        // Compute the transformation mapping
        for (int v = 0; v < targetCurrentHeight; v++) {
            final double tv = (double) (v * intervals) / (double) (targetCurrentHeight - 1) + 1.0F;
            for (int u = 0; u < targetCurrentWidth; u++) {
                final double tu = (double) (u * intervals) / (double) (targetCurrentWidth - 1) + 1.0F;
                swx.prepareForInterpolation(tu, tv, ORIGINAL);
                transformation_x[v][u] = swx.interpolateI();
                swy.prepareForInterpolation(tu, tv, ORIGINAL);
                transformation_y[v][u] = swy.interpolateI();
            }
        }
    }

    /*------------------------------------------------------------------*/
    private double[][] computeRotationMatrix() {
        final double[][] X = new double[2][3];
        final double[][] H = new double[2][2];
        final double[][] V = new double[2][2];
        final double[] W = new double[2];
        Vector sourceVector = null;
        if (sourcePh != null) {
            sourceVector = sourcePh.getPoints();
        } else {
            sourceVector = new Vector();
        }
        Vector targetVector = null;
        if (targetPh != null) {
            targetVector = targetPh.getPoints();
        } else {
            targetVector = new Vector();
        }
        final int n = targetVector.size();
        switch (n) {
            case 0:
                for (int i = 0; i < 2; i++) {
                    for (int j = 0; j < 3; j++) {
                        X[i][j] = (i == j) ? (1.0F) : (0.0F);
                    }
                }
                break;
            case 1:
                for (int i = 0; i < 2; i++) {
                    for (int j = 0; j < 2; j++) {
                        X[i][j] = (i == j) ? (1.0F) : (0.0F);
                    }
                }
                X[0][2] = factorWidth * (double) (((Point) sourceVector.firstElement()).x - ((Point) targetVector.firstElement()).x);
                X[1][2] = factorHeight * (double) (((Point) sourceVector.firstElement()).y - ((Point) targetVector.firstElement()).y);
                break;
            default:
                double xTargetAverage = 0.0F;
                double yTargetAverage = 0.0F;
                for (int i = 0; i < n; i++) {
                    final Point p = (Point) targetVector.elementAt(i);
                    xTargetAverage += factorWidth * (double) p.x;
                    yTargetAverage += factorHeight * (double) p.y;
                }
                xTargetAverage /= (double) n;
                yTargetAverage /= (double) n;
                final double[] xCenteredTarget = new double[n];
                final double[] yCenteredTarget = new double[n];
                for (int i = 0; i < n; i++) {
                    final Point p = (Point) targetVector.elementAt(i);
                    xCenteredTarget[i] = factorWidth * (double) p.x - xTargetAverage;
                    yCenteredTarget[i] = factorHeight * (double) p.y - yTargetAverage;
                }
                double xSourceAverage = 0.0F;
                double ySourceAverage = 0.0F;
                for (int i = 0; i < n; i++) {
                    final Point p = (Point) sourceVector.elementAt(i);
                    xSourceAverage += factorWidth * (double) p.x;
                    ySourceAverage += factorHeight * (double) p.y;
                }
                xSourceAverage /= (double) n;
                ySourceAverage /= (double) n;
                final double[] xCenteredSource = new double[n];
                final double[] yCenteredSource = new double[n];
                for (int i = 0; i < n; i++) {
                    final Point p = (Point) sourceVector.elementAt(i);
                    xCenteredSource[i] = factorWidth * (double) p.x - xSourceAverage;
                    yCenteredSource[i] = factorHeight * (double) p.y - ySourceAverage;
                }
                for (int i = 0; i < 2; i++) {
                    for (int j = 0; j < 2; j++) {
                        H[i][j] = 0.0F;
                    }
                }
                for (int k = 0; k < n; k++) {
                    H[0][0] += xCenteredTarget[k] * xCenteredSource[k];
                    H[0][1] += xCenteredTarget[k] * yCenteredSource[k];
                    H[1][0] += yCenteredTarget[k] * xCenteredSource[k];
                    H[1][1] += yCenteredTarget[k] * yCenteredSource[k];
                }
                // COSS: Watch out that this H is the transpose of the one
                // defined in the text. That is why X=V*U^t is the inverse of
                // of the rotation matrix.
                unwarpJMathTools.singularValueDecomposition(H, W, V);
                if (((H[0][0] * H[1][1] - H[0][1] * H[1][0]) * (V[0][0] * V[1][1] - V[0][1] * V[1][0])) < 0.0F) {
                    if (W[0] < W[1]) {
                        V[0][0] *= -1.0F;
                        V[1][0] *= -1.0F;
                    } else {
                        V[0][1] *= -1.0F;
                        V[1][1] *= -1.0F;
                    }
                }
                for (int i = 0; i < 2; i++) {
                    for (int j = 0; j < 2; j++) {
                        X[i][j] = 0.0F;
                        for (int k = 0; k < 2; k++) {
                            X[i][j] += V[i][k] * H[j][k];
                        }
                    }
                }
                X[0][2] = xSourceAverage - X[0][0] * xTargetAverage - X[0][1] * yTargetAverage;
                X[1][2] = ySourceAverage - X[1][0] * xTargetAverage - X[1][1] * yTargetAverage;
                break;
        }
        return X;
    } /* end computeRotationMatrix */

    /*------------------------------------------------------------------*/
    private void computeScaleResidues(int intervals, // input, number of intevals
    final double[][] cx, // Input, spline coefficients
    final double[][] cy, final double[] dx, // Input/Output. At the input it has the
    final double[] dy // residue so far, at the output this
    ) {
        // Set these coefficients to an interpolator
        unwarpJImageModel swx = new unwarpJImageModel(cx);
        unwarpJImageModel swy = new unwarpJImageModel(cy);
        // Get the list of landmarks
        Vector sourceVector = null;
        if (sourcePh != null) {
            sourceVector = sourcePh.getPoints();
        } else {
            sourceVector = new Vector();
        }
        Vector targetVector = null;
        if (targetPh != null) {
            targetVector = targetPh.getPoints();
        } else {
            targetVector = new Vector();
        }
        final int K = targetVector.size();
        for (int k = 0; k < K; k++) {
            // Get the landmark coordinate in the target image
            final Point sourcePoint = (Point) sourceVector.elementAt(k);
            final Point targetPoint = (Point) targetVector.elementAt(k);
            double u = factorWidth * (double) targetPoint.x;
            double v = factorHeight * (double) targetPoint.y;
            // Express it in "spline" units
            double tu = (double) (u * intervals) / (double) (targetCurrentWidth - 1) + 1.0F;
            double tv = (double) (v * intervals) / (double) (targetCurrentHeight - 1) + 1.0F;
            // Transform this coordinate to the source image
            swx.prepareForInterpolation(tu, tv, false);
            double x = swx.interpolateI();
            swy.prepareForInterpolation(tu, tv, false);
            double y = swy.interpolateI();
            // Substract the result from the residual
            dx[k] = factorWidth * (double) sourcePoint.x - x;
            dy[k] = factorHeight * (double) sourcePoint.y - y;
        }
    }

    /*--------------------------------------------------------------------------*/
    private void computeTotalWorkload() {
        // This code is an excerpt from doRegistration() to compute the exact
        // number of steps
        // Now refine with the different scales
        int state; // state=-1 --> Finish
        // state= 0 --> Increase deformation detail
        // state= 1 --> Increase image detail
        // state= 2 --> Do nothing until the finest image scale
        if (min_scale_deformation == max_scale_deformation) {
            state = 1;
        } else {
            state = 0;
        }
        int s = min_scale_deformation;
        int currentDepth = target.getCurrentDepth();
        int workload = 0;
        while (state != -1) {
            // Update the deformation coefficients only in states 0 and 1
            if (state == 0 || state == 1) {
                // Optimize deformation coefficients
                if (imageWeight != 0) {
                    workload += 300 * (currentDepth + 1);
                }
            }
            // Prepare for next iteration
            switch (state) {
                case 0:
                    // Finer details in the deformation
                    if (s < max_scale_deformation) {
                        s++;
                        if (currentDepth > min_scale_image) {
                            state = 1;
                        } else {
                            state = 0;
                        }
                    } else if (currentDepth > min_scale_image) {
                        state = 1;
                    } else {
                        state = 2;
                    }
                    break;
                case 1:
// Finer details in the image, go on  optimizing
                case 2:
                    // Finer details in the image, do not optimize
                    // Compute next state
                    if (state == 1) {
                        if (s == max_scale_deformation && currentDepth == min_scale_image) {
                            state = 2;
                        } else if (s == max_scale_deformation) {
                            state = 1;
                        } else {
                            state = 0;
                        }
                    } else if (state == 2) {
                        if (currentDepth == 0) {
                            state = -1;
                        } else {
                            state = 2;
                        }
                    }
                    // Pop another image and prepare the deformation
                    if (currentDepth != 0) {
                        currentDepth--;
                    }
                    break;
            }
        }
        unwarpJProgressBar.resetProgressBar();
        unwarpJProgressBar.addWorkload(workload);
    }

    /*--------------------------------------------------------------------------*/
    private double evaluateSimilarity(final double[] c, // Input:  Deformation coefficients
    final int intervals, // Input:  Number of intervals for the deformation
    double[] grad, // Output: Gradient of the similarity
    // Output: the similarity is returned
    final boolean only_image, // Input:  if true, only the image term is considered
    //         and not the regularization
    final boolean show_error // Input:  if true, an image is shown with the error
    ) {
        int cYdim = intervals + 3;
        int cXdim = cYdim;
        int Nk = cYdim * cXdim;
        int twiceNk = 2 * Nk;
        double[] vgradreg = new double[grad.length];
        double[] vgradland = new double[grad.length];
        // Set the transformation coefficients to the interpolator
        swx.setCoefficients(c, cYdim, cXdim, 0);
        swy.setCoefficients(c, cYdim, cXdim, Nk);
        // Initialize gradient
        for (int k = 0; k < twiceNk; k++) {
            vgradreg[k] = vgradland[k] = grad[k] = 0.0F;
        }
        // Estimate the similarity and gradient between both images
        double imageSimilarity = 0.0;
        int Ydim = target.getCurrentHeight();
        int Xdim = target.getCurrentWidth();
        // Prepare to show
        double[][] error_image = null;
        double[][] div_error_image = null;
        double[][] curl_error_image = null;
        double[][] laplacian_error_image = null;
        double[][] jacobian_error_image = null;
        if (show_error) {
            error_image = new double[Ydim][Xdim];
            div_error_image = new double[Ydim][Xdim];
            curl_error_image = new double[Ydim][Xdim];
            laplacian_error_image = new double[Ydim][Xdim];
            jacobian_error_image = new double[Ydim][Xdim];
            for (int v = 0; v < Ydim; v++) {
                for (int u = 0; u < Xdim; u++) {
                    error_image[v][u] = div_error_image[v][u] = curl_error_image[v][u] = laplacian_error_image[v][u] = jacobian_error_image[v][u] = -1.0;
                }
            }
        }
        // Loop over all points in the source image
        int n = 0;
        if (imageWeight != 0 || show_error) {
            double[] xD2 = new double[3]; // Some space for the second derivatives
            double[] yD2 = new double[3]; // of the transformation
            double[] xD = new double[2]; // Some space for the second derivatives
            double[] yD = new double[2]; // of the transformation
            double[] I1D = new double[2]; // Space for the first derivatives of I1
            double hx = (Xdim - 1) / intervals; // Scale in the X axis
            double hy = (Ydim - 1) / intervals; // Scale in the Y axis
            double[] targetCurrentImage = target.getCurrentImage();
            int uv = 0;
            for (int v = 0; v < Ydim; v++) {
                for (int u = 0; u < Xdim; u++, uv++) {
                    // Compute image term .....................................................
                    // Check if this point is in the target mask
                    if (targetMsk.getValue(u / factorWidth, v / factorHeight)) {
                        // Compute value in the source image
                        double I2 = targetCurrentImage[uv];
                        // Compute the position of this point in the target
                        double x = swx.precomputed_interpolateI(u, v);
                        double y = swy.precomputed_interpolateI(u, v);
                        // Check if this point is in the source mask
                        if (sourceMsk.getValue(x / factorWidth, y / factorHeight)) {
                            // Compute the value of the target at that point
                            source.prepareForInterpolation(x, y, PYRAMID);
                            double I1 = source.interpolateI();
                            source.interpolateD(I1D);
                            double I1dx = I1D[0];
                            double I1dy = I1D[1];
                            double error = I2 - I1;
                            double error2 = error * error;
                            if (show_error) {
                                error_image[v][u] = error;
                            }
                            imageSimilarity += error2;
                            // Compute the derivative with respect to all the c coefficients
                            // Cost of the derivatives = 16*(3 mults + 2 sums)
                            // Current cost= 359 mults + 346 sums
                            for (int l = 0; l < 4; l++) {
                                for (int m = 0; m < 4; m++) {
                                    if (swx.prec_yIndex[v][l] == -1 || swx.prec_xIndex[u][m] == -1) {
                                        continue;
                                    }
                                    double weightI = swx.precomputed_getWeightI(l, m, u, v);
                                    int k = swx.prec_yIndex[v][l] * cYdim + swx.prec_xIndex[u][m];
                                    // Compute partial result
                                    // There's also a multiplication by 2 that I will
                                    // do later
                                    double aux = -error * weightI;
                                    // Derivative related to X deformation
                                    grad[k] += aux * I1dx;
                                    // Derivative related to Y deformation
                                    grad[k + Nk] += aux * I1dy;
                                }
                            }
                            n++; // Another point has been successfully evaluated
                        }
                    }
                    // Show regularization images ...........................................
                    if (show_error) {
                        double gradcurlx = 0.0;
                        double gradcurly = 0.0;
                        double graddivx = 0.0;
                        double graddivy = 0.0;
                        double xdx = 0.0;
                        double xdy = 0.0;
                        double ydx = 0.0;
                        double ydy = 0.0;
                        double xdxdy = 0.0;
                        double xdxdx = 0.0;
                        double xdydy = 0.0;
                        double ydxdy = 0.0;
                        double ydxdx = 0.0;
                        double ydydy = 0.0;
                        // Compute the first derivative terms
                        swx.precomputed_interpolateD(xD, u, v);
                        xdx = xD[0] / hx;
                        xdy = xD[1] / hy;
                        swy.precomputed_interpolateD(yD, u, v);
                        ydx = yD[0] / hx;
                        ydy = yD[1] / hy;
                        // Compute the second derivative terms
                        swx.precomputed_interpolateD2(xD2, u, v);
                        xdxdy = xD2[0];
                        xdxdx = xD2[1];
                        xdydy = xD2[2];
                        swy.precomputed_interpolateD2(yD2, u, v);
                        ydxdy = yD2[0];
                        ydxdx = yD2[1];
                        ydydy = yD2[2];
                        // Error in the divergence
                        graddivx = xdxdx + ydxdy;
                        graddivy = xdxdy + ydydy;
                        double graddiv = graddivx * graddivx + graddivy * graddivy;
                        double errorgraddiv = divWeight * graddiv;
                        if (divWeight != 0) {
                            div_error_image[v][u] = errorgraddiv;
                        } else {
                            div_error_image[v][u] = graddiv;
                        }
                        // Compute error in the curl
                        gradcurlx = -xdxdy + ydxdx;
                        gradcurly = -xdydy + ydxdy;
                        double gradcurl = gradcurlx * gradcurlx + gradcurly * gradcurly;
                        double errorgradcurl = curlWeight * gradcurl;
                        if (curlWeight != 0) {
                            curl_error_image[v][u] = errorgradcurl;
                        } else {
                            curl_error_image[v][u] = gradcurl;
                        }
                        // Compute Laplacian error
                        laplacian_error_image[v][u] = xdxdx * xdxdx;
                        laplacian_error_image[v][u] += xdxdy * xdxdy;
                        laplacian_error_image[v][u] += xdydy * xdydy;
                        laplacian_error_image[v][u] += ydxdx * ydxdx;
                        laplacian_error_image[v][u] += ydxdy * ydxdy;
                        laplacian_error_image[v][u] += ydydy * ydydy;
                        // Compute jacobian error
                        jacobian_error_image[v][u] = xdx * ydy - xdy * ydx;
                    }
                }
            }
        }
        // Average the image related terms
        if (n != 0) {
            imageSimilarity *= imageWeight / n;
            double aux = imageWeight * 2.0 / n; // This is the 2 coming from the
            // derivative that I would do later
            for (int k = 0; k < twiceNk; k++) {
                grad[k] *= aux;
            }
        } else if (imageWeight == 0) {
            imageSimilarity = 0;
        } else {
            imageSimilarity = 1 / FLT_EPSILON;
        }
        // Compute regularization term ..............................................
        double regularization = 0.0;
        if (!only_image) {
            for (int i = 0; i < Nk; i++) {
                for (int j = 0; j < Nk; j++) {
                    regularization += c[i] * P11[i][j] * c[j] + // c1^t P11 c1
                    c[Nk + i] * P22[i][j] * c[Nk + j] + // c2^t P22 c2
                    c[i] * P12[i][j] * c[Nk + j]; // c1^t P12 c2
                    vgradreg[i] += 2 * P11[i][j] * c[j]; // 2 P11 c1
                    vgradreg[Nk + i] += 2 * P22[i][j] * c[Nk + j]; // 2 P22 c2
                    vgradreg[i] += P12[i][j] * c[Nk + j]; //   P12 c2
                    vgradreg[Nk + i] += P12[j][i] * c[j]; //   P12^t c1
                }
            }
            regularization *= 1.0 / (Ydim * Xdim);
            for (int k = 0; k < twiceNk; k++) {
                vgradreg[k] *= 1.0 / (Ydim * Xdim);
            }
        }
        // Compute landmark error and derivative ...............................
        // Get the list of landmarks
        double landmarkError = 0.0;
        int K = 0;
        if (targetPh != null) {
            K = targetPh.getPoints().size();
        }
        if (landmarkWeight != 0) {
            Vector sourceVector = null;
            if (sourcePh != null) {
                sourceVector = sourcePh.getPoints();
            } else {
                sourceVector = new Vector();
            }
            Vector targetVector = null;
            if (targetPh != null) {
                targetVector = targetPh.getPoints();
            } else {
                targetVector = new Vector();
            }
            for (int kp = 0; kp < K; kp++) {
                // Get the landmark coordinate in the target image
                final Point sourcePoint = (Point) sourceVector.elementAt(kp);
                final Point targetPoint = (Point) targetVector.elementAt(kp);
                double u = factorWidth * (double) targetPoint.x;
                double v = factorHeight * (double) targetPoint.y;
                // Express it in "spline" units
                double tu = (double) (u * intervals) / (double) (targetCurrentWidth - 1) + 1.0F;
                double tv = (double) (v * intervals) / (double) (targetCurrentHeight - 1) + 1.0F;
                // Transform this coordinate to the source image
                swx.prepareForInterpolation(tu, tv, false);
                double x = swx.interpolateI();
                swy.prepareForInterpolation(tu, tv, false);
                double y = swy.interpolateI();
                // Substract the result from the residual
                double dx = factorWidth * (double) sourcePoint.x - x;
                double dy = factorHeight * (double) sourcePoint.y - y;
                // Add to landmark error
                landmarkError += dx * dx + dy * dy;
                // Compute the derivative with respect to all the c coefficients
                for (int l = 0; l < 4; l++) {
                    for (int m = 0; m < 4; m++) {
                        if (swx.yIndex[l] == -1 || swx.xIndex[m] == -1) {
                            continue;
                        }
                        int k = swx.yIndex[l] * cYdim + swx.xIndex[m];
                        // There's also a multiplication by 2 that I will do later
                        // Derivative related to X deformation
                        vgradland[k] -= dx * swx.getWeightI(l, m);
                        // Derivative related to Y deformation
                        vgradland[k + Nk] -= dy * swy.getWeightI(l, m);
                    }
                }
            }
        }
        if (K != 0) {
            landmarkError *= landmarkWeight / K;
            double aux = 2.0 * landmarkWeight / K;
            // This is the 2 coming from the derivative
            // computation that I would do at the end
            for (int k = 0; k < twiceNk; k++) {
                vgradland[k] *= aux;
            }
        }
        if (only_image) {
            landmarkError = 0;
        }
        // Finish computations .............................................................
        // Add all gradient terms
        for (int k = 0; k < twiceNk; k++) {
            grad[k] += vgradreg[k] + vgradland[k];
        }
        if (show_error) {
            unwarpJMiscTools.showImage("Error", error_image);
            unwarpJMiscTools.showImage("Divergence Error", div_error_image);
            unwarpJMiscTools.showImage("Curl Error", curl_error_image);
            unwarpJMiscTools.showImage("Laplacian Error", laplacian_error_image);
            unwarpJMiscTools.showImage("Jacobian Error", jacobian_error_image);
        }
        if (showMarquardtOptim) {
            if (imageWeight != 0) {
                IJ.write("    Image          error:" + imageSimilarity);
            }
            if (landmarkWeight != 0) {
                IJ.write("    Landmark       error:" + landmarkError);
            }
            if (divWeight != 0 || curlWeight != 0) {
                IJ.write("    Regularization error:" + regularization);
            }
        }
        return imageSimilarity + landmarkError + regularization;
    }

    /*--------------------------------------------------------------------------*/
    private void Marquardt_it(double[] x, boolean[] optimize, double[] gradient, double[] Hessian, double lambda) {
        /* In this function the system (H+lambda*Diag(H))*update=gradient
        is solved for update.
        H is the hessian of the function f,
        gradient is the gradient of the function f,
        Diag(H) is a matrix with the diagonal of H.
         */
        final double TINY = FLT_EPSILON;
        final int M = x.length;
        final int Mmax = 35;
        final int Mused = Math.min(M, Mmax);
        double[][] u = new double[Mused][Mused];
        double[][] v = null; //new double  [Mused][Mused];
        double[] w = null; //new double  [Mused];
        double[] g = new double[Mused];
        double[] update = new double[Mused];
        boolean[] optimizep = new boolean[M];
        System.arraycopy(optimize, 0, optimizep, 0, M);
        lambda += 1.0F;
        if (M > Mmax) {
            /* Find the threshold for the most important components */
            double[] sortedgradient = new double[M];
            for (int i = 0; i < M; i++) {
                sortedgradient[i] = Math.abs(gradient[i]);
            }
            Arrays.sort(sortedgradient);
            double gradient_th = sortedgradient[M - Mmax];
            int m = 0;
            int i;
            // Take the first Mused components with big gradients
            for (i = 0; i < M; i++) {
                if (optimizep[i] && Math.abs(gradient[i]) >= gradient_th) {
                    m++;
                    if (m == Mused) {
                        break;
                    }
                } else {
                    optimizep[i] = false;
                }
            }
            // Set the rest to 0
            for (i = i + 1; i < M; i++) {
                optimizep[i] = false;
            }
        }
        // Gradient descent
        //for (int i=0; i<M; i++) if (optimizep[i]) x[i]-=0.01*gradient[i];
        //if (true) return;
        /* u will be a copy of the Hessian where we take only those
        components corresponding to variables being optimized */
        int kr = 0;
        int iw = 0;
        for (int ir = 0; ir < M; kr = kr + M, ir++) {
            if (optimizep[ir]) {
                int jw = 0;
                for (int jr = 0; jr < M; jr++) {
                    if (optimizep[jr]) {
                        u[iw][jw++] = Hessian[kr + jr];
                    }
                }
                g[iw] = gradient[ir];
                u[iw][iw] *= lambda;
                iw++;
            }
        }
        // Solve he equation system
        /* SVD u=u*w*v^t */
        update = unwarpJMathTools.linearLeastSquares(u, g);
        /* x = x - update */
        kr = 0;
        for (int kw = 0; kw < M; kw++) {
            if (optimizep[kw]) {
                x[kw] -= update[kr++];
            }
        }
    } /* end Marquardt_it */

    /*--------------------------------------------------------------------------*/
    private double optimizeCoeffs(int intervals, double thChangef, double[][] cx, double[][] cy) {
        if (dialog != null && dialog.isStopRegistrationSet()) {
            return 0.0;
        }
        final double TINY = FLT_EPSILON;
        final double EPS = 3.0e-8F;
        final double FIRSTLAMBDA = 1;
        final int MAXITER_OPTIMCOEFF = 300;
        final int CUMULATIVE_SIZE = 5;
        int int3 = intervals + 3;
        int halfM = int3 * int3;
        int M = halfM * 2;
        double rescuedf;
        double f;
        double[] x = new double[M];
        double[] rescuedx = new double[M];
        double[] diffx = new double[M];
        double[] rescuedgrad = new double[M];
        double[] grad = new double[M];
        double[] diffgrad = new double[M];
        double[] Hdx = new double[M];
        double[] rescuedhess = new double[M * M];
        double[] hess = new double[M * M];
        double[] safehess = new double[M * M];
        double[] proposedHess = new double[M * M];
        boolean[] optimize = new boolean[M];
        int i;
        int j;
        int p;
        int iter = 1;
        boolean skip_update;
        boolean ill_hessian;
        double improvementx = (double) Math.sqrt(TINY);
        double lambda = FIRSTLAMBDA;
        double max_normx;
        double distx;
        double aux;
        double gmax;
        double fac;
        double fae;
        double dgdx;
        double dxHdx;
        double sumdiffg;
        double sumdiffx;
        unwarpJCumulativeQueue lastBest = new unwarpJCumulativeQueue(CUMULATIVE_SIZE);
        for (i = 0; i < M; i++) {
            optimize[i] = true;
        }
        /* Form the vector with the current guess for the optimization */
        for (i = 0, p = 0; i < intervals + 3; i++) {
            for (j = 0; j < intervals + 3; j++, p++) {
                x[p] = cx[i][j];
                x[halfM + p] = cy[i][j];
            }
        }
        /* Prepare the precomputed weights for interpolation */
        swx = new unwarpJImageModel(x, intervals + 3, intervals + 3, 0);
        swy = new unwarpJImageModel(x, intervals + 3, intervals + 3, halfM);
        swx.precomputed_prepareForInterpolation(target.getCurrentHeight(), target.getCurrentWidth(), intervals);
        swy.precomputed_prepareForInterpolation(target.getCurrentHeight(), target.getCurrentWidth(), intervals);
        /* First computation of the similarity */
        f = evaluateSimilarity(x, intervals, grad, false, false);
        if (showMarquardtOptim) {
            IJ.write("f(1)=" + f);
        }
        /* Initially the hessian is the identity matrix multiplied by
        the first function value */
        for (i = 0, p = 0; i < M; i++) {
            for (j = 0; j < M; j++, p++) {
                if (i == j) {
                    hess[p] = 1.0F;
                } else {
                    hess[p] = 0.0F;
                }
            }
        }
        rescuedf = f;
        for (i = 0, p = 0; i < M; i++) {
            rescuedx[i] = x[i];
            rescuedgrad[i] = grad[i];
            for (j = 0; j < M; j++, p++) {
                rescuedhess[p] = hess[p];
            }
        }
        int maxiter = MAXITER_OPTIMCOEFF * (source.getCurrentDepth() + 1);
        unwarpJProgressBar.stepProgressBar();
        int last_successful_iter = 0;
        boolean stop = dialog != null && dialog.isStopRegistrationSet();
        while (iter < maxiter && !stop) {
            /* Compute new x ------------------------------------------------- */
            Marquardt_it(x, optimize, grad, hess, lambda);
            /* Stopping criteria --------------------------------------------- */
            /* Compute difference with the previous iteration */
            max_normx = improvementx = 0;
            for (i = 0; i < M; i++) {
                diffx[i] = x[i] - rescuedx[i];
                distx = Math.abs(diffx[i]);
                improvementx += distx * distx;
                aux = Math.abs(rescuedx[i]) < Math.abs(x[i]) ? x[i] : rescuedx[i];
                max_normx += aux * aux;
            }
            if (TINY < max_normx) {
                improvementx = improvementx / max_normx;
            }
            improvementx = (double) Math.sqrt(Math.sqrt(improvementx));
            /* If there is no change with respect to the old geometry then
            finish the iterations */
            if (improvementx < Math.sqrt(TINY)) {
                break;
            }
            /* Estimate the new function value -------------------------------- */
            f = evaluateSimilarity(x, intervals, grad, false, false);
            iter++;
            if (showMarquardtOptim) {
                IJ.write("f(" + iter + ")=" + f + " lambda=" + lambda);
            }
            unwarpJProgressBar.stepProgressBar();
            /* Update lambda -------------------------------------------------- */
            if (rescuedf > f) {
                /* Check if the improvement is only residual */
                lastBest.push_back(rescuedf - f);
                if (lastBest.currentSize() == CUMULATIVE_SIZE && lastBest.getSum() / f < thChangef) {
                    break;
                }
                /* If we have improved then estimate the hessian,
                update the geometry, and decrease the lambda */
                /* Estimate the hessian ....................................... */
                if (showMarquardtOptim) {
                    IJ.write("  Accepted");
                }
                if ((last_successful_iter++ % 10) == 0 && outputLevel > -1) {
                    update_current_output(x, intervals);
                }
                /* Estimate the difference between gradients */
                for (i = 0; i < M; i++) {
                    diffgrad[i] = grad[i] - rescuedgrad[i];
                }
                /* Multiply this difference by the current inverse of the hessian */
                for (i = 0, p = 0; i < M; i++) {
                    Hdx[i] = 0.0F;
                    for (j = 0; j < M; j++, p++) {
                        Hdx[i] += hess[p] * diffx[j];
                    }
                }
                /* Calculate dot products for the denominators ................ */
                dgdx = dxHdx = sumdiffg = sumdiffx = 0.0F;
                skip_update = true;
                for (i = 0; i < M; i++) {
                    dgdx += diffgrad[i] * diffx[i];
                    dxHdx += diffx[i] * Hdx[i];
                    sumdiffg += diffgrad[i] * diffgrad[i];
                    sumdiffx += diffx[i] * diffx[i];
                    if (Math.abs(grad[i]) >= Math.abs(rescuedgrad[i])) {
                        gmax = Math.abs(grad[i]);
                    } else {
                        gmax = Math.abs(rescuedgrad[i]);
                    }
                    if (gmax != 0 && Math.abs(diffgrad[i] - Hdx[i]) > Math.sqrt(EPS) * gmax) {
                        skip_update = false;
                    }
                }
                /* Update hessian ............................................. */
                /* Skip if fac not sufficiently positive */
                if (dgdx > Math.sqrt(EPS * sumdiffg * sumdiffx) && !skip_update) {
                    fae = 1.0F / dxHdx;
                    fac = 1.0F / dgdx;
                    /* Update the hessian after BFGS formula */
                    for (i = 0, p = 0; i < M; i++) {
                        for (j = 0; j < M; j++, p++) {
                            if (i <= j) {
                                proposedHess[p] = hess[p] + fac * diffgrad[i] * diffgrad[j] - fae * (Hdx[i] * Hdx[j]);
                            } else {
                                proposedHess[p] = proposedHess[j * M + i];
                            }
                        }
                    }
                    ill_hessian = false;
                    if (!ill_hessian) {
                        for (i = 0, p = 0; i < M; i++) {
                            for (j = 0; j < M; j++, p++) {
                                hess[p] = proposedHess[p];
                            }
                        }
                    } else if (showMarquardtOptim) {
                        IJ.write("Hessian cannot be safely updated, ill-conditioned");
                    }
                } else if (showMarquardtOptim) {
                    IJ.write("Hessian cannot be safely updated");
                }
                /* Update geometry and lambda ................................. */
                rescuedf = f;
                for (i = 0, p = 0; i < M; i++) {
                    rescuedx[i] = x[i];
                    rescuedgrad[i] = grad[i];
                    for (j = 0; j < M; j++, p++) {
                        rescuedhess[p] = hess[p];
                    }
                }
                if (1e-4 < lambda) {
                    lambda = lambda / 10;
                }
            } else {
                /* else, if it is worse, then recover the last geometry
                and increase lambda, saturate lambda with FIRSTLAMBDA */
                for (i = 0, p = 0; i < M; i++) {
                    x[i] = rescuedx[i];
                    grad[i] = rescuedgrad[i];
                    for (j = 0; j < M; j++, p++) {
                        hess[p] = rescuedhess[p];
                    }
                }
                if (lambda < 1.0 / TINY) {
                    lambda *= 10;
                } else {
                    break;
                }
                if (lambda < FIRSTLAMBDA) {
                    lambda = FIRSTLAMBDA;
                }
            }
            stop = dialog != null && dialog.isStopRegistrationSet();
        }
        // Copy the values back to the input arrays
        for (i = 0, p = 0; i < intervals + 3; i++) {
            for (j = 0; j < intervals + 3; j++, p++) {
                cx[i][j] = x[p];
                cy[i][j] = x[halfM + p];
            }
        }
        unwarpJProgressBar.skipProgressBar(maxiter - iter);
        return f;
    }

    /*-----------------------------------------------------------------------------*/
    private double[][] propagateCoeffsToNextLevel(int intervals, final double[][] c, double expansionFactor // Due to the change of size in the represented image
    ) {
        // Expand the coefficients for the next scale
        intervals *= 2;
        double[][] cs_expand = new double[intervals + 7][intervals + 7];
        // Upsample
        for (int i = 0; i < intervals + 7; i++) {
            for (int j = 0; j < intervals + 7; j++) {
                // If it is not in an even sample then set it to 0
                if (i % 2 == 0 || j % 2 == 0) {
                    cs_expand[i][j] = 0.0F;
                } else {
                    // Now look for this sample in the coarser level
                    int ipc = (i - 1) / 2;
                    int jpc = (j - 1) / 2;
                    cs_expand[i][j] = c[ipc][jpc];
                }
            }
        }
        // Define the FIR filter
        double[][] u2n = new double[4][];
        u2n[0] = null;
        u2n[1] = new double[3];
        u2n[1][0] = 0.5F;
        u2n[1][1] = 1.0F;
        u2n[1][2] = 0.5F;
        u2n[2] = null;
        u2n[3] = new double[5];
        u2n[3][0] = 0.125F;
        u2n[3][1] = 0.5F;
        u2n[3][2] = 0.75F;
        u2n[3][3] = 0.5F;
        u2n[3][4] = 0.125F;
        int[] half_length_u2n = {0, 1, 0, 2};
        int kh = half_length_u2n[transformationSplineDegree];
        // Apply the u2n filter to rows
        double[][] cs_expand_aux = new double[intervals + 7][intervals + 7];
        for (int i = 1; i < intervals + 7; i += 2) {
            for (int j = 0; j < intervals + 7; j++) {
                cs_expand_aux[i][j] = 0.0F;
                for (int k = -kh; k <= kh; k++) {
                    if (j + k >= 0 && j + k <= intervals + 6) {
                        cs_expand_aux[i][j] += u2n[transformationSplineDegree][k + kh] * cs_expand[i][j + k];
                    }
                }
            }
        }
        // Apply the u2n filter to columns
        for (int i = 0; i < intervals + 7; i++) {
            for (int j = 0; j < intervals + 7; j++) {
                cs_expand[i][j] = 0.0F;
                for (int k = -kh; k <= kh; k++) {
                    if (i + k >= 0 && i + k <= intervals + 6) {
                        cs_expand[i][j] += u2n[transformationSplineDegree][k + kh] * cs_expand_aux[i + k][j];
                    }
                }
            }
        }
        // Copy the central coefficients to c
        double[][] newc = new double[intervals + 3][intervals + 3];
        for (int i = 0; i < intervals + 3; i++) {
            for (int j = 0; j < intervals + 3; j++) {
                newc[i][j] = cs_expand[i + 2][j + 2] * expansionFactor;
            }
        }
        // Return the new set of coefficients
        return newc;
    }

    /*------------------------------------------------------------------*/
    private void saveTransformation(int intervals, double[][] cx, double[][] cy) {
        String filename = fn_tnf;
        if (filename.equals("")) {
            // Get the filename to save
            File dir = new File(".");
            String path = "";
            try {
                path = dir.getCanonicalPath() + "/";
            } catch (Exception e) {
                e.printStackTrace();
            }
            filename = sourceImp.getTitle();
            String new_filename = "";
            int dot = filename.lastIndexOf('.');
            if (dot == -1) {
                new_filename = filename + "_transf.txt";
            } else {
                new_filename = filename.substring(0, dot) + "_transf.txt";
            }
            filename = path + filename;
            if (outputLevel > -1) {
                final Frame f = new Frame();
                final FileDialog fd = new FileDialog(f, "Save Transformation", FileDialog.SAVE);
                fd.setFile(new_filename);
                fd.setVisible(true);
                path = fd.getDirectory();
                filename = fd.getFile();
                if ((path == null) || (filename == null)) {
                    return;
                }
                filename = path + filename;
            } else {
                filename = new_filename;
            }
        }
        // Save the file
        try {
            final FileWriter fw = new FileWriter(filename);
            String aux;
            fw.write("Intervals=" + intervals + "\n\n");
            fw.write("X Coeffs -----------------------------------\n");
            for (int i = 0; i < intervals + 3; i++) {
                for (int j = 0; j < intervals + 3; j++) {
                    aux = "" + cx[i][j];
                    while (aux.length() < 21) {
                        aux = " " + aux;
                    }
                    fw.write(aux + " ");
                }
                fw.write("\n");
            }
            fw.write("\n");
            fw.write("Y Coeffs -----------------------------------\n");
            for (int i = 0; i < intervals + 3; i++) {
                for (int j = 0; j < intervals + 3; j++) {
                    aux = "" + cy[i][j];
                    while (aux.length() < 21) {
                        aux = " " + aux;
                    }
                    fw.write(aux + " ");
                }
                fw.write("\n");
            }
            fw.close();
        } catch (IOException e) {
            IJ.error("IOException exception" + e);
        } catch (SecurityException e) {
            IJ.error("Security exception" + e);
        }
    }

    /*------------------------------------------------------------------*/
    private void showDeformationGrid(int intervals, double[][] cx, double[][] cy, ImageStack is) {
        // Initialize output image
        int stepv = Math.min(Math.max(10, targetCurrentHeight / 15), 30);
        int stepu = Math.min(Math.max(10, targetCurrentWidth / 15), 30);
        final double[][] transformedImage = new double[targetCurrentHeight][targetCurrentWidth];
        for (int v = 0; v < targetCurrentHeight; v++) {
            for (int u = 0; u < targetCurrentWidth; u++) {
                transformedImage[v][u] = 255;
            }
        }
        // Ask for memory for the transformation
        double[][] transformation_x = new double[targetCurrentHeight][targetCurrentWidth];
        double[][] transformation_y = new double[targetCurrentHeight][targetCurrentWidth];
        // Compute the deformation
        computeDeformation(intervals, cx, cy, transformation_x, transformation_y);
        // Show deformed grid ........................................
        // Show deformation vectors
        for (int v = 0; v < targetCurrentHeight; v += stepv) {
            for (int u = 0; u < targetCurrentWidth; u += stepu) {
                final double x = transformation_x[v][u];
                final double y = transformation_y[v][u];
                // Draw horizontal line
                int uh = u + stepu;
                if (uh < targetCurrentWidth) {
                    final double xh = transformation_x[v][uh];
                    final double yh = transformation_y[v][uh];
                    unwarpJMiscTools.drawLine(transformedImage, (int) Math.round(x), (int) Math.round(y), (int) Math.round(xh), (int) Math.round(yh), 0);
                }
                // Draw vertical line
                int vv = v + stepv;
                if (vv < targetCurrentHeight) {
                    final double xv = transformation_x[vv][u];
                    final double yv = transformation_y[vv][u];
                    unwarpJMiscTools.drawLine(transformedImage, (int) Math.round(x), (int) Math.round(y), (int) Math.round(xv), (int) Math.round(yv), 0);
                }
            }
        }
        // Set it to the image stack
        FloatProcessor fp = new FloatProcessor(targetCurrentWidth, targetCurrentHeight);
        for (int v = 0; v < targetCurrentHeight; v++) {
            for (int u = 0; u < targetCurrentWidth; u++) {
                fp.putPixelValue(u, v, transformedImage[v][u]);
            }
        }
        is.addSlice("Deformation Grid", fp);
    }

    /*------------------------------------------------------------------*/
    private void showDeformationVectors(int intervals, double[][] cx, double[][] cy, ImageStack is) {
        // Initialize output image
        int stepv = Math.min(Math.max(10, targetCurrentHeight / 15), 30);
        int stepu = Math.min(Math.max(10, targetCurrentWidth / 15), 30);
        final double[][] transformedImage = new double[targetCurrentHeight][targetCurrentWidth];
        for (int v = 0; v < targetCurrentHeight; v++) {
            for (int u = 0; u < targetCurrentWidth; u++) {
                transformedImage[v][u] = 255;
            }
        }
        // Ask for memory for the transformation
        double[][] transformation_x = new double[targetCurrentHeight][targetCurrentWidth];
        double[][] transformation_y = new double[targetCurrentHeight][targetCurrentWidth];
        // Compute the deformation
        computeDeformation(intervals, cx, cy, transformation_x, transformation_y);
        // Show shift field ........................................
        // Show deformation vectors
        for (int v = 0; v < targetCurrentHeight; v += stepv) {
            for (int u = 0; u < targetCurrentWidth; u += stepu) {
                if (targetMsk.getValue(u, v)) {
                    final double x = transformation_x[v][u];
                    final double y = transformation_y[v][u];
                    if (sourceMsk.getValue(x, y)) {
                        unwarpJMiscTools.drawArrow(transformedImage, u, v, (int) Math.round(x), (int) Math.round(y), 0, 2);
                    }
                }
            }
        }
        // Set it to the image stack
        FloatProcessor fp = new FloatProcessor(targetCurrentWidth, targetCurrentHeight);
        for (int v = 0; v < targetCurrentHeight; v++) {
            for (int u = 0; u < targetCurrentWidth; u++) {
                fp.putPixelValue(u, v, transformedImage[v][u]);
            }
        }
        is.addSlice("Deformation Field", fp);
    }

    /*-------------------------------------------------------------------*/
    private void showTransformation(final int intervals, final double[][] cx, // Input, spline coefficients
    final double[][] cy) {
        boolean show_deformation = false;
        // Ask for memory for the transformation
        double[][] transformation_x = new double[targetHeight][targetWidth];
        double[][] transformation_y = new double[targetHeight][targetWidth];
        // Compute the deformation
        computeDeformation(intervals, cx, cy, transformation_x, transformation_y);
        if (show_deformation) {
            unwarpJMiscTools.showImage("Transf. X", transformation_x);
            unwarpJMiscTools.showImage("Transf. Y", transformation_y);
        }
        // Compute the warped image
        FloatProcessor fp = new FloatProcessor(targetWidth, targetHeight);
        FloatProcessor fp_mask = new FloatProcessor(targetWidth, targetHeight);
        FloatProcessor fp_target = new FloatProcessor(targetWidth, targetHeight);
        int uv = 0;
        for (int v = 0; v < targetHeight; v++) {
            for (int u = 0; u < targetWidth; u++, uv++) {
                fp_target.putPixelValue(u, v, target.getImage()[uv]);
                if (!targetMsk.getValue(u, v)) {
                    fp.putPixelValue(u, v, 0);
                    fp_mask.putPixelValue(u, v, 0);
                } else {
                    final double x = transformation_x[v][u];
                    final double y = transformation_y[v][u];
                    if (sourceMsk.getValue(x, y)) {
                        source.prepareForInterpolation(x, y, ORIGINAL);
                        double sval = source.interpolateI();
                        fp.putPixelValue(u, v, sval);
                        fp_mask.putPixelValue(u, v, 255);
                    } else {
                        fp.putPixelValue(u, v, 0);
                        fp_mask.putPixelValue(u, v, 0);
                    }
                }
            }
        }
        fp.resetMinAndMax();
        final ImageStack is = new ImageStack(targetWidth, targetHeight);
        is.addSlice("Registered Image", fp);
        if (outputLevel > -1) {
            is.addSlice("Target Image", fp_target);
        }
        if (outputLevel > -1) {
            is.addSlice("Warped Source Mask", fp_mask);
        }
        if (outputLevel == 2) {
            showDeformationVectors(intervals, cx, cy, is);
            showDeformationGrid(intervals, cx, cy, is);
        }
        output_ip.setStack("Registered Image", is);
        output_ip.setSlice(1);
        output_ip.getProcessor().resetMinAndMax();
        if (outputLevel > -1) {
            output_ip.updateAndRepaintWindow();
        }
    }

    /*------------------------------------------------------------------*/
    private void update_current_output(final double[] c, int intervals) {
        // Set the coefficients to an interpolator
        int cYdim = intervals + 3;
        int cXdim = cYdim;
        int Nk = cYdim * cXdim;
        swx.setCoefficients(c, cYdim, cXdim, 0);
        swy.setCoefficients(c, cYdim, cXdim, Nk);
        // Compute the deformed image
        FloatProcessor fp = new FloatProcessor(targetWidth, targetHeight);
        int uv = 0;
        for (int v = 0; v < targetHeight; v++) {
            for (int u = 0; u < targetWidth; u++, uv++) {
                if (targetMsk.getValue(u, v)) {
                    double down_u = u * factorWidth;
                    double down_v = v * factorHeight;
                    final double tv = (double) (down_v * intervals) / (double) (targetCurrentHeight - 1) + 1.0F;
                    final double tu = (double) (down_u * intervals) / (double) (targetCurrentWidth - 1) + 1.0F;
                    swx.prepareForInterpolation(tu, tv, ORIGINAL);
                    double x = swx.interpolateI();
                    swy.prepareForInterpolation(tu, tv, ORIGINAL);
                    double y = swy.interpolateI();
                    double up_x = x / factorWidth;
                    double up_y = y / factorHeight;
                    if (sourceMsk.getValue(up_x, up_y)) {
                        source.prepareForInterpolation(up_x, up_y, ORIGINAL);
                        fp.putPixelValue(u, v, target.getImage()[uv] - source.interpolateI());
                    } else {
                        fp.putPixelValue(u, v, 0);
                    }
                } else {
                    fp.putPixelValue(u, v, 0);
                }
            }
        }
        double min_val = output_ip.getProcessor().getMin();
        double max_val = output_ip.getProcessor().getMax();
        fp.setMinAndMax(min_val, max_val);
        output_ip.setProcessor("Output", fp);
        output_ip.updateImage();
        // Draw the grid on the target image ...............................
        // Some initialization
        int stepv = Math.min(Math.max(10, targetHeight / 15), 30);
        int stepu = Math.min(Math.max(10, targetWidth / 15), 30);
        final double[][] transformedImage = new double[sourceHeight][sourceWidth];
        double grid_colour = -1e-10;
        uv = 0;
        for (int v = 0; v < sourceHeight; v++) {
            for (int u = 0; u < sourceWidth; u++, uv++) {
                transformedImage[v][u] = source.getImage()[uv];
                if (transformedImage[v][u] > grid_colour) {
                    grid_colour = transformedImage[v][u];
                }
            }
        }
        // Draw grid
        for (int v = 0; v < targetHeight + stepv; v += stepv) {
            for (int u = 0; u < targetWidth + stepu; u += stepu) {
                double down_u = u * factorWidth;
                double down_v = v * factorHeight;
                final double tv = (double) (down_v * intervals) / (double) (targetCurrentHeight - 1) + 1.0F;
                final double tu = (double) (down_u * intervals) / (double) (targetCurrentWidth - 1) + 1.0F;
                swx.prepareForInterpolation(tu, tv, ORIGINAL);
                double x = swx.interpolateI();
                swy.prepareForInterpolation(tu, tv, ORIGINAL);
                double y = swy.interpolateI();
                double up_x = x / factorWidth;
                double up_y = y / factorHeight;
                // Draw horizontal line
                int uh = u + stepu;
                if (uh < targetWidth + stepu) {
                    double down_uh = uh * factorWidth;
                    final double tuh = (double) (down_uh * intervals) / (double) (targetCurrentWidth - 1) + 1.0F;
                    swx.prepareForInterpolation(tuh, tv, ORIGINAL);
                    double xh = swx.interpolateI();
                    swy.prepareForInterpolation(tuh, tv, ORIGINAL);
                    double yh = swy.interpolateI();
                    double up_xh = xh / factorWidth;
                    double up_yh = yh / factorHeight;
                    unwarpJMiscTools.drawLine(transformedImage, (int) Math.round(up_x), (int) Math.round(up_y), (int) Math.round(up_xh), (int) Math.round(up_yh), grid_colour);
                }
                // Draw vertical line
                int vv = v + stepv;
                if (vv < targetHeight + stepv) {
                    double down_vv = vv * factorHeight;
                    final double tvv = (double) (down_vv * intervals) / (double) (targetCurrentHeight - 1) + 1.0F;
                    swx.prepareForInterpolation(tu, tvv, ORIGINAL);
                    double xv = swx.interpolateI();
                    swy.prepareForInterpolation(tu, tvv, ORIGINAL);
                    double yv = swy.interpolateI();
                    double up_xv = xv / factorWidth;
                    double up_yv = yv / factorHeight;
                    unwarpJMiscTools.drawLine(transformedImage, (int) Math.round(up_x), (int) Math.round(up_y), (int) Math.round(up_xv), (int) Math.round(up_yv), grid_colour);
                }
            }
        }
        // Update the target image plus
        FloatProcessor fpg = new FloatProcessor(sourceWidth, sourceHeight);
        for (int v = 0; v < sourceHeight; v++) {
            for (int u = 0; u < sourceWidth; u++) {
                fpg.putPixelValue(u, v, transformedImage[v][u]);
            }
        }
        min_val = sourceImp.getProcessor().getMin();
        max_val = sourceImp.getProcessor().getMax();
        fpg.setMinAndMax(min_val, max_val);
        sourceImp.setProcessor(sourceImp.getTitle(), fpg);
        sourceImp.updateImage();
    }

    /*------------------------------------------------------------------*/
    private double[] xWeight(final double x, final int xIntervals, final boolean extended) {
        int length = xIntervals + 1;
        int j0 = 0;
        int jF = xIntervals;
        if (extended) {
            length += 2;
            j0--;
            jF++;
        }
        final double[] b = new double[length];
        final double interX = (double) xIntervals / (double) (targetCurrentWidth - 1);
        for (int j = j0; j <= jF; j++) {
            b[j - j0] = unwarpJMathTools.Bspline03(x * interX - (double) j);
        }
        return b;
    } /* end xWeight */

    /*------------------------------------------------------------------*/
    private double[] yWeight(final double y, final int yIntervals, final boolean extended) {
        int length = yIntervals + 1;
        int i0 = 0;
        int iF = yIntervals;
        if (extended) {
            length += 2;
            i0--;
            iF++;
        }
        final double[] b = new double[length];
        final double interY = (double) yIntervals / (double) (targetCurrentHeight - 1);
        for (int i = i0; i <= iF; i++) {
            b[i - i0] = unwarpJMathTools.Bspline03(y * interY - (double) i);
        }
        return b;
    } /* end yWeight */
    
} /* end class unwarpJTransformation */