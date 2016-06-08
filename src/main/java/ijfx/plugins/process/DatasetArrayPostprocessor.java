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
package ijfx.plugins.process;

import net.imagej.Dataset;
import org.scijava.module.Module;
import org.scijava.module.ModuleItem;
import org.scijava.module.ModuleService;
import org.scijava.module.process.AbstractPostprocessorPlugin;
import org.scijava.module.process.PostprocessorPlugin;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

/**
 *
 * @author cyril
 */
@Plugin(type = PostprocessorPlugin.class)
public class DatasetArrayPostprocessor extends AbstractPostprocessorPlugin{

    @Parameter
    ModuleService moduleService;
    
    @Parameter
    UIService uiService;
    
    @Override
    public void process(Module module) {
        
        ModuleItem<Dataset[]> singleOutput = moduleService.getSingleOutput(module, Dataset[].class);
        if(singleOutput != null) {
            Dataset[] datasetArray = (Dataset[]) module.getOutput(singleOutput.getName());
            for(Dataset dataset : datasetArray) {
                uiService.show(dataset);
            }
        }
    }
    
}
