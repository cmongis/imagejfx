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

import ijfx.service.overlay.io.NoSourceFileException;
import ijfx.service.overlay.io.OverlayIOService;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.imagej.display.ImageDisplay;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.DialogPrompt;
import org.scijava.ui.UIService;

/**
 *
 * @author Cyril MONGIS, 2016
 */
@Plugin(type = Command.class, menuPath = "Image > Overlay > Save Overlays")
public class SaveOverlays implements Command {

    @Parameter(type = ItemIO.BOTH)
    ImageDisplay imageDisplay;

    @Parameter
    OverlayIOService overlayIoService;
    
    @Parameter
    UIService uiService;
    
    @Override
    public void run() {

        try {
            overlayIoService.saveOverlays(imageDisplay);
        } catch (NoSourceFileException ex) {
            Logger.getLogger(SaveOverlays.class.getName()).log(Level.SEVERE, null, ex);
            uiService.showDialog("This dataset has no source. Save the dataset before saving the Overlays.", DialogPrompt.MessageType.ERROR_MESSAGE);
        } catch (IOException ex) {
            Logger.getLogger(SaveOverlays.class.getName()).log(Level.SEVERE, null, ex);
            //uiService.showDialog("Couldn't save the Overlay. File system access error !", DialogPrompt.MessageType.ERROR_MESSAGE);
        }

    }

}
