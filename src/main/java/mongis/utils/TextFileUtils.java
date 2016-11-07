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

import com.google.common.io.Resources;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class TextFileUtils {

    public static String readFileFromJar(String url) throws IOException {
        return Resources.toString(TextFileUtils.class.getResource(url), Charset.forName("UTF-8"));
    }

    public static String readFileFromJar(Class<?> clazz, String url) throws IOException {
        return readFileFromJar(url, clazz);
    }

    public static String readFileFromJar(String url, Class<?> clazz) throws IOException {
        
        return Resources.toString(clazz.getResource(url), Charset.forName("UTF-8"));
    }

    public static void writeTextFile(File file, String text) throws IOException {
       
        Writer out = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file), "UTF8"));

       out.append(text);

        out.flush();
        out.close();

    }
}
