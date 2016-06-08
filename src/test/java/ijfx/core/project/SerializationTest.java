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
package ijfx.core.project;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ijfx.plugins.DefaultInterval;
import ijfx.plugins.LongInterval;
import java.io.IOException;
import java.util.HashMap;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author cyril
 */
public class SerializationTest {
    
    public static ObjectMapper objectMapper = new ObjectMapper();
    
    
    @Test
    public void saveLoadLongInterval() throws JsonProcessingException, IOException {
        
        LongInterval interval = new DefaultInterval(10,20,5,40);
        //objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);
        objectMapper.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE, "@class");
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("int",interval);
        String serialized = objectMapper.writeValueAsString(hashMap);
        System.out.println(serialized);
        HashMap<String,Object> loaded = objectMapper.readValue(serialized,HashMap.class);
        
        Assert.assertTrue(LongInterval.class.isAssignableFrom(loaded.get("int").getClass()));
        
    }
    
    
}
