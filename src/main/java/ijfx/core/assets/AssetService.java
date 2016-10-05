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
import java.io.File;
import java.util.HashMap;
import org.scijava.plugin.Parameter;
import org.scijava.service.AbstractService;

/**
 * The Asset service was created for batch processing.
 * It basically handles the loading and unloading of assets,
 * which can be any kind of data which is repeatedly reused.
 * The Asset Service loads each assets once and when not used for
 * a time, delete them from memory.
 * @author cyril
 */
public class AssetService extends AbstractService implements IjfxService{
    
    @Parameter
    PluginUtilsService pluginUtilsService;
    
    HashMap<File,Object> assets = new HashMap<>();
    
    public <T> T load(Asset<T> asset) {
        return (T)assets.computeIfAbsent(asset.getFile(), f->loadAsset(asset));
    }
    
    private <T> T loadAsset(Asset<T> asset) {
        AssetLoader<T> loader = pluginUtilsService.createHandler(AssetLoader.class, asset.getAssetType());
        return loader.load(asset);
    }  
}
