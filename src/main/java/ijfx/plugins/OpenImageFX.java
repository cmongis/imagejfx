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

import ijfx.service.dataset.DatasetUtillsService;
import ijfx.ui.main.ImageJFX;
import io.scif.services.DatasetIOService;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.imagej.Dataset;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

/**
 *
 * @author Cyril MONGIS, 2016
 */
@Plugin(type = Command.class)
public class OpenImageFX extends ContextCommand {

    @Parameter(label = "File to open", required = true, type = ItemIO.INPUT)
    File file;

    @Parameter(required = false)
    int imageId = -1;

    @Parameter(type = ItemIO.OUTPUT)
    Dataset output;

    @Parameter
    DatasetIOService datasetIOService;

    @Parameter
    DatasetUtillsService datasetUtilsService;

    @Parameter
    UIService uiService;
    
    
    
    Logger logger = ImageJFX.getLogger();
    
    @Override
    public void run() {
        try {
            if (imageId == -1) {
                output = datasetIOService.open(file.getAbsolutePath());
            }
            else {
                output = datasetUtilsService.open(file, imageId, false);
            }
        } catch (Exception e) {
            uiService.showDialog(String.format("Can't open file : %s",file.getName()));
            logger.log(Level.SEVERE,"Error when opening "+file.getName(),e);
            cancel(null);
        }

    }

}
