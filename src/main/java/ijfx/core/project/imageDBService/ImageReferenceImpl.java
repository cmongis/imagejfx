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
package ijfx.core.project.imageDBService;

import ijfx.core.utils.FileName;
import java.io.File;
import java.util.Objects;

/**
 * This class is used to store informations to retrieve a file on the user file
 * system.
 *
 * @author Cyril MONGIS, 2015
 *
 */
public class ImageReferenceImpl implements ImageReference {

    /**
     * Hash of the image
     */
    private String id;

    /**
     * absolute path of the image
     *
     */
    private String path;
   

    /**
     * Create a new image reference with the given id and a given file path
     *
     * @param id the id to be attributed to the image reference.
     * @param path the absolute path of an image file
     */
    public ImageReferenceImpl(String id, String path) {
        this.path = path;
        this.id = id;
       
    }

    /**
     *
     * @return The id of the image
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     *
     * @return the absolute path of the image
     */
    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String imageName() {
        return FileName.getName(new File(path));

    }


    @Override
    public void setID(String id) {
        this.id = id;
    }

    @Override
    public void setPath(String absolutePath) {
        this.path = absolutePath;
    }

   

    @Override
    public boolean equals(Object object) {
        if (object instanceof ImageReferenceImpl) {
            ImageReferenceImpl ir = (ImageReferenceImpl) object;
            return ir.getId().equals(this.id) && ir.getPath().equals(this.path);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.id);
        hash = 29 * hash + Objects.hashCode(this.path);
        return hash;
    }

}
