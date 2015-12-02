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
package ijfx.bridge;

import ijfx.ui.main.ImageJFX;
import java.util.logging.Logger;
import org.scijava.Priority;
import org.scijava.module.Module;
import org.scijava.module.process.AbstractPreprocessorPlugin;
import org.scijava.module.process.PreprocessorPlugin;
import org.scijava.plugin.Plugin;

/**
 * This is a dummy Preprocessor plugin showing which module has been executed
 *
 * @author MONGIS Cyril
 */
@Plugin(type = PreprocessorPlugin.class, priority = Priority.VERY_LOW_PRIORITY)
public class FxModuleSaver extends AbstractPreprocessorPlugin {

    private final static Logger logger = ImageJFX.getLogger();
    
    @Override
    public void process(Module module) {
        
        
        StringBuilder logBuilder = new StringBuilder();
        
        module.getInfo().inputs().forEach(input -> {
            Object value = input.getValue(module);
            if (value == null) {
                value = "Not SET !";
            } else {
                value = value.toString();
            }
            logBuilder.append(String.format("%s = %s", input.getName(), value));
           
            
            

        });
        
         logger.finest(String.format("###### %s #####\n%s"
                 , module.getDelegateObject().getClass().getSimpleName()
                 ,logBuilder.toString()));

    }

}
