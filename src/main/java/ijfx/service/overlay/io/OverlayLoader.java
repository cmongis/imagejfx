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
package ijfx.service.overlay.io;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.File;
import java.io.IOException;
import java.util.List;
import net.imagej.overlay.LineOverlay;
import net.imagej.overlay.Overlay;
import net.imagej.overlay.PolygonOverlay;
import net.imagej.overlay.RectangleOverlay;
import net.imglib2.RealPoint;
import org.scijava.Context;
import org.scijava.plugin.Parameter;

/**
 *
 * @author cyril
 */
public class OverlayLoader {

    @Parameter
    Context context;
    
    public List<Overlay> load(File f) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Overlay.class, new OverlayDeserializer(context));
        mapper.registerModule(module);

        return mapper.readValue(f, mapper.getTypeFactory().constructCollectionType(List.class, Overlay.class));

    }

    public static void main(String... args) throws IOException {
        OverlayLoader loader = new OverlayLoader();
        System.out.println(new File("./").getAbsolutePath());
        loader.load(new File("./src/test/resources/multidim.ovl.json")).forEach(System.out::println);
    }

}
