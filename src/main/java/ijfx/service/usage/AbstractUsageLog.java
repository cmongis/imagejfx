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
package ijfx.service.usage;

import org.json.JSONObject;

/**
 *
 * @author cyril
 */
public abstract class AbstractUsageLog implements UsageLog {
    
    final private UsageType type;
    final private String name;
    final private UsageLocation location;
    private Object value;
    private boolean valueSet = false;
    
    
    public AbstractUsageLog(UsageType type, String name, UsageLocation location) {
        this.type = type;
        this.name = name;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public UsageType getType() {
        return type;
    }

    public UsageLocation getLocation() {
        return location;
    }

    public Object getValue() {
        
        return value;
    }
    
    public UsageLog setValue(Object object) {
        this.value = object;
        valueSet = true;
        return this;
    }
    
    public JSONObject toJSON() {
        
        JSONObject object = new JSONObject()
                .put("name", getName())
                .put("type",getType().toString())
                .put("location",getLocation().toString());
                
       if(valueSet) object.put("value",value == null ? "null" : value.toString());
        
       return object;
    }
    
    
    
    
}
