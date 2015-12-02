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
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import net.imagej.ImageJService;
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

    public SCIFIO scifio() {
        if (scifio == null) {
            scifio = new SCIFIO(context);
        }
        return scifio;
    }

    public synchronized Image getThumb(File file, int planeIndex, int width, int height) throws IOException {

        // we use SCIFIO initializer to get the right reader
        Reader reader = null;

        WritableImage wi = new WritableImage(width, height);

        try {
            reader = scifio().initializer().initializeReader(file.getAbsolutePath());
        } catch (FormatException ex) {
            ImageJFX.getLogger().log(Level.SEVERE, null, ex);
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
            return null;
        }

        try {
            Plane plane = reader.openPlane(0, planeIndex);
            ImageMetadata iMeta = reader.getMetadata().get(0);

            BufferedImage image = AWTImageTools.openThumbImage(plane, reader, (int) 0, iMeta.getAxesLengthsPlanar(), width, height, true);
            SwingFXUtils.toFXImage(image, wi);

            reader.close();

        } catch (Exception e) {
            ImageJFX.getLogger();
            return null;
        }

        return wi;

    }

    public Task<Image> getThumbTask(final File file, int planeIndex, int width, int height) {
        Task<Image> task = new Task<Image>() {
            public Image call() throws IOException{

                return getThumb(file, planeIndex, width, height);
            }
        };

        return task;
    }

}
