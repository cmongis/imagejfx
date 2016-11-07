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
package ijfx.ui.utils;

import java.io.File;
import java.util.regex.Pattern;
import javafx.util.Pair;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class NamingUtils {
    
    private static Pattern filenamePattern = Pattern.compile("(.*)(\\.[\\w\\d]+$)");
    
    private static Pattern numerotingPattern = Pattern.compile("(.*)_(\\d+)");
   
    
    
    
   
    
    public Pair<String,String> separateExtension(String filename) {
        
        String extension = FilenameUtils.getExtension(filename);
        String basename = FilenameUtils.getBaseName(filename);
        return new Pair<>(basename,extension);
    }
    
    public static File replaceWithExtension(File originalFile,String extension) {
        String basename = FilenameUtils.getBaseName(originalFile.getName());
        return new File(originalFile.getParentFile(),basename+extension);
    }
    
}
