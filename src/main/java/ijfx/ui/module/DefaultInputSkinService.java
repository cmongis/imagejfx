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
package ijfx.ui.module;

import ijfx.ui.main.ImageJFX;
import ijfx.ui.module.input.Input;
import ijfx.ui.module.input.InputSkin;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.scijava.InstantiableException;
import org.scijava.Priority;
import org.scijava.module.Module;
import org.scijava.module.ModuleItem;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 * This service is also a SkinFactory used for creating skin for the InputControl depending
 * on the type of input. The different skins (@InputSkinPlugin) are loaded using SciJava.
 * Using the method <code>canHandle</code> of each plugin, the
 * service detects if a plugin can handle a specific type of input.
 * It requires to keep instances of each plugin all the time.
 * To reduce the memory footprint, a good practice is that each plugin
 * should instanciate graphic element in there <code>init()</code> method and
 * not in the constructor. Thus, the <code>init()</code> is called after
 * dependency injection so if some plugins requires services, they
 * should keep most of their initiation logic in this method.
 * @author Cyril MONGIS
 */


@Plugin(type=Service.class, priority = Priority.LOW_PRIORITY)
public class DefaultInputSkinService extends AbstractService implements InputSkinPluginService {

    @Parameter
    private PluginService pluginService;

    private HashMap<PluginInfo, InputSkinPlugin> skins = new HashMap<>();

    Logger logger = ImageJFX.getLogger();

    @Override
    public void initialize() {
        super.initialize();
        // create an instance of each plugin so the service can have access to the canHandle method
        pluginService.getPluginsOfType(InputSkinPlugin.class).forEach(skinPlugin -> {

            try {
                // also keeps an instance of the @PluginInfo instance
                skins.put(skinPlugin, skinPlugin.createInstance());
            } catch (InstantiableException ex) {
                logger.warning("Error when loading InputSkinPlugin " + skinPlugin.getPluginClass().getName());
                logger.log(Level.SEVERE, null, ex);
            }

        });
    }

    @Override
    public InputSkin createSkin(Input input) {

        // get the first input that can handle a the type
        InputSkinPlugin plugin
                = skins.values().stream().filter(skin -> skin.canHandle(input.getType())).findFirst().orElse(null);

        // if a plugin is found
        if (plugin != null) {
            try {
                // create a new instance from the @PluginInfo object
                InputSkinPlugin createdPlugin = (InputSkinPlugin) pluginService.getPlugin(plugin.getClass()).createInstance();
                
                // inject the context inside the newly created plugin
                getContext().inject(createdPlugin);
                
                // invoke the init method of the plugin using the input object
                createdPlugin.init(input);
                
                // returns the object
                return  createdPlugin;
            } catch (InstantiableException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }

        return null;

    }

    
    @Override
    public boolean canCreateSkinFor(Input input) {
        // filter the list of skins and returns true if at least one of them can handle the input
        return skins.values().stream().filter(skin -> skin.canHandle(input.getType())).count() > 0;
    }

    
    @Override
    public int getHandledInputs(Module module) {

        // go through the input and count the inputs that can be handled
        return (int) module.getInputs()
                .keySet()
                .stream()
                .parallel()
                .filter(keyName -> {
                    ModuleItem input = module.getInfo().getInput(keyName);
                    boolean canCreateSkinFor = canCreateSkinFor(new ModuleInputWrapper(module, input));
                    boolean isResolved = module.isResolved(keyName);
                    boolean isNull = module.getInput(keyName) == null;
                    
                    boolean finalDecision = canCreateSkinFor && !isResolved && input.isRequired();
                    
                    logger.info(
                            String.format("[%s] %s : is resolved (%s) / can create (%s) / isRequired (%s) / isNull (%s) == Final Decision ==> %s"
                                    ,module.getDelegateObject().getClass().getSimpleName()
                                    ,keyName
                                    ,isResolved
                                    ,canCreateSkinFor
                                    ,input.isRequired()
                            ,isNull,finalDecision));
                    return finalDecision;
                            
                })
                .count();

    }

}
