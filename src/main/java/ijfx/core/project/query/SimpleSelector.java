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
package ijfx.core.project.query;

import ijfx.core.project.query.Selector;
import ijfx.core.project.imageDBService.PlaneDB;
import ijfx.ui.main.ImageJFX;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class SimpleSelector implements Selector{
    
    String queryString;
    
    Logger logger = ImageJFX.getLogger();
    
    public static Pattern SIMPLE_QUERY_PATTERN = Pattern.compile("^\"(.*)\"\\s+=\\s+[\\/\\\"](.*)[\\/\\\"]$");
    
    String keyString;
    
    String valueString;
    
    public SimpleSelector() {
        
    }
    
    public SimpleSelector(String queryString) {
        parse(queryString);
    }
    
    public SimpleSelector(String key, String value) {
        parse(String.format("\"%s\" = /%s/",key,value));
        keyString = key;
        valueString = value;
    }
    
    public void parse(String queryString) {
        this.queryString = queryString;
    }
    
    public static boolean canParse(String query) {
        return SIMPLE_QUERY_PATTERN.matcher(query).matches();
    }

    @Override
    public String getQueryString() {
        return queryString;
    }

    @Override
    public boolean matches(PlaneDB planeDB, String metadataSetName) {
        
        
        
        Matcher m = SIMPLE_QUERY_PATTERN.matcher(queryString);
        
        if(m.matches()) {
            
                
              String keyName = keyString == null ? m.group(1) : keyString;
              String valueName = valueString == null ? m.group(2) : valueString;
              
              try {
                String metadataValue = planeDB.getMetaDataSetProperty(metadataSetName).get(keyName).getStringValue();
              } catch(NullPointerException e) {
                  return false;
              }
              
              Pattern p = Pattern.compile(valueName);
              try {
              return p.matcher(planeDB.getMetaDataSetProperty(metadataSetName).get(keyName).getStringValue()).matches();
              }
              catch(Exception e) {
                  logger.warning("Error when matching the PlaneDB");
              }
        }
        
      
        return false;
        
    }
    
    
}
