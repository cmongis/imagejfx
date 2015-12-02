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

import ijfx.core.project.imageDBService.PlaneDB;
import java.util.regex.Pattern;
import mongis.utils.ConditionList;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class SimpleTagSelector implements Selector{

    
    private String[] tags;
    
    final static private String separator = ",";   
    final static private Pattern validPattern = Pattern.compile("([^=]+,)*(.*)");
    
    
    public SimpleTagSelector(String queryString) {
        parse(queryString);
    }
    
    @Override
    public void parse(String queryString) {
        tags = queryString.split(separator);
    }

    @Override
    public String getQueryString() {
        return String.join(separator, tags);
    }

    @Override
    public boolean matches(PlaneDB planeDB, String metadataSetName) {
        
        //boolean[] results = new boolean[tags.length];
        
        ConditionList results = new ConditionList(tags.length);
        
        for(int i = 0; i!= tags.length;i++) {
            String tag1 = tags[i].trim();
            boolean containsTag = false;
            for(String tag2 : planeDB.getTags()) {
                if(tag1.equals(tag2)) {
                    containsTag = true;
                    break;
                }
            }
            
            results.add(containsTag);
            
        }
        
        return results.isAllTrue();
        
    }
    
    public static boolean canParse(String unparsedQuery) {
        return validPattern.matcher(unparsedQuery).matches();
    }
    
    
}
