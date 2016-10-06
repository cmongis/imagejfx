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
package ijfx.core.assets;

import ijfx.service.IjfxService;
import ijfx.service.PluginUtilsService;
import java.util.HashMap;
import java.util.UUID;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 * The Asset service was created for batch processing. It basically handles the
 * loading and unloading of assets, which can be any kind of data which is
 * repeatedly reused. The Asset Service loads each assets once and when not used
 * for a time, delete them from memory.
 *
 * @author cyril
 */
@Plugin(type = Service.class)
public class AssetService extends AbstractService implements IjfxService {

    @Parameter
    PluginUtilsService pluginUtilsService;

    HashMap<UUID, Object> assets = new HashMap<>();

    public <T> T load(Asset<T> asset) {
        
        if(assets.containsKey(asset.getId()) == false) {
            Object o = loadAsset(asset);
           
            assets.put(asset.getId(), o);
        }
        return (T) assets.get(asset.getId());
    }

    private <T> T loadAsset(Asset<T> asset) {
        AssetLoader<T> loader;

        loader = pluginUtilsService.createHandler(AssetLoader.class, asset.getClass());
        if (loader == null) {
            loader = pluginUtilsService.createHandler(AssetLoader.class, asset.getAssetType());
        };
        T t = loader.load(asset);
        if(t == null) {
            throw new IllegalArgumentException("Asset couldn't be loaded because the loader failed");
        }
        return t;
    }
}
