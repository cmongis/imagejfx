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
package ijfx.core.utils;

import mongis.utils.UUIDMap;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author cyril
 */
public class UUIDMapTest {
    
    
    @Test
    public void test() {
        
        UUIDMap<String> uuidMap = new UUIDMap();
        
        final String str1 = "String 1";
        final String str2 = "String 2";
        final String str3 = "String 3";
        
        Assert.assertFalse(uuidMap.get(str1,str2,str3).has());
        
        uuidMap.get(str1,str2,str3).put(str3);
        
        Assert.assertTrue(uuidMap.get(str1,str2,str3).has());
        
        Assert.assertEquals(str3,uuidMap.get(str1,str2,str3).orElse(null));
                
        Assert.assertFalse(uuidMap.get(str2,str1,str3).has());
        
        
    }
    
    
}
