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
package ijfx.plugins.commands.measures;

import ijfx.core.metadata.MetaDataKeyPriority;
import ijfx.core.metadata.MetaDataSet;
import ijfx.core.metadata.MetaDataSetUtils;
import ijfx.ui.datadisplay.metadataset.MetaDataSetDisplay;
import ijfx.ui.main.ImageJFX;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import mongis.utils.TextFileUtils;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.DialogPrompt;
import org.scijava.ui.UIService;
import org.scijava.widget.FileWidget;

/**
 *
 * @author cyril
 */
@Plugin(type = Command.class,label = "Save table as")
public class SaveMetaDataSetAsCsv extends ContextCommand {
     @Parameter(label = "File to save",style = FileWidget.SAVE_STYLE)
    File outputFile;
    
    @Parameter(type = ItemIO.BOTH)
    MetaDataSetDisplay objectDisplay;
    
    @Parameter
    UIService uiService;
    
    @Override
    public void run() {
        
        if(outputFile == null) {
            cancel("File set to null");
            return;
        }
        if(isCanceled()) return;
        
        List<MetaDataSet> rows = objectDisplay;
        String content = MetaDataSetUtils.exportToCSV(rows, ",", true, MetaDataKeyPriority.getPriority(objectDisplay.get(0)));
        try {
            TextFileUtils.writeTextFile(outputFile, content);
        } catch (IOException ex) {
           ImageJFX.getLogger().log(Level.SEVERE, null, ex);
           uiService.showDialog("Error when saving CSV file.", DialogPrompt.MessageType.ERROR_MESSAGE);
        }
    }
}
