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
package ijfx.core.project;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import javafx.concurrent.Task;
import net.imagej.ImageJService;
import org.apache.commons.io.filefilter.IOFileFilter;

/**
 *
 * @author Cyril Quinton
 */
public interface ImageLoaderService extends FormatProvider,ImageJService {

    Task loadImageFromFile(File imageFile, Project project) throws IOException;

    Task loadImageFromFile(List<File> imageFiles, Project project) throws IOException;

    /**
     * Loads every image contained in a directory and its subdirectories.
     *
     * @param rootDirectory the directory containing image files.
     * @param project the project object where the new planes are added.
     */
    Task loadImageFromDirectory(File rootDirectory, Project project) throws IOException;

    Task<FileReferenceStatus> checkFileReferenceTask(Project project);

    boolean fileAlreadyInDB(String id, Project project);

    public IOFileFilter getIOFileFilter();

    public String[] getSupportedExtensions();

    Collection<File> getAllImagesFromDirectory(File file);
    
}
