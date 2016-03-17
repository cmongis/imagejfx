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

import ijfx.core.metadata.MetaData;
import ijfx.core.metadata.MetaDataSet;
import ijfx.ui.main.ImageJFX;
import io.scif.FormatException;
import io.scif.Metadata;
import io.scif.SCIFIO;
import io.scif.config.SCIFIOConfig;
import io.scif.filters.ReaderFilter;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.imagej.axis.Axes;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 *
 * @author cyril
 */
@Plugin(type = Service.class, priority=Priority.VERY_LOW_PRIORITY)
public class DefaultMetaDataExtractionService extends AbstractService implements MetaDataExtractorService {

    private SCIFIO scifio;
    private SCIFIOConfig config;

    private final static Logger logger = ImageJFX.getLogger();

    private final String WRONG_FORMAT = "Couldn't find an appropriate format for the file : %s";
    private final String ACCESS_PROBLEM = "Could'nt access file : %s";

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

        try {
            // ReaderFilter reader = getReaderFilter(file);

            Metadata metadata = getSCIFIO().format().getFormat(file.getAbsolutePath()).createParser().parse(file.getAbsolutePath());

            long serieCount, timeCount, zCount, channelCount, width, height, imageSize, bitsPerPixel;
            System.out.println(metadata);
            System.out.println(Arrays.toString(metadata.get(0).getAxesLengths()));

            serieCount = metadata.getImageCount();
            width = metadata.get(0).getAxisLength(Axes.X);
            height = metadata.get(0).getAxisLength(Axes.Y);
            timeCount = metadata.get(0).getAxisLength(Axes.TIME);
            zCount = metadata.get(0).getAxisLength(Axes.Z);
            channelCount = metadata.get(0).getAxisLength(Axes.CHANNEL);
            bitsPerPixel = metadata.get(0).getBitsPerPixel();
            imageSize = metadata.getDatasetSize();
            
            metadataset.putGeneric(MetaData.WIDTH, width);
            metadataset.putGeneric(MetaData.HEIGHT, height);
            metadataset.putGeneric(MetaData.ZSTACK_NUMBER, zCount);
            metadataset.putGeneric(MetaData.TIME_COUNT, timeCount);
            metadataset.putGeneric(MetaData.BITS_PER_PIXEL, bitsPerPixel);
            metadataset.putGeneric(MetaData.FILE_SIZE, imageSize);
            metadataset.putGeneric(MetaData.SERIE_COUNT, serieCount);
            metadataset.putGeneric(MetaData.CHANNEL_COUNT, channelCount);

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
}
