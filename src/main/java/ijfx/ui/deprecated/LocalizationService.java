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
package ijfx.ui.deprecated;

import ijfx.ui.UiConfiguration;
import ijfx.ui.main.ImageJFX;
import ijfx.service.ui.ConfigurationService;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class LocalizationService extends HashMap<String,String> {
      
    
     
     protected static LocalizationService instance;
     
     
     public static LocalizationService getInstance() {
         if(instance == null) {
             instance = new LocalizationService();
         }
         
         return instance;
     }
     
     public static String PREF_DOMAIN = "WidgetLocalization";
     
     
     
     public String getDefaultLocalization(Object obj) {
         try {
         return obj.getClass().getAnnotation(UiConfiguration.class).localization();
         }
         catch(NullPointerException e) {
             Logger.
                     getLogger(LocalizationService.class.getName())
                     .warning(String.format("%s has no set localization !",obj.getClass().getName()));
             return "";
         }
     }
     
     
     public String getLocalization(Object obj) {
         try {
             UiConfiguration annotation = obj.getClass().getAnnotation(UiConfiguration.class);
             return getPreferences().get(annotation.id(),getDefaultLocalization(obj));
         }
         catch(NullPointerException e) {
             ImageJFX.getLogger()
                     .warning("Couldn't find localization for "+obj.getClass().getName());
             return null;
         }
     }
     
     
     public Preferences getPreferences() {
         return ConfigurationService
                 .getInstance()
                 .getPreferences(PREF_DOMAIN);
     }
     
     private LocalizationService() {
          
     }
     
     
    
}
