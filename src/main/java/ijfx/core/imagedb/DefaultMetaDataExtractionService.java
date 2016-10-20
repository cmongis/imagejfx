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
package ijfx.core.imagedb;

import ijfx.core.metadata.FileSizeMetaData;
import ijfx.core.metadata.MetaData;
import ijfx.core.metadata.MetaDataSet;
import ijfx.core.metadata.MetaDataSetType;
import ijfx.core.utils.DimensionUtils;
import ijfx.plugins.commands.AxisUtils;
import ijfx.service.Timer;
import ijfx.service.TimerService;
import ijfx.ui.main.ImageJFX;
import io.scif.FormatException;
import io.scif.Metadata;
import io.scif.SCIFIO;
import io.scif.bf.BioFormatsFormat;
import io.scif.config.SCIFIOConfig;
import io.scif.filters.ReaderFilter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import loci.formats.codec.NikonCodec;
import mongis.ndarray.NDimensionalArray;
import net.imagej.Dataset;
import net.imagej.axis.Axes;
import net.imagej.axis.CalibratedAxis;
import net.imagej.display.ImageDisplay;
import org.scijava.Priority;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 *
 * @author cyril
 */
@Plugin(type = Service.class, priority = Priority.VERY_LOW_PRIORITY)
public class DefaultMetaDataExtractionService extends AbstractService implements MetaDataExtractionService {

    private SCIFIO scifio;
    private SCIFIOConfig config;

    private final static Logger logger = ImageJFX.getLogger();

    private final String WRONG_FORMAT = "Couldn't find an appropriate format for the file : %s";
    private final String ACCESS_PROBLEM = "Could'nt access file : %s";

    @Parameter
    TimerService timerService;

    public DefaultMetaDataExtractionService() {
        super();

    }

    @Override
    public void initialize() {
        super.initialize();

    }

    private SCIFIO getSCIFIO() {
        if (scifio == null) {
            scifio = new SCIFIO();
            config = new SCIFIOConfig();
            config.checkerSetOpen(false);
            config.groupableSetGroupFiles(false);
            config.imgOpenerSetOpenAllImages(false);
            config.imgOpenerSetComputeMinMax(false);
        }
        return scifio;
    }

    @Override
    public MetaDataSet extractMetaData(File file) {

        MetaDataSet metadataset = new MetaDataSet();
        Timer t = timerService.getTimer("MetaData Extraction");
        try {
            // ReaderFilter reader = getReaderFilter(file);
            t.start();

            Metadata metadata;
            try {
                metadata = getSCIFIO().format().getFormat(file.getAbsolutePath()).createParser().parse(file.getAbsolutePath());
            } catch (FormatException e) {
                metadata = getSCIFIO().format().getFormatFromClass(BioFormatsFormat.class).createParser().parse(file);
            }
            if (metadata == null) {
                throw new FormatException(String.format("Couldn't find a format for %s", file.getName()));
            }
            t.elapsed("Metadata parser creation");
            long serieCount, timeCount, zCount, channelCount, width, height, imageSize, bitsPerPixel;
            String dimensionOrder;

            serieCount = metadata.getImageCount();
            width = metadata.get(0).getAxisLength(Axes.X);
            height = metadata.get(0).getAxisLength(Axes.Y);
            timeCount = metadata.get(0).getAxisLength(Axes.TIME);
            zCount = metadata.get(0).getAxisLength(Axes.Z);
            channelCount = metadata.get(0).getAxisLength(Axes.CHANNEL);
            bitsPerPixel = metadata.get(0).getBitsPerPixel();
            imageSize = metadata.getDatasetSize();
            //dimensionOrder = metadata.get

            metadataset.putGeneric(MetaData.WIDTH, width);
            metadataset.putGeneric(MetaData.HEIGHT, height);
            metadataset.putGeneric(MetaData.ZSTACK_NUMBER, zCount);
            metadataset.putGeneric(MetaData.TIME_COUNT, timeCount);
            metadataset.putGeneric(MetaData.BITS_PER_PIXEL, bitsPerPixel);
            metadataset.put(new FileSizeMetaData(file.length()));
            metadataset.putGeneric(MetaData.SERIE_COUNT, serieCount);
            metadataset.putGeneric(MetaData.CHANNEL_COUNT, channelCount);
            metadataset.putGeneric(MetaData.FILE_NAME, file.getName());
            metadataset.putGeneric(MetaData.ABSOLUTE_PATH, file.getAbsolutePath());

            // creating array containing the axes and axe lengths other than x an y
            long[] dims = new long[metadata.get(0).getAxesLengths().length - 2];
            CalibratedAxis[] axes = new CalibratedAxis[dims.length];
            if (dims.length > 0) {
                System.arraycopy(metadata.get(0).getAxes().toArray(new CalibratedAxis[metadata.get(0).getAxes().size()]), 2, axes, 0, axes.length);
                System.arraycopy(metadata.get(0).getAxesLengths(), 2, dims, 0, dims.length);

                NDimensionalArray ndarray = new NDimensionalArray(dims);

                String[] dimNames = new String[dims.length];

                for (int i = 0; i != dimNames.length; i++) {
                    dimNames[i] = metadata.get(0).getAxis(i + 2).type().getLabel();
                }

                metadataset.putGeneric(MetaData.DIMENSION_ORDER, String.join(",", dimNames));
                metadataset.putGeneric(MetaData.DIMENSION_LENGHS, Arrays.toString(dims));
            }

            return metadataset;

        } catch (FormatException ex) {
            logger.log(Level.SEVERE, String.format(WRONG_FORMAT, file.getName()), ex);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, String.format(ACCESS_PROBLEM, file.getName()), ex);
        }

        return metadataset;

    }

    private ReaderFilter getReaderFilter(File file) throws FormatException, IOException {
        return scifio.initializer().initializeReader(file.getAbsolutePath(), config);
    }

    public List<MetaDataSet> extractPlaneMetaData(MetaDataSet metadataset) {

        List<MetaDataSet> planes = new ArrayList<>(1000);
        String dimensionLengthesString = metadataset.get(MetaData.DIMENSION_LENGHS).getStringValue();
        System.out.println(dimensionLengthesString);
        if (dimensionLengthesString == null || dimensionLengthesString.equals("null") || dimensionLengthesString.equals("[]")) {
            planes.add(new MetaDataSet().merge(metadataset));
            return planes;
        } else {

            long[] dimensionLengthes = DimensionUtils.readLongArray(metadataset.get(MetaData.DIMENSION_LENGHS).getStringValue());

            String[] dimensionLabelArray = metadataset.get(MetaData.DIMENSION_ORDER).getStringValue().split(",");

            NDimensionalArray array = new NDimensionalArray(dimensionLengthes);
            int planeIndex = 0;
            long[][] coordinateList = array.get(0).generateAllPossibilities();
            // generate all the possibilities
            for (long[] coordinate : coordinateList) {

                MetaDataSet planeMetaDataSet = new MetaDataSet().merge(metadataset);
                for (String statsMetaData : MetaData.STATS_RELATED_METADATA) {
                    planeMetaDataSet.remove(statsMetaData);
                }
                for (int d = 0; d != coordinate.length; d++) {
                    String label = dimensionLabelArray[d];
                    long value = coordinate[d];
                    planeMetaDataSet.putGeneric(label, value);

                }
                // putting the plane index
                planeMetaDataSet.putGeneric(MetaData.PLANE_INDEX, planeIndex);

                // indicate metadata type
                planeMetaDataSet.putGeneric(MetaData.METADATA_SET_TYPE_KEY, MetaData.METADATA_SET_TYPE_PLANE);

                // puting the non planar position
                planeMetaDataSet.putGeneric(MetaData.PLANE_NON_PLANAR_POSITION, Arrays.toString(coordinate));
                planes.add(planeMetaDataSet);
                planeIndex++;
            }

        }
        return planes;

    }

    public void fillPositionMetaData(MetaDataSet set, CalibratedAxis[] axes, long[] absoluteCoordinate) {

        String[] dimensionLabelArray = Stream
                .of(axes)
                .map(axe -> axe.type().getLabel())
                .toArray(size -> new String[size]);

        for (int d = 2; d != absoluteCoordinate.length; d++) {
            String label = dimensionLabelArray[d];
            long value = absoluteCoordinate[d];
            set.putGeneric(label, value);

        }
    }

    @Override

    public List<MetaDataSet> extractPlaneMetaData(File file) {
        return extractPlaneMetaData(extractMetaData(file));
    }

    @Override
    public MetaDataSet extractMetaData(Dataset dataset) {

        MetaDataSet set = new MetaDataSet(MetaDataSetType.FILE);
        CalibratedAxis[] axes = AxisUtils.getAxes(dataset);
        set.putGeneric(MetaData.NAME, dataset.getName());
        for (int i = 2; i != dataset.numDimensions(); i++) {
            set.putGeneric(axes[i].type().getLabel(), dataset.numDimensions());
        }

        return set;
    }

    @Override
    public MetaDataSet extractMetaData(ImageDisplay imageDisplay) {

        MetaDataSet set = new MetaDataSet();

        long[] position = new long[imageDisplay.numDimensions()];
        CalibratedAxis[] axes = new CalibratedAxis[imageDisplay.numDimensions()];

        imageDisplay.localize(position);
        imageDisplay.axes(axes);

        set.putGeneric(MetaData.NAME, imageDisplay.getName());
        fillPositionMetaData(set, axes, position);

        return set;
    }

}
