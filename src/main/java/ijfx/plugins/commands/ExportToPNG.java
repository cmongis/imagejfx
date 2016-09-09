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
package ijfx.plugins.commands;

import ijfx.service.overlay.OverlaySelectionService;
import ijfx.ui.main.ImageJFX;
import io.scif.services.DatasetIOService;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import javax.imageio.ImageIO;
import net.imagej.DatasetService;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.OverlayService;
import net.imagej.overlay.RectangleOverlay;
import net.imglib2.Cursor;
import net.imglib2.display.screenimage.awt.ARGBScreenImage;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.DialogPrompt;
import org.scijava.ui.UIService;
import org.scijava.widget.FileWidget;

/**
 *
 * @author cyril
 */
@Plugin(type = Command.class, menuPath = "Edit > Copy to PNG...")
public class ExportToPNG extends ContextCommand {

    @Parameter(label = "save", style = FileWidget.SAVE_STYLE, type = ItemIO.INPUT)
    ImageDisplay imageDisplay;

    @Parameter
    ImageDisplayService imageDisplayService;

    File outputFile;

    @Parameter
    OverlayService overlayService;

    @Parameter
    OverlaySelectionService overlaySelectionService;

    @Parameter
    DatasetService datasetService;

    @Parameter
    DatasetIOService datasetIOService;

    @Parameter
    UIService uiService;

    @Override
    public void run() {

        outputFile = uiService.chooseFile(outputFile, "save");

        if (outputFile == null) {
            cancel("Ouptut file not specified");
            return;
        }

        try {

            ARGBScreenImage screenImage = imageDisplayService.getActiveDatasetView(imageDisplay).getScreenImage();

            RectangleOverlay overlay;

            long xmin = 0;
            long ymin = 0;
            long xmax = screenImage.max(0);
            long ymax = screenImage.max(1);

            if (overlaySelectionService.getSelectedOverlay(imageDisplay) instanceof RectangleOverlay) {
                overlay = (RectangleOverlay) overlaySelectionService.getSelectedOverlay(imageDisplay);

                xmin = Math.round(overlay.getRegionOfInterest().getOrigin(0));
                ymin = Math.round(overlay.getRegionOfInterest().getOrigin(1));
                xmax = Math.round(overlay.getRegionOfInterest().getExtent(0));
                ymax = Math.round(overlay.getRegionOfInterest().getExtent(1));

            }

            IntervalView<ARGBType> interval = Views.offsetInterval(screenImage, new long[]{xmin, ymin}, new long[]{xmax, ymax});

            WritableImage image = new WritableImage((int) interval.dimension(0), (int) interval.dimension(1));

            Cursor<ARGBType> cursor = interval.cursor();

            long[] position = new long[2];

            cursor.reset();
            while (cursor.hasNext()) {
                cursor.fwd();
                cursor.localize(position);
                image.getPixelWriter().setArgb((int) position[0], (int) position[1], cursor.get().get());
            }
            
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", outputFile);
            uiService.showDialog("Exportation over : "+outputFile.getName());
           
        } catch (IOException ex) {
            ImageJFX.getLogger().log(Level.SEVERE, null, ex);
            uiService.showDialog("Error when saving file " + outputFile.getName(), DialogPrompt.MessageType.ERROR_MESSAGE);
        }

    }

}
