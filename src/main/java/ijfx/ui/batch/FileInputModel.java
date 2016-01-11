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
package ijfx.ui.batch;

import java.io.File;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author cyril
 */
public class FileInputModel {
    
    private final File file;

    private final BooleanProperty marked = new SimpleBooleanProperty();
    
    public FileInputModel(File file) {
        this.file = file;
        
    }
    
    public BooleanProperty markedProperty() {
        return marked;
    }
    
    public File getFile() {
        return file;
    }
    
    @Override
    public boolean equals(Object object) {
        if(object instanceof FileInputModel) {
            FileInputModel m = (FileInputModel)object;
            return getFile().equals(m.getFile().equals(object));
        }
        else return false;
    }
    
}
