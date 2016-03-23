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
package ijfx.ui.filter;

import ijfx.core.metadata.MetaDataOwner;
import ijfx.ui.filter.string.DefaultStringFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author Pierre BONNEAU
 */
public class DefaultMetaDataFilterFactory implements MetaDataFilterFactory{
    
    @Override
    public MetaDataOwnerFilter generateFilter(Collection<MetaDataOwner> ownerList, String keyName)throws IOException{
        
        Collection<Object> possibleValues = getAllPossibleValues(ownerList, keyName);
        int type = checktype(ownerList, keyName);
        
        MetaDataOwnerFilter ownerFilter;
        
        if(type == 0){
            ownerFilter = createStringFilter(possibleValues, keyName);
        }
        else{
            ownerFilter = createNumberFilter(possibleValues, keyName);
        }
        
        return ownerFilter;
    }

    
    public Collection<Object> getAllPossibleValues(Collection<MetaDataOwner> ownerList, String keyName){
        
        Collection<Object> possibleValues = new ArrayList<>();
        
        ownerList.stream().forEach(owner -> possibleValues.add(owner.getMetaDataSet().get(keyName).getValue()));
        
        return possibleValues;
    }
    
    
    public int checktype(Collection<MetaDataOwner> ownerList, String keyName){
        
        boolean sameType = true;
        
        Iterator it = ownerList.iterator();
        
        MetaDataOwner owner = (MetaDataOwner) it.next();
        
        int type = owner.getMetaDataSet().get(keyName).getType();
        
        while(it.hasNext()){
            owner = (MetaDataOwner) it.next();
            int newType = owner.getMetaDataSet().get(keyName).getType();
            if(newType != type){
                type = 0;
                break;
            }
        }
        
        return type;
    }
    
    
    public MetaDataOwnerFilter createStringFilter(Collection<Object> possibleValues, String keyName) throws IOException{
        
        StringFilter filter = new DefaultStringFilter();
        MetaDataOwnerFilter wrapper = new StringFilterWrapper(filter, keyName);
        
        return wrapper;
    }
    
    
    public MetaDataOwnerFilter createNumberFilter(Collection<Object> possibleValues, String keyName){
        
        NumberFilter filter = new DefaultNumberFilter();
        MetaDataOwnerFilter wrapper = new NumberFilterWrapper(filter, keyName);
        
        return wrapper;
    }
    
    

}
