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
package ijfx.plugins.commands.measures;

import ijfx.core.metadata.GenericMetaData;
import ijfx.core.metadata.MetaData;
import ijfx.plugins.commands.BinaryToOverlay;
import ijfx.service.overlay.OverlayUtilsService;
import ijfx.service.ui.MeasurementService;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.imagej.ImgPlusService;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.OverlayService;
import net.imagej.overlay.Overlay;
import net.imagej.overlay.ThresholdOverlay;
import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.logic.BitType;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.widget.NumberWidget;

/**
 *
 * @author cyril
 */
@Plugin(type = Command.class, menuPath = "Analyze > Analyse particles")
public class MeasureAllOverlays extends ContextCommand {

    @Parameter
    MeasurementService measurementService;

    @Parameter(type = ItemIO.BOTH)
    ImageDisplay imageDisplay;

    @Parameter
    ImageDisplayService imageDisplayService;
    
    @Parameter
    OverlayUtilsService overlayUtilsService;

    @Parameter(label = "Minimum object size", style = NumberWidget.SLIDER_STYLE, min = "1.0", max = "20.0")
    double areaFilter = 3.0;

    @Parameter
    OverlayService overlayService;

    @Parameter
    ImgPlusService imgPlusService;

    @Override
    public void run() {

        if(isCanceled()) return;
        
        ThresholdOverlay thresholdOverlay = measurementService.getThresholdOverlay(imageDisplay).orElse(null);

        if (thresholdOverlay != null) {
            overlayUtilsService.removeAllOverlay(imageDisplay);
            Overlay[] transform = BinaryToOverlay.transform(getContext(), createBinaryMask(thresholdOverlay), true);

            imageDisplay.addAll(
                    Stream
                    .of(transform)
                    .map(imageDisplayService::createDataView)
                    .collect(Collectors.toList())
            );

        }

        List<Overlay> activeOverlay = overlayService.getOverlays(imageDisplay);

        measurementService.measureOverlays(imageDisplay, activeOverlay, o -> o.getMetaDataSet().getOrDefault(MetaData.LBL_AREA, new GenericMetaData(MetaData.LBL_AREA, 0.0)).getDoubleValue() >= areaFilter);
    }

    private Img<BitType> createBinaryMask(ThresholdOverlay overlay) {

        ImgFactory<BitType> factory = new ArrayImgFactory<>();
        long[] dim = new long[2];
        dim[0] = imageDisplay.dimension(0);
        dim[1] = imageDisplay.dimension(1);
        Img<BitType> img = factory.create(dim, new BitType(false));

        Cursor<BitType> cursor = img.cursor();
        cursor.reset();
        while (cursor.hasNext()) {
            cursor.fwd();
            cursor.localize(dim);
            cursor.get().set(overlay.classify(dim) == 0);
        }

        return img;
    }

}
