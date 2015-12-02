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
package ijfx.service.ui;

import ijfx.ui.main.ImageJFX;
import ijfx.ui.plugin.LUTComboBox;
import ijfx.ui.plugin.LUTView;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.ImageJService;
import net.imagej.display.DatasetView;
import net.imagej.display.ImageDisplayService;
import net.imagej.lut.LUTService;
import net.imglib2.display.ColorTable;
import org.scijava.Priority;
import org.scijava.display.DisplayService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = Service.class, priority = Priority.LOW_PRIORITY)
public class FxImageService extends AbstractService implements ImageJService{

    @Parameter
    ImageDisplayService imageDisplayService;

    @Parameter
    DisplayService displayService;

    @Parameter
    LUTService lutService;
    
    @Parameter
    DatasetService datasetService;
    
    private static final HashMap<String,LUTView> lutViewMap = new HashMap<>();
    
    @Override
    public void initialize() {
        super.initialize();
        
       lutService.findLUTs().forEach((key, url) -> {
            try {
                ColorTable table = lutService.loadLUT(url);
                lutViewMap.put(key,new LUTView(key,table).render(lutService, this));
                
            } catch (IOException ex) {
                ImageJFX.getLogger().log(Level.SEVERE, null, ex);
            }
        });
        
    }
    
    public Image datasetToImage(Dataset dataset) {
        return bufferedImageToImage(datasetToBufferedImage(dataset));
    }

    public BufferedImage datasetToBufferedImage(Dataset dataset) {
        final DatasetView view = (DatasetView) imageDisplayService.createDataView(dataset);

        view.setPosition(0, 0);
        view.rebuild();
        return view.getScreenImage().image();
    }

    public Image bufferedImageToImage(BufferedImage image) {
        WritableImage writableImage = new WritableImage(image.getWidth(), image.getHeight());
        SwingFXUtils.toFXImage(image, writableImage);

        return writableImage;
    }
    
    public static Map<String,LUTView> getLUTViewMap() {
        return lutViewMap;
    }
    

}
