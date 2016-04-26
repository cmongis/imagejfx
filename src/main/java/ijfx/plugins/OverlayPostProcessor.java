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

import java.util.Arrays;
import java.util.Map;
import net.imagej.display.ImageDisplay;
import net.imagej.display.OverlayService;
import net.imagej.overlay.Overlay;
import org.scijava.module.Module;
import org.scijava.module.ModuleItem;
import org.scijava.module.ModuleService;
import org.scijava.module.process.AbstractPostprocessorPlugin;
import org.scijava.module.process.PostprocessorPlugin;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = PostprocessorPlugin.class)
public class OverlayPostProcessor extends AbstractPostprocessorPlugin implements PostprocessorPlugin {

    @Parameter
    ModuleService moduleService;

    @Parameter
    OverlayService overlayService;

    @Override
    public void process(Module module) {

        System.out.println("Processing :" + module.getClass().getSimpleName());

        // getting the single output
        ModuleItem<Overlay[]> singleOutput = moduleService.getSingleOutput(module, Overlay[].class);

        Overlay[] overlays;
        ImageDisplay display = null;

        // checking the inputs
        Map<String, Object> inputs = module.getInputs();
        for (String key : inputs.keySet()) {
            Object value = inputs.get(key);
            if (ImageDisplay.class.isAssignableFrom(value.getClass())) {
                display = (ImageDisplay) value;
            }
        }

       

        // aborting if no singleinput is a list of overlay
        if (singleOutput == null || display == null) {
            return;
        }
        
         // checking the output
        overlays = singleOutput.getValue(module);

        overlayService.addOverlays(display, Arrays.asList(overlays));

    }
}
