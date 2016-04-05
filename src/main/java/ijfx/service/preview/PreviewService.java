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

import io.scif.gui.AWTImageTools;
import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import net.imagej.Dataset;
import net.imagej.ImageJService;
import net.imagej.Position;
import net.imagej.axis.CalibratedAxis;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
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

//    @Parameter
//    ImageDisplay imageDisplay;
    public PreviewService() {
    }

    public Image getImageDisplay() {
        
        Position activePosition = imageDisplayService.getActivePosition();
        Dataset dataset = imageDisplayService.getActiveDataset();
        long[] dimension = new long[dataset.numDimensions()-2];
        activePosition.localize(dimension);
        RandomAccess<RealType<?>> randomAccess = dataset.randomAccess();
        long[] position = new long[dataset.numDimensions()];
        position[0]=0;
        position[1]=0;
        System.arraycopy(dimension, 0, position, 2, dimension.length);
        randomAccess.setPosition(position);
        int width =(int) dataset.max(0);
        int height = (int)dataset.max(1);
        
        double [][] arrayImage = new double[width][height];
        for (int x = 0; x != width; x++) {
            randomAccess.setPosition(x,0);                
            for (int y = 0; y != height; y++) {
                randomAccess.setPosition(y,1);
                arrayImage[x][y] = randomAccess.get().getRealDouble();
            }

        }
        WritableImage wi = new WritableImage(width, height);
        BufferedImage bufferedImage = AWTImageTools.makeImage(arrayImage, width, height);
        SwingFXUtils.toFXImage(bufferedImage,wi);
        return wi;
    }

}
