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
package ijfx.service.batch.preprocessors;

import ijfx.service.batch.BatchSingleInput;
import ijfx.ui.batch.BatchPrepreprocessorPlugin;
import java.util.Map;
import net.imagej.plugins.commands.binary.Binarize;
import org.scijava.module.Module;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Cyril MONGIS, 2016
 */
@Plugin(type = BatchPrepreprocessorPlugin.class)
public class BinarizePreprocessor implements BatchPrepreprocessorPlugin {

    @Override
    public void process(BatchSingleInput input, Module module, Map<String, Object> parameters) {
        if (module.getDelegateObject().getClass() == Binarize.class) {
            
            parameters.forEach((key,value)->{
                System.out.printf("%s = %s\n",key,value);
            });
            if (module.getInput("inputData") == null) {
                module.setInput("inputData", input.getDataset());
               
            }
            module.setInput("changeInput", false);
        }
    }

}
