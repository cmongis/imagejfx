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

import java.util.Optional;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.display.DatasetView;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.lut.LUTService;
import net.imglib2.display.ColorTable;
import net.imglib2.display.ColorTable16;
import net.imglib2.display.ColorTable8;
import org.apache.commons.lang.ArrayUtils;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.display.DisplayService;
import org.scijava.module.ModuleService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Invert ColorTable values.
 *
 * @author Tuan anh TRINH
 */
@Plugin(type = Command.class, menuPath = "Image>Color>Invert LUT")
public class LUTInvert implements Command {

    @Parameter
    ModuleService moduleService;

    @Parameter
    CommandService commandService;

    @Parameter
    ImageDisplayService imageDisplayService;

    @Parameter
    DatasetService datasetService;

    @Parameter
    DisplayService displayService;

    @Parameter
    LUTService lUTService;

    @Parameter(type = ItemIO.BOTH)
    Dataset dataset;

    @Override
    public void run() {
        Optional<ImageDisplay> imageDisplayOptional
                = imageDisplayService
                .getImageDisplays()
                .stream()
                .filter((i) -> imageDisplayService.getActiveDataset(i) == dataset)
                .findFirst();
        ImageDisplay imageDisplay = imageDisplayOptional.get();
        dataset = imageDisplayService.getActiveDataset(imageDisplay);
        for (int i = 0; i < dataset.getColorTableCount(); i++) {
            ColorTable colorTable = dataset.getColorTable(i);
            ColorTable colorTableInverted = invertColorTable(colorTable);
            dataset.setColorTable(colorTableInverted, i);
        }
        for (int k = 0; k < imageDisplay.size(); k++) {
            DatasetView datasetView = ((DatasetView) imageDisplay.get(k));
            for (int j = 0; j < datasetView.getColorTables().size(); j++) {
                ColorTable colorTable = dataset.getColorTable(j);
                ColorTable colorTableInverted = invertColorTable(colorTable);
                datasetView.setColorTable(colorTableInverted, j);
            }

        }
        dataset.update();
        imageDisplay.update();
    }

    /**
     * Invert ColorTable, but work only with ColorTable8 and ColorTable16
     *
     * @param colorTable
     * @return
     */
    public static ColorTable invertColorTable(ColorTable colorTable) {
        //Couldn't find a generic way...
        if (colorTable instanceof ColorTable8) {
            ColorTable8 colorTable8 = (ColorTable8) colorTable;
            byte[][] values = colorTable8.getValues();
            for (byte[] value : values) {
                ArrayUtils.reverse(value);
            }
            ColorTable8 intvertColorTable8 = new ColorTable8(values);
            return intvertColorTable8;
        } else if (colorTable instanceof ColorTable16) {
            ColorTable16 colorTable16 = (ColorTable16) colorTable;
            short[][] values = colorTable16.getValues();
            for (short[] value : values) {
                ArrayUtils.reverse(value);
            }
            ColorTable16 intvertColorTable16 = new ColorTable16(values);
            return intvertColorTable16;
        } else {
            //Has to be implemented for an other type
            return null;
        }
    }
}
