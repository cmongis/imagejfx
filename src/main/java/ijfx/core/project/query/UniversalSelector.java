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
import ijfx.core.metadata.MetaDataSet;
import ijfx.core.project.imageDBService.PlaneDB;
import java.util.Map;
import javafx.beans.property.ReadOnlyMapProperty;
import mongis.utils.ConditionList;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = Selector.class, priority = Priority.VERY_LOW_PRIORITY * 2)
public class UniversalSelector implements Selector {

    String queryString;

    private static final String PHRASE = "contains the word \"%s\" in one of metadata";
    
    @Override
    public void parse(String queryString) {
        this.queryString = queryString;
    }

    @Override
    public boolean canParse(String queryString) {
        return true;
    }

    @Override
    public String getQueryString() {
        return queryString;
    }

    @Override
    public boolean matches(PlaneDB t, String metadataSetName) {

       if (t.getMetaDataSet() == null) {
                return false;
            }
            boolean onMatch = false;
            final ConditionList conditionList = new ConditionList(t.getMetaDataSet().size());

            t.getMetaDataSet().forEach((key, value) -> {
                if (value.getStringValue().toLowerCase().contains(queryString.toLowerCase())) {
                    conditionList.add(true);
                }
            });

            return conditionList.isOneTrue();

    }

    public String findKey(Map<String, ? extends Object> metadata, String key) {

        return metadata.keySet().stream().filter(mapKey -> mapKey.toLowerCase().equals(key.toLowerCase())).findFirst().orElse(null);

    }

    @Override
    public String phraseMe() {
        return String.format(PHRASE.format(PHRASE, queryString));
    }

}
