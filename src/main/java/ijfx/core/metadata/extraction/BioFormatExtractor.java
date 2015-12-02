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
package ijfx.core.metadata.extraction;

import ijfx.core.metadata.GenericMetaData;
import ijfx.core.metadata.MetaData;
import ijfx.core.metadata.MetaDataSet;
import ijfx.ui.main.ImageJFX;
import io.scif.FormatException;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import ijfx.core.metadata.extraction.completor.FromNameCompletor;
import loci.formats.ImageReader;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = Service.class, priority = Priority.LOW_PRIORITY)
public class BioFormatExtractor extends AbstractService implements MetaDataExtractorService {

   
    
    
    @Override
    public PlaneList extract(File file) {
        
        ImageFile imageFile = new ImageFile(file.getAbsolutePath());

        /*
         ImageReader r = new ImageProcessorReader(
         new ChannelSeparator(LociPrefs.makeImageReader()));
         */
        ImageReader r = new ImageReader();
        PlaneList planes = new PlaneList();

        try {
            r.setId(file.getAbsolutePath());
        }  catch (IOException ex) {

            ImageJFX.getLogger().log(Level.SEVERE, null, ex);
            return planes;
        } catch (loci.formats.FormatException ex) {
            Logger.getLogger(BioFormatExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }

        int width = r.getSizeX();
        int height = r.getSizeY();
        int channels = r.getSizeC();
        int time = r.getSizeT();
        int stacks = r.getSizeZ();




        MetaDataSet commonMetaData = new MetaDataSet();
        FromNameCompletor completor = new FromNameCompletor();
        commonMetaData.merge(completor.extract(imageFile));
        commonMetaData.putGeneric(MetaData.FOLDER_NAME, file.getParentFile().getName());
        commonMetaData.putGeneric(MetaData.WIDTH, width);
        commonMetaData.putGeneric(MetaData.HEIGHT, height);
        commonMetaData.putGeneric(MetaData.FILE_NAME, file.getName());
        long dateLong = file.lastModified();
         Date date = new Date(dateLong);
         Calendar calendar = Calendar.getInstance();
         calendar.setTime(date);
         int day = calendar.get(Calendar.DAY_OF_MONTH);
         int month = calendar.get(Calendar.MONTH) + 1;
         int year = calendar.get(Calendar.YEAR);
         calendar = Calendar.getInstance();
         //normalize the number of millisecond. it becomes the time at midnight of the last modification of the file. 

         calendar.set(year, month, day);
         dateLong = calendar.getTimeInMillis();

         commonMetaData.putGeneric(MetaData.DATE, dateLong);
         commonMetaData.putGeneric(MetaData.YEAR, year);
         commonMetaData.putGeneric(MetaData.MONTH, month);
         commonMetaData.putGeneric(MetaData.DAY, day); 
         
         Map<String,Object> globalMeta = r.getGlobalMetadata();
         for (String key: globalMeta.keySet()) {
             commonMetaData.putGeneric(key, globalMeta.get(key));
         }

        for (int z = 0; z < stacks; z++) {
            for (int c = 0; c < channels; c++) {
                for (int t = 0; t < time; t++) {
                    BioFormatPlane plane = new BioFormatPlane(r, t, z, c);
                    MetaDataSet set = new MetaDataSet();
                    set.merge(commonMetaData);
                    if (time > 1) {
                        set.putGeneric(MetaData.TIME, t);
                    }
                    if (channels > 1) {
                        set.putGeneric(MetaData.CHANNEL, c);
                        //set.putGeneric(MetaData.CHANNEL_NAME, retrieve.getChannelName(1, c));

                    }

                    if (stacks > 1) {
                        set.putGeneric(MetaData.Z_POSITION, z);
                    }
                    try {
                        long planeIndex = plane.getPlaneIndex();
                        set.put(new GenericMetaData(MetaData.PLANE_INDEX, planeIndex));
                        // System.out.println(set);plane.getPlaneIndex
                        plane.setMetaDataSet(set);
                        plane.setSourceFile(imageFile);
                        planes.add(plane);
                    } catch (Exception ex) {
                        ImageJFX.getLogger();

                    }
                }
            }
        }

        return planes;
        
        
        //return new PlaneList();

    }

}
