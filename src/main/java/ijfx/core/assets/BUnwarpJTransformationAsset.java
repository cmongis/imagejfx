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

import bunwarpj.Param;
import bunwarpj.Transformation;
import java.io.File;
import java.util.UUID;
import static javax.swing.Spring.height;
import static javax.swing.Spring.width;
import net.imagej.Dataset;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class BUnwarpJTransformationAsset implements Asset<Transformation>{

    
    File landmarkFile;

    
    bunwarpj.Param parameters;

    int sourceWidth;
    int sourceHeight;
    int targetWidth;
    int targetHeight;
    
    public BUnwarpJTransformationAsset() {
        
    }
    
    public BUnwarpJTransformationAsset(
            File file, bunwarpj.Param parameters
            , int sourceWidth
            , int sourceHeight
            , int targetWidth
            , int targetHeight) {
        setLandmarkFile(file);
        setParameters(parameters);
        setSourceWidth(sourceWidth);
        setSourceHeight(sourceHeight);
        setTargetWidth(targetWidth);
        setTargetHeight(targetHeight);
        
    }
    
    
    public BUnwarpJTransformationAsset setSourceDimension(int width, int height) {
        setSourceWidth(width);
        setSourceHeight(height);
        return this;
    }
    
    
    
    public BUnwarpJTransformationAsset setTargetDimension(int width, int height) {
        setSourceWidth(width);
        setSourceHeight(height);
        return this;
    }
    
    
    public BUnwarpJTransformationAsset setSourceDimension(Dataset source) {
        setSourceWidth((int)source.dimension(0));
        setSourceHeight((int)source.dimension(1));
        return this;
    }
    
    
    
    public BUnwarpJTransformationAsset setTargetDimension(Dataset target) {
        setTargetWidth((int)target.dimension(0));
        setTargetHeight((int)target.dimension(1));
        return this;
    }
    
    
    public BUnwarpJTransformationAsset setParameters(Param parameters) {
        this.parameters = parameters;
        return this;
    }

    public Param getParameters() {
        return parameters;
    }
    
    
    
    public BUnwarpJTransformationAsset setLandmarkFile(File landmarkFile) {
        this.landmarkFile = landmarkFile;
        return this;
    }

    public int getSourceWidth() {
        return sourceWidth;
    }

    public void setSourceWidth(int sourceWidth) {
        this.sourceWidth = sourceWidth;
    }

    public int getSourceHeight() {
        return sourceHeight;
    }

    public void setSourceHeight(int sourceHeight) {
        this.sourceHeight = sourceHeight;
    }

    public int getTargetWidth() {
        return targetWidth;
    }

    public void setTargetWidth(int targetWidth) {
        this.targetWidth = targetWidth;
    }

    public int getTargetHeight() {
        return targetHeight;
    }

    public void setTargetHeight(int targetHeight) {
        this.targetHeight = targetHeight;
    }
    
    
    
    
    @Override
    public UUID getId() {
        return UUID.nameUUIDFromBytes((landmarkFile.getAbsolutePath()+parameters.toString()).getBytes());
    }

    @Override
    public File getFile() {
        return landmarkFile;
    }

    @Override
    public Class<Transformation> getAssetType() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
