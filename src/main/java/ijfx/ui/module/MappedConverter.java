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
package ijfx.ui.module;

import ijfx.core.utils.Converter;
import java.util.HashMap;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class MappedConverter<R> extends HashMap<String,R> implements Converter<String,R> {

    public MappedConverter() {
        super();
    }
    
    public MappedConverter(Object... keyAndValue) {
        if(keyAndValue.length % 2 != 0) {
            throw new IllegalArgumentException("Key and values should be in pair.");
        }
        
        for(int i = 0; i!= keyAndValue.length; i+=2) {
            
            put((String)keyAndValue[i],(R)keyAndValue[i+1]);
            
        }
        
        
    }
    
    @Override
    public String backward(R b) {
        return entrySet()
                .stream()
                .filter(entry->entry.getValue().equals(b))
                .findFirst()
                .map(set->set.getKey())
                .orElse(null);
    }

    @Override
    public R forward(String b) {
        R r = get(b);
        return r;
    }
    
}
