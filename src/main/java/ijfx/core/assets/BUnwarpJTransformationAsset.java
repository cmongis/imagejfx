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

/**
 *
 * @author cyril
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
    
    public BUnwarpJTransformationAsset(File file, bunwarpj.Param parameters,) {
        setLandmarkFile(landmarkFile);
        setParameters(parameters);
    }
    
    public void setParameters(Param parameters) {
        this.parameters = parameters;
    }

    public Param getParameters() {
        return parameters;
    }
    
    
    
    public void setLandmarkFile(File landmarkFile) {
        this.landmarkFile = landmarkFile;
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
