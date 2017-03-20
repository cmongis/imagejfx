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

import java.io.File;
import java.text.DecimalFormat;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class FileUtils {

    public static String readableFileSize(long size) {
        if (size <= 0) {
            return "0";
        }
        final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static String changeExtensionTo(String path, String extension) {

        if(extension.startsWith(DOT)) {
            extension = extension.substring(1);
        }
        
        return new StringBuilder()
                .append(FilenameUtils.removeExtension(path))
                .append(".")
                .append(extension)
                .toString();

    }

    private static String DOT = ".";

    /**
     *
     * @param input file
     * @param extension with or without dot
     * @return
     */
    public static File ensureExtension(File file, String extension) {

        if (file.getName().endsWith(extension)) {
            return file;
        }

        if (!extension.startsWith(DOT)) {
            extension = DOT + extension;
        }

        return new File(changeExtensionTo(file.getAbsolutePath(), extension));

    }
}
