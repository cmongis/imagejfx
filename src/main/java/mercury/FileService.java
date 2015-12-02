/*
 * /*
 *     This file is part of ImageJ FX.
 *
 *     ImageJ FX is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     ImageJ FX is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with ImageJ FX.  If not, see <http://www.gnu.org/licenses/>. 
 *
 * 	Copyright 2015,2016 Cyril MONGIS, Michael Knop
 *
 */
package mercury;

import ijfx.ui.main.ImageJFX;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.stage.FileChooser;
import mercury.core.Deferred;
import mercury.core.AngularMethod;
import mercury.core.JSONParameters;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class FileService {

    @AngularMethod(description = "Opens a dialog allowing the user to select a file", inputDescription = "title,default folder", inputExample = "'Open a example folder',''",sync=true)
    public File openFileDialog(String title, String def) {

        if(title == null) title = "Open folder";
        if(def == null) def = "/";
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        File chosenFile = fileChooser.showOpenDialog(null);

        return chosenFile;

    }
    
   
    
    public String readTextFileSync(String path) {
        try {
            return new String(Files.readAllBytes(Paths.get(path)));
        } catch (IOException ex) {
            ImageJFX.getLogger().log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    @AngularMethod(description = "read a text file and return the input",inputDescription = "file path",inputExample = "{'path':'/path/to/file'}",outputDescription = "text inside the file as string",outputExample = "I have a lot of work today.\nIt was incredible!")
    public void readTextFile(Deferred defered, JSONParameters param) {
        String path  = param.getFirstString();
        if(path == null) {
            defered.parseAndReject("{}");
            return;
        }
        defered.resolve(readTextFileSync(path));
    }
    
   

}
