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

import ij.ImagePlus;
import java.awt.Button;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/*====================================================================
|   unwarpJClearAll
\===================================================================*/
/*------------------------------------------------------------------*/
class unwarpJClearAll extends Dialog implements ActionListener {

    /* begin class unwarpJClearAll */
    /*....................................................................
    Private variables
    ....................................................................*/
    private ImagePlus sourceImp;
    private ImagePlus targetImp;
    private unwarpJPointHandler sourcePh;
    private unwarpJPointHandler targetPh;

    /*....................................................................
    Public methods
    ....................................................................*/
    /*------------------------------------------------------------------*/
    public void actionPerformed(final ActionEvent ae) {
        if (ae.getActionCommand().equals("Clear All")) {
            sourcePh.removePoints();
            targetPh.removePoints();
            setVisible(false);
        } else if (ae.getActionCommand().equals("Cancel")) {
            setVisible(false);
        }
    } /* end actionPerformed */

    /*------------------------------------------------------------------*/
    public Insets getInsets() {
        return new Insets(0, 20, 20, 20);
    } /* end getInsets */

    /*------------------------------------------------------------------*/
    unwarpJClearAll(final Frame parentWindow, final ImagePlus sourceImp, final ImagePlus targetImp, final unwarpJPointHandler sourcePh, final unwarpJPointHandler targetPh) {
        super(parentWindow, "Removing Points", true);
        this.sourceImp = sourceImp;
        this.targetImp = targetImp;
        this.sourcePh = sourcePh;
        this.targetPh = targetPh;
        setLayout(new GridLayout(0, 1));
        final Button removeButton = new Button("Clear All");
        removeButton.addActionListener(this);
        final Button cancelButton = new Button("Cancel");
        cancelButton.addActionListener(this);
        final Label separation1 = new Label("");
        final Label separation2 = new Label("");
        add(separation1);
        add(removeButton);
        add(separation2);
        add(cancelButton);
        pack();
    } /* end unwarpJClearAll */
    
} /* end class unwarpJClearAll */