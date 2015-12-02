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
import java.io.File;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class DummyMetaDataGenerator  {

    
    public PlaneList extract(File file) {

        PlaneList result = new PlaneList();

        for (String well : new String[]{"A01", "A02", "A03", "A04"}) {
            
            ImageFile imageFile = new ImageFile();
            imageFile.setSourceFile(new File("./dummy"+well+".tif"));
            
            int planeId = 0;
            for (int position = 1; position != 10; position++) {
                
                for (int channel = 0; channel != 3; channel++) {
                    for (double z = 0.0; z <= 1; z += 0.1) {
                        DummyPlane plane = new DummyPlane(imageFile);
                        plane.addMetaData(new GenericMetaData("plane id",planeId));
                        plane.addMetaData(new GenericMetaData(MetaData.WIDTH,412));
                        plane.addMetaData(new GenericMetaData(MetaData.HEIGHT,313));
                        plane.addMetaData(new GenericMetaData(MetaData.CHANNEL,channel));
                        plane.addMetaData(new GenericMetaData(MetaData.Z_POSITION,new Double(z).toString()));
                        plane.addMetaData(new GenericMetaData("position",position));
                        plane.addMetaData(new GenericMetaData("xyCalibration","1.343"));
                        plane.addMetaData(new GenericMetaData("microscope name","Nikon Ti Eclipse"));
                        plane.addMetaData(new GenericMetaData("well name",well));
                        imageFile.add(plane);
                        result.add(plane);
                    }
                }
            }
        }

        return result;

    }

   
    
    
    
    public class DummyPlane implements ImagePlane {
        
        MetaDataSet dataSet = new MetaDataSet();
        ImageFile source;
        
        public long getPlaneIndex() {
            return 0;
        }
        
        public DummyPlane(ImageFile source) {
            this.source = source;
        }
        
        public void addMetaData(MetaData metadata) {
            dataSet.put(metadata);
        }
        
        @Override
        public ImageFile getSourceFile() {
            return source;
        }

        @Override
        public MetaDataSet getMetaDataSet() {
           return dataSet;
        }

        @Override
        public void setMetaDataSet(MetaDataSet set) {
            dataSet = set;
        }

        @Override
        public Object getPixels() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
        public boolean savePixels(String path) {
          return true;  
        };
    }
}
