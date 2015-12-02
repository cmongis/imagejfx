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
package ijfx.ui.plugin;

import ijfx.service.ui.FxImageService;
import javafx.scene.image.ImageView;
import net.imagej.Dataset;
import net.imagej.lut.LUTService;
import net.imglib2.display.ColorTable;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class LUTView {
    String name;
    ColorTable table;
    protected ImageView imageView;


    public LUTView(String name, ColorTable table) {
       
        this.table = table;
        this.name = name;
    }

    public LUTView render(LUTService lutService, FxImageService fxImageService) {
        final Dataset dataset = lutService.createDataset(name, table);
        imageView = new ImageView(fxImageService.datasetToImage(dataset));
        return this;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public String toString() {
        return name;
    }

    public ColorTable getColorTable() {
        return table;
    }
    
}
