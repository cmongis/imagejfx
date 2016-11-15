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

import com.google.common.collect.Lists;
import ijfx.core.hash.HashService;
import ijfx.ui.main.ImageJFX;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import mongis.utils.TextFileUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.CanReadFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.OrFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.scijava.Context;
import org.scijava.Priority;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 *
 * @author Cyril Quinton
 */
@Plugin(type = Service.class, priority = Priority.LOW_PRIORITY)
public class DefaultImageLoaderService extends AbstractService implements ImageLoaderService {

    @Parameter
    private Context context;
    @Parameter
    private HashService hashService;

    private final String FORMAT_FILE_NAME = "/supportedFormats.txt";
    public List<String> formats = new ArrayList<>();

    @Override
    public IOFileFilter getIOFileFilter() {
        List<IOFileFilter> suffixFilters = new ArrayList<>();
        for (String ext : getSupportedExtensions()) {
            
            suffixFilters.add(new SuffixFileFilter(ext));
        }
        IOFileFilter suffixFilter = new OrFileFilter(suffixFilters);
        return suffixFilter;
    }

    public static IOFileFilter canReadFilter(IOFileFilter filter) {
        return new AndFileFilter(filter, CanReadFileFilter.CAN_READ);
    }

    public static IOFileFilter getDirectoryFilter() {
        return DirectoryFileFilter.INSTANCE;
    }

    @Override
    public String[] getSupportedExtensions() {
        if (formats.isEmpty()) {
            loadFormats();
        }
        int size = formats.size();
        String[] extensions = new String[size];
        for (int i = 0; i < size; i++) {
            String filterExt = formats.get(i);
            extensions[i] = filterExt.substring(2, filterExt.length());
        }
        return extensions;
    }

    private void loadFormats() {
        try {
            formats.clear();
            String formatsFromFile = TextFileUtils.readFileFromJar(DefaultImageLoaderService.class,FORMAT_FILE_NAME);
            // Old code deleted after bug submission. Waiting for bug correction confirmation.
            // -- DefaultProjectManagerService.readFile(new File(getClass().getResource(FORMAT_FILE_NAME).getPath()));
            
            String[] lines = formatsFromFile.split("\n");
            for (String ext : lines) {
                formats.add("*" + ext);
            }
        } catch (IOException ex) {
            ImageJFX.getLogger().log(Level.SEVERE, "Error when loading the file containing all the possible formats.", ex);
        }
    }

   

    @Override
    public Collection<File> getAllImagesFromDirectory(File file) {
        if(file.isDirectory() == false) return Lists.newArrayList(file);
        return FileUtils.listFiles(file, getSupportedExtensions(), true);
    }

    @Override
    public Collection<File> getAllImagesFromDirecoty(File file, boolean recursive) {
        if(recursive) return getAllImagesFromDirectory(file);
        else {
            final IOFileFilter filter = getIOFileFilter();
            return Stream.of(file.listFiles())
                    .filter(filter::accept)
                    .collect(Collectors.toList());
            
        }
    }

}
