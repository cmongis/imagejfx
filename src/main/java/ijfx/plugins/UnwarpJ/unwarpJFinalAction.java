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

import ij.ImagePlus;
import ij.gui.ImageWindow;
import ij.process.FloatProcessor;

/*====================================================================
|   unwarpJDialog
\===================================================================*/
/*====================================================================
|   unwarpJFile
\===================================================================*/
/*====================================================================
|   unwarpJFinalAction
\===================================================================*/
/*------------------------------------------------------------------*/
class unwarpJFinalAction implements Runnable {

    /*....................................................................
    Private variables
    ....................................................................*/
    private Thread t;
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

    /*....................................................................
    Public methods
    ....................................................................*/
    /*********************************************************************
     * Return the thread associated with this <code>unwarpJFinalAction</code>
     * object.
     ********************************************************************/
    public Thread getThread() {
        return t;
    } /* end getThread */

    /*********************************************************************
     * Perform the registration
     ********************************************************************/
    public void run() {
        // Create output image
        int Ydimt = target.getHeight();
        int Xdimt = target.getWidth();
        int Xdims = source.getWidth();
        final FloatProcessor fp = new FloatProcessor(Xdimt, Ydimt);
        for (int i = 0; i < Ydimt; i++) {
            for (int j = 0; j < Xdimt; j++) {
                if (sourceMsk.getValue(j, i) && targetMsk.getValue(j, i)) {
                    fp.putPixelValue(j, i, (target.getImage())[i * Xdimt + j] - (source.getImage())[i * Xdims + j]);
                } else {
                    fp.putPixelValue(j, i, 0);
                }
            }
        }
        fp.resetMinAndMax();
        final ImagePlus ip = new ImagePlus("Output", fp);
        final ImageWindow iw = new ImageWindow(ip);
        ip.updateImage();
        // Perform the registration
        final unwarpJTransformation warp = new unwarpJTransformation(sourceImp, targetImp, source, target, sourcePh, targetPh, sourceMsk, targetMsk, min_scale_deformation, max_scale_deformation, min_scale_image, divWeight, curlWeight, landmarkWeight, imageWeight, stopThreshold, outputLevel, showMarquardtOptim, accurate_mode, dialog.isSaveTransformationSet(), "", ip, dialog);
        warp.doRegistration();
        dialog.ungrayImage(sourcePh.getPointAction());
        dialog.ungrayImage(targetPh.getPointAction());
        dialog.restoreAll();
        dialog.freeMemory();
    }

    /*********************************************************************
     * Pass parameter from <code>unwarpJDialog</code> to
     * <code>unwarpJFinalAction</code>.
     ********************************************************************/
    public void setup(final ImagePlus sourceImp, final ImagePlus targetImp, final unwarpJImageModel source, final unwarpJImageModel target, final unwarpJPointHandler sourcePh, final unwarpJPointHandler targetPh, final unwarpJMask sourceMsk, final unwarpJMask targetMsk, final int min_scale_deformation, final int max_scale_deformation, final int min_scale_image, final double divWeight, final double curlWeight, final double landmarkWeight, final double imageWeight, final double stopThreshold, final int outputLevel, final boolean showMarquardtOptim, final int accurate_mode) {
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
    } /* end setup */

    /*********************************************************************
     * Start a thread under the control of the main event loop. This thread
     * has access to the progress bar, while methods called directly from
     * within <code>unwarpJDialog</code> do not because they are
     * under the control of its own event loop.
     ********************************************************************/
    public unwarpJFinalAction(final unwarpJDialog dialog) {
        this.dialog = dialog;
        t = new Thread(this);
        t.setDaemon(true);
    }
    
}
