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
package ijfx.core.imagedb;

import java.io.File;
import java.util.Collection;
import net.imagej.ImageJService;
import org.apache.commons.io.filefilter.IOFileFilter;

/**
 *
 * @author Cyril Quinton
 */
public interface ImageLoaderService extends ImageJService {



    public IOFileFilter getIOFileFilter();

    public String[] getSupportedExtensions();

    Collection<File> getAllImagesFromDirectory(File file);
    
    Collection<File> getAllImagesFromDirecoty(File file, boolean recursive);
    
}
