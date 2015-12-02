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

import ijfx.core.metadata.MetaDataSet;
import java.util.ArrayList;
import java.util.Set;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class PlaneList extends ArrayList<ImagePlane> {
    
    public ArrayList<String> getMetaDataKey() {
        
        ArrayList<String> keyList = new ArrayList<String>();
        
        // for each plane of the list
        this.forEach(plane -> {
            // for each key of the metadata set
            plane.getMetaDataSet().keySet().forEach(key -> {
                
                if (!keyList.contains(key)) {
                    keyList.add(key);
                }
            });
        });
        
        return keyList;
    }
    
    public void keepOnlyMetaDataInCommon(ArrayList<String> finalList, Set keySet) {
        
        ArrayList<String> toRemove = new ArrayList<String>();
        finalList.forEach(key -> {
            if (!keySet.contains(key)) {
                toRemove.add(key);
            }
        });
        
        finalList.removeAll(toRemove);
        
    }
    
    public ArrayList<String> getMetaDataInCommon() {
        
        if (size() == 0) {
            return new ArrayList<String>();
        }
        
        ArrayList<String> initial = new ArrayList<String>(get(0).getMetaDataSet().keySet());
        
        this.forEach(plane -> {
            keepOnlyMetaDataInCommon(initial, plane.getMetaDataSet().keySet());
        });
        
        return initial;
    }
    
    public ArrayList<Object> getAllPossibleValuesFor(String key) {
        ArrayList<Object> possibleValues = new ArrayList<Object>();
        
        this.forEach(plane -> {
            Object value = plane.getMetaDataSet().get(key).getValue();
            if (possibleValues.contains(value) == false) {
                possibleValues.add(value);
            }
        });
        
        return possibleValues;
    }

    // add this set to all planes of the list
    public void mergeMetaDataSet(MetaDataSet set) {
        
        
        this.forEach(plane -> {
            plane.getMetaDataSet().merge(set);
        });
    }
    
    
    
    
}
