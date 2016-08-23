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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import net.imagej.threshold.AbstractThresholdMethod;
import net.imagej.threshold.ThresholdMethod;
import net.imagej.threshold.ThresholdService;
import org.scijava.Context;
import org.scijava.plugin.Parameter;

/**
 *
 * @author cyril
 */
@Deprecated
public class ThresholdMethodDeserializer<T extends ThresholdMethod> extends JsonDeserializer<T> {

    @Parameter
    ThresholdService thresholdService;
    
    public ThresholdMethodDeserializer(Context c) {
        c.inject(this);
    }
    
    @Override
    public T deserialize(JsonParser jp, DeserializationContext dc) throws IOException, JsonProcessingException {
        String name = jp.nextTextValue();
        return (T) thresholdService.getThresholdMethod(name);
    
    }
    
    
}
