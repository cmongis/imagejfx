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

import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageWindow;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Stack;
import java.util.StringTokenizer;

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
|   unwarpJMiscTools
\===================================================================*/
class unwarpJMiscTools {

    /* Apply a given splines transformation to the source image.
    The source image is modified. The target image is used to know
    the output size. */
    public static void applyTransformationToSource(ImagePlus sourceImp, ImagePlus targetImp, unwarpJImageModel source, int intervals, double[][] cx, double[][] cy) {
        int targetHeight = targetImp.getProcessor().getHeight();
        int targetWidth = targetImp.getProcessor().getWidth();
        int sourceHeight = sourceImp.getProcessor().getHeight();
        int sourceWidth = sourceImp.getProcessor().getWidth();
        // Ask for memory for the transformation
        double[][] transformation_x = new double[targetHeight][targetWidth];
        double[][] transformation_y = new double[targetHeight][targetWidth];
        // Compute the deformation
        // Set these coefficients to an interpolator
        unwarpJImageModel swx = new unwarpJImageModel(cx);
        unwarpJImageModel swy = new unwarpJImageModel(cy);
        // Compute the transformation mapping
        boolean ORIGINAL = false;
        for (int v = 0; v < targetHeight; v++) {
            final double tv = (double) (v * intervals) / (double) (targetHeight - 1) + 1.0F;
            for (int u = 0; u < targetWidth; u++) {
                final double tu = (double) (u * intervals) / (double) (targetWidth - 1) + 1.0F;
                swx.prepareForInterpolation(tu, tv, ORIGINAL);
                transformation_x[v][u] = swx.interpolateI();
                swy.prepareForInterpolation(tu, tv, ORIGINAL);
                transformation_y[v][u] = swy.interpolateI();
            }
        }
        // Compute the warped image
        FloatProcessor fp = new FloatProcessor(targetWidth, targetHeight);
        for (int v = 0; v < targetHeight; v++) {
            for (int u = 0; u < targetWidth; u++) {
                final double x = transformation_x[v][u];
                final double y = transformation_y[v][u];
                if (x >= 0 && x < sourceWidth && y >= 0 && y < sourceHeight) {
                    source.prepareForInterpolation(x, y, ORIGINAL);
                    fp.putPixelValue(u, v, source.interpolateI());
                } else {
                    fp.putPixelValue(u, v, 0);
                }
            }
        }
        fp.resetMinAndMax();
        sourceImp.setProcessor(sourceImp.getTitle(), fp);
        sourceImp.updateImage();
    }

    /*------------------------------------------------------------------*/
    /* Draw an arrow between two points.
    The arrow head is in (x2,y2) */
    public static void drawArrow(double[][] canvas, int x1, int y1, int x2, int y2, double color, int arrow_size) {
        drawLine(canvas, x1, y1, x2, y2, color);
        int arrow_size2 = 2 * arrow_size;
        // Do not draw the arrow_head if the arrow is very small
        if ((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1) < arrow_size * arrow_size) {
            return;
        }
        // Vertical arrow
        if (x2 == x1) {
            if (y2 > y1) {
                drawLine(canvas, x2, y2, x2 - arrow_size, y2 - arrow_size2, color);
                drawLine(canvas, x2, y2, x2 + arrow_size, y2 - arrow_size2, color);
            } else {
                drawLine(canvas, x2, y2, x2 - arrow_size, y2 + arrow_size2, color);
                drawLine(canvas, x2, y2, x2 + arrow_size, y2 + arrow_size2, color);
            }
        } else if (y2 == y1) {
            if (x2 > x1) {
                drawLine(canvas, x2, y2, x2 - arrow_size2, y2 - arrow_size, color);
                drawLine(canvas, x2, y2, x2 - arrow_size2, y2 + arrow_size, color);
            } else {
                drawLine(canvas, x2, y2, x2 + arrow_size2, y2 - arrow_size, color);
                drawLine(canvas, x2, y2, x2 + arrow_size2, y2 + arrow_size, color);
            }
        } else {
            // Calculate the angle of rotation and adjust for the quadrant
            double t1 = Math.abs(new Integer(y2 - y1).doubleValue());
            double t2 = Math.abs(new Integer(x2 - x1).doubleValue());
            double theta = Math.atan(t1 / t2);
            if (x2 < x1) {
                if (y2 < y1) {
                    theta = Math.PI + theta;
                } else {
                    theta = -(Math.PI + theta);
                }
            } else if (x2 > x1 && y2 < y1) {
                theta = 2 * Math.PI - theta;
            }
            double cosTheta = Math.cos(theta);
            double sinTheta = Math.sin(theta);
            // Create the other points and translate the arrow to the origin
            java.awt.Point p2 = new java.awt.Point(-arrow_size2, -arrow_size);
            java.awt.Point p3 = new java.awt.Point(-arrow_size2, +arrow_size);
            // Rotate the points (without using matrices!)
            int x = new Long(Math.round((cosTheta * p2.x) - (sinTheta * p2.y))).intValue();
            p2.y = new Long(Math.round((sinTheta * p2.x) + (cosTheta * p2.y))).intValue();
            p2.x = x;
            x = new Long(Math.round((cosTheta * p3.x) - (sinTheta * p3.y))).intValue();
            p3.y = new Long(Math.round((sinTheta * p3.x) + (cosTheta * p3.y))).intValue();
            p3.x = x;
            // Translate back to desired location and add to polygon
            p2.translate(x2, y2);
            p3.translate(x2, y2);
            drawLine(canvas, x2, y2, p2.x, p2.y, color);
            drawLine(canvas, x2, y2, p3.x, p3.y, color);
        }
    }

    /*------------------------------------------------------------------*/
    /* Draw a line between two points.
    Bresenham's algorithm */
    public static void drawLine(double[][] canvas, int x1, int y1, int x2, int y2, double color) {
        int temp;
        int dy_neg = 1;
        int dx_neg = 1;
        int switch_x_y = 0;
        int neg_slope = 0;
        int tempx;
        int tempy;
        int dx = x2 - x1;
        if (dx == 0) {
            if (y1 > y2) {
                for (int n = y2; n <= y1; n++) {
                    Point(canvas, n, x1, color);
                }
                return;
            } else {
                for (int n = y1; n <= y2; n++) {
                    Point(canvas, n, x1, color);
                }
                return;
            }
        }
        int dy = y2 - y1;
        if (dy == 0) {
            if (x1 > x2) {
                for (int n = x2; n <= x1; n++) {
                    Point(canvas, y1, n, color);
                }
                return;
            } else {
                for (int n = x1; n <= x2; n++) {
                    Point(canvas, y1, n, color);
                }
                return;
            }
        }
        float m = (float) dy / dx;
        if (m > 1 || m < -1) {
            temp = x1;
            x1 = y1;
            y1 = temp;
            temp = x2;
            x2 = y2;
            y2 = temp;
            dx = x2 - x1;
            dy = y2 - y1;
            m = (float) dy / dx;
            switch_x_y = 1;
        }
        if (x1 > x2) {
            temp = x1;
            x1 = x2;
            x2 = temp;
            temp = y1;
            y1 = y2;
            y2 = temp;
            dx = x2 - x1;
            dy = y2 - y1;
            m = (float) dy / dx;
        }
        if (m < 0) {
            if (dy < 0) {
                dy_neg = -1;
                dx_neg = 1;
            } else {
                dy_neg = 1;
                dx_neg = -1;
            }
            neg_slope = 1;
        }
        int d = 2 * (dy * dy_neg) - (dx * dx_neg);
        int incrH = 2 * dy * dy_neg;
        int incrHV = 2 * ((dy * dy_neg) - (dx * dx_neg));
        int x = x1;
        int y = y1;
        tempx = x;
        tempy = y;
        if (switch_x_y == 1) {
            temp = x;
            x = y;
            y = temp;
        }
        Point(canvas, y, x, color);
        x = tempx;
        y = tempy;
        while (x < x2) {
            if (d <= 0) {
                x++;
                d += incrH;
            } else {
                d += incrHV;
                x++;
                if (neg_slope == 0) {
                    y++;
                } else {
                    y--;
                }
            }
            tempx = x;
            tempy = y;
            if (switch_x_y == 1) {
                temp = x;
                x = y;
                y = temp;
            }
            Point(canvas, y, x, color);
            x = tempx;
            y = tempy;
        }
    }

    /*------------------------------------------------------------------*/
    public static void extractImage(final ImageProcessor ip, double[] image) {
        int k = 0;
        int height = ip.getHeight();
        int width = ip.getWidth();
        if (ip instanceof ByteProcessor) {
            final byte[] pixels = (byte[]) ip.getPixels();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++, k++) {
                    image[k] = (double) (pixels[k] & 0xFF);
                }
            }
        } else if (ip instanceof ShortProcessor) {
            final short[] pixels = (short[]) ip.getPixels();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++, k++) {
                    if (pixels[k] < (short) 0) {
                        image[k] = (double) pixels[k] + 65536.0F;
                    } else {
                        image[k] = (double) pixels[k];
                    }
                }
            }
        } else if (ip instanceof FloatProcessor) {
            final float[] pixels = (float[]) ip.getPixels();
            for (int p = 0; p < height * width; p++) {
                image[p] = pixels[p];
            }
        }
    }

    public static void extractImage(final ImageProcessor ip, double[][] image) {
        int k = 0;
        int height = ip.getHeight();
        int width = ip.getWidth();
        if (ip instanceof ByteProcessor) {
            final byte[] pixels = (byte[]) ip.getPixels();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++, k++) {
                    image[y][x] = (double) (pixels[k] & 0xFF);
                }
            }
        } else if (ip instanceof ShortProcessor) {
            final short[] pixels = (short[]) ip.getPixels();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++, k++) {
                    if (pixels[k] < (short) 0) {
                        image[y][x] = (double) pixels[k] + 65536.0F;
                    } else {
                        image[y][x] = (double) pixels[k];
                    }
                }
            }
        } else if (ip instanceof FloatProcessor) {
            final float[] pixels = (float[]) ip.getPixels();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++, k++) {
                    image[y][x] = pixels[k];
                }
            }
        }
    }

    /*------------------------------------------------------------------*/
    /* Load landmarks from file. */
    public static void loadPoints(String filename, Stack sourceStack, Stack targetStack) {
        java.awt.Point sourcePoint;
        java.awt.Point targetPoint;
        try {
            final FileReader fr = new FileReader(filename);
            final BufferedReader br = new BufferedReader(fr);
            String line;
            String index;
            String xSource;
            String ySource;
            String xTarget;
            String yTarget;
            int separatorIndex;
            int k = 1;
            if (!(line = br.readLine()).equals("Index\txSource\tySource\txTarget\tyTarget")) {
                fr.close();
                IJ.write("Line " + k + ": 'Index\txSource\tySource\txTarget\tyTarget'");
                return;
            }
            ++k;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                separatorIndex = line.indexOf('\t');
                if (separatorIndex == -1) {
                    fr.close();
                    IJ.write("Line " + k + ": #Index# <tab> #xSource# <tab> #ySource# <tab> #xTarget# <tab> #yTarget#");
                    return;
                }
                index = line.substring(0, separatorIndex);
                index = index.trim();
                line = line.substring(separatorIndex);
                line = line.trim();
                separatorIndex = line.indexOf('\t');
                if (separatorIndex == -1) {
                    fr.close();
                    IJ.write("Line " + k + ": #Index# <tab> #xSource# <tab> #ySource# <tab> #xTarget# <tab> #yTarget#");
                    return;
                }
                xSource = line.substring(0, separatorIndex);
                xSource = xSource.trim();
                line = line.substring(separatorIndex);
                line = line.trim();
                separatorIndex = line.indexOf('\t');
                if (separatorIndex == -1) {
                    fr.close();
                    IJ.write("Line " + k + ": #Index# <tab> #xSource# <tab> #ySource# <tab> #xTarget# <tab> #yTarget#");
                    return;
                }
                ySource = line.substring(0, separatorIndex);
                ySource = ySource.trim();
                line = line.substring(separatorIndex);
                line = line.trim();
                separatorIndex = line.indexOf('\t');
                if (separatorIndex == -1) {
                    fr.close();
                    IJ.write("Line " + k + ": #Index# <tab> #xSource# <tab> #ySource# <tab> #xTarget# <tab> #yTarget#");
                    return;
                }
                xTarget = line.substring(0, separatorIndex);
                xTarget = xTarget.trim();
                yTarget = line.substring(separatorIndex);
                yTarget = yTarget.trim();
                sourcePoint = new java.awt.Point(Integer.valueOf(xSource).intValue(), Integer.valueOf(ySource).intValue());
                sourceStack.push(sourcePoint);
                targetPoint = new java.awt.Point(Integer.valueOf(xTarget).intValue(), Integer.valueOf(yTarget).intValue());
                targetStack.push(targetPoint);
            }
            fr.close();
        } catch (FileNotFoundException e) {
            IJ.error("File not found exception" + e);
            return;
        } catch (IOException e) {
            IJ.error("IOException exception" + e);
            return;
        } catch (NumberFormatException e) {
            IJ.error("Number format exception" + e);
            return;
        }
    }

    public static void loadTransformation(String filename, final double[][] cx, final double[][] cy) {
        try {
            final FileReader fr = new FileReader(filename);
            final BufferedReader br = new BufferedReader(fr);
            String line;
            // Read number of intervals
            line = br.readLine();
            int lineN = 1;
            StringTokenizer st = new StringTokenizer(line, "=");
            if (st.countTokens() != 2) {
                fr.close();
                IJ.write("Line " + lineN + "+: Cannot read number of intervals");
                return;
            }
            st.nextToken();
            int intervals = Integer.valueOf(st.nextToken()).intValue();
            // Skip next 2 lines
            line = br.readLine();
            line = br.readLine();
            lineN += 2;
            // Read the cx coefficients
            for (int i = 0; i < intervals + 3; i++) {
                line = br.readLine();
                lineN++;
                st = new StringTokenizer(line);
                if (st.countTokens() != intervals + 3) {
                    fr.close();
                    IJ.write("Line " + lineN + ": Cannot read enough coefficients");
                    return;
                }
                for (int j = 0; j < intervals + 3; j++) {
                    cx[i][j] = Double.valueOf(st.nextToken()).doubleValue();
                }
            }
            // Skip next 2 lines
            line = br.readLine();
            line = br.readLine();
            lineN += 2;
            // Read the cy coefficients
            for (int i = 0; i < intervals + 3; i++) {
                line = br.readLine();
                lineN++;
                st = new StringTokenizer(line);
                if (st.countTokens() != intervals + 3) {
                    fr.close();
                    IJ.write("Line " + lineN + ": Cannot read enough coefficients");
                    return;
                }
                for (int j = 0; j < intervals + 3; j++) {
                    cy[i][j] = Double.valueOf(st.nextToken()).doubleValue();
                }
            }
            fr.close();
        } catch (FileNotFoundException e) {
            IJ.error("File not found exception" + e);
            return;
        } catch (IOException e) {
            IJ.error("IOException exception" + e);
            return;
        } catch (NumberFormatException e) {
            IJ.error("Number format exception" + e);
            return;
        }
    }

    /*------------------------------------------------------------------*/
    public static int numberOfIntervalsOfTransformation(String filename) {
        try {
            final FileReader fr = new FileReader(filename);
            final BufferedReader br = new BufferedReader(fr);
            String line;
            // Read number of intervals
            line = br.readLine();
            int lineN = 1;
            StringTokenizer st = new StringTokenizer(line, "=");
            if (st.countTokens() != 2) {
                fr.close();
                IJ.write("Line " + lineN + "+: Cannot read number of intervals");
                return -1;
            }
            st.nextToken();
            int intervals = Integer.valueOf(st.nextToken()).intValue();
            fr.close();
            return intervals;
        } catch (FileNotFoundException e) {
            IJ.error("File not found exception" + e);
            return -1;
        } catch (IOException e) {
            IJ.error("IOException exception" + e);
            return -1;
        } catch (NumberFormatException e) {
            IJ.error("Number format exception" + e);
            return -1;
        }
    }

    /*------------------------------------------------------------------*/
    /* Plot a point in a canvas. */
    public static void Point(double[][] canvas, int y, int x, double color) {
        if (y < 0 || y >= canvas.length) {
            return;
        }
        if (x < 0 || x >= canvas[0].length) {
            return;
        }
        canvas[y][x] = color;
    }

    /*------------------------------------------------------------------*/
    public static void printMatrix(final String title, final double[][] array) {
        int Ydim = array.length;
        int Xdim = array[0].length;
        System.out.println(title);
        for (int i = 0; i < Ydim; i++) {
            for (int j = 0; j < Xdim; j++) {
                System.out.print(array[i][j] + " ");
            }
            System.out.println();
        }
    }

    /*------------------------------------------------------------------*/
    public static void showImage(final String title, final double[] array, final int Ydim, final int Xdim) {
        final FloatProcessor fp = new FloatProcessor(Xdim, Ydim);
        int ij = 0;
        for (int i = 0; i < Ydim; i++) {
            for (int j = 0; j < Xdim; j++, ij++) {
                fp.putPixelValue(j, i, array[ij]);
            }
        }
        fp.resetMinAndMax();
        final ImagePlus ip = new ImagePlus(title, fp);
        final ImageWindow iw = new ImageWindow(ip);
        ip.updateImage();
    }

    /*------------------------------------------------------------------*/
    public static void showImage(final String title, final double[][] array) {
        int Ydim = array.length;
        int Xdim = array[0].length;
        final FloatProcessor fp = new FloatProcessor(Xdim, Ydim);
        for (int i = 0; i < Ydim; i++) {
            for (int j = 0; j < Xdim; j++) {
                fp.putPixelValue(j, i, array[i][j]);
            }
        }
        fp.resetMinAndMax();
        final ImagePlus ip = new ImagePlus(title, fp);
        final ImageWindow iw = new ImageWindow(ip);
        ip.updateImage();
    }
    
} /* End of MiscTools class */