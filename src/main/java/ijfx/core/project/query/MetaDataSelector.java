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

import ijfx.core.metadata.MetaData;
import ijfx.core.project.imageDBService.PlaneDB;
import ijfx.ui.main.ImageJFX;
import java.util.Map;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Cyril MONGIS, 2015
 */
@Plugin(type = Selector.class, priority = Priority.LOW_PRIORITY)
public class MetaDataSelector implements Selector {

    String queryString;

    Logger logger = ImageJFX.getLogger();

    //public static Pattern SIMPLE_QUERY_PATTERN = Pattern.compile("^\"(.*)\"\\s+=\\s+[\\/\\\"](.*)[\\/\\\"]$");
    public static Pattern SIMPLE_QUERY_PATTERN = Pattern.compile("(.*)=(.*)");
    String keyString;

    String valueString;

    private static final String PHRASE = "the metadata *%s* has the exact value *%s*";
    
    
    public MetaDataSelector() {

    }

    public MetaDataSelector(String queryString) {
        parse(queryString);
    }

    public MetaDataSelector(String key, String value) {
        parse(String.format("\"%s\" = /%s/", key, value));
        keyString = key;
        valueString = value;
    }

    public void parse(String queryString) {
        this.queryString = queryString;
    }

    public boolean canParse(String query) {
        return SIMPLE_QUERY_PATTERN.matcher(query).matches();
    }

    @Override
    public String getQueryString() {
        return queryString;
    }

    @Override
    public boolean matches(PlaneDB planeDB, String metadataSetName) {

        Matcher m = SIMPLE_QUERY_PATTERN.matcher(queryString);
        if (metadataSetName == null) {
            metadataSetName = PlaneDB.MODIFIED_METADATASET;
        }

        Map<String, MetaData> metadataSet = planeDB.metaDataSetProperty();

        if (m.matches()) {

            String keyName = keyString == null ? m.group(1) : keyString;
            String valueName = valueString == null ? m.group(2) : valueString;
            
            // finds the right key even ignoring the case 
            keyName = findKey(metadataSet, keyName.trim());
            
            keyString = keyName;
            
            valueName = valueName.trim();
            valueString = valueName;
            try {
                String metadataValue = planeDB.getMetaDataSet().get(valueName).getStringValue();

                return metadataValue.toLowerCase().equals(valueName.toLowerCase());
            } catch (NullPointerException e) {
                return false;
            }

        }

        return false;

    }

    public String findKey(Map<String, ? extends Object> metadata, String key) {

        return metadata.keySet().stream().filter(mapKey -> mapKey.toLowerCase().equals(key.toLowerCase())).findFirst().orElse(key);

    }

    @Override
    public String phraseMe() {
        
         Matcher m = SIMPLE_QUERY_PATTERN.matcher(queryString);
         if(m.matches()) {
             return String.format(PHRASE,m.group(1).trim(),m.group(2).trim());
         }
        
        return "??";
        
    }
}
