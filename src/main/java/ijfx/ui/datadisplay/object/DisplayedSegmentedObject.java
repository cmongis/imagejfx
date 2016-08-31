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
package ijfx.ui.datadisplay.object;

import ijfx.core.metadata.MetaDataSet;
import ijfx.service.batch.SegmentedObject;
import ijfx.service.overlay.OverlaySelectionService;
import ijfx.service.overlay.OverlayStatService;
import java.lang.ref.WeakReference;
import java.util.List;
import net.imagej.display.ImageDisplay;
import net.imagej.overlay.Overlay;
import org.scijava.plugin.Parameter;

/**
 *
 * @author cyril
 */
public class DisplayedSegmentedObject implements SegmentedObject{
    
    public final SegmentedObject object;
    
    public final WeakReference<ImageDisplay> imageDisplay;
    
    @Parameter
    OverlaySelectionService selectionService;
    
    @Parameter
    OverlayStatService ijfxStatsService;
    
    public DisplayedSegmentedObject(ImageDisplay imageDisplay, SegmentedObject object) {
        this.object = object;
        this.imageDisplay = new WeakReference<>(imageDisplay);
                
        imageDisplay.getContext().inject(this);
    }

    @Override
    public Overlay getOverlay() {
        return object.getOverlay();
    }

    @Override
    public MetaDataSet getMetaDataSet() {
        return object.getMetaDataSet();
    }
    
    public void setSelection(boolean selection) {
        if(imageDisplay.get() != null)
        selectionService.setOverlaySelection(imageDisplay.get(), getOverlay(),selection);
    }
    
    public Double[] getPixelsValues() {
        return ijfxStatsService.getValueListFromImageDisplay(imageDisplay.get(), object.getOverlay());
    }
    
    
    
    
}
