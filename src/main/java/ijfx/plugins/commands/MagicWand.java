/*
 * /*
 *     This file is part of ImageJ FX.
 *
 *     ImageJ FX is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     ImageJ FX is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with ImageJ FX.  If not, see <http://www.gnu.org/licenses/>. 
 *
 * 	Copyright 2015,2016 Cyril MONGIS, Michael Knop
 *
 */
package ijfx.plugins.commands;

import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.ThresholdToSelection;
import ij.plugin.frame.Recorder;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ijfx.plugins.adapter.IJ1Service;
import ijfx.service.object_detection.ObjectDetectionService;
import java.util.List;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.Position;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.OverlayService;
import net.imagej.overlay.Overlay;
import net.imglib2.RandomAccess;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.ui.UIService;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class MagicWand implements Command{

    @Parameter
    private Double x = 0d;

    @Parameter
    private Double y = 0d;

    @Parameter
    private ImageDisplay imageDisplay;
    
    @Parameter
    IJ1Service iJ1Service;

    @Parameter
    private ImageDisplayService imageDisplayService;

    @Parameter
    private UIService uiService;

    @Parameter
    DatasetService datasetService;

    @Parameter
    ObjectDetectionService objectDetectionService;
    
    @Parameter
    OverlayService overlayService;
    
    public Dataset getDataset() {

        return imageDisplayService.getActiveDataset(imageDisplay);
    }

    
    
    private int originX;

    private int originY;

    @Override
    public void run() {

        
        
        Position position = imageDisplayService.getActivePosition(imageDisplay);
        Dataset dataset = getDataset();
        
        long pos[] = new long[position.numDimensions()];

        position.localize(pos);

        getPixelsNears(pos, dataset.randomAccess());
        
        ImagePlus imp = iJ1Service.unwrapDataset(dataset);
        
        iJ1Service.configureImagePlus(imp, imageDisplay);
       
        
        doWand(imp, originX, originY, 1);
        
        
        //System.out.println(maskIp);
       
        //maskIp.threshold(1);
        //maskIp.invert();
        
        new ImagePlus("",maskIp).show();
        
        List<Overlay> overlayFromImagePlus = objectDetectionService.getOverlayFromImagePlus(new ImagePlus("",maskIp));

        overlayService.addOverlays(imageDisplay, overlayFromImagePlus);

    }

    public <T extends RealType> void getPixelsNears(long[] pos, RandomAccess<T> r) {

        originX = (int) Math.round(this.x);
        originY = (int) Math.round(this.y);
        //r.setPosition(pos);
        //r.setPosition(0,(int)Math.round(x));
        //r.setPosition(1,(int)Math.round(y));
       // r.setPosition(new int[]{originX, originY});
       // T value = r.get();

    }

    /**
     * An ImageJ magic wand with selectable tolerance and gradient detection.
     * This PlugIn works on the gray value for RGB images
     *
     * Parameters ==========
     *
     * Gray Value Tolerance: The selection is expanded to all image points as
     * long as the difference between the gray level (brightness) of the point
     * clicked and the image point is less than the Gray Value Tolerance.
     *
     * Gradient Tolerance: Irrespective of Gray Value Tolerance, the selection
     * is not expanded if the gray level gradient is larger than the Gradient
     * Tolerance.
     *
     * Usage ===== - When called from the plugins menu, the tool is installed in
     * the ImageJ Toolbar This replaces all other custom tools, e..g those from
     * the startup macro. - Put the following into your
     * ImageJ/macros/StartupMacros.txt file to have this Wand Tool as a standard
     * tool: macro 'Wand Tool-Cf00Lee55O2233' { getCursorLoc(x, y, z, flags);
     * call('Wand_Tool.mousePressed', x, y, flags); } macro 'Wand Tool Options'
     * { call('Wand_Tool.showDialog'); }" Left-click the tool icon for selecting
     * the tool Right-click or double-click the tool icon for the options menu
     * where the tolerance can be selected
     *
     * To do: - shift-click, alt-click - include holes option - fix exception on
     * cancel dialog - units for calibrated images, spatial calibration for
     * gradient - save roi - preview (with last seed) in dialog
     *
     * Michael Schmid, 2009-May-25
     */
    static double toleranceGrayVal = 10;
    static double toleranceGrayGrad = 5;

    int width, height;
    int[] dirOffset, dirXoffset, dirYoffset;    // offsets of  neighbor pixels for addressing
    ByteProcessor maskIp;
    int[] coordinates;
    int coordinateMask;

    

    /*
     public static void mousePressed(String xString, String yString,  
String flagString) {
         int x = Integer.parseInt(xString);
         int y = Integer.parseInt(yString);
         int flags = Integer.parseInt(flagString);
         ImagePlus imp = WindowManager.getCurrentImage();
         if (imp==null) return;
         imp.getWindow().setCursor(Cursor.CROSSHAIR_CURSOR);
         new Wand_Tool().doWand(imp, x, y, flags);
     }/=*/
    public static void showDialog() {
        boolean save = Recorder.recordInMacros;
        if (Recorder.record) {
            Recorder.recordInMacros = true;
            Recorder.setCommand("Wand Tool");
        }
        GenericDialog gd = new GenericDialog("Wand Tool Options");
        gd.addNumericField("Gray Value Tolerance", toleranceGrayVal,
                2);
        gd.addNumericField("Gradient Tolerance", toleranceGrayGrad, 2);
        gd.showDialog();
        if (!gd.wasCanceled()) {
            toleranceGrayVal = gd.getNextNumber();
            toleranceGrayGrad = gd.getNextNumber();
            if (Recorder.record) {
                Recorder.saveCommand();
            }
        }
        Recorder.recordInMacros = save;
    }

    void doWand(ImagePlus imp, int x0, int y0, int flags) {
        
        //System.out.println(String.format("x0 = %d, y0 = %d"));
        
        ImageProcessor ip = imp.getProcessor();
        prepare(ip);
        boolean useGradient = toleranceGrayGrad < toleranceGrayVal;
        float toleranceGrayGrad2 = (float) (toleranceGrayGrad * toleranceGrayGrad);
        float grayRef = ip.getPixelValue(x0, y0);
        byte[] mPixels = (byte[]) maskIp.getPixels();
        //simple flood fill algorithm
        int lastCoord = 0;
        int offset0 = x0 + y0 * width;
        mPixels[offset0] = -1;
        coordinates[0] = offset0;
        for (int iCoord = 0; iCoord <= lastCoord; iCoord++) {
            int offset = coordinates[iCoord & coordinateMask];
            int x = offset % width;
            int y = offset / width;
            boolean isInner = (x != 0 && y != 0 && x != (width - 1) && y
                    != (height - 1));
            float v = ip.getPixelValue(x, y);
            boolean largeGradient = false;
            float xGradient = 0, yGradient = 0;
            if (useGradient) {
                if (isInner) {
                    float vpp = ip.getPixelValue(x + 1, y + 1);
                    float vpm = ip.getPixelValue(x + 1, y - 1);
                    float vmp = ip.getPixelValue(x - 1, y + 1);
                    float vmm = ip.getPixelValue(x - 1, y - 1);

                    xGradient = 0.125f * ( //Sobel-filter like  gradient
                             2f*(ip.getPixelValue(x + 1, y)
                            - ip.getPixelValue(x - 1, y))
                             + vpp - vmm + (vpm - vmp)
                    ); // v(x+1) - v(x-1)
                     yGradient = 0.125f * (2f * (ip.getPixelValue(x, y + 1)
                            - ip.getPixelValue(x, y - 1))
                            + vpp - vmm - (vpm - vmp)); // v(y+1) - v(y-1)
                } else {
                    int xCount = 0, yCount = 0;
                    for (int d = 0; d < 8; d++) {
                        if (isWithin(ip, x, y,
                                d)) {
                            int x2 = x + dirXoffset[d];
                            int y2 = y + dirYoffset[d];
                            float v2 = ip.getPixelValue(x2, y2);
                            int weight = (2 - (d & 0x1)); //2 for  straight
                            // , 1for diag 
                            xGradient += dirXoffset[d] * (v2 - v) * weight;
                            xCount += weight * (dirXoffset[d] != 0 ? 1 : 0);
                            yGradient += dirYoffset[d] * (v2 - v) * weight;
                            yCount += weight * (dirYoffset[d] != 0 ? 1 : 0);
                        }
                    }
                    xGradient /= xCount;
                    yGradient /= yCount;
                }
                largeGradient = xGradient * xGradient
                        + yGradient * yGradient > toleranceGrayGrad2;
            }
            for (int d = 0; d < 8; d++) {           //analyze all  neighbors(in 8 directions)
                int offset2 = offset + dirOffset[d];
                if ((isInner || isWithin(ip, x, y, d)) && mPixels[offset2] == 0) {
                    int x2 = x + dirXoffset[d];
                    int y2 = y + dirYoffset[d];
                    float v2 = ip.getPixelValue(x2, y2);
                    if (v2 > grayRef + toleranceGrayVal || v2 < grayRef
                            - toleranceGrayVal) {
                        mPixels[offset2] = 1; //out-of-bounds,   don't analyze any more
                     } else if (!largeGradient || (v2 - v)
                            * (xGradient * dirXoffset[d] + yGradient * dirYoffset[d]) <= 0) {
                        mPixels[offset2] = -1;  //add new point
                        if (lastCoord - iCoord > coordinateMask) {
                            expandCoordinateArray();
                        }
                        lastCoord++;
                        coordinates[lastCoord & coordinateMask]
                                = offset2;
                    }
                }
            } //for direction d
            if ((iCoord & 0xfff) == 1) {
               // IJ.showProgress(iCoord / (double) (width * height));
            }
        } //for iCoord
        
        //convert mask to selection
        maskIp.setThreshold(255, 255, ImageProcessor.NO_LUT_UPDATE);
        ThresholdToSelection tts = new ThresholdToSelection();
        tts.setup("", imp);
        tts.run(maskIp);
       
       
    }

    /**
     * Create static class variables: A mask, arrays of offsets within a pixel
     * array for directions in clockwise order: 0=(x,y-1), 1=(x+1,y-1), ...
     * 7=(x-1,y)
     */
    void prepare(ImageProcessor ip) {
        int width = ip.getWidth();
        int height = ip.getHeight();
        dirXoffset = new int[]{0, 1, 1, 1,
            0, -1, -1, -1};
        dirYoffset = new int[]{-1, -1, 0, 1,
            1, 1, 0, -1,};
        dirOffset = new int[]{-width, -width + 1, +1, +width + 1,
            +width, +width - 1, -1, -width - 1};
        maskIp = new ByteProcessor(width, height);
        coordinateMask = 0xfff;
        coordinates = new int[coordinateMask + 1];
        this.height = height;
        this.width = width;
    }

    void expandCoordinateArray() {
        int newSize = 2 * (coordinateMask + 1);
        int newMask = newSize - 1;
        int[] newCoordinates = new int[newSize];
        System.arraycopy(coordinates, 0, newCoordinates, 0,
                coordinateMask + 1);
        System.arraycopy(coordinates, 0, newCoordinates,
                coordinateMask + 1, coordinateMask + 1);
        coordinates = newCoordinates;
        coordinateMask = newMask;
    }

    /**
     * returns whether the neighbor in a given direction is within the image
     * NOTE: it is assumed that the pixel x,y itself is within the image! Uses
     * class variables width, height: dimensions of the image
     *
     * @param x x-coordinate of the pixel that has a neighbor in the given
     * direction
     * @param y y-coordinate of the pixel that has a neighbor in the given
     * direction
     * @param direction the direction from the pixel towards the neighbor (see
     * makeDirectionOffsets)
     * @return true if the neighbor is within the image (provided that x, y is
     * within)
     */
    boolean isWithin(ImageProcessor ip, int x, int y, int direction) {
        int width = ip.getWidth();
        int height = ip.getHeight();
        int xmax = width - 1;
        int ymax = height - 1;
        switch (direction) {
            case 0:
                return (y > 0);
            case 1:
                return (x < xmax && y > 0);
            case 2:
                return (x < xmax);
            case 3:
                return (x < xmax && y < ymax);
            case 4:
                return (y < ymax);
            case 5:
                return (x > 0 && y < ymax);
            case 6:
                return (x > 0);
            case 7:
                return (x > 0 && y > 0);
        }
        return false;   //to make the compiler happy :-)
    } // isWithin

    void clear(ByteProcessor maskIp) {
        byte[] pixels = (byte[]) maskIp.getPixels();
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = 0;
        }
    }

   

}
