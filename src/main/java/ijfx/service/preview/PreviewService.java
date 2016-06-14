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

import ijfx.service.batch.BatchService;
import ijfx.service.batch.BatchSingleInput;
import ijfx.service.batch.DisplayBatchInput;
import ijfx.service.log.DefaultLoggingService;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.ImageJService;
import net.imagej.Position;
import net.imagej.axis.AxisType;
import net.imagej.axis.CalibratedAxis;
import net.imagej.display.DatasetView;
import net.imagej.display.ImageDisplayService;
import net.imglib2.RandomAccess;
import net.imglib2.display.ColorTable;
import net.imglib2.display.ColorTable8;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.CommandService;
import org.scijava.display.DisplayService;
import org.scijava.module.Module;
import org.scijava.module.ModuleService;
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
    private DisplayService displayService;

    @Parameter
    private ImageDisplayService imageDisplayService;

    @Parameter
    private DatasetService datasetService;

    @Parameter
    private CommandService commandService;

    @Parameter
    private ModuleService moduleService;

    @Parameter
    private BatchService batchService;
    
    @Parameter
    private DefaultLoggingService logService;
    
    private int width;
    private int height;
    private int x;
    private int y;

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setParameters(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
        int widthDataset = (int) imageDisplayService.getActiveDataset().max(0);
        int heightDataset = (int) imageDisplayService.getActiveDataset().max(1);
        if (x < 0 || y < 0) {
            this.x = (int) (widthDataset / 2.0 - width / 2.0);
            this.y = (int) (heightDataset / 2.0 - height / 2.0);
        }
        if (widthDataset < width || heightDataset < height) {
            this.x = 0;
            this.y = 0;
            width = widthDataset;
            height = heightDataset;

        }
    }

    public PreviewService() {
    }

    public Image getImageDisplay(String command, Map<String, Object> inputMap) {
        Dataset preview = getPreviewDataset();
        preview = applyCommand(preview, command, inputMap);
        BufferedImage bufferedImage = datasetToBufferedImage(preview);

        WritableImage wi = new WritableImage(bufferedImage.getWidth(), bufferedImage.getHeight());
        SwingFXUtils.toFXImage(bufferedImage, wi);
        return wi;
    }

    /**
     * Create empty dataset. The size of the dataset set from width and height.
     * All the other dimensions are set to 1.
     *
     * @param input
     * @return output
     */
    public Dataset getEmptyDataset(Dataset input) {
        AxisType[] axisType = new AxisType[2];
        CalibratedAxis[] axeArray = new CalibratedAxis[2];
        input.axes(axeArray);

        long[] dims = new long[2];
        for (int i = 0; i < dims.length; i++) {
            axisType[i] = axeArray[i].type();
        }
        dims[0] = width;
        dims[1] = height;
        Dataset output = datasetService.create(dims, input.getName(), axisType, input.getValidBits(), input.isSigned(), false);
        return output;
    }

    public < T extends RealType< T>> Dataset getPreviewDataset() {

        Position activePosition = imageDisplayService.getActivePosition();

        Dataset datasetOrigin = imageDisplayService.getActiveDataset();
        Dataset datasetOutput = getEmptyDataset(datasetOrigin);

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
                randomAccessOutput.get().setReal(randomAccessOrigin.get().getRealFloat());
            }
        }

//
//        RandomAccess<T> randomAccessOrigin = (RandomAccess<T>) datasetOrigin.randomAccess();
//        Cursor<T> cursorOutput = (Cursor<T>) datasetOutput.cursor();
//        while (cursorOutput.hasNext()) {
//            cursorOutput.fwd();
//            randomAccessOrigin.setPosition(cursorOutput);
//            cursorOutput.get().set(randomAccessOrigin.get());
//            
//        }
        return datasetOutput;
    }

    /**
     * Create an Image from the dataset and setLut
     *
     * @param dataset
     * @return bufferedImage
     */
    public BufferedImage datasetToBufferedImage(Dataset dataset) {
        DatasetView activeDataview = imageDisplayService.getActiveDatasetView();
        final DatasetView view = (DatasetView) imageDisplayService.createDataView(dataset);
        Position activePosition = imageDisplayService.getActivePosition();

        //Sometimes activePosition is invalid
        try {
            view.getData().setChannelMaximum(0, activeDataview.getChannelMax(activePosition.getIntPosition(0)));
            view.getData().setChannelMinimum(0, activeDataview.getChannelMin(activePosition.getIntPosition(0)));

            //view.setPosition(activePosition);
            view.setColorMode(activeDataview.getColorMode());

            //Has to be rebuil to create colorTable
            view.rebuild();
            long[] dimension = new long[dataset.numDimensions() - 2];
            activePosition.localize(dimension);

            //Set LUT
            setLUT(activeDataview, view);

            int maxChannel = (int) activeDataview.getChannelMax(activePosition.getIntPosition(0));
            int minChannel = (int) activeDataview.getChannelMin(activePosition.getIntPosition(0));
            view.setChannelRange(0, minChannel, maxChannel);
        } catch (Exception e) {

        }
        view.rebuild();
        BufferedImage bufferedImage = view.getScreenImage().image();
        return bufferedImage;
    }

    /**
     * Apply the <code>command</code> to the <code>dataset</code> using
     * <code>inputMap</code> as parameters.
     *
     * @param dataset
     * @param command
     * @param inputMap
     * @return
     */
    public Dataset applyCommand(Dataset dataset, String command, Map<String, Object> inputMap) {
        try {
            stringToObject(inputMap);
            BatchSingleInput batchSingleInput = new DisplayBatchInput();
            this.context().inject(batchSingleInput);
            batchSingleInput.setDataset(dataset);

            Module module = moduleService.createModule(commandService.getCommand(command));
            //CommandInfo commandInfo = new CommandInfo(command);
            // Module module = new CommandModule(commandInfo);
            try {
                this.context().inject(module.getDelegateObject());

            } catch (Exception e) {
                logService.severe(e);
            }
            batchService.executeModule(batchSingleInput, module, inputMap);
            Dataset result = batchSingleInput.getDataset();
            return result;
        } catch (Exception e) {
            logService.severe(e);
        }
        return null;
    }

    private Map<String, Object> stringToObject(Map<String, Object> inputMap) {
        String keyword = "ToObject:";
        inputMap.forEach((s, o) -> {
            try {
                String valueString = (String) o;
                if (valueString.contains(keyword)) {

                    valueString = valueString.replaceFirst(keyword, "");
                    Object object = Class.forName(valueString).getConstructor().newInstance();
                    inputMap.put(s, object);
                    Logger.getLogger("Create new instance of " + object.getClass().toString());
                }
            } catch (Exception e) {
                //Logger.getLogger(PreviewService.class.getName()).log(Level.SEVERE, null, e);
            }
        });
        return inputMap;
    }

    public void setLUT(DatasetView input, DatasetView output) {
        Position activePosition = imageDisplayService.getActivePosition();
        List<ColorTable> colorTable = input.getColorTables();

        long[] dimension = new long[output.getData().numDimensions() - 2];
        activePosition.localize(dimension);

        //Set LUT
        if (input.getData().getImgPlus().getCompositeChannelCount() == 1) {
            output.setColorTable(colorTable.get(activePosition.getIntPosition(0)), 0);
        } else {
            byte[][] values = ((ColorTable8) colorTable.get(0)).getValues().clone();
            for (int i = 0; i < colorTable.size(); i++) {
                byte[][] b = ((ColorTable8) colorTable.get(i)).getValues();
                values[i] = b[i];

            }
            ColorTable8 colorTable8 = new ColorTable8(values);
            output.setColorTable(colorTable8, 0);
        }
    }

    public ImageDisplayService getImageDisplayService() {
        return imageDisplayService;
    }

}
