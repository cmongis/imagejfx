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
import ijfx.service.Timer;
import ijfx.service.TimerService;
import ijfx.ui.main.ImageJFX;
import io.scif.FormatException;
import io.scif.Metadata;
import io.scif.SCIFIO;
import io.scif.config.SCIFIOConfig;
import io.scif.filters.ReaderFilter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.stream.StreamSource;
import mongis.ndarray.NDimensionalArray;
import net.imagej.axis.Axes;
import org.apache.commons.math3.geometry.spherical.oned.S1Point;
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
public class DefaultMetaDataExtractionService extends AbstractService implements MetaDataExtractorService {

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
            Metadata metadata = getSCIFIO().format().getFormat(file.getAbsolutePath()).createParser().parse(file.getAbsolutePath());
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

            if (metadata.get(0).getAxesNonPlanar().size() > 0) {
                NDimensionalArray ndarray = new NDimensionalArray(metadata.get(0).getAxesLengthsNonPlanar());

                String[] dimNames = metadata
                        .get(0)
                        .getAxesNonPlanar()
                        .stream().map(axe -> axe.type().getLabel())
                        .toArray(size -> new String[size]);

                long[] dimLengths = metadata.get(0).getAxesLengthsNonPlanar();

                metadataset.putGeneric(MetaData.DIMENSION_ORDER, String.join(",", dimNames));
                metadataset.putGeneric(MetaData.DIMENSION_LENGHS, Arrays.toString(dimLengths));
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

        List<MetaDataSet> planes = new ArrayList<>();
        String dimensionLengthesString = metadataset.get(MetaData.DIMENSION_LENGHS).getStringValue();
        if (dimensionLengthesString == null) {

            planes.add(new MetaDataSet().merge(metadataset));
            return planes;
        } else {
           
            long[] dimensionLengthes = readLongArray(metadataset.get(MetaData.DIMENSION_LENGHS).getStringValue());
            
            String[] dimensionLabelArray = metadataset.get(MetaData.DIMENSION_ORDER).getStringValue().split(",");
            
            NDimensionalArray array = new NDimensionalArray(dimensionLengthes);
            int i = 0;
            
            // generate all the possibilities
            for(long[] coordinate : array.get(0).generateAllPossibilities()) {
                
                MetaDataSet planeMetaDataSet = new MetaDataSet().merge(metadataset);
                
                for(int d = 0; d!= coordinate.length;d++) {
                    String label = dimensionLabelArray[d];
                    long value = coordinate[d];
                    
                    
                    planeMetaDataSet.putGeneric(label, value);
                    
                }  
                
                planes.add(planeMetaDataSet);
            }
            
        }
        return planes;

    }

    public static long[] readLongArray(String str) {
        // the string is usually a string of type "[12,324,32]

        // deleting the []
        int begin = 0;
        int end = str.length() - 1;

        str = str.substring(begin, end);

        String[] numbers = str.split(",");
        long[] longs = new long[numbers.length];
        for(int i = 0;i!=numbers.length;i++) {
            longs[i] = Long.decode(numbers[i]);
        }
        return longs;
    }

   

    @Override
    public List<MetaDataSet> extractPlaneMetaData(File file) {
        return extractPlaneMetaData(extractMetaData(file));
    }

}
