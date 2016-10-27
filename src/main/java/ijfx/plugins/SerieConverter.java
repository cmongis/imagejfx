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
package ijfx.plugins;

import ijfx.ui.main.ImageJFX;
import io.scif.config.SCIFIOConfig;
import io.scif.img.ImgIOException;
import io.scif.img.ImgOpener;
import io.scif.img.SCIFIOImgPlus;
import io.scif.services.DatasetIOService;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.apache.commons.io.FilenameUtils;
import org.scijava.Context;
import org.scijava.app.StatusService;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

/**
 *
 * @author cyril
 */
@Plugin(type = Command.class,menuPath="Plugins > KnopLab > Serie Separator",description="Separate files containing multiple series into multiple files.")
public class SerieConverter extends ContextCommand {

    @Parameter
    File file;

    @Parameter
    DatasetIOService datasetIOService;

    @Parameter
    Context context;
    
    @Parameter
    StatusService statusService;
    
    @Parameter
    UIService uiService;
    
    @Parameter
    DatasetService datasetService;
    
    
    
    Logger logger = ImageJFX.getLogger();
    
    private final static String STATUS_TEXt = "Saving serie %d/%d";
    
    private final static String SERIE_NAME_SEPARATOR = "_serie_";
    private final static String EXTENSION = ".tif";
    
    String baseName;
    
    @Override
    public void run() {
        
         try {
            final ImgOpener imageOpener = new ImgOpener(context);

            final SCIFIOConfig config = new SCIFIOConfig();
            
            config.imgOpenerSetOpenAllImages(true);
            
            statusService.showStatus("Loading image...");
            
            final List<SCIFIOImgPlus<?>> openImgs = imageOpener.openImgs(file.getAbsolutePath(), config);
            
            Consumer<Integer> progress = i->statusService.showStatus(i, openImgs.size(), String.format(STATUS_TEXt,i+1,openImgs.size()));
            
            for(int i = 0;i!= openImgs.size();i++) {
                
                progress.accept(i);
                
                save((SCIFIOImgPlus)openImgs.get(i), i);
                
                
            }
            
            uiService.showDialog("Convertion finished.");
            
            
            
        } catch (ImgIOException ex) {
           logger.log(Level.SEVERE, null, ex);
           cancel("Error when reading the image");
          
           
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
            cancel("Error when saving images. Perhaps the folder is Read-Only");
        }
    }

    private <T extends RealType<T> & NativeType<T>> void save(SCIFIOImgPlus<T> imgPlus, int number) throws IOException {
            
            
        Dataset dataset = datasetService.create(imgPlus);
        
        datasetIOService.save(dataset, new File(file.getParentFile(),getFileName(number)).getAbsolutePath());
       

    }

    private String getFileName(int i) {
        if(baseName == null) {
            baseName = FilenameUtils.getBaseName(file.getName());
            
        }
        return new StringBuilder()
                .append(baseName)
                .append(SERIE_NAME_SEPARATOR)
                .append(i)
                .append(EXTENSION)
                .toString();
    }
}
