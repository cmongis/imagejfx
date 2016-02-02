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
package ijfx.ui.project_manager.projectdisplay;

import ijfx.core.metadata.MetaData;
import ijfx.core.project.imageDBService.PlaneDB;

/**
 *
 * @author cyril
 */
public class PlaneOrMetaData {
    
    PlaneDB planeDB;
    
    MetaData metaData;
    
    public static String ROOT = "root";
    
    
    public boolean isPlane() {
        return planeDB != null;
    }
    
    public boolean isMetaData() {
        return metaData != null;
    }
    
    public boolean isBoth() {
        return isPlane() && isMetaData();
    }

    public PlaneOrMetaData() {
    }
    
    public PlaneOrMetaData(MetaData m) {
        this.metaData = m;
    }
    public PlaneOrMetaData(PlaneDB plane) {
        this.planeDB = plane;
    }

    public MetaData getMetaData() {
        return metaData;
    }

    public PlaneDB getPlaneDB() {
        return planeDB;
    }
    
    @Override
    public String toString() {
        if(isMetaData()) {
            
            
            
            return getMetaData().getName() + " : " + getMetaData().getStringValue();
        }
        else {
            return "Plane !";
        }
    }
    
    public void setPlane(PlaneDB planeDB) {
        this.planeDB = planeDB;
    }
    
    
    
}
