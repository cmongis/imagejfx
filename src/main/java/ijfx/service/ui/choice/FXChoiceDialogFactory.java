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
package ijfx.service.ui.choice;

import ijfx.service.PluginUtilsService;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.PluginService;

/**
 *
 * @author cyril
 */
public class FXChoiceDialogFactory implements ChoiceDialogFactory {

    @Parameter
    PluginUtilsService pluginUtilsService;

    @Parameter
    PluginService pluginService;

    public FXChoiceDialogFactory(Context context) {
        context.inject(this);
    }
    
    @Override
    public <T> ChoiceDialog<T> create(Class<? extends T> clazz, List<T> choices) {

        try {
            
            
            
            Choice<T> wrapper = pluginUtilsService.createHandler(Choice.class, clazz);
            
            
            
            if (wrapper == null) {
                return null;
            }

            return new FXChoiceDialog()
                    .addChoices(choices.stream().map(wrapper::create).collect(Collectors.toList()));
        } catch (IOException ex) {
            Logger.getLogger(FXChoiceDialogFactory.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;

    }
    
   

}
