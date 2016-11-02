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
package ijfx.service.workflow.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import net.imagej.threshold.ThresholdMethod;
import org.scijava.Context;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Cyril MONGIS, 2016
 */
@Deprecated
public class ThresholdMethodSerializer<T extends ThresholdMethod> extends JsonSerializer<T>{

    public  ThresholdMethodSerializer(Context c) {
        c.inject(this);
    }
    public void serialize(T t, JsonGenerator jg, SerializerProvider sp) throws IOException, JsonProcessingException {
    
        Plugin annotation = t.getClass().getAnnotation(Plugin.class);
        String name = annotation.name();
        jg.writeObject(name);
        
    }
    
    
}
