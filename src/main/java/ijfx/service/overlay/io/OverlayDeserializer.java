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
import ijfx.ui.main.ImageJFX;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import net.imagej.overlay.LineOverlay;
import net.imagej.overlay.Overlay;
import net.imagej.overlay.PolygonOverlay;
import net.imagej.overlay.RectangleOverlay;
import net.imglib2.RealPoint;
import org.scijava.Context;
import org.scijava.util.ColorRGB;

/**
 *
 * @author cyril
 */
class OverlayDeserializer extends JsonDeserializer<Overlay> {
    
    private final Context context;

    Logger logger = ImageJFX.getLogger();
    
    public OverlayDeserializer(Context context) {
        this.context = context;
    }

    @Override
    public Overlay deserialize(JsonParser jp, DeserializationContext dc) throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);
        String type = node.get(JsonOverlayToken.OVERLAY_TYPE).asText();
        
        if (type.equals(JsonOverlayToken.RECTANGLE_OVERLAY)) {
            
            return loadRectangle(node);
        }
        if (type.equals(JsonOverlayToken.POLYGON_OVERLAY)) {
            return loadPolygon(node);
        }
        if (type.equals(JsonOverlayToken.LINE_OVERLAY)) {
            return loadLine(node);
        }
        return null;
    }

    private Overlay loadRectangle(JsonNode node) {
        RectangleOverlay overlay = new RectangleOverlay(context);
        double[] origin = getArray(node, JsonOverlayToken.ORIGIN);
        double[] extent = getArray(node, JsonOverlayToken.EXTENT);
        for (int i = 0; i != origin.length; i++) {
            overlay.setOrigin(origin[i], i);
            overlay.setExtent(extent[i], i);
        }
        
        double[] fill_color = getArray(node, JsonOverlayToken.FILL_COLOR);
        double[] line_color = getArray(node, JsonOverlayToken.LINE_COLOR);
        
        ColorRGB fcolor = new ColorRGB((int)fill_color[0], (int)fill_color[1], (int)fill_color[2]);
        ColorRGB lcolor = new ColorRGB((int)line_color[0], (int)line_color[1], (int)line_color[2]);
        
        double width = node.get(JsonOverlayToken.LINE_WIDTH).doubleValue();
        
        overlay.setFillColor(fcolor);
        overlay.setLineColor(lcolor);
        
        overlay.setLineWidth(width);
        
        return overlay;
    }

    private Overlay loadPolygon(JsonNode node) {
        PolygonOverlay overlay = new PolygonOverlay(context);
        Integer dimension = 0;
        
         double[] xpoints = getArray(node,"xpoints");
        double[] ypoints = getArray(node,"ypoints");
        
        int pointCount = xpoints.length;
        
        // it means there is no array
        if(pointCount <= 0) {
            logger.severe("Invalid polygon array !");
            return null;
        }
        
        else {
        for (int p = 0; p != pointCount; p++) {
            overlay.getRegionOfInterest().addVertex(p, new RealPoint(xpoints[p],ypoints[p]));
        }
        
        double[] fill_color = getArray(node, JsonOverlayToken.FILL_COLOR);
        double[] line_color = getArray(node, JsonOverlayToken.LINE_COLOR);
        
        ColorRGB fcolor = new ColorRGB((int)fill_color[0], (int)fill_color[1], (int)fill_color[2]);
        ColorRGB lcolor = new ColorRGB((int)line_color[0], (int)line_color[1], (int)line_color[2]);
        
        double width = node.get(JsonOverlayToken.LINE_WIDTH).doubleValue();
        
        overlay.setFillColor(fcolor);
        overlay.setLineColor(lcolor);
        
        overlay.setLineWidth(width);        
        return overlay;
        }
    }
    
    private double[] getArray(JsonNode node, String fieldName) {
        JsonNode arrayNode = node.get(fieldName);
        if(arrayNode == null) return new double[0];
        int arraySize = arrayNode.size();
        if(arraySize < 0) return new double[0];
        else {
            return IntStream.range(0, arraySize)
                    .mapToDouble(i->arrayNode.get(i).asDouble())
                    .toArray();
        }
    }

    private Overlay loadLine(JsonNode node) {
        LineOverlay lineOverlay = new LineOverlay(context);
        double[] begin = getArray(node, JsonOverlayToken.BEGIN);
        double[] end = getArray(node, JsonOverlayToken.END);
        for (int i = 0; i != begin.length; i++) {
            lineOverlay.setLineStart(begin);
            lineOverlay.setLineEnd(end);
        }
        
        double[] fill_color = getArray(node, JsonOverlayToken.FILL_COLOR);
        double[] line_color = getArray(node, JsonOverlayToken.LINE_COLOR);
        
        ColorRGB fcolor = new ColorRGB((int)fill_color[0], (int)fill_color[1], (int)fill_color[2]);
        ColorRGB lcolor = new ColorRGB((int)line_color[0], (int)line_color[1], (int)line_color[2]);
        
        double width = node.get(JsonOverlayToken.LINE_WIDTH).doubleValue();
        
        lineOverlay.setFillColor(fcolor);
        lineOverlay.setLineColor(lcolor);
        
        lineOverlay.setLineWidth(width);
        
        return lineOverlay;
    }
    /*
    private double[] getArray(JsonNode node, String key) {
        JsonNode arrayNode = node.get(key);
        double[] values = new double[arrayNode.size()];
        for (int i = 0; i != values.length; i++) {
            values[i] = arrayNode.get(i).asDouble();
        }
        return values;
    }*/
    
}
