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

import java.util.Vector;

/*====================================================================
|   unwarpJCredits
\===================================================================*/
/*====================================================================
|   unwarpJCumulativeQueue
\===================================================================*/
class unwarpJCumulativeQueue extends Vector {

    private int ridx;
    private int widx;
    private int currentLength;
    private double sum;

    /*------------------------------------------------------------------*/
    public unwarpJCumulativeQueue(int length) {
        currentLength = ridx = widx = 0;
        setSize(length);
    }

    /*------------------------------------------------------------------*/
    public int currentSize() {
        return currentLength;
    }

    /*------------------------------------------------------------------*/
    public double getSum() {
        return sum;
    }

    /*------------------------------------------------------------------*/
    public double pop_front() {
        if (currentLength == 0) {
            return 0.0;
        }
        double x = ((Double) elementAt(ridx)).doubleValue();
        currentLength--;
        sum -= x;
        ridx++;
        if (ridx == size()) {
            ridx = 0;
        }
        return x;
    }

    /*------------------------------------------------------------------*/
    public void push_back(double x) {
        if (currentLength == size()) {
            pop_front();
        }
        setElementAt(new Double(x), widx);
        currentLength++;
        sum += x;
        widx++;
        if (widx == size()) {
            widx = 0;
        }
    }
    
} /* end class unwarpJCumulativeQueue */