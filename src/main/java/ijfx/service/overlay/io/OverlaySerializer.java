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
import org.scijava.util.ColorRGB;

/**
 *
 * @author cyril
 */
public class OverlaySerializer extends JsonSerializer<Overlay> {

    @Override
    public void serialize(Overlay overlay, JsonGenerator jg, SerializerProvider sp) throws IOException, JsonProcessingException {

        if (overlay instanceof LineOverlay) {
            saveLineOverlay((LineOverlay) overlay, jg);
        }
        if (overlay instanceof RectangleOverlay) {
            saveRectangleOverlay((RectangleOverlay) overlay, jg);
        }
        if (overlay instanceof PolygonOverlay) {
            savePolytonOverlay((PolygonOverlay) overlay, jg);
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
    
    private void writeDoubleArray(JsonGenerator jg, String fieldName, double[] array) throws IOException {
        jg.writeFieldName(fieldName);
        jg.writeStartArray();
        for(double d : array) {
            jg.writeNumber(d);
        }
        jg.writeEndArray();
    }

    private void saveRectangleOverlay(RectangleOverlay rectangleOverlay, JsonGenerator jg) throws IOException {
        jg.writeStartObject();
        jg.writeStringField(JsonOverlayToken.OVERLAY_TYPE, JsonOverlayToken.RECTANGLE_OVERLAY);
        int dimensionCount = rectangleOverlay.numDimensions();

        Double[] origin = Utils.extractArray(rectangleOverlay::getOrigin, dimensionCount);
        Double[] extent = Utils.extractArray(rectangleOverlay::getExtent, dimensionCount);
        
        ColorRGB fcolor = rectangleOverlay.getFillColor();
        ColorRGB lcolor = rectangleOverlay.getLineColor();
        
        Integer[] fill_color = {fcolor.getRed(), fcolor.getGreen(), fcolor.getBlue()};
        Integer[] line_color = {lcolor.getRed(), lcolor.getGreen(), lcolor.getBlue()};
        
        double width = rectangleOverlay.getLineWidth();
        

        writeNumberArray(jg, JsonOverlayToken.ORIGIN, origin);
        writeNumberArray(jg, JsonOverlayToken.EXTENT, extent);
        
        writeNumberArray(jg, JsonOverlayToken.FILL_COLOR, fill_color);
        writeNumberArray(jg, JsonOverlayToken.LINE_COLOR, line_color);
        
        jg.writeFieldName(JsonOverlayToken.LINE_WIDTH);
        jg.writeNumber(width);        

        jg.writeEndObject();

    }

    private void savePolytonOverlay(PolygonOverlay overlay, JsonGenerator jg) throws IOException {
        // {
        jg.writeStartObject();
        int numDimension = overlay.numDimensions();

        // "ovl_type":"polygon"
        jg.writeStringField(JsonOverlayToken.OVERLAY_TYPE, JsonOverlayToken.POLYGON_OVERLAY);

        // "points":[
      
        int vertexCount = overlay.getRegionOfInterest().getVertexCount();

        double[] xpoints = IntStream
                .range(0, vertexCount)
                .mapToDouble(i -> overlay.getRegionOfInterest().getVertex(i).getDoublePosition(0))
                .toArray();
        double[] ypoints = IntStream
                .range(0, vertexCount)
                .mapToDouble(i -> overlay.getRegionOfInterest().getVertex(i).getDoublePosition(1))
                .toArray();
        
        writeDoubleArray(jg, "xpoints", xpoints);
        writeDoubleArray(jg, "ypoints", ypoints);
        // }

        ColorRGB fcolor = overlay.getFillColor();
        ColorRGB lcolor = overlay.getLineColor();
        
        Integer[] fill_color = {fcolor.getRed(), fcolor.getGreen(), fcolor.getBlue()};
        Integer[] line_color = {lcolor.getRed(), lcolor.getGreen(), lcolor.getBlue()};
        
        double width = overlay.getLineWidth();
        
        writeNumberArray(jg, JsonOverlayToken.FILL_COLOR, fill_color);
        writeNumberArray(jg, JsonOverlayToken.LINE_COLOR, line_color);
        
        jg.writeFieldName(JsonOverlayToken.LINE_WIDTH);
        jg.writeNumber(width);         
        
        jg.writeEndObject();

    }

    private void saveLineOverlay(LineOverlay overlay, JsonGenerator jg) throws IOException {

        jg.writeStartObject();
        jg.writeStringField(JsonOverlayToken.OVERLAY_TYPE, JsonOverlayToken.LINE_OVERLAY);

        Double[] lineStart = Utils.extractArray(overlay::getLineStart, overlay.numDimensions());
        Double[] lineEnd = Utils.extractArray(overlay::getLineEnd, overlay.numDimensions());

        writeNumberArray(jg, JsonOverlayToken.BEGIN, lineStart);
        writeNumberArray(jg, JsonOverlayToken.END, lineEnd);
        
        ColorRGB fcolor = overlay.getFillColor();
        ColorRGB lcolor = overlay.getLineColor();
        
        Integer[] fill_color = {fcolor.getRed(), fcolor.getGreen(), fcolor.getBlue()};
        Integer[] line_color = {lcolor.getRed(), lcolor.getGreen(), lcolor.getBlue()};
        
        double width = overlay.getLineWidth();
        
        writeNumberArray(jg, JsonOverlayToken.FILL_COLOR, fill_color);
        writeNumberArray(jg, JsonOverlayToken.LINE_COLOR, line_color);
        
        jg.writeFieldName(JsonOverlayToken.LINE_WIDTH);
        jg.writeNumber(width);          

        jg.writeEndObject();

    }

}
