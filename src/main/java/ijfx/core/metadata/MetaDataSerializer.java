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
package ijfx.core.metadata;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class MetaDataSerializer extends JsonSerializer<MetaDataSet> {

   
    @Override
    public void serialize(MetaDataSet t, JsonGenerator jg, SerializerProvider sp) throws IOException, JsonGenerationException {
        jg.writeStartObject();
        for (MetaData m : t.values()) {
            jg.writeObjectField(m.getName(), m.getValue());
        }
        jg.writeEndObject();
    }

    
    
}
