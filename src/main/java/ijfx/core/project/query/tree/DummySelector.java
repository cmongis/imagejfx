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
package ijfx.core.project.query.tree;

import ijfx.core.project.imageDBService.PlaneDB;
import ijfx.core.project.query.Selector;

/**
 *
 * @author cyril
 */
public class DummySelector implements Selector {
    
    String queryString;
   

    public DummySelector(String queryString) {
       
        this.queryString = queryString;
    }

    @Override
    public void parse(String queryString) {
        this.queryString = queryString;
    }

    @Override
    public String getQueryString() {
        return queryString;
    }

    @Override
    public boolean matches(PlaneDB planeDB, String metadataSetName) {
        return true;
    }

    @Override
    public boolean canParse(String queryString) {
        return true;
    }
    
    
    
}
