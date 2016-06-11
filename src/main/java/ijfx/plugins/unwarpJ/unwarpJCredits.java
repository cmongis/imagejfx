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

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/*====================================================================
|   unwarpJCredits
\===================================================================*/
/*------------------------------------------------------------------*/
class unwarpJCredits extends Dialog {

    /* begin class unwarpJCredits */
    /*....................................................................
    Public methods
    ....................................................................*/
    /*------------------------------------------------------------------*/
    public Insets getInsets() {
        return new Insets(0, 20, 20, 20);
    } /* end getInsets */

    /*------------------------------------------------------------------*/
    public unwarpJCredits(final Frame parentWindow) {
        super(parentWindow, "UnwarpJ", true);
        setLayout(new BorderLayout(0, 20));
        final Label separation = new Label("");
        final Panel buttonPanel = new Panel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        final Button doneButton = new Button("Done");
        doneButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent ae) {
                if (ae.getActionCommand().equals("Done")) {
                    dispose();
                }
            }
        });
        buttonPanel.add(doneButton);
        final TextArea text = new TextArea(22, 72);
        text.setEditable(false);
        text.append("\n");
        text.append(" This work is based on the following paper:\n");
        text.append("\n");
        text.append(" C.O.S. Sorzano, P. Th" + (char) 233 + "venaz, M. Unser\n");
        text.append(" Elastic Registration of Biological Images Using Vector-Spline Regularization\n");
        text.append(" IEEE Transactions on Biomedical Engineering\n");
        text.append(" vol. ??, no. ??, pp. ??-??, July 2005.\n");
        text.append("\n");
        text.append(" This paper is available on-line at\n");
        text.append(" http://bigwww.epfl.ch/publications/sorzano0501.html\n");
        text.append("\n");
        text.append(" Other relevant on-line publications are available at\n");
        text.append(" http://bigwww.epfl.ch/publications/\n");
        text.append("\n");
        text.append(" Additional help available at\n");
        text.append(" http://bigwww.epfl.ch/thevenaz/UnwarpJ/\n");
        text.append("\n");
        text.append(" You'll be free to use this software for research purposes, but\n");
        text.append(" you should not redistribute it without our consent. In addition,\n");
        text.append(" we expect you to include a citation or acknowledgment whenever\n");
        text.append(" you present or publish results that are based on it.\n");
        add("North", separation);
        add("Center", text);
        add("South", buttonPanel);
        pack();
    } /* end unwarpJCredits */
    
} /* end class unwarpJCredits */