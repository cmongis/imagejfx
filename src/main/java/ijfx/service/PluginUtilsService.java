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
package ijfx.service;

import ijfx.core.Handles;
import ijfx.ui.main.ImageJFX;
import java.util.logging.Level;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;
import org.scijava.plugin.SciJavaPlugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 *
 * @author Cyril MONGIS, 2016
 */
@Plugin(type = Service.class)
public class PluginUtilsService extends AbstractService implements IjfxService {
    
    
    @Parameter
    PluginService pluginService;
    
    
    /**
     * 
     * @param <T> the type of plugin that should be returned
     * @param <H> the type of object the plugin should handle (specified using the @Handles annotation)
     * @param pluginType the type of plugin to create
     * @param handledType the type of object the plugin should handle
     * @return the instantiated plugin
     */
    public <T extends SciJavaPlugin,H> T createHandler(Class<T> pluginType, Class<H> handledType) {
        try {
         return pluginService
                .getPluginsOfType(pluginType)
                .stream()
                .filter(infos->filter(infos,handledType))
                .findFirst()
                .map(pluginService::createInstance)
                .orElse(null);
        }
        catch(Exception e) {
            ImageJFX.getLogger().log(Level.SEVERE,String.format("Error when creating plugin of %s handling %s",pluginType.getSimpleName(),handledType.getSimpleName()),e);
        }
        return null;
    }
    
    private <T extends SciJavaPlugin,H> boolean filter(PluginInfo<T> infos, Class<H> handledType) {
        Handles annotation = infos.getPluginClass().getAnnotation(Handles.class);
        
        return annotation != null && annotation.type().isAssignableFrom(handledType);
    }
    
}
