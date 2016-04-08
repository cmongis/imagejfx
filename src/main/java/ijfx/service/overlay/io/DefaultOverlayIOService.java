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
package ijfx.service.overlay.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.imagej.Dataset;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.OverlayService;
import net.imagej.overlay.Overlay;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 *
 * @author cyril
 */

@Plugin(type = Service.class)
public class DefaultOverlayIOService extends AbstractService implements OverlayIOService {

    @Parameter
    ImageDisplayService imageDisplayService;

    @Parameter
    OverlayService overlayService;

    @Parameter
    Context context;

    @Override
    public void saveOverlays(List<Overlay> overlay, File ovlFile) throws IOException{
        new OverlaySaver().save(overlay, ovlFile);
    }

    @Override
    public void saveOverlays(List<Overlay> overlay, Dataset dataset) throws NoSourceFileException,IOException {
        if (dataset.getSource() == null) {
            throw new NoSourceFileException();
        }
        File imageFile = new File(dataset.getSource());

        saveOverlays(overlay, getOverlayFileFromImageFile(imageFile));
    }

    @Override
    public void saveOverlays(ImageDisplay display) throws NoSourceFileException,IOException {
        List<Overlay> overlays = overlayService.getOverlays(display);
        saveOverlays(overlays, imageDisplayService.getActiveDataset(display));
    }

    @Override
    public List<Overlay> loadOverlays(File file) {
        OverlayLoader loader = new OverlayLoader();
        context.inject(loader);
        try {
            return loader.load(file);
        } catch (IOException ex) {
            Logger.getLogger(DefaultOverlayIOService.class.getName()).log(Level.SEVERE, null, ex);
            return new ArrayList<>();
        }
    }
    
    

}
