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
import ij.gui.GUI;
import ij.gui.ImageCanvas;
import ij.gui.Toolbar;
import ij.process.FloatProcessor;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;

/*====================================================================
|   unwarpJDialog
\===================================================================*/
/*------------------------------------------------------------------*/
class unwarpJDialog extends Dialog implements ActionListener {

    /* begin class unwarpJDialog */
    /*....................................................................
    Private variables
    ....................................................................*/
    // Advanced dialog
    private Dialog advanced_dlg = null;
    // List of available images in ImageJ
    private ImagePlus[] imageList;
    // Image representations (canvas and ImagePlus)
    private ImageCanvas sourceIc;
    private ImageCanvas targetIc;
    private ImagePlus sourceImp;
    private ImagePlus targetImp;
    // Image models
    private unwarpJImageModel source;
    private unwarpJImageModel target;
    // Image Masks
    private unwarpJMask sourceMsk;
    private unwarpJMask targetMsk;
    // Point handlers for the landmarks
    private unwarpJPointHandler sourcePh;
    private unwarpJPointHandler targetPh;
    // Toolbar handler
    private boolean clearMask = false;
    private unwarpJPointToolbar tb = new unwarpJPointToolbar(Toolbar.getInstance(), this);
    // Final action
    private boolean finalActionLaunched = false;
    private boolean stopRegistration = false;
    // Dialog related
    private final Button DoneButton = new Button("Done");
    private TextField min_scaleDeformationTextField;
    private TextField max_scaleDeformationTextField;
    private TextField divWeightTextField;
    private TextField curlWeightTextField;
    private TextField landmarkWeightTextField;
    private TextField imageWeightTextField;
    private TextField stopThresholdTextField;
    private int sourceChoiceIndex = 0;
    private int targetChoiceIndex = 1;
    private static int min_scale_deformation = 0;
    private static int max_scale_deformation = 2;
    private static int mode = 1;
    private Checkbox ckRichOutput;
    private Checkbox ckSaveTransformation;
    // Transformation parameters
    private static int MIN_SIZE = 8;
    private static double divWeight = 0;
    private static double curlWeight = 0;
    private static double landmarkWeight = 0;
    private static double imageWeight = 1;
    private static boolean richOutput = false;
    private static boolean saveTransformation = false;
    private static int min_scale_image = 0;
    private static int imagePyramidDepth = 3;
    private static double stopThreshold = 1e-2;

    /*....................................................................
    Public methods
    ....................................................................*/
    /*------------------------------------------------------------------*/
    public void actionPerformed(final ActionEvent ae) {
        if (ae.getActionCommand().equals("Cancel")) {
            dispose();
            restoreAll();
        } else if (ae.getActionCommand().equals("Done")) {
            dispose();
            joinThreads();
            imagePyramidDepth = max_scale_deformation - min_scale_deformation + 1;
            divWeight = Double.valueOf(divWeightTextField.getText()).doubleValue();
            curlWeight = Double.valueOf(curlWeightTextField.getText()).doubleValue();
            landmarkWeight = Double.valueOf(landmarkWeightTextField.getText()).doubleValue();
            imageWeight = Double.valueOf(imageWeightTextField.getText()).doubleValue();
            richOutput = ckRichOutput.getState();
            saveTransformation = ckSaveTransformation.getState();
            int outputLevel = 1;
            boolean showMarquardtOptim = false;
            if (richOutput) {
                outputLevel++;
                showMarquardtOptim = true;
            }
            unwarpJFinalAction finalAction = new unwarpJFinalAction(this);
            finalAction.setup(sourceImp, targetImp, source, target, sourcePh, targetPh, sourceMsk, targetMsk, min_scale_deformation, max_scale_deformation, min_scale_image, divWeight, curlWeight, landmarkWeight, imageWeight, stopThreshold, outputLevel, showMarquardtOptim, mode);
            finalActionLaunched = true;
            tb.setAllUp();
            tb.repaint();
            finalAction.getThread().start();
        } else if (ae.getActionCommand().equals("Credits...")) {
            final unwarpJCredits dialog = new unwarpJCredits(IJ.getInstance());
            GUI.center(dialog);
            dialog.setVisible(true);
        } else if (ae.getActionCommand().equals("Advanced Options")) {
            advanced_dlg.setVisible(true);
        } else if (ae.getActionCommand().equals("Done")) {
            advanced_dlg.setVisible(false);
        }
    } /* end actionPerformed */

    /*------------------------------------------------------------------*/
    public void applyTransformationToSource(int intervals, double[][] cx, double[][] cy) {
        // Apply transformation
        unwarpJMiscTools.applyTransformationToSource(sourceImp, targetImp, source, intervals, cx, cy);
        // Restart the computation of the model
        cancelSource();
        targetPh.removePoints();
        createSourceImage();
        setSecondaryPointHandlers();
    }

    /*------------------------------------------------------------------*/
    public void createAdvancedOptions() {
        advanced_dlg = new Dialog(new Frame(), "Advanced Options", true);
        // Create min_scale_deformation, max_scale_deformation panel
        advanced_dlg.setLayout(new GridLayout(0, 1));
        final Choice min_scale_deformationChoice = new Choice();
        final Choice max_scale_deformationChoice = new Choice();
        final Panel min_scale_deformationPanel = new Panel();
        final Panel max_scale_deformationPanel = new Panel();
        min_scale_deformationPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        max_scale_deformationPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        final Label min_scale_deformationLabel = new Label("Initial Deformation: ");
        final Label max_scale_deformationLabel = new Label("Final Deformation: ");
        min_scale_deformationChoice.add("Very Coarse");
        min_scale_deformationChoice.add("Coarse");
        min_scale_deformationChoice.add("Fine");
        min_scale_deformationChoice.add("Very Fine");
        max_scale_deformationChoice.add("Very Coarse");
        max_scale_deformationChoice.add("Coarse");
        max_scale_deformationChoice.add("Fine");
        max_scale_deformationChoice.add("Very Fine");
        min_scale_deformationChoice.select(min_scale_deformation);
        max_scale_deformationChoice.select(max_scale_deformation);
        min_scale_deformationChoice.addItemListener(new ItemListener() {
            public void itemStateChanged(final ItemEvent ie) {
                final int new_min_scale_deformation = min_scale_deformationChoice.getSelectedIndex();
                int new_max_scale_deformation = max_scale_deformation;
                if (max_scale_deformation < new_min_scale_deformation) {
                    new_max_scale_deformation = new_min_scale_deformation;
                }
                if (new_min_scale_deformation != min_scale_deformation || new_max_scale_deformation != max_scale_deformation) {
                    min_scale_deformation = new_min_scale_deformation;
                    max_scale_deformation = new_max_scale_deformation;
                    computeImagePyramidDepth();
                    restartModelThreads();
                }
                min_scale_deformationChoice.select(min_scale_deformation);
                max_scale_deformationChoice.select(max_scale_deformation);
            }
        });
        max_scale_deformationChoice.addItemListener(new ItemListener() {
            public void itemStateChanged(final ItemEvent ie) {
                final int new_max_scale_deformation = max_scale_deformationChoice.getSelectedIndex();
                int new_min_scale_deformation = min_scale_deformation;
                if (new_max_scale_deformation < min_scale_deformation) {
                    new_min_scale_deformation = new_max_scale_deformation;
                }
                if (new_max_scale_deformation != max_scale_deformation || new_min_scale_deformation != min_scale_deformation) {
                    min_scale_deformation = new_min_scale_deformation;
                    max_scale_deformation = new_max_scale_deformation;
                    computeImagePyramidDepth();
                    restartModelThreads();
                }
                max_scale_deformationChoice.select(max_scale_deformation);
                min_scale_deformationChoice.select(min_scale_deformation);
            }
        });
        min_scale_deformationPanel.add(min_scale_deformationLabel);
        max_scale_deformationPanel.add(max_scale_deformationLabel);
        min_scale_deformationPanel.add(min_scale_deformationChoice);
        max_scale_deformationPanel.add(max_scale_deformationChoice);
        // Create div and curl weight panels
        final Panel divWeightPanel = new Panel();
        divWeightPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        final Label label_divWeight = new Label();
        label_divWeight.setText("Divergence Weight:");
        divWeightTextField = new TextField("", 5);
        divWeightTextField.setText("" + divWeight);
        divWeightTextField.addTextListener(new TextListener() {
            public void textValueChanged(final TextEvent e) {
                boolean validNumber = true;
                try {
                    divWeight = Double.valueOf(divWeightTextField.getText()).doubleValue();
                } catch (NumberFormatException n) {
                    validNumber = false;
                }
                DoneButton.setEnabled(validNumber);
            }
        });
        divWeightPanel.add(label_divWeight);
        divWeightPanel.add(divWeightTextField);
        divWeightPanel.setVisible(true);
        final Panel curlWeightPanel = new Panel();
        curlWeightPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        final Label label_curlWeight = new Label();
        label_curlWeight.setText("Curl Weight:");
        curlWeightTextField = new TextField("", 5);
        curlWeightTextField.setText("" + curlWeight);
        curlWeightTextField.addTextListener(new TextListener() {
            public void textValueChanged(final TextEvent e) {
                boolean validNumber = true;
                try {
                    curlWeight = Double.valueOf(curlWeightTextField.getText()).doubleValue();
                } catch (NumberFormatException n) {
                    validNumber = false;
                }
                DoneButton.setEnabled(validNumber);
            }
        });
        curlWeightPanel.add(label_curlWeight);
        curlWeightPanel.add(curlWeightTextField);
        curlWeightPanel.setVisible(true);
        // Create landmark and image weight panels
        final Panel landmarkWeightPanel = new Panel();
        landmarkWeightPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        final Label label_landmarkWeight = new Label();
        label_landmarkWeight.setText("Landmark Weight:");
        landmarkWeightTextField = new TextField("", 5);
        landmarkWeightTextField.setText("" + landmarkWeight);
        landmarkWeightTextField.addTextListener(new TextListener() {
            public void textValueChanged(final TextEvent e) {
                boolean validNumber = true;
                try {
                    landmarkWeight = Double.valueOf(landmarkWeightTextField.getText()).doubleValue();
                } catch (NumberFormatException n) {
                    validNumber = false;
                }
                DoneButton.setEnabled(validNumber);
            }
        });
        landmarkWeightPanel.add(label_landmarkWeight);
        landmarkWeightPanel.add(landmarkWeightTextField);
        landmarkWeightPanel.setVisible(true);
        final Panel imageWeightPanel = new Panel();
        imageWeightPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        final Label label_imageWeight = new Label();
        label_imageWeight.setText("Image Weight:");
        imageWeightTextField = new TextField("", 5);
        imageWeightTextField.setText("" + imageWeight);
        imageWeightTextField.addTextListener(new TextListener() {
            public void textValueChanged(final TextEvent e) {
                boolean validNumber = true;
                try {
                    imageWeight = Double.valueOf(imageWeightTextField.getText()).doubleValue();
                } catch (NumberFormatException n) {
                    validNumber = false;
                }
                DoneButton.setEnabled(validNumber);
            }
        });
        imageWeightPanel.add(label_imageWeight);
        imageWeightPanel.add(imageWeightTextField);
        imageWeightPanel.setVisible(true);
        // Create stopThreshold panel
        final Panel stopThresholdPanel = new Panel();
        stopThresholdPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        final Label label_stopThreshold = new Label();
        label_stopThreshold.setText("Stop Threshold:");
        stopThresholdTextField = new TextField("", 5);
        stopThresholdTextField.setText("" + stopThreshold);
        stopThresholdTextField.addTextListener(new TextListener() {
            public void textValueChanged(final TextEvent e) {
                boolean validNumber = true;
                try {
                    stopThreshold = Double.valueOf(stopThresholdTextField.getText()).doubleValue();
                } catch (NumberFormatException n) {
                    validNumber = false;
                }
                DoneButton.setEnabled(validNumber);
            }
        });
        stopThresholdPanel.add(label_stopThreshold);
        stopThresholdPanel.add(stopThresholdTextField);
        stopThresholdPanel.setVisible(true);
        // Create checkbox for creating rich output
        final Panel richOutputPanel = new Panel();
        richOutputPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        ckRichOutput = new Checkbox("Verbose", richOutput);
        richOutputPanel.add(ckRichOutput);
        // Create checkbox for saving the transformation
        final Panel saveTransformationPanel = new Panel();
        saveTransformationPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        ckSaveTransformation = new Checkbox("Save Transformation", saveTransformation);
        saveTransformationPanel.add(ckSaveTransformation);
        // Create close button
        final Panel buttonPanel = new Panel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        final Button CloseButton = new Button("Close");
        CloseButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent ae) {
                if (ae.getActionCommand().equals("Close")) {
                    advanced_dlg.dispose();
                }
            }
        });
        buttonPanel.add(CloseButton);
        // Build separations
        final Label separation1 = new Label("");
        // Create the dialog
        advanced_dlg.add(min_scale_deformationPanel);
        advanced_dlg.add(max_scale_deformationPanel);
        advanced_dlg.add(divWeightPanel);
        advanced_dlg.add(curlWeightPanel);
        advanced_dlg.add(landmarkWeightPanel);
        advanced_dlg.add(imageWeightPanel);
        advanced_dlg.add(stopThresholdPanel);
        advanced_dlg.add(richOutputPanel);
        advanced_dlg.add(saveTransformationPanel);
        advanced_dlg.add(separation1);
        advanced_dlg.add(buttonPanel);
        advanced_dlg.pack();
        advanced_dlg.setVisible(false);
    }

    /*------------------------------------------------------------------*/
    public void freeMemory() {
        advanced_dlg = null;
        imageList = null;
        sourceIc = null;
        targetIc = null;
        sourceImp = null;
        targetImp = null;
        source = null;
        target = null;
        sourcePh = null;
        targetPh = null;
        tb = null;
        Runtime.getRuntime().gc();
    }

    /*------------------------------------------------------------------*/
    public void grayImage(final unwarpJPointHandler ph) {
        if (ph == sourcePh) {
            int Xdim = source.getWidth();
            int Ydim = source.getHeight();
            FloatProcessor fp = new FloatProcessor(Xdim, Ydim);
            int ij = 0;
            double[] source_data = source.getImage();
            for (int i = 0; i < Ydim; i++) {
                for (int j = 0; j < Xdim; j++, ij++) {
                    if (sourceMsk.getValue(j, i)) {
                        fp.putPixelValue(j, i, source_data[ij]);
                    } else {
                        fp.putPixelValue(j, i, 0.5 * source_data[ij]);
                    }
                }
            }
            fp.resetMinAndMax();
            sourceImp.setProcessor(sourceImp.getTitle(), fp);
            sourceImp.updateImage();
        } else {
            int Xdim = target.getWidth();
            int Ydim = target.getHeight();
            FloatProcessor fp = new FloatProcessor(Xdim, Ydim);
            double[] target_data = target.getImage();
            int ij = 0;
            for (int i = 0; i < Ydim; i++) {
                for (int j = 0; j < Xdim; j++, ij++) {
                    if (targetMsk.getValue(j, i)) {
                        fp.putPixelValue(j, i, target_data[ij]);
                    } else {
                        fp.putPixelValue(j, i, 0.5 * target_data[ij]);
                    }
                }
            }
            fp.resetMinAndMax();
            targetImp.setProcessor(targetImp.getTitle(), fp);
            targetImp.updateImage();
        }
    }

    /*------------------------------------------------------------------*/
    public boolean isFinalActionLaunched() {
        return finalActionLaunched;
    }

    /*------------------------------------------------------------------*/
    public boolean isClearMaskSet() {
        return clearMask;
    }

    /*------------------------------------------------------------------*/
    public boolean isSaveTransformationSet() {
        return saveTransformation;
    }

    /*------------------------------------------------------------------*/
    public boolean isStopRegistrationSet() {
        return stopRegistration;
    }

    /*------------------------------------------------------------------*/
    public Insets getInsets() {
        return new Insets(0, 20, 20, 20);
    } /* end getInsets */

    /*------------------------------------------------------------------*/
    public void joinThreads() {
        try {
            source.getThread().join();
            target.getThread().join();
        } catch (InterruptedException e) {
            IJ.error("Unexpected interruption exception" + e);
        }
    } /* end joinSourceThread */

    /*------------------------------------------------------------------*/
    public void restoreAll() {
        cancelSource();
        cancelTarget();
        tb.restorePreviousToolbar();
        Toolbar.getInstance().repaint();
        unwarpJProgressBar.resetProgressBar();
        Runtime.getRuntime().gc();
    } /* end restoreAll */

    /*------------------------------------------------------------------*/
    public void setClearMask(boolean val) {
        clearMask = val;
    }

    /*------------------------------------------------------------------*/
    public void setStopRegistration() {
        stopRegistration = true;
    }

    /*------------------------------------------------------------------*/
    public unwarpJDialog(final Frame parentWindow, final ImagePlus[] imageList) {
        super(parentWindow, "UnwarpJ", false);
        this.imageList = imageList;
        // Start concurrent image processing threads
        createSourceImage();
        createTargetImage();
        setSecondaryPointHandlers();
        // Create Source panel
        setLayout(new GridLayout(0, 1));
        final Choice sourceChoice = new Choice();
        final Choice targetChoice = new Choice();
        final Panel sourcePanel = new Panel();
        sourcePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        final Label sourceLabel = new Label("Source: ");
        addImageList(sourceChoice);
        sourceChoice.select(sourceChoiceIndex);
        sourceChoice.addItemListener(new ItemListener() {
            public void itemStateChanged(final ItemEvent ie) {
                final int newChoiceIndex = sourceChoice.getSelectedIndex();
                if (sourceChoiceIndex != newChoiceIndex) {
                    stopSourceThread();
                    if (targetChoiceIndex != newChoiceIndex) {
                        sourceChoiceIndex = newChoiceIndex;
                        cancelSource();
                        targetPh.removePoints();
                        createSourceImage();
                        setSecondaryPointHandlers();
                    } else {
                        stopTargetThread();
                        targetChoiceIndex = sourceChoiceIndex;
                        sourceChoiceIndex = newChoiceIndex;
                        targetChoice.select(targetChoiceIndex);
                        permuteImages();
                    }
                }
                repaint();
            }
        });
        sourcePanel.add(sourceLabel);
        sourcePanel.add(sourceChoice);
        // Create target panel
        final Panel targetPanel = new Panel();
        targetPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        final Label targetLabel = new Label("Target: ");
        addImageList(targetChoice);
        targetChoice.select(targetChoiceIndex);
        targetChoice.addItemListener(new ItemListener() {
            public void itemStateChanged(final ItemEvent ie) {
                final int newChoiceIndex = targetChoice.getSelectedIndex();
                if (targetChoiceIndex != newChoiceIndex) {
                    stopTargetThread();
                    if (sourceChoiceIndex != newChoiceIndex) {
                        targetChoiceIndex = newChoiceIndex;
                        cancelTarget();
                        sourcePh.removePoints();
                        createTargetImage();
                        setSecondaryPointHandlers();
                    } else {
                        stopSourceThread();
                        sourceChoiceIndex = targetChoiceIndex;
                        targetChoiceIndex = newChoiceIndex;
                        sourceChoice.select(sourceChoiceIndex);
                        permuteImages();
                    }
                }
                repaint();
            }
        });
        targetPanel.add(targetLabel);
        targetPanel.add(targetChoice);
        // Create mode panel
        setLayout(new GridLayout(0, 1));
        final Choice modeChoice = new Choice();
        final Panel modePanel = new Panel();
        modePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        final Label modeLabel = new Label("Registration Mode: ");
        modeChoice.add("Fast");
        modeChoice.add("Accurate");
        modeChoice.select(mode);
        modeChoice.addItemListener(new ItemListener() {
            public void itemStateChanged(final ItemEvent ie) {
                final int mode = modeChoice.getSelectedIndex();
                if (mode == 0) {
                    // Fast
                    min_scale_image = 1;
                } else if (mode == 1) {
                    // Accurate
                    min_scale_image = 0;
                }
                repaint();
            }
        });
        modePanel.add(modeLabel);
        modePanel.add(modeChoice);
        // Build Advanced Options panel
        final Panel advancedOptionsPanel = new Panel();
        advancedOptionsPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        final Button advancedOptionsButton = new Button("Advanced Options");
        advancedOptionsButton.addActionListener(this);
        advancedOptionsPanel.add(advancedOptionsButton);
        // Build Done Cancel Credits panel
        final Panel buttonPanel = new Panel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        DoneButton.addActionListener(this);
        final Button cancelButton = new Button("Cancel");
        cancelButton.addActionListener(this);
        final Button creditsButton = new Button("Credits...");
        creditsButton.addActionListener(this);
        buttonPanel.add(cancelButton);
        buttonPanel.add(DoneButton);
        buttonPanel.add(creditsButton);
        // Build separations
        final Label separation1 = new Label("");
        final Label separation2 = new Label("");
        // Finally build dialog
        add(separation1);
        add(sourcePanel);
        add(targetPanel);
        add(modePanel);
        add(advancedOptionsPanel);
        add(separation2);
        add(buttonPanel);
        pack();
        createAdvancedOptions();
    } /* end unwarpJDialog */

    /*------------------------------------------------------------------*/
    public void ungrayImage(final unwarpJPointAction pa) {
        if (pa == sourcePh.getPointAction()) {
            int Xdim = source.getWidth();
            int Ydim = source.getHeight();
            FloatProcessor fp = new FloatProcessor(Xdim, Ydim);
            int ij = 0;
            double[] source_data = source.getImage();
            for (int i = 0; i < Ydim; i++) {
                for (int j = 0; j < Xdim; j++, ij++) {
                    fp.putPixelValue(j, i, source_data[ij]);
                }
            }
            fp.resetMinAndMax();
            sourceImp.setProcessor(sourceImp.getTitle(), fp);
            sourceImp.updateImage();
        } else {
            int Xdim = target.getWidth();
            int Ydim = target.getHeight();
            FloatProcessor fp = new FloatProcessor(Xdim, Ydim);
            double[] target_data = target.getImage();
            int ij = 0;
            for (int i = 0; i < Ydim; i++) {
                for (int j = 0; j < Xdim; j++, ij++) {
                    fp.putPixelValue(j, i, target_data[ij]);
                }
            }
            fp.resetMinAndMax();
            targetImp.setProcessor(targetImp.getTitle(), fp);
            targetImp.updateImage();
        }
    }

    /*....................................................................
    Private methods
    ....................................................................*/
    /*------------------------------------------------------------------*/
    private void addImageList(final Choice choice) {
        for (int k = 0; k < imageList.length; k++) {
            choice.add(imageList[k].getTitle());
        }
    } /* end addImageList */

    /*------------------------------------------------------------------*/
    private void cancelSource() {
        sourcePh.killListeners();
        sourcePh = null;
        sourceIc = null;
        sourceImp.killRoi();
        sourceImp = null;
        source = null;
        sourceMsk = null;
        Runtime.getRuntime().gc();
    } /* end cancelSource */

    /*------------------------------------------------------------------*/
    private void cancelTarget() {
        targetPh.killListeners();
        targetPh = null;
        targetIc = null;
        targetImp.killRoi();
        targetImp = null;
        target = null;
        targetMsk = null;
        Runtime.getRuntime().gc();
    } /* end cancelTarget */

    /*------------------------------------------------------------------*/
    private void computeImagePyramidDepth() {
        imagePyramidDepth = max_scale_deformation - min_scale_deformation + 1;
    }

    /*------------------------------------------------------------------*/
    private void createSourceImage() {
        sourceImp = imageList[sourceChoiceIndex];
        sourceImp.setSlice(1);
        source = new unwarpJImageModel(sourceImp.getProcessor(), false);
        source.setPyramidDepth(imagePyramidDepth + min_scale_image);
        source.getThread().start();
        sourceIc = sourceImp.getWindow().getCanvas();
        if (sourceImp.getStackSize() == 1) {
            // Create an empy mask
            sourceMsk = new unwarpJMask(sourceImp.getProcessor(), false);
        } else {
            // Take the mask from the second slice
            sourceImp.setSlice(2);
            sourceMsk = new unwarpJMask(sourceImp.getProcessor(), true);
            sourceImp.setSlice(1);
        }
        sourcePh = new unwarpJPointHandler(sourceImp, tb, sourceMsk, this);
        tb.setSource(sourceImp, sourcePh);
    } /* end createSourceImage */

    /*------------------------------------------------------------------*/
    private void createTargetImage() {
        targetImp = imageList[targetChoiceIndex];
        targetImp.setSlice(1);
        target = new unwarpJImageModel(targetImp.getProcessor(), true);
        target.setPyramidDepth(imagePyramidDepth + min_scale_image);
        target.getThread().start();
        targetIc = targetImp.getWindow().getCanvas();
        if (targetImp.getStackSize() == 1) {
            // Create an empy mask
            targetMsk = new unwarpJMask(targetImp.getProcessor(), false);
        } else {
            // Take the mask from the second slice
            targetImp.setSlice(2);
            targetMsk = new unwarpJMask(targetImp.getProcessor(), true);
            targetImp.setSlice(1);
        }
        targetPh = new unwarpJPointHandler(targetImp, tb, targetMsk, this);
        tb.setTarget(targetImp, targetPh);
    } /* end createTargetImage */

    /*------------------------------------------------------------------*/
    private void permuteImages() {
        // Swap image canvas
        final ImageCanvas swapIc = sourceIc;
        sourceIc = targetIc;
        targetIc = swapIc;
        // Swap ImagePlus
        final ImagePlus swapImp = sourceImp;
        sourceImp = targetImp;
        targetImp = swapImp;
        // Swap Mask
        final unwarpJMask swapMsk = sourceMsk;
        sourceMsk = targetMsk;
        targetMsk = swapMsk;
        // Swap Point Handlers
        final unwarpJPointHandler swapPh = sourcePh;
        sourcePh = targetPh;
        targetPh = swapPh;
        setSecondaryPointHandlers();
        // Inform the Toolbar about the change
        tb.setSource(sourceImp, sourcePh);
        tb.setTarget(targetImp, targetPh);
        // Restart the computation with each image
        source = new unwarpJImageModel(sourceImp.getProcessor(), false);
        source.setPyramidDepth(imagePyramidDepth + min_scale_image);
        source.getThread().start();
        target = new unwarpJImageModel(targetImp.getProcessor(), true);
        target.setPyramidDepth(imagePyramidDepth + min_scale_image);
        target.getThread().start();
    } /* end permuteImages */

    /*------------------------------------------------------------------*/
    private void removePoints() {
        sourcePh.removePoints();
        targetPh.removePoints();
    }

    /*------------------------------------------------------------------*/
    private void restartModelThreads() {
        // Stop threads
        stopSourceThread();
        stopTargetThread();
        // Remove the current image models
        source = null;
        target = null;
        Runtime.getRuntime().gc();
        // Now restart the threads
        source = new unwarpJImageModel(sourceImp.getProcessor(), false);
        source.setPyramidDepth(imagePyramidDepth + min_scale_image);
        source.getThread().start();
        target = new unwarpJImageModel(targetImp.getProcessor(), true);
        target.setPyramidDepth(imagePyramidDepth + min_scale_image);
        target.getThread().start();
    }

    /*------------------------------------------------------------------*/
    private void setSecondaryPointHandlers() {
        sourcePh.setSecondaryPointHandler(targetImp, targetPh);
        targetPh.setSecondaryPointHandler(sourceImp, sourcePh);
    } /* end setSecondaryPointHandler */

    /*------------------------------------------------------------------*/
    private void stopSourceThread() {
        // Stop the source image model
        while (source.getThread().isAlive()) {
            source.getThread().interrupt();
        }
        source.getThread().interrupted();
    } /* end stopSourceThread */

    /*------------------------------------------------------------------*/
    private void stopTargetThread() {
        // Stop the target image model
        while (target.getThread().isAlive()) {
            target.getThread().interrupt();
        }
        target.getThread().interrupted();
    } /* end stopTargetThread */
    
} /* end class unwarpJDialog */