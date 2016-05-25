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
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.measure.Calibration;
import java.awt.Event;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/*====================================================================
|   unwarpJPointAction
\===================================================================*/
/*------------------------------------------------------------------*/
class unwarpJPointAction extends ImageCanvas implements KeyListener, MouseListener, MouseMotionListener {

    /* begin class unwarpJPointAction */
    /*....................................................................
    Public variables
    ....................................................................*/
    public static final int ADD_CROSS = 0;
    public static final int MOVE_CROSS = 1;
    public static final int REMOVE_CROSS = 2;
    public static final int MASK = 3;
    public static final int INVERTMASK = 4;
    public static final int FILE = 5;
    public static final int STOP = 7;
    public static final int MAGNIFIER = 11;
    /*....................................................................
    Private variables
    ....................................................................*/
    private ImagePlus mainImp;
    private ImagePlus secondaryImp;
    private unwarpJPointHandler mainPh;
    private unwarpJPointHandler secondaryPh;
    private unwarpJPointToolbar tb;
    private unwarpJDialog dialog;
    private long mouseDownTime;

    /*....................................................................
    Public methods
    ....................................................................*/
    /*------------------------------------------------------------------*/
    public void keyPressed(final KeyEvent e) {
        if (tb.getCurrentTool() == MASK || tb.getCurrentTool() == INVERTMASK) {
            return;
        }
        final Point p = mainPh.getPoint();
        if (p == null) {
            return;
        }
        final int x = p.x;
        final int y = p.y;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_DELETE:
            case KeyEvent.VK_BACK_SPACE:
                mainPh.removePoint();
                secondaryPh.removePoint();
                updateAndDraw();
                break;
            case KeyEvent.VK_DOWN:
                mainPh.movePoint(mainImp.getWindow().getCanvas().screenX(x), mainImp.getWindow().getCanvas().screenY(y + (int) Math.ceil(1.0 / mainImp.getWindow().getCanvas().getMagnification())));
                mainImp.setRoi(mainPh);
                break;
            case KeyEvent.VK_LEFT:
                mainPh.movePoint(mainImp.getWindow().getCanvas().screenX(x - (int) Math.ceil(1.0 / mainImp.getWindow().getCanvas().getMagnification())), mainImp.getWindow().getCanvas().screenY(y));
                mainImp.setRoi(mainPh);
                break;
            case KeyEvent.VK_RIGHT:
                mainPh.movePoint(mainImp.getWindow().getCanvas().screenX(x + (int) Math.ceil(1.0 / mainImp.getWindow().getCanvas().getMagnification())), mainImp.getWindow().getCanvas().screenY(y));
                mainImp.setRoi(mainPh);
                break;
            case KeyEvent.VK_TAB:
                mainPh.nextPoint();
                secondaryPh.nextPoint();
                updateAndDraw();
                break;
            case KeyEvent.VK_UP:
                mainPh.movePoint(mainImp.getWindow().getCanvas().screenX(x), mainImp.getWindow().getCanvas().screenY(y - (int) Math.ceil(1.0 / mainImp.getWindow().getCanvas().getMagnification())));
                mainImp.setRoi(mainPh);
                break;
        }
    } /* end keyPressed */

    /*------------------------------------------------------------------*/
    public void keyReleased(final KeyEvent e) {
    } /* end keyReleased */

    /*------------------------------------------------------------------*/
    public void keyTyped(final KeyEvent e) {
    } /* end keyTyped */

    /*------------------------------------------------------------------*/
    public void mouseClicked(final MouseEvent e) {
    } /* end mouseClicked */

    /*------------------------------------------------------------------*/
    public void mouseDragged(final MouseEvent e) {
        final int x = e.getX();
        final int y = e.getY();
        if (tb.getCurrentTool() == MOVE_CROSS) {
            mainPh.movePoint(x, y);
            updateAndDraw();
        }
        mouseMoved(e);
    } /* end mouseDragged */

    /*------------------------------------------------------------------*/
    public void mouseEntered(final MouseEvent e) {
        WindowManager.setCurrentWindow(mainImp.getWindow());
        mainImp.getWindow().toFront();
        updateAndDraw();
    } /* end mouseEntered */

    /*------------------------------------------------------------------*/
    public void mouseExited(final MouseEvent e) {
        IJ.showStatus("");
    } /* end mouseExited */

    /*------------------------------------------------------------------*/
    public void mouseMoved(final MouseEvent e) {
        setControl();
        final int x = mainImp.getWindow().getCanvas().offScreenX(e.getX());
        final int y = mainImp.getWindow().getCanvas().offScreenY(e.getY());
        IJ.showStatus(mainImp.getLocationAsString(x, y) + getValueAsString(x, y));
    } /* end mouseMoved */

    /*------------------------------------------------------------------*/
    public void mousePressed(final MouseEvent e) {
        if (dialog.isFinalActionLaunched()) {
            return;
        }
        int x = e.getX();
        int xp;
        int y = e.getY();
        int yp;
        int currentPoint;
        boolean doubleClick = (System.currentTimeMillis() - mouseDownTime) <= 250L;
        mouseDownTime = System.currentTimeMillis();
        switch (tb.getCurrentTool()) {
            case ADD_CROSS:
                xp = mainImp.getWindow().getCanvas().offScreenX(x);
                yp = mainImp.getWindow().getCanvas().offScreenY(y);
                mainPh.addPoint(xp, yp);
                xp = positionX(mainImp, secondaryImp, mainImp.getWindow().getCanvas().offScreenX(x));
                yp = positionY(mainImp, secondaryImp, mainImp.getWindow().getCanvas().offScreenY(y));
                secondaryPh.addPoint(xp, yp);
                updateAndDraw();
                break;
            case MOVE_CROSS:
                currentPoint = mainPh.findClosest(x, y);
                secondaryPh.setCurrentPoint(currentPoint);
                updateAndDraw();
                break;
            case REMOVE_CROSS:
                currentPoint = mainPh.findClosest(x, y);
                mainPh.removePoint(currentPoint);
                secondaryPh.removePoint(currentPoint);
                updateAndDraw();
                break;
            case MASK:
            case INVERTMASK:
                if (mainPh.canAddMaskPoints()) {
                    if (!doubleClick) {
                        if (dialog.isClearMaskSet()) {
                            mainPh.clearMask();
                            dialog.setClearMask(false);
                            dialog.ungrayImage(this);
                        }
                        x = positionX(mainImp, secondaryImp, mainImp.getWindow().getCanvas().offScreenX(x));
                        y = positionY(mainImp, secondaryImp, mainImp.getWindow().getCanvas().offScreenY(y));
                        mainPh.addMaskPoint(x, y);
                    } else {
                        mainPh.closeMask(tb.getCurrentTool());
                    }
                    updateAndDraw();
                } else {
                    IJ.error("A mask cannot be manually assigned since the mask was already in the stack");
                }
                break;
            case MAGNIFIER:
                final int flags = e.getModifiers();
                if ((flags & (Event.ALT_MASK | Event.META_MASK | Event.CTRL_MASK)) != 0) {
                    mainImp.getWindow().getCanvas().zoomOut(x, y);
                } else {
                    mainImp.getWindow().getCanvas().zoomIn(x, y);
                }
                break;
        }
    } /* end mousePressed */

    /*------------------------------------------------------------------*/
    public void mouseReleased(final MouseEvent e) {
    } /* end mouseReleased */

    /*------------------------------------------------------------------*/
    public void setSecondaryPointHandler(final ImagePlus secondaryImp, final unwarpJPointHandler secondaryPh) {
        this.secondaryImp = secondaryImp;
        this.secondaryPh = secondaryPh;
    } /* end setSecondaryPointHandler */

    /*------------------------------------------------------------------*/
    public unwarpJPointAction(final ImagePlus imp, final unwarpJPointHandler ph, final unwarpJPointToolbar tb, final unwarpJDialog dialog) {
        super(imp);
        this.mainImp = imp;
        this.mainPh = ph;
        this.tb = tb;
        this.dialog = dialog;
    } /* end unwarpJPointAction */

    /*....................................................................
    Private methods
    ....................................................................*/
    /*------------------------------------------------------------------*/
    private String getValueAsString(final int x, final int y) {
        final Calibration cal = mainImp.getCalibration();
        final int[] v = mainImp.getPixel(x, y);
        final int mainImptype = mainImp.getType();
        if (mainImptype == mainImp.GRAY8 || mainImptype == mainImp.GRAY16) {
            final double cValue = cal.getCValue(v[0]);
            if (cValue == v[0]) {
                return ", value=" + v[0];
            } else {
                return ", value=" + IJ.d2s(cValue) + " (" + v[0] + ")";
            }
        } else if (mainImptype == mainImp.GRAY32) {
            return ", value=" + Float.intBitsToFloat(v[0]);
        } else if (mainImptype == mainImp.COLOR_256) {
            return ", index=" + v[3] + ", value=" + v[0] + "," + v[1] + "," + v[2];
        } else if (mainImptype == mainImp.COLOR_RGB) {
            return ", value=" + v[0] + "," + v[1] + "," + v[2];
        } else {
            return "";
        }
    } /* end getValueAsString */

    /*------------------------------------------------------------------*/
    private int positionX(final ImagePlus imp1, final ImagePlus imp2, final int x) {
        return (x * imp2.getWidth()) / imp1.getWidth();
    } /* end PositionX */

    /*------------------------------------------------------------------*/
    private int positionY(final ImagePlus imp1, final ImagePlus imp2, final int y) {
        return (y * imp2.getHeight()) / imp1.getHeight();
    } /* end PositionY */

    /*------------------------------------------------------------------*/
    private void setControl() {
        switch (tb.getCurrentTool()) {
            case ADD_CROSS:
                mainImp.getWindow().getCanvas().setCursor(crosshairCursor);
                break;
            case FILE:
            case MAGNIFIER:
            case MOVE_CROSS:
            case REMOVE_CROSS:
            case MASK:
            case INVERTMASK:
            case STOP:
                mainImp.getWindow().getCanvas().setCursor(defaultCursor);
                break;
        }
    } /* end setControl */

    /*------------------------------------------------------------------*/
    private void updateAndDraw() {
        mainImp.setRoi(mainPh);
        secondaryImp.setRoi(secondaryPh);
    } /* end updateAndDraw */
    
} /* end class unwarpJPointAction */