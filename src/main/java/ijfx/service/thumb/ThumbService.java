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
import ijfx.service.batch.SilentImageDisplay;
import ijfx.ui.main.ImageJFX;
import io.scif.Format;
import io.scif.FormatException;
import io.scif.ImageMetadata;
import io.scif.Plane;
import io.scif.Reader;
import io.scif.SCIFIO;
import io.scif.bf.BioFormatsFormat;
import io.scif.gui.AWTImageTools;
import io.scif.services.FormatService;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javax.imageio.ImageIO;
import net.imagej.Dataset;
import net.imagej.ImageJService;
import net.imagej.display.DatasetView;
import net.imagej.display.ImageDisplay;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
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
    
    Logger logger = ImageJFX.getLogger();
    
    private static final String THUMB_FORMAT = ".png";
    
    private static final File THUMB_FOLDER = new File(ImageJFX.getConfigDirectory(), "thumbs/");
    
    public SCIFIO scifio() {
        if (scifio == null) {
            scifio = new SCIFIO(context);
        }
        return scifio;
    }
    
    public Image getThumb(File file, int planeIndex, int width, int height) throws IOException {

        // we use SCIFIO initializer to get the right reader
        Reader reader = null;
        
        if(planeIndex == -1) planeIndex = 0;
        
        if (thumbExists(file, planeIndex, width, height)) {
            logger.info("Thumb already exists. Returning " + getThumbFile(file, planeIndex, width, height));
            return new Image("file:" + getThumbFile(file, planeIndex, width, height).getAbsolutePath(), true);
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
            
            //Dataset extractPlane = imagePlaneService.extractPlane(file, planeIndex);
            //SummaryStatistics stats = statsService.getDatasetSummaryStatistics(extractPlane);
            
            //ImageDisplay display = new SilentImageDisplay(getContext(), extractPlane);
            //DatasetView view = (DatasetView) display.getActiveView();
            
            //view.setChannelRange(0, stats.getMin(), stats.getMax());
            //BufferedImage bf = AWTImageTools.makeUnsigned(view.getScreenImage().image());
            //BufferedImage scale = AWTImageTools.scale(bf, width, height, true);
            Plane plane = reader.openPlane(0, planeIndex);
            ImageMetadata iMeta = reader.getMetadata().get(0);

            //ImagePlaneService.
            //AWTImageTools.openThumbImage(plane, reader, planeIndex, axes, height, height, true);
            BufferedImage image = AWTImageTools.openThumbImage(plane, reader, (int) 0, iMeta.getAxesLengthsPlanar(), width, height, true);
            
            SwingFXUtils.toFXImage(image, wi);
            ImageIO.write(image,"png",getThumbFile(file, planeIndex, width, height));
            //reader.close();

        } catch (Exception e) {
            ImageJFX.getLogger().log(Level.SEVERE, "Error when preparing thumbs", e);
            return null;
        }
        
        return wi;
        
    }
    
    private boolean thumbExists(File file, int planeIndex, int width, int height) {
        return getThumbFile(file, planeIndex, width, height).exists();
    }

    private File getThumbFile(File file, int planeIndex, int width, int height) {
        return new File(getThumbFolder(), getThumbFileName(file, planeIndex, width, height));
    }
    
    private File getThumbFolder() {
        if (THUMB_FOLDER.exists() == false) {
            THUMB_FOLDER.mkdir();
        }
        return THUMB_FOLDER;
    }
    
    private String getThumbUUID(File file, int planeIndex, int width, int height) {
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
    
    private String getThumbFileName(File file, int planeIndex, int width, int height) {
        return getThumbUUID(file, planeIndex, width, height) + THUMB_FORMAT;
    }
    
    public Task<Image> getThumbTask(final File file, int planeIndex, int width, int height) {
        Task<Image> task = new Task<Image>() {
            public Image call() throws IOException {
                
                return getThumb(file, planeIndex, width, height);
            }
        };
        
        return task;
    }
    
}
