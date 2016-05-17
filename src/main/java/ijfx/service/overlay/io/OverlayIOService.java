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
package ijfx.service.overlay.io;

import ijfx.service.IjfxService;
import ijfx.ui.utils.NamingUtils;
import java.io.File;
import java.io.IOException;
import java.util.List;
import net.imagej.Dataset;
import net.imagej.display.ImageDisplay;
import net.imagej.overlay.Overlay;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author cyril
 */
public interface OverlayIOService extends IjfxService{
    
    public static final String OVERLAY_FILE_EXTENSION = ".ovl.json";
   
    
    /**
     * Takes a list of overlay and save it inside a file
     * The file is in json format but is called usually .ovl.json
     * @param overlay
     * @param ovlFile
     */
    public void saveOverlays(List<Overlay> overlay, File ovlFile) throws IOException;
    
    default void saveOverlaysNextToFile(List<Overlay> overlay, File imageFile) throws IOException{
        String filename = FilenameUtils.getBaseName(imageFile.getName()) + OVERLAY_FILE_EXTENSION;
        
        saveOverlays(overlay, new File(imageFile.getParentFile(),filename));
        
    }
    
    /**
     * Save the a list of overlay next to the file associated to the dataset.
     * 
     * Eg. the dataset should the source 
     * @param overlay
     * @param dataset 
     * @throws ijfx.service.overlay.NoSourceFileException if the dataset has no source file associated
     */
    public void saveOverlays(List<Overlay> overlay, Dataset dataset) throws NoSourceFileException,IOException;

    /** 
     * Save the overlays associated to the image display
     * next to the dataset origin file as ovl format
     * @param display
     */
    public void saveOverlays(ImageDisplay display) throws NoSourceFileException,IOException;
    
    public default File getOverlayFileFromImageFile(File imageFile) {
        String nameWithoutExtension = imageFile.getName().replaceAll("\\.[\\w\\d]+$", "");
        return new File(imageFile.getParent(), nameWithoutExtension + OVERLAY_FILE_EXTENSION);
    }
    
    public default File getImageFileFromOverlayFile(File overlayFile) {
        String jsonFileName = overlayFile.getName();
        String basename = FilenameUtils.getBaseName(jsonFileName);
        
        File[] listFiles = overlayFile.getParentFile().listFiles(f->f.getName().startsWith(basename) && f.getName().equals(jsonFileName) == false);
        if(listFiles.length < 1) {
            return null;
        }
        else {
            return listFiles[0];
        }
    }
    
    public List<Overlay> loadOverlays(File overlayJsonFile);
    
}
