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
package ijfx.core.metadata.extraction;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class ImageFile {
    
    
    PlaneList imagePlaneList = new PlaneList();
    
    File sourceFile;

    public ImageFile() {
        
    }
    
    public ImageFile(String file) {
        setSourceFile(new File(file));
    }
    
    
    public void add(ImagePlane plane) {
        getImagePlaneList().add(plane);
    }
    
    public PlaneList getImagePlaneList() {
        return imagePlaneList;
    }

    public void setImagePlaneList(PlaneList imagePlaneList) {
        
        this.imagePlaneList = imagePlaneList;
    }

    public File getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(File sourceFile) {
        this.sourceFile = sourceFile;
    }
    
    
    
    
}
