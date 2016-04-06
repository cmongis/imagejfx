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
package ijfx.service.preview;

import ij.process.ImageProcessor;
import ijfx.service.ui.FxImageService;
import io.scif.gui.AWTImageTools;
import java.awt.Color;
import java.awt.image.BufferedImage;
import static java.lang.Math.toIntExact;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.ImageJService;
import net.imagej.Position;
import net.imagej.axis.AxisType;
import net.imagej.axis.CalibratedAxis;
import net.imagej.display.DataView;
import net.imagej.display.DatasetView;
import net.imagej.display.DefaultDatasetView;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.type.numeric.RealType;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 *
 * @author Tuan anh TRINH
 */
@Plugin(type = Service.class)
public class PreviewService extends AbstractService implements ImageJService {

    @Parameter
    ImageDisplayService imageDisplayService;

    @Parameter
    DatasetService datasetService;

    public PreviewService() {
    }

    public Image getImageDisplay() {

        BufferedImage bufferedImage = datasetToBufferedImage(getPreviewDataset(200, 200, 0, 0));

        WritableImage wi = new WritableImage(bufferedImage.getWidth(), bufferedImage.getHeight());
        SwingFXUtils.toFXImage(bufferedImage, wi);
        return wi;
    }

    public Dataset getEmptyDataset(Dataset input, int width, int height) {
        AxisType[] axisType = new AxisType[input.numDimensions()];
        CalibratedAxis[] axeArray = new CalibratedAxis[input.numDimensions()];
        input.axes(axeArray);

        long[] dims = new long[axeArray.length];
        for (int i = 0; i < dims.length; i++) {
            axisType[i] = axeArray[i].type();
            dims[i] = toIntExact(input.max(i) + 1);

        }
        dims[0] = width;
        dims[1] = height;
        return datasetService.create(dims, input.getName(), axisType, input.getValidBits(), input.isSigned(), false);
    }

    public < T extends RealType< T>> Dataset getPreviewDataset(int width, int height, int x, int y) {

        Position activePosition = imageDisplayService.getActivePosition();

        Dataset datasetOrigin = imageDisplayService.getActiveDataset();
        Dataset datasetOutput = getEmptyDataset(datasetOrigin, width, height);

        long[] dimension = new long[datasetOrigin.numDimensions() - 2];
        activePosition.localize(dimension);

        long[] position = new long[datasetOrigin.numDimensions()];
        position[0] = 0;
        position[1] = 0;
        System.arraycopy(dimension, 0, position, 2, dimension.length);
        RandomAccess<T> randomAccessOrigin = (RandomAccess<T>) datasetOrigin.randomAccess();
        RandomAccess<T> randomAccessOutput = (RandomAccess<T>) datasetOutput.randomAccess();
        randomAccessOrigin.setPosition(position);

        for (int i = x; i < width + x; i++) {
            randomAccessOrigin.setPosition(i, 0);
            randomAccessOutput.setPosition(i - x, 0);

            for (int j = y; j < height + y; j++) {
                randomAccessOrigin.setPosition(j, 1);
                randomAccessOutput.setPosition(j - y, 1);
                randomAccessOutput.get().set(randomAccessOrigin.get());
            }
        }

        return datasetOutput;
    }

    public BufferedImage datasetToBufferedImage(Dataset dataset) {

        final DatasetView view = (DatasetView) imageDisplayService.createDataView(dataset);
        Position activePosition = imageDisplayService.getActivePosition();
        view.setPosition(activePosition);
        view.rebuild();
        return view.getScreenImage().image();
    }

}
