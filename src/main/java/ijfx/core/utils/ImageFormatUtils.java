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
package ijfx.core.utils;

import ijfx.ui.main.ImageJFX;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import mongis.utils.TextFileUtils;

/**
 *
 * @author cyril
 */
public class ImageFormatUtils {
     
    private static final String SUPPORTED_EXTENSION_FILE = "/ijfx/core/project/supportedFormats.txt";
    
    private static String SUPPORTED_EXTENSION_FILE_CONTENT;
    
    private static String getSupportedExtensionFileContent() {
        if(SUPPORTED_EXTENSION_FILE_CONTENT == null) {
            try {
                SUPPORTED_EXTENSION_FILE_CONTENT = TextFileUtils.readFileFromJar(SUPPORTED_EXTENSION_FILE);
            } catch (IOException ex) {
                ImageJFX.getLogger().log(Level.SEVERE, "Couldn't load supported format file", ex);
                SUPPORTED_EXTENSION_FILE_CONTENT = "";
            }
        }
        return SUPPORTED_EXTENSION_FILE_CONTENT;
    }
    
    public static String[] getSupportedExtensions() {
        
        return getSupportedExtensionFileContent().split("\n");
        
        
    }
    
    public static String[] getSupportedExtensionsWithoutDot() {
        String[] supportedExtensions = getSupportedExtensions();
        String[] extensionsWithoutDot = new String[supportedExtensions.length];
        int i = 0;
        for(String extension : supportedExtensions) {
            extensionsWithoutDot[i++] = extension.substring(1, extension.length());
        }
        return extensionsWithoutDot;
    }
    
    
}
