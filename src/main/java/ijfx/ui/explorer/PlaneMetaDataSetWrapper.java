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

import ijfx.bridge.ImageJContainer;
import ijfx.core.metadata.MetaData;
import ijfx.core.metadata.MetaDataSet;
import ijfx.core.metadata.MetaDataSetType;
import ijfx.core.utils.DimensionUtils;
import ijfx.service.ImagePlaneService;
import ijfx.service.thumb.ThumbService;
import ijfx.ui.activity.ActivityService;
import ijfx.ui.main.ImageJFX;
import io.scif.services.DatasetIOService;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.image.Image;
import mongis.utils.AsyncCallable;
import net.imagej.Dataset;
import net.imagej.DefaultDataset;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.ui.UIService;

/**
 *
 * @author cyril
 */
public class PlaneMetaDataSetWrapper implements Explorable{
         private final MetaDataSet m;

        BooleanProperty selectedProperty = new SimpleBooleanProperty(false);

        @Parameter
        ThumbService thumbService;
        
        @Parameter
        UIService uiService;
        
        @Parameter
        ActivityService activityService;
        
        @Parameter
        DatasetIOService datasetIoService;
        
        @Parameter
        ImagePlaneService imagePlaneService;
        
      
        public PlaneMetaDataSetWrapper(Context context, MetaDataSet m) {
            context.inject(this);
            this.m = m;
            m.setType(MetaDataSetType.PLANE);
        }

        @Override
        public String getTitle() {
            return m.get(MetaData.FILE_NAME).getStringValue();
        }

        @Override
        public String getSubtitle() {
            return new StringBuilder()
                    .append(composeSubtitle(m.get(MetaData.CHANNEL), "C"))
                    .append(composeSubtitle(m.get(MetaData.Z_POSITION), "Z"))
                    .append(composeSubtitle(m.get(MetaData.TIME), "T"))
                    .toString();
        }

        @Override
        public String getInformations() {
            return null;
        }

        @Override
        public Image getImage() {
            try {
               // return thumbService.getThumb(getFile(), m.get(MetaData.PLANE_INDEX).getIntegerValue()-1, 100, 100);
               
                long[] position = DimensionUtils.readLongArray(m.get(MetaData.PLANE_NON_PLANAR_POSITION).getStringValue());
               return thumbService.getThumb(getFile(), position, 100, 100);
            
            } catch (Exception e) {
                ImageJFX.getLogger().log(Level.SEVERE, "Error when loading preview for Plane", e);
            }
            return null;
        }

        @Override
        public void open() {

            new AsyncCallable()
                    .run(this::getDataset)
                    .then(dataset -> {
                        uiService.show(dataset);
                        
                        activityService.openByType(ImageJContainer.class);
                    })
                    .start();
        }

        @Override
        public BooleanProperty selectedProperty() {
            return selectedProperty;
        }

        private File getFile() {
            return new File(getMetaDataSet().get(MetaData.ABSOLUTE_PATH).getStringValue());
        }

        @Override
        public MetaDataSet getMetaDataSet() {
            return m;
        }

        private String composeSubtitle(MetaData m, String keyName) {
            if (m == null || m.isNull()) {
                return "";
            } else {
                return String.format("%s = %s ", keyName, m.getStringValue());
            }
        }
        
        
        @Override
        public Dataset getDataset() {

            try {

                MetaData nonPlanarPosition = m.get(MetaData.PLANE_NON_PLANAR_POSITION);
                
                if (nonPlanarPosition == null || nonPlanarPosition.isNull()) {
                    return datasetIoService.open(m.get(MetaData.ABSOLUTE_PATH).getStringValue());
                } else {
                    long[] location;
                   
                    location = DimensionUtils.readLongArray(nonPlanarPosition.getStringValue());
                    long[] dimLengths = DimensionUtils.readLongArray(m.get(MetaData.DIMENSION_LENGHS).getStringValue());
                    
                    //Dataset virtualDataset = imagePlaneService.openVirtualDataset(getFile());
                    return imagePlaneService.extractPlane(getFile(), location);
                    //return imagePlaneService.extractPlane(file, location)
                    //return imagePlaneService.isolatePlane(imagePlaneService.openVirtualDataset(getFile()), location);
                }

            } catch (Exception ex) {
                ImageJFX.getLogger().log(Level.SEVERE, null, ex);
            }
            return null;
        }        
    }

