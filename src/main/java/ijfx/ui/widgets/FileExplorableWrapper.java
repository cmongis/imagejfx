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
import ijfx.plugins.OpenImageFX;
import ijfx.service.log.DefaultLoggingService;
import ijfx.service.thumb.ThumbService;
import ijfx.ui.explorer.AbstractExplorable;
import ijfx.ui.main.ImageJFX;
import io.scif.services.DatasetIOService;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.image.Image;
import net.imagej.Dataset;
import org.scijava.Context;
import org.scijava.command.CommandModule;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class FileExplorableWrapper extends AbstractExplorable{

    
    private final File file;
    
    @Parameter
    DatasetIOService datasetIoService;
    
    @Parameter
    ThumbService thumbService;
    
    @Parameter
    DefaultLoggingService logService;
    
    @Parameter
            CommandService commandService;
    
    Dataset dataset;
    
    Logger logger = ImageJFX.getLogger();
    
    public FileExplorableWrapper(Context context, File f) {
        this(f);
        context.inject(this);
    }
    
    public FileExplorableWrapper(File f) {
        super();
        this.file = f;
        
        getMetaDataSet().putGeneric(MetaData.NAME, f.getName());
        getMetaDataSet().put(new FileSizeMetaData(f.length()));
        getMetaDataSet().putGeneric(MetaData.ABSOLUTE_PATH,f.getAbsolutePath());
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
        if (file.getName().endsWith("png") || file.getName().endsWith("jpg")) {
                return new Image(file.getAbsolutePath());
            } else {
                try {
                    return thumbService.getThumb(file, 0, null, 100, 100);
                } catch (Exception ex) {
                    logService.warn(ex, "Couldn't load file %s", file.getAbsolutePath());
                }
            }
            return null;
    }

    @Override
    public void open() throws Exception {
          Future<CommandModule> run = commandService.run(OpenImageFX.class, true, "file", file,"imageId",-1);
            run.get();
    }

    @Override
    public Dataset getDataset() {
        try {
            if(dataset == null) {
            dataset =  datasetIoService.open(file.getAbsolutePath());
            }
        } catch (IOException ex) {
           ImageJFX.getLogger().log(Level.SEVERE, null, ex);
        }
        catch(NullPointerException ex) {
            ImageJFX.getLogger().log(Level.SEVERE, "The context was not initalized", ex);
        }
        return dataset;
    }

    @Override
    public File getFile() {
        return file;
    }
    
    
    public void dispose() {
        dataset = null;
    }
    
}
