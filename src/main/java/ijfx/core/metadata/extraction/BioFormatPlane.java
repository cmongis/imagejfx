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

import ijfx.core.metadata.MetaDataSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import loci.formats.ImageReader;

public class BioFormatPlane implements ImagePlane {

    int z;
    int t;
    int c;
    MetaDataSet set;
    ImageReader reader;
    ImageFile file;

    public BioFormatPlane(ImageReader reader, int t, int z, int c) {
        this.z = z;
        this.t = t;
        this.c = c;
        this.reader = reader;
    }

    @Override
    public long getPlaneIndex() {
        return reader.getIndex(z, c, t);
    }

    @Override
    public ImageFile getSourceFile() {
        return file;
    }

    @Override
    public MetaDataSet getMetaDataSet() {
        return set;
    }

    @Override
    public void setMetaDataSet(MetaDataSet set) {
        this.set = set;
    }

    @Override
    public Object getPixels() {
        return null;
    }

    @Override
    public boolean savePixels(String path) {
        /*
         try {
         // ImageProcessor ip = reader.openProcessors(reader.getIndex(z, c, t))[0];
                
         // IJ.save(new ImagePlus("",reader.openProcessors(reader.getIndex(z, c, t))[0]),path);
         // ip.setPixels(null);
         // ip = null;
               
         } catch (FormatException ex) {
         ImageJFX.getLogger();
         } catch (IOException ex) {
         ImageJFX.getLogger();
         }
         return true;*/
        return false;
    }

    public void setSourceFile(ImageFile file) {
        this.file = file;
    }

}
