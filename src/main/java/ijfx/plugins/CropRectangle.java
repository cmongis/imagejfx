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
package ijfx.plugins;

import ijfx.core.utils.DimensionUtils;
import ijfx.service.dataset.DatasetUtillsService;
import ijfx.service.overlay.OverlayUtilsService;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.ImgPlusService;
import net.imagej.display.ImageDisplayService;
import net.imagej.ops.OpService;
import net.imagej.overlay.RectangleOverlay;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = Command.class, label = "Crop Rectangle...", menuPath = "Image > Crop Rectangle...")
public class CropRectangle extends ContextCommand {

    @Parameter
    OpService opService;

    @Parameter(type = ItemIO.INPUT)
    Dataset input;

    @Parameter(type = ItemIO.OUTPUT)
    Dataset output;

    @Parameter(label = "X")
    int x;

    @Parameter(label = "Y")
    int y;

    @Parameter(label = "Width")
    int width;

    @Parameter(label = "Height")
    int height;

    @Parameter
    DatasetUtillsService datasetUtilsService;

    @Parameter
    OverlayUtilsService overlayUtilsSrv;

    @Parameter
    DatasetService datasetService;

    @Parameter
    ImgPlusService imgPlusService;

    @Parameter
    ImageDisplayService imageDisplayService;

    public void run() {

        crop((RandomAccessibleInterval) input);

        datasetUtilsService.copyInfos(input, output);

    }

    public <T extends RealType<T>> void crop(RandomAccessibleInterval<T> rai) {

        long[] dimension = DimensionUtils.getDimension(rai);
        long[] offset = new long[dimension.length];

        offset[0] = x;
        offset[1] = y;
        dimension[0] = width;
        dimension[1] = height;

        output = datasetService.create(Views.offsetInterval(rai, offset, dimension));

        datasetUtilsService.copyInfos(input, output);

    }

    public void init() {

        if (imageDisplayService.getActiveDataset() == input) {
            RectangleOverlay rectangleOverlay = overlayUtilsSrv.findOverlayOfType(imageDisplayService.getActiveImageDisplay(), RectangleOverlay.class);
            if (rectangleOverlay != null) {
                x = toInt(rectangleOverlay.getOrigin(0));
                y = toInt(rectangleOverlay.getOrigin(1));
                width = toInt(rectangleOverlay.getExtent(0));
                height = toInt(rectangleOverlay.getExtent(1));
            }
        }
    }

    private int toInt(double d) {
        return new Double(d).intValue();
    }

}
