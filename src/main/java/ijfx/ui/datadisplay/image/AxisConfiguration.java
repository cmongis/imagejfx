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
package ijfx.ui.datadisplay.image;

import java.util.Arrays;
import java.util.function.Consumer;
import net.imagej.axis.CalibratedAxis;
import net.imagej.display.ImageDisplay;

/**
 *
 * @author Cyril MONGIS, 2016
 */
class AxisConfiguration {

    private ImageDisplay imageDisplay;
    private CalibratedAxis[] axes;
    private Consumer<AxisConfiguration> onChange;

    public AxisConfiguration(ImageDisplay dataset) {
        imageDisplay = dataset;
        rebuild();
    }

    private void rebuild() {
        axes = new CalibratedAxis[imageDisplay.numDimensions()];
        imageDisplay.axes(axes);

    }

    public int numAxis() {
        return axes.length;
    }

    public CalibratedAxis[] axes() {
        return axes;
    }

    public void check(ImageDisplay imageDisplay, boolean update) {
        AxisConfiguration conf = new AxisConfiguration(imageDisplay);
        if (!equals(conf) && onChange != null) {
            if (update) {
                rebuild();
            }
            onChange.accept(conf);
        }
    }

    public AxisConfiguration setOnChange(Consumer<AxisConfiguration> runnable) {
        this.onChange = runnable;
        return this;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (object instanceof ImageDisplay) {
            return equals(new AxisConfiguration((ImageDisplay) object));
        }
        if (object instanceof AxisConfiguration == false) {
            return false;
        }
        AxisConfiguration other = (AxisConfiguration) object;
        if (other.numAxis() != numAxis()) {
            return false;
        }
        return Arrays.equals(axes, other.axes());
    }

}
