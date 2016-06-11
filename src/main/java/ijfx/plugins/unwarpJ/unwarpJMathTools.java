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
/*====================================================================
|   unwarpJMask
\===================================================================*/
/*====================================================================
|   unwarpJMathTools
\===================================================================*/
class unwarpJMathTools {

    private static final double FLT_EPSILON = (double) Float.intBitsToFloat((int) 0x33FFFFFF);
    private static final int MAX_SVD_ITERATIONS = 1000;

    /*------------------------------------------------------------------*/
    public static double Bspline01(double x) {
        x = Math.abs(x);
        if (x < 1.0F) {
            return 1.0F - x;
        } else {
            return 0.0F;
        }
    } /* Bspline01 */

    /*------------------------------------------------------------------*/
    public static double Bspline02(double x) {
        x = Math.abs(x);
        if (x < 0.5F) {
            return 3.0F / 4.0F - x * x;
        } else if (x < 1.5F) {
            x -= 1.5F;
            return 0.5F * x * x;
        } else {
            return 0.0F;
        }
    } /* Bspline02 */

    /*------------------------------------------------------------------*/
    public static double Bspline03(double x) {
        x = Math.abs(x);
        if (x < 1.0F) {
            return 0.5F * x * x * (x - 2.0F) + (2.0F / 3.0F);
        } else if (x < 2.0F) {
            x -= 2.0F;
            return x * x * x / (-6.0F);
        } else {
            return 0.0F;
        }
    } /* Bspline03 */

    /*------------------------------------------------------------------*/
    public static double EuclideanNorm(final double a, final double b) {
        final double absa = Math.abs(a);
        final double absb = Math.abs(b);
        if (absb < absa) {
            return absa * Math.sqrt(1.0 + (absb * absb / (absa * absa)));
        } else {
            return (absb == 0.0F) ? (0.0F) : (absb * Math.sqrt(1.0 + (absa * absa / (absb * absb))));
        }
    } /* end EuclideanNorm */

    /*------------------------------------------------------------------*/
    public static boolean invertMatrixSVD(int Ydim, // Input,
    int Xdim, // Input,
    final double[][] B, // Input, matrix to invert
    final double[][] iB // Output, inverted matrix
    ) {
        boolean underconstrained = false;
        final double[] W = new double[Xdim];
        final double[][] V = new double[Xdim][Xdim];
        // B=UWV^t (U is stored in B)
        singularValueDecomposition(B, W, V);
        // B^-1=VW^-1U^t
        // Compute W^-1
        int Nzeros = 0;
        for (int k = 0; k < Xdim; k++) {
            if (Math.abs(W[k]) < FLT_EPSILON) {
                W[k] = 0.0F;
                Nzeros++;
            } else {
                W[k] = 1.0F / W[k];
            }
        }
        if (Ydim - Nzeros < Xdim) {
            underconstrained = true;
        }
        // Compute VW^-1
        for (int i = 0; i < Xdim; i++) {
            for (int j = 0; j < Xdim; j++) {
                V[i][j] *= W[j];
            }
        }
        // Compute B^-1
        // iB should have been already resized
        for (int i = 0; i < Xdim; i++) {
            for (int j = 0; j < Ydim; j++) {
                iB[i][j] = 0.0F;
                for (int k = 0; k < Xdim; k++) {
                    iB[i][j] += V[i][k] * B[j][k];
                }
            }
        }
        return underconstrained;
    } /* invertMatrixSVD */

    /*------------------------------------------------------------------*/
    /*********************************************************************
     * Gives the least-squares solution to (A * x = b) such that
     * (A^T * A)^-1 * A^T * b = x is a vector of size (column), where A is
     * a (line x column) matrix, and where b is a vector of size (line).
     * The result may differ from that obtained by a singular-value
     * decomposition in the cases where the least-squares solution is not
     * uniquely defined (SVD returns the solution of least norm, not QR).
     *
     * @param A An input matrix A[line][column] of size (line x column)
     * @param b An input vector b[line] of size (line)
     * @return An output vector x[column] of size (column)
     ********************************************************************/
    public static double[] linearLeastSquares(final double[][] A, final double[] b) {
        final int lines = A.length;
        final int columns = A[0].length;
        final double[][] Q = new double[lines][columns];
        final double[][] R = new double[columns][columns];
        final double[] x = new double[columns];
        double s;
        for (int i = 0; i < lines; i++) {
            for (int j = 0; j < columns; j++) {
                Q[i][j] = A[i][j];
            }
        }
        QRdecomposition(Q, R);
        for (int i = 0; i < columns; i++) {
            s = 0.0F;
            for (int j = 0; j < lines; j++) {
                s += Q[j][i] * b[j];
            }
            x[i] = s;
        }
        for (int i = columns - 1; 0 <= i; i--) {
            s = R[i][i];
            if ((s * s) == 0.0F) {
                x[i] = 0.0F;
            } else {
                x[i] /= s;
            }
            for (int j = i - 1; 0 <= j; j--) {
                x[j] -= R[j][i] * x[i];
            }
        }
        return x;
    } /* end linearLeastSquares */

    /*------------------------------------------------------------------*/
    public static double nchoosek(int n, int k) {
        if (k > n) {
            return 0;
        }
        if (k == 0) {
            return 1;
        }
        if (k == 1) {
            return n;
        }
        if (k > n / 2) {
            k = n - k;
        }
        double prod = 1;
        for (int i = 1; i <= k; i++) {
            prod *= (n - k + i) / i;
        }
        return prod;
    }

    /*------------------------------------------------------------------*/
    /*********************************************************************
     * Decomposes the (line x column) input matrix Q into an orthonormal
     * output matrix Q of same size (line x column) and an upper-diagonal
     * square matrix R of size (column x column), such that the matrix
     * product (Q * R) gives the input matrix, and such that the matrix
     * product (Q^T * Q) gives the identity.
     *
     * @param Q An in-place (line x column) matrix Q[line][column], which
     * expects as input the matrix to decompose, and which returns as
     * output an orthonormal matrix
     * @param R An output (column x column) square matrix R[column][column]
     ********************************************************************/
    public static void QRdecomposition(final double[][] Q, final double[][] R) {
        final int lines = Q.length;
        final int columns = Q[0].length;
        final double[][] A = new double[lines][columns];
        double s;
        for (int j = 0; j < columns; j++) {
            for (int i = 0; i < lines; i++) {
                A[i][j] = Q[i][j];
            }
            for (int k = 0; k < j; k++) {
                s = 0.0F;
                for (int i = 0; i < lines; i++) {
                    s += A[i][j] * Q[i][k];
                }
                for (int i = 0; i < lines; i++) {
                    Q[i][j] -= s * Q[i][k];
                }
            }
            s = 0.0F;
            for (int i = 0; i < lines; i++) {
                s += Q[i][j] * Q[i][j];
            }
            if ((s * s) == 0.0F) {
                s = 0.0F;
            } else {
                s = 1.0F / Math.sqrt(s);
            }
            for (int i = 0; i < lines; i++) {
                Q[i][j] *= s;
            }
        }
        for (int i = 0; i < columns; i++) {
            for (int j = 0; j < i; j++) {
                R[i][j] = 0.0F;
            }
            for (int j = i; j < columns; j++) {
                R[i][j] = 0.0F;
                for (int k = 0; k < lines; k++) {
                    R[i][j] += Q[k][i] * A[k][j];
                }
            }
        }
    } /* end QRdecomposition */

    /* -----------------------------------------------------------------*/
    public static void showMatrix(int Ydim, int Xdim, final double[][] A) {
        for (int i = 0; i < Ydim; i++) {
            for (int j = 0; j < Xdim; j++) {
                System.out.print(A[i][j] + " ");
            }
            System.out.println();
        }
    }

    /*------------------------------------------------------------------*/
    public static void singularValueDecomposition(final double[][] U, final double[] W, final double[][] V) {
        final int lines = U.length;
        final int columns = U[0].length;
        final double[] rv1 = new double[columns];
        double norm;
        double scale;
        double c;
        double f;
        double g;
        double h;
        double s;
        double x;
        double y;
        double z;
        int l = 0;
        int nm = 0;
        boolean flag;
        g = scale = norm = 0.0F;
        for (int i = 0; i < columns; i++) {
            l = i + 1;
            rv1[i] = scale * g;
            g = s = scale = 0.0F;
            if (i < lines) {
                for (int k = i; k < lines; k++) {
                    scale += Math.abs(U[k][i]);
                }
                if (scale != 0.0) {
                    for (int k = i; k < lines; k++) {
                        U[k][i] /= scale;
                        s += U[k][i] * U[k][i];
                    }
                    f = U[i][i];
                    g = (0.0 <= f) ? (-Math.sqrt((double) s)) : (Math.sqrt((double) s));
                    h = f * g - s;
                    U[i][i] = f - g;
                    for (int j = l; j < columns; j++) {
                        s = 0.0F;
                        for (int k = i; k < lines; k++) {
                            s += U[k][i] * U[k][j];
                        }
                        f = s / h;
                        for (int k = i; k < lines; k++) {
                            U[k][j] += f * U[k][i];
                        }
                    }
                    for (int k = i; k < lines; k++) {
                        U[k][i] *= scale;
                    }
                }
            }
            W[i] = scale * g;
            g = s = scale = 0.0F;
            if ((i < lines) && (i != (columns - 1))) {
                for (int k = l; k < columns; k++) {
                    scale += Math.abs(U[i][k]);
                }
                if (scale != 0.0) {
                    for (int k = l; k < columns; k++) {
                        U[i][k] /= scale;
                        s += U[i][k] * U[i][k];
                    }
                    f = U[i][l];
                    g = (0.0 <= f) ? (-Math.sqrt(s)) : (Math.sqrt(s));
                    h = f * g - s;
                    U[i][l] = f - g;
                    for (int k = l; k < columns; k++) {
                        rv1[k] = U[i][k] / h;
                    }
                    for (int j = l; j < lines; j++) {
                        s = 0.0F;
                        for (int k = l; k < columns; k++) {
                            s += U[j][k] * U[i][k];
                        }
                        for (int k = l; k < columns; k++) {
                            U[j][k] += s * rv1[k];
                        }
                    }
                    for (int k = l; k < columns; k++) {
                        U[i][k] *= scale;
                    }
                }
            }
            norm = ((Math.abs(W[i]) + Math.abs(rv1[i])) < norm) ? (norm) : (Math.abs(W[i]) + Math.abs(rv1[i]));
        }
        for (int i = columns - 1; 0 <= i; i--) {
            if (i < (columns - 1)) {
                if (g != 0.0) {
                    for (int j = l; j < columns; j++) {
                        V[j][i] = U[i][j] / (U[i][l] * g);
                    }
                    for (int j = l; j < columns; j++) {
                        s = 0.0F;
                        for (int k = l; k < columns; k++) {
                            s += U[i][k] * V[k][j];
                        }
                        for (int k = l; k < columns; k++) {
                            if (s != 0.0) {
                                V[k][j] += s * V[k][i];
                            }
                        }
                    }
                }
                for (int j = l; j < columns; j++) {
                    V[i][j] = V[j][i] = 0.0F;
                }
            }
            V[i][i] = 1.0F;
            g = rv1[i];
            l = i;
        }
        for (int i = (lines < columns) ? (lines - 1) : (columns - 1); 0 <= i; i--) {
            l = i + 1;
            g = W[i];
            for (int j = l; j < columns; j++) {
                U[i][j] = 0.0F;
            }
            if (g != 0.0) {
                g = 1.0F / g;
                for (int j = l; j < columns; j++) {
                    s = 0.0F;
                    for (int k = l; k < lines; k++) {
                        s += U[k][i] * U[k][j];
                    }
                    f = s * g / U[i][i];
                    for (int k = i; k < lines; k++) {
                        if (f != 0.0) {
                            U[k][j] += f * U[k][i];
                        }
                    }
                }
                for (int j = i; j < lines; j++) {
                    U[j][i] *= g;
                }
            } else {
                for (int j = i; j < lines; j++) {
                    U[j][i] = 0.0F;
                }
            }
            U[i][i] += 1.0F;
        }
        for (int k = columns - 1; 0 <= k; k--) {
            for (int its = 1; its <= MAX_SVD_ITERATIONS; its++) {
                flag = true;
                for (l = k; 0 <= l; l--) {
                    nm = l - 1;
                    if ((Math.abs(rv1[l]) + norm) == norm) {
                        flag = false;
                        break;
                    }
                    if ((Math.abs(W[nm]) + norm) == norm) {
                        break;
                    }
                }
                if (flag) {
                    c = 0.0F;
                    s = 1.0F;
                    for (int i = l; i <= k; i++) {
                        f = s * rv1[i];
                        rv1[i] *= c;
                        if ((Math.abs(f) + norm) == norm) {
                            break;
                        }
                        g = W[i];
                        h = EuclideanNorm(f, g);
                        W[i] = h;
                        h = 1.0F / h;
                        c = g * h;
                        s = -f * h;
                        for (int j = 0; j < lines; j++) {
                            y = U[j][nm];
                            z = U[j][i];
                            U[j][nm] = y * c + z * s;
                            U[j][i] = z * c - y * s;
                        }
                    }
                }
                z = W[k];
                if (l == k) {
                    if (z < 0.0) {
                        W[k] = -z;
                        for (int j = 0; j < columns; j++) {
                            V[j][k] = -V[j][k];
                        }
                    }
                    break;
                }
                if (its == MAX_SVD_ITERATIONS) {
                    return;
                }
                x = W[l];
                nm = k - 1;
                y = W[nm];
                g = rv1[nm];
                h = rv1[k];
                f = ((y - z) * (y + z) + (g - h) * (g + h)) / (2.0F * h * y);
                g = EuclideanNorm(f, 1.0F);
                f = ((x - z) * (x + z) + h * ((y / (f + ((0.0 <= f) ? (Math.abs(g)) : (-Math.abs(g))))) - h)) / x;
                c = s = 1.0F;
                for (int j = l; j <= nm; j++) {
                    int i = j + 1;
                    g = rv1[i];
                    y = W[i];
                    h = s * g;
                    g = c * g;
                    z = EuclideanNorm(f, h);
                    rv1[j] = z;
                    c = f / z;
                    s = h / z;
                    f = x * c + g * s;
                    g = g * c - x * s;
                    h = y * s;
                    y *= c;
                    for (int jj = 0; jj < columns; jj++) {
                        x = V[jj][j];
                        z = V[jj][i];
                        V[jj][j] = x * c + z * s;
                        V[jj][i] = z * c - x * s;
                    }
                    z = EuclideanNorm(f, h);
                    W[j] = z;
                    if (z != 0.0F) {
                        z = 1.0F / z;
                        c = f * z;
                        s = h * z;
                    }
                    f = c * g + s * y;
                    x = c * y - s * g;
                    for (int jj = 0; jj < lines; jj++) {
                        y = U[jj][j];
                        z = U[jj][i];
                        U[jj][j] = y * c + z * s;
                        U[jj][i] = z * c - y * s;
                    }
                }
                rv1[l] = 0.0F;
                rv1[k] = f;
                W[k] = x;
            }
        }
    } /* end singularValueDecomposition */

    public static void singularValueBackSubstitution(final double[][] U, /* input matrix */ final double[] W, /* vector of singular values */ final double[][] V, /* untransposed orthogonal matrix */ final double[] B, /* input vector */ final double[] X /* returned solution */ ) {
        /* solve (U.W.Transpose(V)).X == B in terms of X */
        /* {U, W, V} are given by SingularValueDecomposition */
        /* by convention, set w[i,j]=0 to get (1/w[i,j])=0 */
        /* the size of the input matrix U is (Lines x Columns) */
        /* the size of the vector (1/W) of singular values is (Columns) */
        /* the size of the untransposed orthogonal matrix V is (Columns x Columns) */
        /* the size of the input vector B is (Lines) */
        /* the size of the output vector X is (Columns) */
        final int Lines = U.length;
        final int Columns = U[0].length;
        double[] aux = new double[Columns];
        // A=UWV^t
        // A^-1=VW^-1U^t
        // X=A^-1*B
        // Perform aux=W^-1 U^t B
        for (int i = 0; i < Columns; i++) {
            aux[i] = 0.0F;
            if (Math.abs(W[i]) > FLT_EPSILON) {
                for (int j = 0; j < Lines; j++) {
                    aux[i] += U[j][i] * B[j]; // U^t B
                }
                aux[i] /= W[i]; // W^-1 U^t B
            }
        }
        // Perform X=V aux
        for (int i = 0; i < Columns; i++) {
            X[i] = 0.0F;
            for (int j = 0; j < Columns; j++) {
                X[i] += V[i][j] * aux[j];
            }
        }
    }
    
} /* End MathTools */