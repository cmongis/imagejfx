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
package ijfx.core.project.query;

import ijfx.core.metadata.MetaData;
import ijfx.core.project.imageDBService.PlaneDB;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.beans.property.ReadOnlyMapProperty;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = Selector.class)
public class ContainStringSelector implements Selector{

    String query;
    
    String metadataKey;
    String keyword;
    
    public static final String PHRASE = "*%s* contains the word *%s*";
    public static final Pattern PATTERN = Pattern.compile("([\\d\\s\\w]) contains (.*)");
    
    @Override
    public void parse(String queryString) {
        canParse(queryString);
    }

    @Override
    public boolean canParse(String queryString) {
        Matcher m = PATTERN.matcher(queryString);
        
        if(m.matches()) {
            metadataKey = m.group(1);
            keyword = m.group(2);
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public String getQueryString() {
        return query;
    }

    @Override
    public boolean matches(PlaneDB planeDB, String metadataSetName) {
        ReadOnlyMapProperty<String, MetaData> metaDataSetProperty = planeDB.getMetaDataSetProperty(metadataSetName);
        String keyName = Selector.findKey(metaDataSetProperty, metadataKey);
        
        
        if(keyName == null) return false;
        
        return metaDataSetProperty.get(keyName).getStringValue().contains(keyword.toLowerCase());
        
    }

    @Override
    public String phraseMe() {
        return String.format(PHRASE, metadataKey,keyword);
    }
    
}
