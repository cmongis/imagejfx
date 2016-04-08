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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.function.IntFunction;
import java.util.stream.IntStream;
import javafx.util.Callback;
import net.imagej.overlay.LineOverlay;
import net.imagej.overlay.Overlay;
import net.imagej.overlay.PolygonOverlay;
import net.imagej.overlay.RectangleOverlay;

/**
 *
 * @author cyril
 */
public class OverlaySerializer extends JsonSerializer<Overlay> {

    @Override
    public void serialize(Overlay overlay, JsonGenerator jg, SerializerProvider sp) throws IOException, JsonProcessingException {

        if(overlay instanceof LineOverlay) {
            saveLineOverlay((LineOverlay)overlay, jg);
        }
        if(overlay instanceof RectangleOverlay) {
            saveRectangleOverlay((RectangleOverlay)overlay, jg);
        }
        if(overlay instanceof PolygonOverlay) {
            savePolytonOverlay((PolygonOverlay)overlay,jg);
        }
        
    }

    private void writeNumberArray(JsonGenerator jg, String arrayName, Number[] numbers) throws IOException {

        jg.writeFieldName(arrayName);
        jg.writeStartArray();
        for (Number n : numbers) {
            jg.writeNumber(n.doubleValue());
        }
        jg.writeEndArray();
    }

    private void writeDoubleArray(JsonGenerator jg, Double[] array) throws IOException {
        jg.writeStartArray();
        for (Double n : array) {
            jg.writeNumber(n);
        }
        jg.writeEndArray();
    }

    private void saveRectangleOverlay(RectangleOverlay rectangleOverlay, JsonGenerator jg) throws IOException {
        jg.writeStartObject();
        jg.writeStringField(JsonOverlayToken.OVERLAY_TYPE, JsonOverlayToken.RECTANGLE_OVERLAY);
        int dimensionCount = rectangleOverlay.numDimensions();

        Double[] origin = Utils.extractArray(rectangleOverlay::getOrigin, dimensionCount);
        Double[] extent = Utils.extractArray(rectangleOverlay::getExtent, dimensionCount);

        writeNumberArray(jg, JsonOverlayToken.ORIGIN, origin);
        writeNumberArray(jg, JsonOverlayToken.EXTENT, extent);

        jg.writeEndObject();

    }

    private void savePolytonOverlay(PolygonOverlay overlay, JsonGenerator jg) throws IOException {
        // {
        jg.writeStartObject();
        int numDimension = overlay.numDimensions();

        // "ovl_type":"polygon"
        jg.writeStringField(JsonOverlayToken.OVERLAY_TYPE, JsonOverlayToken.POLYGON_OVERLAY);

        // "points":[
        jg.writeArrayFieldStart("points");

        for (int i = 0; i != overlay.getRegionOfInterest().getVertexCount(); i++) {

            // Getting the point coordinates for each dimension
            Double[] point = Utils.extractArray(overlay.getRegionOfInterest().getVertex(i)::getDoublePosition, numDimension);

            // [10,203,10394]
            writeDoubleArray(jg, point);
        }
        //  ]
        jg.writeEndArray();

        // }
        jg.writeEndObject();

    }

    private void saveLineOverlay(LineOverlay overlay, JsonGenerator jg) throws IOException {

        jg.writeStartObject();
        jg.writeStringField(JsonOverlayToken.OVERLAY_TYPE, JsonOverlayToken.POLYGON_OVERLAY);
        
        Double[] lineStart = Utils.extractArray(overlay::getLineStart,overlay.numDimensions());
        Double[] lineEnd = Utils.extractArray(overlay::getLineEnd, overlay.numDimensions());
        
        writeNumberArray(jg, JsonOverlayToken.BEGIN, lineStart);
        writeNumberArray(jg, JsonOverlayToken.END,lineEnd);
        
        jg.writeEndObject();

    }

}
