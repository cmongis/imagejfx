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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import java.util.ArrayList;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class MercuryLocalAppPackage {
    
    
    private ArrayList<MercuryLocalApp> apps = new ArrayList<>();
    
    @JsonCreator
    public MercuryLocalAppPackage() {
        
    }
    
    @JsonSetter("apps")
    public void setAppList(ArrayList<MercuryLocalApp> apps) {
        this.apps = apps;
    }
    
    @JsonGetter("apps")
    public ArrayList<MercuryLocalApp> getAppList() {
        return apps;
    }
    
}
