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

import ijfx.bridge.ImageJContainer;
import ijfx.core.imagedb.ImageRecord;
import ijfx.core.metadata.MetaDataSet;
import ijfx.service.thumb.ThumbService;
import ijfx.ui.activity.ActivityService;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.image.Image;
import mongis.utils.FileUtils;
import org.scijava.Context;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugins.commands.io.OpenFile;

/**
 * Wrapper class that transform an ImageRecord to an Explorable
 * @author cyril
 */
public class ImageRecordIconizer implements Explorable {

    private final ImageRecord imageRecord;

    @Parameter
    ThumbService thumbService;
    
    @Parameter
    CommandService commandService;
    
    @Parameter
    ActivityService activityService;
    
    private BooleanProperty selectedProperty;
    
    boolean selected = false;
    
    public ImageRecordIconizer(Context context, ImageRecord imageRecord) {
        context.inject(this);
        this.imageRecord = imageRecord;
        
    } 
    @Override
    public String getTitle() {
        return imageRecord.getFile().getName();
    }

    @Override
    public String getSubtitle() {
        return FileUtils.readableFileSize(imageRecord.getFile().length());
    }

    @Override
    public String getInformations() {
        return "";
    }

    @Override
    public Image getImage() {
        try {
            return thumbService.getThumb(imageRecord.getFile(), 0, 100, 100);
        } catch (IOException ex) {
            Logger.getLogger(ImageRecordIconizer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public void open() {
        
        HashMap<String,Object> inputs = new HashMap<>();
        inputs.put("inputFile",imageRecord.getFile());
        
        commandService.run(OpenFile.class,true,inputs);
        
        activityService.openByType(ImageJContainer.class);
    }

    @Override
    public MetaDataSet getMetaDataSet() {
        
        return imageRecord.getMetaDataSet();
    }

    public ImageRecord getImageRecord() {
        return imageRecord;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    @Override
    public BooleanProperty selectedProperty() {
        if(selectedProperty == null) {
           selectedProperty = new SimpleBooleanProperty(this, "selected", selected);
        }
        return  selectedProperty;
    }
    
    
    
}
