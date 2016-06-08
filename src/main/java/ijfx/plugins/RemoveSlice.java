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
package ijfx.plugins;

import ijfx.plugins.commands.AxisUtils;
import net.imagej.Dataset;
import net.imagej.axis.AxisType;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.plugins.commands.restructure.DeleteData;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.module.ModuleService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.DialogPrompt;
import org.scijava.ui.UIService;

/**
 *
 * @author cyril
 */
@Plugin(type = Command.class, initializer = "init")
public class RemoveSlice implements Command {

    @Parameter(label = "Interval to remove")
    LongInterval interval;

    @Parameter(type = ItemIO.BOTH)
    Dataset dataset;

    AxisType axistype;

    @Parameter
    ModuleService moduleService;

    @Parameter
    CommandService commandService;

    @Parameter
    UIService uiService;

    @Parameter
    ImageDisplayService imageDisplayService;

    public void init() {

        if (interval == null) {
            interval = new DefaultAxisInterval(0, 100);
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
        }

    }

    @Override
    public void run() {

        DeleteData deleteData = commandService.create(DeleteData.class
        );

        AxisType axisType = AxisUtils.getSliceAxis(dataset);

        if (axisType == null) {
            uiService.showDialog("The dataset doesn't contain any Z or Time axis", DialogPrompt.MessageType.ERROR_MESSAGE);
        }

        deleteData.setInput("axisName", axisType.toString());
        deleteData.setInput("dataset", dataset);
        deleteData.setInput("position", interval.getLowValue());
        deleteData.setInput("quantity", interval.getHighValue());
        deleteData.run();

    }

}
