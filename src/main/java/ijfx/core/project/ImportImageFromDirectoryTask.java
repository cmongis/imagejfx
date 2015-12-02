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

import ijfx.core.project.command.Command;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import org.apache.commons.io.FileUtils;
import org.scijava.Context;

/**
 *
 * @author Cyril Quinton
 */
public class ImportImageFromDirectoryTask extends Task<Command> {

    private final Project project;
    private final File rootDir;
    private final Context context;
    private final ResourceBundle rb;
    private final ImageLoaderService imageLoader;

    public ImportImageFromDirectoryTask(File rootDir, Project project, Context context) {
        this.project = project;
        this.rootDir = rootDir;
        this.context = context;
        rb = ProjectManagerService.rb;
        imageLoader = context.getService(DefaultImageLoaderService.class);

    }

    @Override
    protected Command call() throws Exception {
        List<File> imageFiles = new ArrayList<>();
        //Iterator<File> it = FileUtils.iterateFiles(rootDirectory, getIOFileFilter(),getDirectoryFilter());
        Iterator<File> it = FileUtils.iterateFiles(rootDir, imageLoader.getSupportedExtensions(), true);
        updateMessage(rb.getString("searchingForImageFile"));
        while (it.hasNext()) {
            if (isCancelled()) {
                
                return null;     
                
            }
            File file = it.next();
            //updateMessage(rb.getString("found") + " " + file.getName() );
            imageFiles.add(file);
        }
        
        // creating an importation task
        Task<Command> task = new ImportImageFileTask(imageFiles, project, context);

        // linking the property to this task
        task.messageProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                updateMessage(newValue);
            }
        });
        task.progressProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                updateProgress(newValue.doubleValue(), 1);
            }
        });
        
        // running the task ??? (perhaps perhaps it should be run in an other thread and then wait)
        task.run();
        
        // returning the result of the task
        return task.get();
    }

}
