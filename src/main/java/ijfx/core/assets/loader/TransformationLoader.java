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

import bunwarpj.Transformation;
import ijfx.core.Handles;
import ijfx.core.assets.Asset;
import ijfx.core.assets.AssetLoader;
import ijfx.core.assets.BUnwarpJTransformationAsset;
import ijfx.plugins.adapter.IJ1Service;
import java.awt.Point;
import java.util.Stack;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Cyril MONGIS
 * @author Tuan-ahn Trin
 */
@Plugin(type = AssetLoader.class)
@Handles(type = BUnwarpJTransformationAsset.class)
public class TransformationLoader implements AssetLoader<Transformation>
{
    
   
    
    @Parameter
    Context context;
    
    @Parameter
    IJ1Service ij1Service;
    
    public Transformation load(Asset<Transformation> rawAsset) {
        
        if(rawAsset instanceof BUnwarpJTransformationAsset == false) {
            throw new IllegalArgumentException("TransformationLoader can only deal with BUnwarpJTransformationAssets");
        }
        
        BUnwarpJTransformationAsset asset = (BUnwarpJTransformationAsset)rawAsset;
        Stack<Point> sourceStack = new Stack<>();
        Stack<Point> targetStack = new Stack<>();
        asset.getParameters().landmarkWeight = 1.0;
        asset.getParameters().imageWeight = 0.0;
        asset.getParameters().img_subsamp_fact = 0;
        asset.getParameters().mode = 2;
        bunwarpj.MiscTools.loadPoints(asset.getFile().getAbsolutePath(), sourceStack, targetStack);
        Transformation transformation = bunwarpj.bUnwarpJ_.computeTransformationBatch(asset.getSourceWidth(), asset.getSourceHeight(), asset.getTargetWidth(), asset.getTargetHeight(), sourceStack, targetStack, asset.getParameters());
        return transformation;
    }
}
