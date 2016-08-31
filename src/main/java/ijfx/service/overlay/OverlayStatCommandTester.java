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
package ijfx.service.overlay;

import ijfx.plugins.commands.BinaryToOverlay;
import ijfx.service.batch.BatchSingleInput;
import ijfx.service.batch.ImageDisplayBatchInput;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.OverlayService;
import net.imagej.overlay.Overlay;
import net.imagej.table.DefaultGenericTable;
import org.scijava.Context;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

/**
 *
 * @author Pierre BONNEAU
 */

@Deprecated
public class OverlayStatCommandTester implements Command{
    
    @Parameter
    ImageDisplayService imageDisplayService;
    
    @Parameter
    OverlayService overlayService;
    
    @Parameter
    OverlayStatService overlayStatService;
    
    @Parameter
    Context context;
    
    @Parameter
    private UIService uiService;    
    
    
    @Override
    public void run(){
        ImageDisplay display = imageDisplayService.getActiveImageDisplay();
        
        List<Overlay> overlay = overlayService.getOverlays();
        
        overlayService.addOverlays(display, overlay);
        
        List<HashMap<String, Double>> map = overlay.stream()
                .map(o -> {
                    try {
                        
                        return overlayStatService.getStatisticsAsMap(display,o);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(o -> o != null)
                .collect(Collectors.toList());

        DefaultGenericTable resultTable = new DefaultGenericTable();
        if (map.size() > 0) {
            int headerNumber = map.get(0).keySet().size();

            String[] headers = map.get(0).keySet().toArray(new String[headerNumber]);
            resultTable.insertColumns(0, headers);

            for (int rowNumber = 0; rowNumber != map.size(); rowNumber++) {
                final int finalRowNumber = rowNumber;
                resultTable.insertRow(finalRowNumber);
                map.get(rowNumber).forEach((key, value) -> {
                    System.out.println(String.format("Setting the value %s to %.3f (%d)", key, value, finalRowNumber));

                    resultTable.set(key, finalRowNumber, value);
                });
            }

        }
        
        uiService.show(resultTable);        
    }
}
