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
package ijfx.core.assets.loader;

import ijfx.core.Handles;
import ijfx.core.assets.Asset;
import ijfx.core.assets.AssetLoader;
import io.scif.services.DatasetIOService;
import java.io.IOException;
import net.imagej.Dataset;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Cyril MONGIS, 2016
 */
@Plugin(type = AssetLoader.class)
@Handles(type = Dataset.class)
public class DatasetAssetLoader implements AssetLoader<Dataset>{

    @Parameter
    DatasetIOService datasetIOService;
    
    @Override
    public Dataset load(Asset<Dataset> asset) {
        try {
            if(asset.getFile().exists() == false) throw new IllegalArgumentException("The file to be loaded doesn't exist !");
            return datasetIOService.open(asset.getFile().getAbsolutePath());
        } catch (IOException ex) {
            return null;
        }
    }
    
}
