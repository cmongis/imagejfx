/*
 * /*
 *     This file is part of ImageJ FX.
 *
 *     ImageJ FX is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     ImageJ FX is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with ImageJ FX.  If not, see <http://www.gnu.org/licenses/>. 
 *
 * 	Copyright 2015,2016 Cyril MONGIS, Michael Knop
 *
 */
package ijfx.ui.plugin;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import ijfx.ui.main.Localization;
import org.scijava.plugin.Plugin;
import ijfx.ui.main.ImageJFX;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import net.imagej.plugins.commands.typechange.TypeChanger;
import net.imagej.types.DataTypeService;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import ijfx.ui.UiConfiguration;

//@Plugin(type = FxWidgetPlugin.class)
//@UiConfiguration(id = "float-image-button", context = "imagej+image-open", localization = Localization.LEFT)
public class FloatTheImage extends AbstractContextButton {

    @Parameter
    CommandService commandService;

    @Parameter
    DataTypeService dataTypeService;

    public FloatTheImage() {
        // defines the label and icon of the button
        super(FontAwesomeIcon.LEAF);
    }

    @Override
    public void onAction(ActionEvent event) {

        String type = dataTypeService.getTypeByAttributes(64,true,false,false,true).longName();
        
        final Future future = commandService.run(TypeChanger.class, true,
                // setting command parameters
                "typeName",type, "combineChannels", false);
        
        submitFuture(future);

    }

    
    
}
