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
package ijfx.ui.widgets;

import ijfx.core.metadata.FileSizeMetaData;
import ijfx.core.metadata.MetaData;
import ijfx.ui.explorer.AbstractExplorable;
import java.io.File;
import javafx.scene.image.Image;
import net.imagej.Dataset;

/**
 *
 * @author cyril
 */
public class FileExplorableWrapper extends AbstractExplorable{

    
    private final File file;
    
    
    
    public FileExplorableWrapper(File f) {
        super();
        this.file = f;
        
        getMetaDataSet().putGeneric(MetaData.NAME, f.getName());
        getMetaDataSet().put(new FileSizeMetaData(f.length()));
    }
    
    @Override
    public String getTitle() {
        return file.getName();
    }

    @Override
    public String getSubtitle() {
        return getMetaDataSet().get(MetaData.FILE_SIZE).toString();
    }

    @Override
    public String getInformations() {
        return "";
    }

    @Override
    public Image getImage() {
        return null;
    }

    @Override
    public void open() throws Exception {
        
    }

    @Override
    public Dataset getDataset() {
        return null;
    }
    
}
