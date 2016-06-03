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
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.Roi;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.Vector;

/*====================================================================
|   unwarpJPointAction
\===================================================================*/
/*====================================================================
|   unwarpJPointHandler
\===================================================================*/
/*------------------------------------------------------------------*/
class unwarpJPointHandler extends Roi {

    /* begin class unwarpJPointHandler */
    /*....................................................................
    Private variables
    ....................................................................*/
    private static final int CROSS_HALFSIZE = 5;
    // Colors
    private static final int GAMUT = 1024;
    private final Color[] spectrum = new Color[GAMUT];
    private final boolean[] usedColor = new boolean[GAMUT];
    private final Vector listColors = new Vector(0, 16);
    private int currentColor = 0;
    // List of crosses
    private final Vector listPoints = new Vector(0, 16);
    private int currentPoint = -1;
    private int numPoints = 0;
    private boolean started = false;
    // List of points for the mask
    private final Vector listMaskPoints = new Vector(0, 16);
    private boolean maskClosed = false;
    // Some useful references
    private ImagePlus imp;
    private unwarpJPointAction pa;
    private unwarpJPointToolbar tb;
    private unwarpJMask mask;
    private unwarpJDialog dialog;

    /*....................................................................
    Public methods
    ....................................................................*/
    /*------------------------------------------------------------------*/
    public void addMaskPoint(final int x, final int y) {
        if (maskClosed) {
            return;
        }
        final Point p = new Point(x, y);
        listMaskPoints.addElement(p);
    }

    /*------------------------------------------------------------------*/
    public void addPoint(final int x, final int y) {
        if (numPoints < GAMUT) {
            final Point p = new Point(x, y);
            listPoints.addElement(p);
            if (!usedColor[currentColor]) {
                usedColor[currentColor] = true;
            } else {
                int k;
                for (k = 0; k < GAMUT; k++) {
                    currentColor++;
                    currentColor &= GAMUT - 1;
                    if (!usedColor[currentColor]) {
                        break;
                    }
                }
                if (GAMUT <= k) {
                    throw new IllegalStateException("Unexpected lack of available colors");
                }
            }
            int stirredColor = 0;
            int c = currentColor;
            for (int k = 0; k < (int) Math.round(Math.log((double) GAMUT) / Math.log(2.0)); k++) {
                stirredColor <<= 1;
                stirredColor |= (c & 1);
                c >>= 1;
            }
            listColors.addElement(new Integer(stirredColor));
            currentColor++;
            currentColor &= GAMUT - 1;
            currentPoint = numPoints;
            numPoints++;
        } else {
            IJ.error("Maximum number of points reached");
        }
    } /* end addPoint */

    /*------------------------------------------------------------------*/
    /* False if the image is coming from a stack */
    public boolean canAddMaskPoints() {
        return !mask.isFromStack();
    }

    /*------------------------------------------------------------------*/
    public void clearMask() {
        // Clear mask information in this object
        listMaskPoints.removeAllElements();
        maskClosed = false;
        mask.clearMask();
    }

    /*------------------------------------------------------------------*/
    public void closeMask(int tool) {
        listMaskPoints.addElement(listMaskPoints.elementAt(0));
        maskClosed = true;
        mask.setMaskPoints(listMaskPoints);
        mask.fillMask(tool);
        dialog.grayImage(this);
    }

    /*------------------------------------------------------------------*/
    public void draw(final Graphics g) {
        // Draw landmarks
        if (started) {
            final double mag = (double) ic.getMagnification();
            final int dx = (int) (mag / 2.0);
            final int dy = (int) (mag / 2.0);
            for (int k = 0; k < numPoints; k++) {
                final Point p = (Point) listPoints.elementAt(k);
                g.setColor(spectrum[((Integer) listColors.elementAt(k)).intValue()]);
                if (k == currentPoint) {
                    if (WindowManager.getCurrentImage() == imp) {
                        g.drawLine(ic.screenX(p.x - CROSS_HALFSIZE - 1) + dx, ic.screenY(p.y - 1) + dy, ic.screenX(p.x - 1) + dx, ic.screenY(p.y - 1) + dy);
                        g.drawLine(ic.screenX(p.x - 1) + dx, ic.screenY(p.y - 1) + dy, ic.screenX(p.x - 1) + dx, ic.screenY(p.y - CROSS_HALFSIZE - 1) + dy);
                        g.drawLine(ic.screenX(p.x - 1) + dx, ic.screenY(p.y - CROSS_HALFSIZE - 1) + dy, ic.screenX(p.x + 1) + dx, ic.screenY(p.y - CROSS_HALFSIZE - 1) + dy);
                        g.drawLine(ic.screenX(p.x + 1) + dx, ic.screenY(p.y - CROSS_HALFSIZE - 1) + dy, ic.screenX(p.x + 1) + dx, ic.screenY(p.y - 1) + dy);
                        g.drawLine(ic.screenX(p.x + 1) + dx, ic.screenY(p.y - 1) + dy, ic.screenX(p.x + CROSS_HALFSIZE + 1) + dx, ic.screenY(p.y - 1) + dy);
                        g.drawLine(ic.screenX(p.x + CROSS_HALFSIZE + 1) + dx, ic.screenY(p.y - 1) + dy, ic.screenX(p.x + CROSS_HALFSIZE + 1) + dx, ic.screenY(p.y + 1) + dy);
                        g.drawLine(ic.screenX(p.x + CROSS_HALFSIZE + 1) + dx, ic.screenY(p.y + 1) + dy, ic.screenX(p.x + 1) + dx, ic.screenY(p.y + 1) + dy);
                        g.drawLine(ic.screenX(p.x + 1) + dx, ic.screenY(p.y + 1) + dy, ic.screenX(p.x + 1) + dx, ic.screenY(p.y + CROSS_HALFSIZE + 1) + dy);
                        g.drawLine(ic.screenX(p.x + 1) + dx, ic.screenY(p.y + CROSS_HALFSIZE + 1) + dy, ic.screenX(p.x - 1) + dx, ic.screenY(p.y + CROSS_HALFSIZE + 1) + dy);
                        g.drawLine(ic.screenX(p.x - 1) + dx, ic.screenY(p.y + CROSS_HALFSIZE + 1) + dy, ic.screenX(p.x - 1) + dx, ic.screenY(p.y + 1) + dy);
                        g.drawLine(ic.screenX(p.x - 1) + dx, ic.screenY(p.y + 1) + dy, ic.screenX(p.x - CROSS_HALFSIZE - 1) + dx, ic.screenY(p.y + 1) + dy);
                        g.drawLine(ic.screenX(p.x - CROSS_HALFSIZE - 1) + dx, ic.screenY(p.y + 1) + dy, ic.screenX(p.x - CROSS_HALFSIZE - 1) + dx, ic.screenY(p.y - 1) + dy);
                        if (1.0 < ic.getMagnification()) {
                            g.drawLine(ic.screenX(p.x - CROSS_HALFSIZE) + dx, ic.screenY(p.y) + dy, ic.screenX(p.x + CROSS_HALFSIZE) + dx, ic.screenY(p.y) + dy);
                            g.drawLine(ic.screenX(p.x) + dx, ic.screenY(p.y - CROSS_HALFSIZE) + dy, ic.screenX(p.x) + dx, ic.screenY(p.y + CROSS_HALFSIZE) + dy);
                        }
                    } else {
                        g.drawLine(ic.screenX(p.x - CROSS_HALFSIZE + 1) + dx, ic.screenY(p.y - CROSS_HALFSIZE + 1) + dy, ic.screenX(p.x + CROSS_HALFSIZE - 1) + dx, ic.screenY(p.y + CROSS_HALFSIZE - 1) + dy);
                        g.drawLine(ic.screenX(p.x - CROSS_HALFSIZE + 1) + dx, ic.screenY(p.y + CROSS_HALFSIZE - 1) + dy, ic.screenX(p.x + CROSS_HALFSIZE - 1) + dx, ic.screenY(p.y - CROSS_HALFSIZE + 1) + dy);
                    }
                } else {
                    g.drawLine(ic.screenX(p.x - CROSS_HALFSIZE) + dx, ic.screenY(p.y) + dy, ic.screenX(p.x + CROSS_HALFSIZE) + dx, ic.screenY(p.y) + dy);
                    g.drawLine(ic.screenX(p.x) + dx, ic.screenY(p.y - CROSS_HALFSIZE) + dy, ic.screenX(p.x) + dx, ic.screenY(p.y + CROSS_HALFSIZE) + dy);
                }
            }
            if (updateFullWindow) {
                updateFullWindow = false;
                imp.draw();
            }
        }
        // Draw mask
        int numberMaskPoints = listMaskPoints.size();
        if (numberMaskPoints != 0) {
            final double mag = (double) ic.getMagnification();
            final int dx = (int) (mag / 2.0);
            final int dy = (int) (mag / 2.0);
            int CIRCLE_RADIUS = CROSS_HALFSIZE / 2;
            int CIRCLE_DIAMETER = 2 * CIRCLE_RADIUS;
            for (int i = 0; i < numberMaskPoints; i++) {
                final Point p = (Point) listMaskPoints.elementAt(i);
                g.setColor(Color.yellow);
                g.drawOval(ic.screenX(p.x) - CIRCLE_RADIUS + dx, ic.screenY(p.y) - CIRCLE_RADIUS + dy, CIRCLE_DIAMETER, CIRCLE_DIAMETER);
                if (i != 0) {
                    Point previous_p = (Point) listMaskPoints.elementAt(i - 1);
                    g.drawLine(ic.screenX(p.x) + dx, ic.screenY(p.y) + dy, ic.screenX(previous_p.x) + dx, ic.screenY(previous_p.y) + dy);
                }
            }
        }
    } /* end draw */

    /*------------------------------------------------------------------*/
    public int findClosest(int x, int y) {
        if (numPoints == 0) {
            return currentPoint;
        }
        x = ic.offScreenX(x);
        y = ic.offScreenY(y);
        Point p = new Point((Point) listPoints.elementAt(currentPoint));
        double distance = (double) (x - p.x) * (double) (x - p.x) + (double) (y - p.y) * (double) (y - p.y);
        for (int k = 0; k < numPoints; k++) {
            p = (Point) listPoints.elementAt(k);
            final double candidate = (double) (x - p.x) * (double) (x - p.x) + (double) (y - p.y) * (double) (y - p.y);
            if (candidate < distance) {
                distance = candidate;
                currentPoint = k;
            }
        }
        return currentPoint;
    } /* end findClosest */

    /*------------------------------------------------------------------*/
    public Point getPoint() {
        return (0 <= currentPoint) ? (Point) listPoints.elementAt(currentPoint) : (null);
    } /* end getPoint */

    /*------------------------------------------------------------------*/
    public unwarpJPointAction getPointAction() {
        return pa;
    }

    /*------------------------------------------------------------------*/
    public int getCurrentPoint() {
        return currentPoint;
    } /* end getCurrentPoint */

    /*------------------------------------------------------------------*/
    public Vector getPoints() {
        return listPoints;
    } /* end getPoints */

    /*------------------------------------------------------------------*/
    public void killListeners() {
        final ImageWindow iw = imp.getWindow();
        final ImageCanvas ic = iw.getCanvas();
        ic.removeKeyListener(pa);
        ic.removeMouseListener(pa);
        ic.removeMouseMotionListener(pa);
        ic.addMouseMotionListener(ic);
        ic.addMouseListener(ic);
        ic.addKeyListener(IJ.getInstance());
    } /* end killListeners */

    /*------------------------------------------------------------------*/
    public void movePoint(int x, int y) {
        if (0 <= currentPoint) {
            x = ic.offScreenX(x);
            y = ic.offScreenY(y);
            x = (x < 0) ? (0) : (x);
            x = (imp.getWidth() <= x) ? (imp.getWidth() - 1) : (x);
            y = (y < 0) ? (0) : (y);
            y = (imp.getHeight() <= y) ? (imp.getHeight() - 1) : (y);
            listPoints.removeElementAt(currentPoint);
            final Point p = new Point(x, y);
            listPoints.insertElementAt(p, currentPoint);
        }
    } /* end movePoint */

    /*------------------------------------------------------------------*/
    public void nextPoint() {
        currentPoint = (currentPoint == (numPoints - 1)) ? (0) : (currentPoint + 1);
    } /* end nextPoint */

    /*------------------------------------------------------------------*/
    public void removePoint() {
        if (0 < numPoints) {
            listPoints.removeElementAt(currentPoint);
            usedColor[((Integer) listColors.elementAt(currentPoint)).intValue()] = false;
            listColors.removeElementAt(currentPoint);
            numPoints--;
        }
        currentPoint = numPoints - 1;
        if (currentPoint < 0) {
            tb.setTool(pa.ADD_CROSS);
        }
    } /* end removePoint */

    /*------------------------------------------------------------------*/
    public void removePoint(final int k) {
        if (0 < numPoints) {
            listPoints.removeElementAt(k);
            usedColor[((Integer) listColors.elementAt(k)).intValue()] = false;
            listColors.removeElementAt(k);
            numPoints--;
        }
        currentPoint = numPoints - 1;
        if (currentPoint < 0) {
            tb.setTool(pa.ADD_CROSS);
        }
    } /* end removePoint */

    /*------------------------------------------------------------------*/
    public void removePoints() {
        listPoints.removeAllElements();
        listColors.removeAllElements();
        for (int k = 0; k < GAMUT; k++) {
            usedColor[k] = false;
        }
        currentColor = 0;
        numPoints = 0;
        currentPoint = -1;
        tb.setTool(pa.ADD_CROSS);
        imp.setRoi(this);
    } /* end removePoints */

    /*------------------------------------------------------------------*/
    public void setCurrentPoint(final int currentPoint) {
        this.currentPoint = currentPoint;
    } /* end setCurrentPoint */

    /*------------------------------------------------------------------*/
    public void setTestSourceSet(final int set) {
        removePoints();
        switch (set) {
            case 1:
                // Deformed_Lena 1
                addPoint(11, 11);
                addPoint(200, 6);
                addPoint(197, 204);
                addPoint(121, 111);
                break;
            case 2:
                // Deformed_Lena 1
                addPoint(6, 6);
                addPoint(202, 7);
                addPoint(196, 210);
                addPoint(10, 214);
                addPoint(120, 112);
                addPoint(68, 20);
                addPoint(63, 163);
                addPoint(186, 68);
                break;
        }
    } /* end setTestset */

    /*------------------------------------------------------------------*/
    public void setTestTargetSet(final int set) {
        removePoints();
        switch (set) {
            case 1:
                addPoint(11, 11);
                addPoint(185, 15);
                addPoint(154, 200);
                addPoint(123, 92);
                break;
            case 2:
                // Deformed_Lena 1
                addPoint(6, 6);
                addPoint(185, 14);
                addPoint(154, 200);
                addPoint(3, 178);
                addPoint(121, 93);
                addPoint(67, 14);
                addPoint(52, 141);
                addPoint(178, 68);
                break;
        }
    } /* end setTestset */

    /*------------------------------------------------------------------*/
    public void setSecondaryPointHandler(final ImagePlus secondaryImp, final unwarpJPointHandler secondaryPh) {
        pa.setSecondaryPointHandler(secondaryImp, secondaryPh);
    } /* end setSecondaryPointHandler */

    /*------------------------------------------------------------------*/
    /* Constructor with graphical capabilities */
    public unwarpJPointHandler(final ImagePlus imp, final unwarpJPointToolbar tb, final unwarpJMask mask, final unwarpJDialog dialog) {
        super(0, 0, imp.getWidth(), imp.getHeight(), imp);
        this.imp = imp;
        this.tb = tb;
        this.dialog = dialog;
        pa = new unwarpJPointAction(imp, this, tb, dialog);
        final ImageWindow iw = imp.getWindow();
        final ImageCanvas ic = iw.getCanvas();
        iw.requestFocus();
        iw.removeKeyListener(IJ.getInstance());
        iw.addKeyListener(pa);
        ic.removeMouseMotionListener(ic);
        ic.removeMouseListener(ic);
        ic.removeKeyListener(IJ.getInstance());
        ic.addKeyListener(pa);
        ic.addMouseListener(pa);
        ic.addMouseMotionListener(pa);
        setSpectrum();
        started = true;
        this.mask = mask;
        clearMask();
    } /* end unwarpJPointHandler */

    /* Constructor without graphical capabilities */
    public unwarpJPointHandler(final ImagePlus imp) {
        super(0, 0, imp.getWidth(), imp.getHeight(), imp);
        this.imp = imp;
        tb = null;
        dialog = null;
        pa = null;
        started = true;
        mask = null;
    } /* end unwarpJPointHandler */

    /*....................................................................
    Private methods
    ....................................................................*/
    /*------------------------------------------------------------------*/
    private void setSpectrum() {
        final int bound1 = GAMUT / 6;
        final int bound2 = GAMUT / 3;
        final int bound3 = GAMUT / 2;
        final int bound4 = (2 * GAMUT) / 3;
        final int bound5 = (5 * GAMUT) / 6;
        final int bound6 = GAMUT;
        final float gamutChunk1 = (float) bound1;
        final float gamutChunk2 = (float) (bound2 - bound1);
        final float gamutChunk3 = (float) (bound3 - bound2);
        final float gamutChunk4 = (float) (bound4 - bound3);
        final float gamutChunk5 = (float) (bound5 - bound4);
        final float gamutChunk6 = (float) (bound6 - bound5);
        int k = 0;
        do {
            spectrum[k] = new Color(1.0F, (float) k / gamutChunk1, 0.0F);
            usedColor[k] = false;
        } while (++k < bound1);
        do {
            spectrum[k] = new Color(1.0F - (float) (k - bound1) / gamutChunk2, 1.0F, 0.0F);
            usedColor[k] = false;
        } while (++k < bound2);
        do {
            spectrum[k] = new Color(0.0F, 1.0F, (float) (k - bound2) / gamutChunk3);
            usedColor[k] = false;
        } while (++k < bound3);
        do {
            spectrum[k] = new Color(0.0F, 1.0F - (float) (k - bound3) / gamutChunk4, 1.0F);
            usedColor[k] = false;
        } while (++k < bound4);
        do {
            spectrum[k] = new Color((float) (k - bound4) / gamutChunk5, 0.0F, 1.0F);
            usedColor[k] = false;
        } while (++k < bound5);
        do {
            spectrum[k] = new Color(1.0F, 0.0F, 1.0F - (float) (k - bound5) / gamutChunk6);
            usedColor[k] = false;
        } while (++k < bound6);
    } /* end setSpectrum */
    
} /* end class unwarpJPointHandler */