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
package ijfx.ui.module.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import org.scijava.module.ModuleInfo;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class ModuleSerializer extends JsonSerializer<ModuleInfo> {

    @Override
    public void serialize(ModuleInfo t, JsonGenerator jg, SerializerProvider sp) throws IOException, JsonProcessingException {
        jg.writeStartObject();

        jg.writeStringField("className", t.getDelegateClassName());
        jg.writeStringField("label", t.getLabel());
        jg.writeObjectField("iconPath", t.getIconPath());
        jg.writeObjectField("isEnabled", t.isEnabled());
        jg.writeObjectField("isValid", t.isValid());
        jg.writeObjectField("title", t.getTitle());
        jg.writeObjectField("description", t.getDescription());
        jg.writeObjectField("priority", t.getPriority());
        jg.writeObjectField("inputs", t.inputs());
        jg.writeObjectField("outputs", t.outputs());
        jg.writeEndObject();
        //jg.writeObjectField("",t.);
        //jg.writeObjectField("",t.);

    }

}
