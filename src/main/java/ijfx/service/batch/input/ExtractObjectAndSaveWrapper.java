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
package ijfx.service.batch.input;

import ijfx.plugins.commands.BinaryToOverlay;
import ijfx.service.batch.BatchSingleInput;
import ijfx.service.overlay.io.OverlayIOService;
import ijfx.ui.main.ImageJFX;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.imagej.overlay.Overlay;
import org.scijava.Context;
import org.scijava.plugin.Parameter;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class ExtractObjectAndSaveWrapper extends AbstractSaverWrapper{

    
    private boolean objectsAreWhite = false;
    
    @Parameter
    OverlayIOService overlayIoService;
    
    @Parameter
    Context context;
    
    public ExtractObjectAndSaveWrapper(BatchSingleInput input,boolean objectsAreWhite) {
        super(input);
        this.objectsAreWhite = objectsAreWhite;
        input.getDataset().getContext().inject(this);
    }

    @Override
    public void save() {

        try {
            List<Overlay> overlayList = Arrays.asList(BinaryToOverlay.transform(context, getDataset(), objectsAreWhite));
            overlayIoService.saveOverlays(overlayList, new File(getDataset().getSource()));
        } catch (IOException ex) {
            ImageJFX.getLogger().log(Level.SEVERE, null, ex);
        }
        
        getWrappedObject().save();
        
    }
    
}
