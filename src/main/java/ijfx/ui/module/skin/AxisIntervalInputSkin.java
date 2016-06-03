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
package ijfx.ui.module.skin;

import ijfx.plugins.AxisInterval;
import ijfx.plugins.DefaultAxisInterval;
import ijfx.plugins.LongInterval;
import ijfx.plugins.RemoveSlice;
import ijfx.plugins.commands.AxisUtils;
import ijfx.ui.module.InputSkinPlugin;
import net.imagej.axis.AxisType;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = InputSkinPlugin.class)
public class AxisIntervalInputSkin extends AbstractIntervalInputSkin<AxisInterval> {

    @Parameter
    ImageDisplayService imageDisplayService;

    @Override
    protected AxisInterval createInitialInterval() {

        DefaultAxisInterval interval = new DefaultAxisInterval(0, 100);
        long min, max, low, high;

        ImageDisplay imageDisplay = imageDisplayService.getActiveImageDisplay();

        AxisType axisType = AxisUtils.getSliceAxis(imageDisplay);
        if (axisType == null) {
            min = 0;
            max = 0;
            low = min;
            high = max;
        } else {
            int axisIndex = imageDisplay.dimensionIndex(axisType);

            min = imageDisplay.min(axisIndex);
            max = imageDisplay.max(axisIndex);
            low = min;
            high = max;

        }

        return interval;
    }

    @Override
    public boolean canHandle(Class<?> clazz) {

        return AxisInterval.class.isAssignableFrom(clazz);
    }

}
