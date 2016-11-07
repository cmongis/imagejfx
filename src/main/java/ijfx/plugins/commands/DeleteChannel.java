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

import net.imagej.Dataset;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.plugins.commands.restructure.DeleteData;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.command.ContextCommand;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.DialogPrompt;

/**
 *
 * @author Cyril MONGIS, 2016
 */
@Plugin(type = Command.class, menuPath = "Image > Color > Delete channel", initializer = "init")
public class DeleteChannel extends ContextCommand {

    @Parameter
    CommandService commandService;

    @Parameter(type = ItemIO.BOTH)
    Dataset dataset;

    @Parameter
    ImageDisplayService imageDisplayService;

  
    Integer channel;

    @Override
    public void run() {
 if (AxisUtils.hasAxisType(dataset, Axes.CHANNEL) == false) {
            cancel("This image is not a multichannel image");
            return;
        }
         DeleteData deleteData = commandService.create(DeleteData.class
        );

        AxisType axisType = Axes.CHANNEL;

         if (imageDisplayService.getActiveDataset() == dataset) {

            ImageDisplay display = imageDisplayService.getActiveImageDisplay();
            channel = display.getIntPosition(Axes.CHANNEL);
            System.out.println(channel);
        }

        deleteData.setInput("axisName", axisType.toString());
        deleteData.setInput("dataset", dataset);
        deleteData.setInput("position", channel+1);
        deleteData.setInput("quantity", 1);
        deleteData.run();
        
    }

    public void init() {

       

       

    }

}
