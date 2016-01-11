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

import ijfx.core.hash.HashService;
import ijfx.core.project.imageDBService.ImageReference;
import ijfx.core.project.imageDBService.PlaneDB;
import ijfx.ui.main.ImageJFX;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import mongis.utils.TextFileUtils;
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

    private final static String FORMAT_FILE_NAME = "supportedFormats.txt";
    public List<String> formats = new ArrayList<>();

    public IOFileFilter getIOFileFilter() {
        List<IOFileFilter> suffixFilters = new ArrayList<>();
        for (String ext : formats) {
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
            String formatsFromFile = TextFileUtils.readFileFromJar(this, FORMAT_FILE_NAME);
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
    public Task loadImageFromFile(File imageFile, Project project) throws IOException {
        return loadImageFromFile(Arrays.asList(imageFile), project);
    }

    private Task loadImageFromFileCommands(List<File> imageFiles, Project project) throws IOException {
        return new ImportImageFileTask(imageFiles, project, context);
    }

    @Override
    public Task loadImageFromFile(List<File> imageFiles, Project project) throws IOException {
        return loadImageFromFileCommands(imageFiles, project);

    }

    @Override
    public boolean fileAlreadyInDB(String id, Project project) {
        for (PlaneDB planeDB : project.getImages()) {
            if (planeDB.getImageReference().getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> getAcceptedFormat() {
        if (formats.isEmpty()) {
            loadFormats();
        }
        return formats;
    }

    @Override
    public Task loadImageFromDirectory(File rootDirectory, Project project) throws IOException {
        return new ImportImageFromDirectoryTask(rootDirectory, project, context);
    }

    @Override
    public Task<FileReferenceStatus> checkFileReferenceTask(Project project) {
        Task<FileReferenceStatus> task = new Task<FileReferenceStatus>() {
            private FileReferenceStatus fileReferenceStatus = new DefaultFileReferenceStatus(project);

            @Override
            protected FileReferenceStatus call() throws Exception {
                updateMessage(ProjectManagerService.rb.getString("checkingFileReference"));
                List<PlaneDB> planeList = project.getImages();
                int totalNumber = planeList.size();
                HashSet<ImageReference> hashSet = new HashSet(planeList.size());
                //iterating on every unique image reference
                for (int i = 0; i < planeList.size(); i++) {
                    if (!isCancelled()) {
                        updateProgress(i, totalNumber);
                        PlaneDB plane = planeList.get(i);
                        ImageReference ir = plane.getImageReference();
                        if (!hashSet.contains(ir)) {
                            hashSet.add(ir);
                            if (filePathExist(ir.getPath())) {
                                if (hasChanged(new File(ir.getPath()), ir.getId(), hashService)) {
                                    fileReferenceStatus.getIncorrectIDList().add(ir);
                                }

                            } else {
                                fileReferenceStatus.getWrongPathList().add(ir);
                            }
                        }
                    }

                }
                if (fileReferenceStatus.isOK()) {
                    updateMessage(ProjectManagerService.rb.getString("fileReferenceOK"));
                    Thread.sleep(2000);
                }
                return fileReferenceStatus;

            }

            private boolean filePathExist(String path) {
                return new File(path).exists();
            }

            private boolean hasChanged(File file, String id, HashService hashService) throws IOException {
                String calculateId = hashService.getHash(file);
                return !calculateId.equals(id);
            }
        };
        return task;
    }

}
