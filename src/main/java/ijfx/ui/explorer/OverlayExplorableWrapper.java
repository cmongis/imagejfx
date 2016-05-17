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
package ijfx.ui.explorer;

import ijfx.core.imagedb.ImageRecordService;
import ijfx.service.overlay.OverlayStatService;
import ijfx.service.overlay.OverlayStatistics;
import ijfx.service.overlay.PolygonOverlayStatistics;
import ijfx.ui.main.ImageJFX;
import java.io.File;
import java.util.logging.Level;
import javafx.scene.image.Image;
import net.imagej.Dataset;
import net.imagej.overlay.Overlay;
import net.imagej.overlay.PolygonOverlay;
import org.scijava.Context;
import org.scijava.plugin.Parameter;

/**
 *
 * @author cyril
 */
public class OverlayExplorableWrapper extends AbstractExplorable {

    private final Overlay overlay;

    private final File source;

    private OverlayStatistics statistics;

    private boolean isValid;
    
    @Parameter
    private OverlayStatService overlayStatService;

    @Parameter
    private ImageRecordService imageRecordService;

    public OverlayExplorableWrapper(Context context, File source, Overlay overlay) {

        context.inject(this);

        this.overlay = overlay;

        this.source = source;

        getMetaDataSet().merge(imageRecordService.getRecord(source).getMetaDataSet());

        if (overlay instanceof PolygonOverlay) {
            try {
                this.statistics = new PolygonOverlayStatistics(overlay, context);
                overlayStatService.getStatistics(statistics).forEach((key, value) -> {
                    getMetaDataSet().putGeneric(key, value);
                });
            } catch (Exception e) {
                statistics = null;
                ImageJFX.getLogger().log(Level.SEVERE, "Error when creating a wrapper for "+source.getAbsolutePath(), e);
            }
        } else {
            this.statistics = null;
        }
    }

    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public String getSubtitle() {
        return "";
    }

    @Override
    public String getInformations() {
        return "";
    }

    @Override
    public Image getImage() {
        return null;
    }

    @Override
    public void open() {
    }

    @Override
    public Dataset getDataset() {
        return null;
    }

    public File getSource() {
        return source;
    }
    
    public boolean isValid() {
        return statistics != null;
    }

}
