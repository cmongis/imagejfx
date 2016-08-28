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

import static ijfx.plugins.LUTInvert.invertColorTable;
import ijfx.plugins.commands.ApplyLUT;
import ijfx.ui.plugin.LUTCreatorDialog;
import ijfx.ui.plugin.LutViewChanger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import javafx.application.Platform;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.axis.Axes;
import net.imagej.display.DatasetView;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.lut.LUTService;
import net.imglib2.display.ColorTable;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.display.DisplayService;
import org.scijava.module.ModuleService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Tuan anh TRINH
 */
@Plugin(type = Command.class, menuPath = "Image>Color>Custom LUT")
public class CustomLUT implements Command {

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
    DatasetView datasetView;

    @Override
    public void run() {
//         LutViewChanger lutViewChanger = new LUTCreatorDialog(new ArrayList<>());
        Platform.runLater(() -> {

            LutViewChanger lutViewChanger = new LUTCreatorDialog(new ArrayList<>()).showAndWait().orElseThrow(IllegalArgumentException::new);

            applyLUT(lutViewChanger.getColorTable());
        });

    }

    public void applyLUT(ColorTable table) {
//        displayService.get
//        DatasetView datasetView = imageDisplayService.getActiveDatasetView();
        HashMap<String, Object> params = new HashMap<>();
        params.put("colorTable", table);
        int channel = imageDisplayService.getActiveDatasetView().getIntPosition(Axes.CHANNEL);
        channel = channel == -1 ? 0 : channel;
        datasetView.setColorTable(table, channel);
    }
}
