/*
    This file is part of ImageJ FX.

    ImageJ FX is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    ImageJ FX is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with ImageJ FX.  If not, see <http://www.gnu.org/licenses/>. 
    
     Copyright 2015,2016 Cyril MONGIS, Michael Knop
	
 */
package ijfx.ui.explorer;

import ijfx.service.batch.SegmentedObject;
import ijfx.ui.widgets.FileExplorableWrapper;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import org.scijava.Context;
import org.scijava.io.RecentFileService;
import org.scijava.plugin.Parameter;

/**
 *
 * @author cyril
 */
public class RecentFileFolder implements Folder{

    private final static String NAME = "Recent Files";
    
    private final ObjectProperty<Task> currentTask = new SimpleObjectProperty<>();
    
    @Parameter
    RecentFileService recentFileService;
    
    private List<File> fileList = new ArrayList<>();
    
    
    public RecentFileFolder(Context context) {
        context.inject(this);
        
        
        
    }
    
    @Override
    public String getName() {
       return NAME;
    }

    @Override
    public void setName(String name) {
       
    }

    @Override
    public File getDirectory() {
        return null;
    }

    @Override
    public List<Explorable> getFileList() {
       return recentFileService.getRecentFiles()
               .stream()
               .map(path->new FileExplorableWrapper(new File(path)))
               .collect(Collectors.toList());
    }

    @Override
    public List<Explorable> getPlaneList() {
        return new ArrayList<>();
    }

    @Override
    public List<Explorable> getObjectList() {
       return new ArrayList<>();
    }
    @Override
    public Property<Task> currentTaskProperty() {
       return currentTask;
    }

    @Override
    public void addObjects(List<SegmentedObject> objects) {
      
    }

    @Override
    public boolean isFilePartOf(File f) {
       return recentFileService
               .getRecentFiles()
               .stream()
               .filter(path->f.getAbsolutePath().equals(path))
               .count() > 0;
    }
    
}
