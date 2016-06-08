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
package ijfx.plugins.commands;

import ijfx.service.sampler.DatasetSamplerService;
import net.imagej.Dataset;
import net.imagej.axis.AxisType;
import org.scijava.ItemIO;
import org.scijava.ItemVisibility;
import org.scijava.command.ContextCommand;
import org.scijava.plugin.Parameter;
import org.scijava.ui.DialogPrompt;
import org.scijava.ui.UIService;

/**
 *
 * @author cyril
 */
public abstract class SeparateCommandBase extends ContextCommand {

    @Parameter(type = ItemIO.INPUT)
    Dataset input;

    @Parameter(type = ItemIO.OUTPUT)
    Dataset[] output;

    @Parameter
    DatasetSamplerService datasetSamplerService;

    @Parameter
    UIService uiService;

    @Parameter(visibility = ItemVisibility.INVISIBLE)
    Boolean forceSeparation = false;

    @Override
    public void run() {

        long size = input.dimension(input.dimensionIndex(getAxis()));

        if (!forceSeparation && size > 10) {
            DialogPrompt.Result answer = uiService.showDialog(String.format("This will create %d windows,\nare you sure you want to continute ?", size), DialogPrompt.MessageType.QUESTION_MESSAGE);
            if (DialogPrompt.Result.YES_OPTION != answer) {
                return;
            }
        }

        output = new Dataset[(int) size];

        for (int i = 0; i != size; i++) {

            output[i] = datasetSamplerService.isolateDimension(input, getAxis(), i);

        }
        
        

    }

    protected abstract AxisType getAxis();

}
