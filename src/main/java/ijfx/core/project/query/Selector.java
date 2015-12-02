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

import ijfx.core.metadata.MetaDataSet;
import ijfx.core.project.imageDBService.PlaneDB;

/**
 *
 * @author Cyril Quinton
 */
public interface Selector {
    public static String SELECTOR_STRING = "selector";
   
    
    public void parse(String queryString);
    public String getQueryString();
    
    //String getParsedSelector();

    //List<StringPosition> getLogicalOperatorPositions();
    
    //String getTagQuery(String tag);
    
    //String getMetaDataQuery(MetaData metaData);
    
    //String getMetaDataQuery(String key, String value);
    
    //String getSeparator();

   
    public boolean matches(PlaneDB planeDB, String metadataSetName);

}
