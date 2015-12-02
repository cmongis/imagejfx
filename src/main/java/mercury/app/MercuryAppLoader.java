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
package mercury.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import ijfx.ui.main.ImageJFX;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class MercuryAppLoader {

    public static String APP_DEFINITION_FILENAME = "app.json";
    public static String APP_PACKAGE_DEF_FILENAME = "apps.json";

    Logger logger = ImageJFX.getLogger();
    
    ObjectMapper mapper;

    HashMap<String, MercuryApp> appMap = new HashMap<>();

    public MercuryAppLoader() {
        mapper = new ObjectMapper();
    }

    public MercuryAppLoader scanDirectory(String dir) {
        scanDirectory(new File(dir));
        return this;
    }

    public MercuryAppLoader scanDirectory(File dir) {
        if(dir.exists() == false) {
            
            return this;
        }
        Arrays.asList(dir.listFiles()).stream().parallel().forEach(appDirectory -> {
            logger.info("Scanning " + appDirectory.getName() + "...");
            loadApp(appDirectory).forEach(app -> {
               logger.info("App found : " + app.getName());
                appMap.put(app.getId(), app);
            });

        });

        return this;
    }

    public MercuryApp getApp(String appId) { 
        return appMap.get(appId);
    }

    public ArrayList<MercuryApp> loadApp(File appFolder) {
        File appPropertyFile = new File(appFolder, APP_DEFINITION_FILENAME);
        ArrayList<MercuryApp> foundApps = new ArrayList<>();
        if (appPropertyFile.exists()) {
            try {
               logger.info("Trying to read it...");
                MercuryLocalApp app = mapper.readValue(appPropertyFile, MercuryLocalApp.class);
                app.setAppPath(appFolder.getAbsolutePath());
                foundApps.add(app);
            } catch (IOException ex) {
                logger.warning("Couldn't read app : " + appFolder.getName());
                ex.printStackTrace();
                logger.log(Level.SEVERE, null, ex);
            }
        }
        File appPackageFile = new File(appFolder, APP_PACKAGE_DEF_FILENAME);
        if (appPackageFile.exists()) {
            try {
                MercuryLocalAppPackage appPackage = mapper.readValue(appPackageFile, MercuryLocalAppPackage.class);

                appPackage.getAppList().forEach(app -> {
                    app.setAppPath(appFolder.getAbsolutePath());
                    foundApps.add(app);
                    
                    foundApps.forEach(fapp->System.out.println(fapp.getAppURL()));
                });

            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
        return foundApps;
    }

    public static void main(String... args) {
        new MercuryAppLoader().scanDirectory("/home/cyril/Copy/Work/IJFX/ijfx-web/");
    }

    Collection<MercuryApp> getAppList() {
        return appMap.values();
    }

    public boolean appExists(String app) {
        
        return appMap.containsKey(app);
    }

}
