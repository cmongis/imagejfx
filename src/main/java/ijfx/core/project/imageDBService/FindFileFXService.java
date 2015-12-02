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

import ijfx.core.hash.HashService;
import ijfx.core.project.ImageLoaderService;
import ijfx.core.project.DefaultImageLoaderService;
import ijfx.core.project.ProjectManagerService;
import java.io.File;
import java.io.FileFilter;
import java.util.Iterator;
import java.util.ResourceBundle;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.scijava.Context;
/*
 * look recursively (or not) for an image that has a hash value
 * equal to the object id in the specified directory tree
 * If the image is found, the absolute path is updated in the image reference
 * object
 *
 * @author Cyril Quinton
 */

public class FindFileFXService extends Service<File> {

    private final ImageReference imageReference;
    private File rootDirectory;
    private final ResourceBundle rb;
    private String currentDirectory;
    private boolean recursive;
    private String fileName;
    private final ImageLoaderService formatProvider;
    private final HashService hashService;

    public FindFileFXService(ImageReference imagerReference, Context context) {
        this.imageReference = imagerReference;
        this.hashService = context.getService(HashService.class);
        recursive = false;
        rb = ProjectManagerService.rb;
        formatProvider = context.getService(DefaultImageLoaderService.class);
    }

    public void searchRecursively(boolean recursive) {
        this.recursive = recursive;
    }

    public void setFileName(String name) {
        this.fileName = name;
    }

    public void setRootDirectory(File root) {
        this.rootDirectory = root;
    }

    private IOFileFilter createFileFilter() {

        if (fileName == null) {
            return formatProvider.getIOFileFilter();
        } else {
            //return new NameFileFilter(fileName);
            return new IOFileFilter() {

                @Override
                public boolean accept(File file) {
                    return file.getName().equals(fileName);
                }

                @Override
                public boolean accept(File file, String string) {
                    return false;
                }
            };
        }

    }

    private IOFileFilter createDirFilter() {
        if (recursive) {
            return TrueFileFilter.TRUE;
        } else {
            return FalseFileFilter.FALSE;
        }
    }

    @Override
    protected Task<File> createTask() {
        
        Task<File> task = new Task<File>() {

            @Override
            protected File call() throws Exception {
                setDirectory(currentDirectory);
                File file;
                String directory = rootDirectory.getAbsolutePath();
                Iterator<File> it = FileUtils.iterateFiles(rootDirectory, formatProvider.getSupportedExtensions(), recursive);
                while (it.hasNext()) {

                    if (isCancelled()) {
                        break;
                    }
                    file = it.next();
                    if ((fileName != null && file.getName().equals(fileName) || fileName == null)) {
                        if (directory == null || !file.getParentFile().getName().equals(directory)) {
                            setDirectory(file.getParentFile().getName());
                        }
                        String id = hashService.getHash(file);
                        if (imageReference.getId().equals(id)) {
                            updateMessage(rb.getString("found"));
                            return file;
                        }
                    }

                }

                updateMessage(rb.getString("notFound"));
                return null;

            }

            private void setDirectory(String path) {
                currentDirectory = path;
                updateMessage(String.format("%s %s", rb.getString("searchingIn"), currentDirectory));
            }

            @Override
            protected void succeeded() {
                updateProgress(1, 1);
            }

            @Override
            protected void cancelled() {
                updateProgress(0, 1);
            }
        };

        return task;

    }

}
