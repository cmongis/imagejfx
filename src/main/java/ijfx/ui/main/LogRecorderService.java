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
package ijfx.ui.main;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;


/**
 *
 * @author Cyril MONGIS, 2015
 */
public class LogRecorderService {

    ByteArrayOutputStream baos = new ByteArrayOutputStream(1000000);
    
    private static LogRecorderService logRecorder;

    File currentLog = new File("log-current.txt");
    File lastLog = new File("log-last.txt");
    
    Logger logger;
    
    FileHandler fileHandler;
    
    private LogRecorderService() {
        
        logger = ImageJFX.getLogger();
        try {
            
            Handler handler = new StreamHandler(baos, new SimpleFormatter());
            handler.setLevel(Level.ALL);
            logger.addHandler(handler);
            
            if(lastLog.exists()) {
                lastLog.delete();
            }
            
            if(currentLog.exists()) {
                currentLog.renameTo(lastLog);
            }
            
            

           fileHandler = new FileHandler(currentLog.getAbsolutePath());
            fileHandler.setFormatter(new SimpleFormatter());
            fileHandler.setLevel(Level.ALL);
            
            fileHandler.flush();
            
            logger.addHandler(fileHandler);
            
            
            
            
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        logger.setLevel(Level.ALL);

    }

    public static LogRecorderService getInstance() {
        if (logRecorder == null) {
            logRecorder = new LogRecorderService();
        }

        return logRecorder;
    }

    public String getLog() {
       fileHandler.flush();
        try {
            return new String(Files.readAllBytes(Paths.get(currentLog.getAbsolutePath())));
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return "Log couldn't be read :-(";
    }

}
