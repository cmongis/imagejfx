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

/*====================================================================
|   unwarpJPointAction
\===================================================================*/
/*====================================================================
|   unwarpJPointHandler
\===================================================================*/
/*====================================================================
|   unwarpJPointToolbar
\===================================================================*/
/*====================================================================
|   unwarpJProgressBar
\===================================================================*/
/*********************************************************************
 * This class implements the interactions when dealing with ImageJ's
 * progress bar.
 ********************************************************************/
class unwarpJProgressBar {

    /* begin class unwarpJProgressBar */
    /*....................................................................
    Private variables
    ....................................................................*/
    /*********************************************************************
     * Same time constant than in ImageJ version 1.22
     ********************************************************************/
    private static final long TIME_QUANTUM = 50L;
    private static volatile long lastTime = System.currentTimeMillis();
    private static volatile int completed = 0;
    private static volatile int workload = 0;

    /*....................................................................
    Public methods
    ....................................................................*/
    /*********************************************************************
     * Extend the amount of work to perform by <code>batch</code>.
     *
     * @param batch Additional amount of work that need be performed.
     ********************************************************************/
    public static synchronized void addWorkload(final int batch) {
        workload += batch;
    } /* end addWorkload */

    /*********************************************************************
     * Erase the progress bar and cancel pending operations.
     ********************************************************************/
    public static synchronized void resetProgressBar() {
        final long timeStamp = System.currentTimeMillis();
        if ((timeStamp - lastTime) < TIME_QUANTUM) {
            try {
                Thread.sleep(TIME_QUANTUM - timeStamp + lastTime);
            } catch (InterruptedException e) {
                IJ.error("Unexpected interruption exception" + e);
            }
        }
        lastTime = timeStamp;
        completed = 0;
        workload = 0;
        IJ.showProgress(1.0);
    } /* end resetProgressBar */

    /*********************************************************************
     * Perform <code>stride</code> operations at once.
     *
     * @param stride Amount of work that is skipped.
     ********************************************************************/
    public static synchronized void skipProgressBar(final int stride) {
        completed += stride - 1;
        stepProgressBar();
    } /* end skipProgressBar */

    /*********************************************************************
     * Perform <code>1</code> operation unit.
     ********************************************************************/
    public static synchronized void stepProgressBar() {
        final long timeStamp = System.currentTimeMillis();
        completed = completed + 1;
        if ((TIME_QUANTUM <= (timeStamp - lastTime)) | (completed == workload)) {
            lastTime = timeStamp;
            IJ.showProgress((double) completed / (double) workload);
        }
    } /* end stepProgressBar */

    /*********************************************************************
     * Acknowledge that <code>batch</code> work has been performed.
     *
     * @param batch Completed amount of work.
     ********************************************************************/
    public static synchronized void workloadDone(final int batch) {
        workload -= batch;
        completed -= batch;
    } /* end workloadDone */
    
} /* end class unwarpJProgressBar */