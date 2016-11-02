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
package ijfx.service.batch;

import ijfx.core.metadata.GenericMetaData;
import ijfx.core.metadata.MetaData;
import ijfx.core.metadata.MetaDataSet;
import ijfx.core.metadata.MetaDataSetType;
import ijfx.service.overlay.OverlayStatService;
import ijfx.service.overlay.PixelStatistics;
import ijfx.service.overlay.PixelStatisticsBase;
import net.imagej.overlay.Overlay;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import ijfx.service.overlay.OverlayShapeStatistics;
import ijfx.service.overlay.OverlayStatistics;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class DefaultSegmentedObject implements SegmentedObject {

    
    
    private Overlay overlay;

    
    private MetaDataSet set = new MetaDataSet();
    
    
    @Parameter
    private OverlayStatService overlayStatsService;
    
    
    public DefaultSegmentedObject() {
        set.setType(MetaDataSetType.OBJECT);
    }

    public void setOverlay(Overlay overlay) {
        this.overlay = overlay;
        set.putGeneric(MetaData.NAME, overlay.getName());
    }
    
    public void setMetaDataSet(MetaDataSet set) {
        this.set = set;
    }
    
    public DefaultSegmentedObject(Overlay overlay, OverlayStatistics overlayStatistics) {
        this();
        overlay.getContext().inject(this);
        setOverlay(overlay);
        
        set.merge(overlayStatsService.getStatisticsAsMap(overlayStatistics));
        
    }

    @Override
    public Overlay getOverlay() {
        return overlay;
    }

    @Override
    public MetaDataSet getMetaDataSet() {
        return set;
    }
}
