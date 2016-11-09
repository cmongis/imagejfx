/*
 * /*
 *     This file is part of ImageJ FX.
 *
 *     ImageJ FX is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     ImageJ FX is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with ImageJ FX.  If not, see <http://www.gnu.org/licenses/>. 
 *
 * 	Copyright 2015,2016 Cyril MONGIS, Michael Knop
 *
 */
package ijfx.service.thumb;

import ijfx.core.stats.IjfxStatisticService;
import ijfx.service.ImagePlaneService;
import ijfx.service.Timer;
import ijfx.service.TimerService;
import ijfx.service.dataset.DatasetUtillsService;
import ijfx.service.preview.PreviewService;
import ijfx.ui.main.ImageJFX;
import io.scif.SCIFIO;
import io.scif.services.FormatService;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javax.imageio.ImageIO;
import mongis.utils.CallbackTask;
import net.imagej.ImageJService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.view.Views;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = Service.class)
public class ThumbService extends AbstractService implements ImageJService {

    SCIFIO scifio;

    @Parameter
    Context context;

    @Parameter
    FormatService formatService;

    @Parameter
    ImagePlaneService imagePlaneService;

    @Parameter
    IjfxStatisticService statsService;

    @Parameter
    DatasetUtillsService datasetUtilsService;

    @Parameter
    PreviewService previewService;

    @Parameter
    TimerService timerService;

    Logger logger = ImageJFX.getLogger();

    private static final Boolean lock = new Boolean(true);

    private static final String THUMB_FORMAT = ".png";

    private static final File THUMB_FOLDER = new File(ImageJFX.getConfigDirectory(), "thumbs/");

    public SCIFIO scifio() {
        if (scifio == null) {
            scifio = new SCIFIO(context);
        }
        return scifio;
    }

    public Image getThumb(File file, long[] nonSpacialPosition, int width, int height) throws IOException {
        return getThumb(file, 0, nonSpacialPosition, width, height);
    }

    public Image getThumb(File file, int imageId, long[] nonSpacialPosition, int width, int height) throws IOException {

        Timer timer = timerService.getTimer(this.getClass());
        String position;
        if (nonSpacialPosition == null) {
            position = "file";
        } else {
            position = LongStream
                    .of(nonSpacialPosition)
                    .mapToObj(l -> "" + l)
                    .collect(Collectors.joining());
        }

        File thumbFile = getThumbFile(file, position, width, height);

        if (thumbFile.exists()) {

            logger.info("Loading thumb " + thumbFile.getAbsolutePath());
            timer.start();
            Image image = new Image("file:" + thumbFile.getAbsolutePath());
            timer.elapsed("loading png");
            return image;
        }
        RandomAccessibleInterval interval;
        timer.start();
        synchronized (lock) {

            interval = datasetUtilsService.open(file, imageId, true);
        }
        timer.elapsed("opening dataset");
        if (nonSpacialPosition == null) {
            nonSpacialPosition = new long[interval.numDimensions() - 2];
        }

        interval = imagePlaneService.plane(interval, nonSpacialPosition);

        Image image = getThumb(interval, width, height);

        new CallbackTask<>()
                .tryRun(() -> saveImage(image, thumbFile))
                .start();

        return image;

    }

    public void saveImage(Image image, File file) throws Exception {
        synchronized (lock) {
            BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);

            ImageIO.write(bImage, "png", file);
        }
    }

    public Image getThumb(RandomAccessibleInterval interval, int width, int height) {

        if (width == 0 || height == 0) {
            throw new IllegalArgumentException("Width or height cannot be equals to 0");
        }
        int sub = (int) interval.dimension(0) / width;

        sub = sub <= 2 ? 0 : sub - 1;

        RandomAccessibleInterval subsample;
        if (sub == 0) {
            subsample = interval;
        } else {
            subsample = Views.subsample(interval, sub);
        }

        Image image = previewService.datasetToImage(subsample);
        return image;
    }

    /*
    public Image getThumb(File file, Integer imageId, Integer planeIndex, int width, int height) throws IOException {

        // we use SCIFIO initializer to get the right reader
        Reader reader = null;

        if (planeIndex == -1) {
            planeIndex = 0;
        }

        if (thumbExists(file, planeIndex.toString(), width, height)) {
            logger.info("Thumb already exists. Returning " + getThumbFile(file, planeIndex.toString(), width, height));
            return new Image("file:" + getThumbFile(file, planeIndex.toString(), width, height).getAbsolutePath(), true);
        }

        WritableImage wi = new WritableImage(width, height);

        try {
            ImageJFX.getLogger().info("Reading " + file.getAbsolutePath());
            reader = scifio().initializer().initializeReader(file.getAbsolutePath());
        } catch (FormatException ex) {
            logger.warning("SCIFIO Reader doesn't work with " + file.getName());
            reader = null;
        }

        // but sometimes, it does work for DV files for examples, so we call the Bioformat Reader directlry
        if (reader == null) {

            try {
                Format format = new BioFormatsFormat();
                context.inject(format);
                reader = format.createReader();

                reader.setSource(file.getAbsolutePath());

            } catch (FormatException ex) {
                ImageJFX.getLogger().log(Level.SEVERE, null, ex);

            }
        }

        // if there is still 
        if (reader == null) {
            logger.warning("Couldn't find an appropriate logger for " + file.getName());
            return null;
        }

        try {
            logger.info("Calculating Thumb.");

            Plane plane = reader.openPlane(imageId, planeIndex);
            ImageMetadata iMeta = reader.getMetadata().get(imageId);

            //ImagePlaneService.
            //AWTImageTools.openThumbImage(plane, reader, planeIndex, axes, height, height, true);
            BufferedImage image = AWTImageTools.openThumbImage(plane, reader, (int) 0, iMeta.getAxesLengthsPlanar(), width, height, true);

            SwingFXUtils.toFXImage(image, wi);
            ImageIO.write(image, "png", getThumbFile(file, planeIndex.toString(), width, height));
            //reader.close();

        } catch (Exception e) {
            ImageJFX.getLogger().log(Level.SEVERE, "Error when preparing thumbs", e);
            return null;
        }

        return wi;
    }

    /*
    public Image getThumb(File file, Integer planeIndex, int width, int height) throws IOException {
        return getThumb(file, 0, planeIndex, width, height);
    }*/
    private boolean thumbExists(File file, String planeIndex, int width, int height) {
        return getThumbFile(file, planeIndex, width, height).exists();
    }

    private File getThumbFile(File file, String planeIndex, int width, int height) {
        return new File(getThumbFolder(), getThumbFileName(file, planeIndex, width, height));
    }

    private File getThumbFolder() {
        if (THUMB_FOLDER.exists() == false) {
            THUMB_FOLDER.mkdir();
        }
        return THUMB_FOLDER;
    }

    private String getThumbUUID(File file, String planeIndex, int width, int height) {
        return UUID
                .nameUUIDFromBytes(
                        new StringBuilder()
                        .append(file.getAbsolutePath())
                        .append("v2")
                        .append(planeIndex)
                        .append(width)
                        .append(height)
                        .toString()
                        .getBytes()
                )
                .toString();
    }

    private String getThumbFileName(File file, String planeIndex, int width, int height) {
        return getThumbUUID(file, planeIndex.toString(), width, height) + THUMB_FORMAT;
    }

    /*
    public Task<Image> getThumbTask(final File file, int planeIndex, int width, int height) {
        Task<Image> task = new Task<Image>() {
            public Image call() throws IOException {

                return getThumb(file, planeIndex, width, height);
            }
        };

        return task;
    }*/
}
