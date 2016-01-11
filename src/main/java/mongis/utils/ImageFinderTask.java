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
package mongis.utils;

import ijfx.core.utils.ImageFormatUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.concurrent.Task;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author cyril
 */
public class ImageFinderTask extends Task<List<File>> {
    
    private final File root;

    public ImageFinderTask(File root) {
        this.root = root;
    }

    @Override
    protected List<File> call() throws Exception {
        ArrayList<File> files = new ArrayList<>();
        
        Iterator<File> iterateFiles = FileUtils.iterateFiles(root, ImageFormatUtils.getSupportedExtensionsWithoutDot(), true);
       
        while (iterateFiles.hasNext()) {
            
            File f = iterateFiles.next();
           
            
            files.add(f);
            updateMessage("File found :" + files.size());
        }
        return files;
    }
    
}
