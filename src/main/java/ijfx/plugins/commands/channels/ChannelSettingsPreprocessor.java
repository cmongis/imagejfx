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
package ijfx.plugins.commands.channels;

import net.imagej.Dataset;
import net.imagej.display.ImageDisplayService;
import org.bytedeco.javacpp.opencv_core.RefOrVoid.type;
import org.scijava.module.Module;
import org.scijava.module.ModuleItem;
import org.scijava.module.ModuleService;
import org.scijava.module.process.AbstractPreprocessorPlugin;
import org.scijava.module.process.PreprocessorPlugin;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = PreprocessorPlugin.class)
public class ChannelSettingsPreprocessor extends AbstractPreprocessorPlugin  {

    @Parameter
    ImageDisplayService imageDisplayService;

    @Parameter
    ModuleService moduleService;
    
    @Override
    public void process(Module module) {
       
        ModuleItem<ChannelSettings> singleInput = moduleService.getSingleInput(module, ChannelSettings.class);
        ModuleItem<Dataset> dataset = moduleService.getSingleInput(module, Dataset.class);
        if(singleInput != null) {
            if(!module.isInputResolved(singleInput.getName())) {
                module.setInput(singleInput.getName(), new DefaultChannelSettings().importFromDatasetView(imageDisplayService.getActiveDatasetView()));
            }
        }
    }
    
   
    
}
