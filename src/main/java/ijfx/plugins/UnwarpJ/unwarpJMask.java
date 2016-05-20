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
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.Vector;

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
/* This class is responsible for the mask preprocessing that takes
place concurrently with user-interface events. It contains methods
to compute the mask pyramids. */
class unwarpJMask {

    /* begin class unwarpJMask */
    /*....................................................................
    Private variables
    ....................................................................*/
    // Mask related
    private boolean[] mask;
    private int width;
    private int height;
    private Polygon polygon = null;
    private boolean mask_from_the_stack;

    /*....................................................................
    Public methods
    ....................................................................*/
    /* Bounding box for the mask.
    An array is returned with the convention [x0,y0,xF,yF]. This array
    is returned in corners. This vector should be already resized. */
    public void BoundingBox(int[] corners) {
        if (polygon.npoints != 0) {
            Rectangle boundingbox = polygon.getBounds();
            corners[0] = (int) boundingbox.x;
            corners[1] = (int) boundingbox.y;
            corners[2] = corners[0] + (int) boundingbox.width;
            corners[3] = corners[1] + (int) boundingbox.height;
        } else {
            corners[0] = 0;
            corners[1] = 0;
            corners[2] = width;
            corners[3] = height;
        }
    }

    /*------------------------------------------------------------------*/
    /* Set to true every pixel of the full-size mask. */
    public void clearMask() {
        int k = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                mask[k++] = true;
            }
        }
        polygon = new Polygon();
    } /* end clearMask */

    /*------------------------------------------------------------------*/
    /* Fill the mask associated to the mask points. */
    public void fillMask(int tool) {
        int k = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                mask[k] = polygon.contains(x, y);
                if (tool == unwarpJPointAction.INVERTMASK) {
                    mask[k] = !mask[k];
                }
                k++;
            }
        }
    }

    /*------------------------------------------------------------------*/
    /* Returns the value of the mask at a certain pixel.
    If the sample is not integer then the closest point is returned. */
    public boolean getValue(double x, double y) {
        int u = (int) Math.round(x);
        int v = (int) Math.round(y);
        if (u < 0 || u >= width || v < 0 || v >= height) {
            return false;
        } else {
            return mask[v * width + u];
        }
    }

    /*------------------------------------------------------------------*/
    /* Get a point from the mask. */
    public Point getPoint(int i) {
        return new Point(polygon.xpoints[i], polygon.ypoints[i]);
    }

    /*------------------------------------------------------------------*/
    /* True if the mask was taken from the stack. */
    public boolean isFromStack() {
        return mask_from_the_stack;
    }

    /*------------------------------------------------------------------*/
    /* Get the number of points in the mask. */
    public int numberOfMaskPoints() {
        return polygon.npoints;
    }

    /*------------------------------------------------------------------*/
    /* Read mask from file.
    An error is shown if the file read is not of the same size as the
    previous mask. */
    public void readFile(String filename) {
        ImagePlus aux = new ImagePlus(filename);
        if (aux.getWidth() != width || aux.getHeight() != height) {
            IJ.error("Mask in file is not of the expected size");
        }
        ImageProcessor ip = aux.getProcessor();
        int k = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++, k++) {
                if (ip.getPixelValue(x, y) != 0) {
                    mask[k] = true;
                } else {
                    mask[k] = false;
                }
            }
        }
    }

    /*------------------------------------------------------------------*/
    /* Show mask. */
    public void showMask() {
        double[][] img = new double[height][width];
        int k = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (mask[k++]) {
                    img[y][x] = 1;
                } else {
                    img[y][x] = 0;
                }
            }
        }
        unwarpJMiscTools.showImage("Mask", img);
    }

    /*------------------------------------------------------------------*/
    /* Set the mask points. */
    public void setMaskPoints(final Vector listMaskPoints) {
        int imax = listMaskPoints.size();
        for (int i = 0; i < imax; i++) {
            Point p = (Point) listMaskPoints.elementAt(i);
            polygon.addPoint(p.x, p.y);
        }
    }

    /*------------------------------------------------------------------*/
    /* Sets the value of the mask at a certain pixel. */
    public void setValue(int u, int v, boolean value) {
        if (u >= 0 && u < width && v >= 0 && v < height) {
            mask[v * width + u] = value;
        }
    }

    /*------------------------------------------------------------------*/
    /* Empty constructor, the input image is used only to take the
    image size. */
    public unwarpJMask(final ImageProcessor ip, boolean take_mask) {
        width = ip.getWidth();
        height = ip.getHeight();
        mask = new boolean[width * height];
        if (!take_mask) {
            mask_from_the_stack = false;
            clearMask();
        } else {
            mask_from_the_stack = true;
            int k = 0;
            if (ip instanceof ByteProcessor) {
                final byte[] pixels = (byte[]) ip.getPixels();
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++, k++) {
                        mask[k] = (pixels[k] != 0);
                    }
                }
            } else if (ip instanceof ShortProcessor) {
                final short[] pixels = (short[]) ip.getPixels();
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++, k++) {
                        mask[k] = (pixels[k] != 0);
                    }
                }
            } else if (ip instanceof FloatProcessor) {
                final float[] pixels = (float[]) ip.getPixels();
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++, k++) {
                        mask[k] = (pixels[k] != 0.0F);
                    }
                }
            }
        }
    } /* end unwarpJMask */
    
} /* end class unwarpJMask */