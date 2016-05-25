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
import java.awt.Button;
import java.awt.CheckboxGroup;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Stack;
import java.util.Vector;

/*====================================================================
|   unwarpJDialog
\===================================================================*/
/*====================================================================
|   unwarpJFile
\===================================================================*/
/*------------------------------------------------------------------*/
class unwarpJFile extends Dialog implements ActionListener {

    /* begin class unwarpJFile */
    /*....................................................................
    Private variables
    ....................................................................*/
    private final CheckboxGroup choice = new CheckboxGroup();
    private ImagePlus sourceImp;
    private ImagePlus targetImp;
    private unwarpJPointHandler sourcePh;
    private unwarpJPointHandler targetPh;
    private unwarpJDialog dialog;

    /*....................................................................
    Public methods
    ....................................................................*/
    /*------------------------------------------------------------------*/
    public void actionPerformed(final ActionEvent ae) {
        this.setVisible(false);
        if (ae.getActionCommand().equals("Save Landmarks As...")) {
            savePoints();
        } else if (ae.getActionCommand().equals("Load Landmarks...")) {
            loadPoints();
        } else if (ae.getActionCommand().equals("Show Landmarks")) {
            showPoints();
        } else if (ae.getActionCommand().equals("Load Transformation")) {
            loadTransformation();
        } else if (ae.getActionCommand().equals("Cancel")) {
        }
    } /* end actionPerformed */

    /*------------------------------------------------------------------*/
    public Insets getInsets() {
        return new Insets(0, 20, 20, 20);
    } /* end getInsets */

    /*------------------------------------------------------------------*/
    unwarpJFile(final Frame parentWindow, final ImagePlus sourceImp, final ImagePlus targetImp, final unwarpJPointHandler sourcePh, final unwarpJPointHandler targetPh, final unwarpJDialog dialog) {
        super(parentWindow, "I/O Menu", true);
        this.sourceImp = sourceImp;
        this.targetImp = targetImp;
        this.sourcePh = sourcePh;
        this.targetPh = targetPh;
        this.dialog = dialog;
        setLayout(new GridLayout(0, 1));
        final Button saveAsButton = new Button("Save Landmarks As...");
        final Button loadButton = new Button("Load Landmarks...");
        final Button show_PointsButton = new Button("Show Landmarks");
        final Button loadTransfButton = new Button("Load Transformation");
        final Button cancelButton = new Button("Cancel");
        saveAsButton.addActionListener(this);
        loadButton.addActionListener(this);
        show_PointsButton.addActionListener(this);
        loadTransfButton.addActionListener(this);
        cancelButton.addActionListener(this);
        final Label separation1 = new Label("");
        final Label separation2 = new Label("");
        add(separation1);
        add(loadButton);
        add(saveAsButton);
        add(show_PointsButton);
        add(loadTransfButton);
        add(separation2);
        add(cancelButton);
        pack();
    } /* end unwarpJFile */

    /*....................................................................
    Private methods
    ....................................................................*/
    /*------------------------------------------------------------------*/
    private void loadPoints() {
        final Frame f = new Frame();
        final FileDialog fd = new FileDialog(f, "Load Points", FileDialog.LOAD);
        fd.setVisible(true);
        final String path = fd.getDirectory();
        final String filename = fd.getFile();
        if ((path == null) || (filename == null)) {
            return;
        }
        Stack sourceStack = new Stack();
        Stack targetStack = new Stack();
        unwarpJMiscTools.loadPoints(path + filename, sourceStack, targetStack);
        sourcePh.removePoints();
        targetPh.removePoints();
        while ((!sourceStack.empty()) && (!targetStack.empty())) {
            Point sourcePoint = (Point) sourceStack.pop();
            Point targetPoint = (Point) targetStack.pop();
            sourcePh.addPoint(sourcePoint.x, sourcePoint.y);
            targetPh.addPoint(targetPoint.x, targetPoint.y);
        }
    } /* end loadPoints */

    /*------------------------------------------------------------------*/
    private void loadTransformation() {
        final Frame f = new Frame();
        final FileDialog fd = new FileDialog(f, "Load Transformation", FileDialog.LOAD);
        fd.setVisible(true);
        final String path = fd.getDirectory();
        final String filename = fd.getFile();
        if ((path == null) || (filename == null)) {
            return;
        }
        String fn_tnf = path + filename;
        int intervals = unwarpJMiscTools.numberOfIntervalsOfTransformation(fn_tnf);
        double[][] cx = new double[intervals + 3][intervals + 3];
        double[][] cy = new double[intervals + 3][intervals + 3];
        unwarpJMiscTools.loadTransformation(fn_tnf, cx, cy);
        // Apply transformation
        dialog.applyTransformationToSource(intervals, cx, cy);
    }

    /*------------------------------------------------------------------*/
    private void savePoints() {
        final Frame f = new Frame();
        final FileDialog fd = new FileDialog(f, "Save Points", FileDialog.SAVE);
        String filename = targetImp.getTitle();
        int dot = filename.lastIndexOf('.');
        if (dot == -1) {
            fd.setFile(filename + ".txt");
        } else {
            filename = filename.substring(0, dot);
            fd.setFile(filename + ".txt");
        }
        fd.setVisible(true);
        final String path = fd.getDirectory();
        filename = fd.getFile();
        if ((path == null) || (filename == null)) {
            return;
        }
        try {
            final FileWriter fw = new FileWriter(path + filename);
            final Vector sourceList = sourcePh.getPoints();
            final Vector targetList = targetPh.getPoints();
            Point sourcePoint;
            Point targetPoint;
            String n;
            String xSource;
            String ySource;
            String xTarget;
            String yTarget;
            fw.write("Index\txSource\tySource\txTarget\tyTarget\n");
            for (int k = 0; k < sourceList.size(); k++) {
                n = "" + k;
                while (n.length() < 5) {
                    n = " " + n;
                }
                sourcePoint = (Point) sourceList.elementAt(k);
                xSource = "" + sourcePoint.x;
                while (xSource.length() < 7) {
                    xSource = " " + xSource;
                }
                ySource = "" + sourcePoint.y;
                while (ySource.length() < 7) {
                    ySource = " " + ySource;
                }
                targetPoint = (Point) targetList.elementAt(k);
                xTarget = "" + targetPoint.x;
                while (xTarget.length() < 7) {
                    xTarget = " " + xTarget;
                }
                yTarget = "" + targetPoint.y;
                while (yTarget.length() < 7) {
                    yTarget = " " + yTarget;
                }
                fw.write(n + "\t" + xSource + "\t" + ySource + "\t" + xTarget + "\t" + yTarget + "\n");
            }
            fw.close();
        } catch (IOException e) {
            IJ.error("IOException exception" + e);
        } catch (SecurityException e) {
            IJ.error("Security exception" + e);
        }
    } /* end savePoints */

    /*------------------------------------------------------------------*/
    private void showPoints() {
        final Vector sourceList = sourcePh.getPoints();
        final Vector targetList = targetPh.getPoints();
        Point sourcePoint;
        Point targetPoint;
        String n;
        String xTarget;
        String yTarget;
        String xSource;
        String ySource;
        IJ.getTextPanel().setFont(new Font("Monospaced", Font.PLAIN, 12));
        IJ.setColumnHeadings("Index\txSource\tySource\txTarget\tyTarget");
        for (int k = 0; k < sourceList.size(); k++) {
            n = "" + k;
            while (n.length() < 5) {
                n = " " + n;
            }
            sourcePoint = (Point) sourceList.elementAt(k);
            xTarget = "" + sourcePoint.x;
            while (xTarget.length() < 7) {
                xTarget = " " + xTarget;
            }
            yTarget = "" + sourcePoint.y;
            while (yTarget.length() < 7) {
                yTarget = " " + yTarget;
            }
            targetPoint = (Point) targetList.elementAt(k);
            xSource = "" + targetPoint.x;
            while (xSource.length() < 7) {
                xSource = " " + xSource;
            }
            ySource = "" + targetPoint.y;
            while (ySource.length() < 7) {
                ySource = " " + ySource;
            }
            IJ.write(n + "\t" + xSource + "\t" + ySource + "\t" + xTarget + "\t" + yTarget);
        }
    } /* end showPoints */
    
} /* end class unwarpJFile */