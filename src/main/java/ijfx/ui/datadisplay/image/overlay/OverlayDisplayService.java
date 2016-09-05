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
package ijfx.ui.datadisplay.image.overlay;

import ijfx.service.IjfxService;
import net.imagej.overlay.Overlay;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;
import org.scijava.service.AbstractService;
import org.scijava.service.SciJavaService;

/**
 *
 * @author cyril
 */
@Plugin(type = SciJavaService.class)
public class OverlayDisplayService extends AbstractService implements IjfxService {

    //HashMap<Overlay, OverlayDrawer> drawerMap = new HashMap<>();
    //HashMap<Overlay, OverlayModifier> modifierMap = new HashMap<>();
    @Parameter
    PluginService pluginService;

    @Parameter
    Context context;

    public OverlayModifier createModifier(Overlay overlay) {
        return findPluginFor(OverlayModifier.class, overlay);
    }

    //public OverlayDrawer createDrawer(Overlay overlay) {
      //  return findPluginFor(OverlayDrawer.class, overlay);
    //}

    public  OverlayDrawer createDrawer(Class<?> o) {
        
        for (OverlayDrawer drawer : pluginService.createInstancesOfType(OverlayDrawer.class)) {
            if(drawer.canHandle(o))
            return drawer;
        }
        return null;
    }
    
    private <T extends ClassHandler, C> T findPluginFor(Class<? extends T> handlerType, C overlay) {

        // if (map.containsKey(overlay) == false || map.get(overlay) == null) {
        for (T plugin : pluginService.createInstancesOfType(handlerType)) {
            if (plugin.canHandle(overlay)) {

                return plugin;
                // map.put(overlay, plugin);

            }
        }
        // }

        return null;
    }

}
