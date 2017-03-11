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
package ijfx.service.ui.choice;

import java.nio.IntBuffer;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;

/**
 *
 * @author cyril
 */
public class PixelRasterUtils {
    
    public static PixelRaster fromImage(Image image) {
        
        if (image == null) {
            return null;
        }
        
        return new ImagePixelRasterWrapper(image);
    }
    
    public static Image toImage(PixelRaster raster) {
        
        
        if(raster instanceof ImagePixelRasterWrapper) {
            return ((ImagePixelRasterWrapper)raster).getImage();
        }
        
        if(raster == null) {
            return null;
        }
        
        WritableImage image = new WritableImage(raster.getWidth(), raster.getHeight());
        
        
        
        image.getPixelWriter().setPixels(0, 0, raster.getWidth(),raster.getHeight(),PixelFormat.getIntArgbInstance() , IntBuffer.wrap(raster.getPixels()), raster.getWidth());
        
        return image;
        
    }
}
