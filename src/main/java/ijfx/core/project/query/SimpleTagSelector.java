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
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import mongis.utils.ConditionList;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = Selector.class, priority = Priority.VERY_LOW_PRIORITY)
public class SimpleTagSelector implements Selector{

    
    private String[] tags;
    
    final static  public String TAG_SEPARATOR = ",";   
    final static  public Pattern TAG_QUERY_PATTERN = Pattern.compile("tagged with\\:?\\s*(.*)");
    
    
    final private String PHRASE = "tagged with *%s*";
    
    final private String PHRASE_DELIMITER = "*, *";
    
    
    String queryString;
    
    public SimpleTagSelector() {
        
    }
    
    public SimpleTagSelector(String queryString) {
        parse(queryString);
    }
    
    @Override
    public void parse(String queryString) {
        
        this.queryString = queryString;
        Matcher m = TAG_QUERY_PATTERN.matcher(queryString);
        if(m.matches()) {
            System.out.println("tags : "+m.group(1));
            tags = m.group(1).split(TAG_SEPARATOR);
        }
      
    }

    @Override
    public String getQueryString() {
        return String.join(TAG_SEPARATOR, tags);
    }

    @Override
    public boolean matches(PlaneDB planeDB, String metadataSetName) {
        
        //boolean[] results = new boolean[tags.length];
       
            parse(queryString);
        
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
    
    public boolean canParse(String unparsedQuery) {
        return TAG_QUERY_PATTERN.matcher(unparsedQuery).matches();
    }

    @Override
    public String phraseMe() {
        
        
        return String.format(PHRASE, String.join(PHRASE_DELIMITER,tags));
    }
    
    
}
