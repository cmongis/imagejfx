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
package ijfx.service.ui.hint;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

/**
 *
 * @author cyril
 */
public class DefaultHint implements Hint{
    
   
    @JsonProperty(value="id",required=false)
    private String id;
    
    @JsonProperty("text")
    private String text;
    
    @JsonProperty("target")
    private String target;

    @JsonProperty(value = "isRead",required = false)
    private boolean isRead = false;
    
    
    public DefaultHint() {}
    
    public DefaultHint(String target, String text) {
        setTarget(target);
        setText(text);
    }
    
    public DefaultHint(Hint h) {
        setId(h.getId());
        setText(h.getText());
        setTarget(h.getTarget());
        isRead = h.isRead();
    }
    
    
    
    public String getId() {
        if(id == null) {
            id = UUID.nameUUIDFromBytes((getTarget()+getText()).getBytes()).toString();
        }
        return id;
    }

    public Hint setId(String id) {
        this.id = id;
        return this;
    }

    public String getText() {
        return text;
    }

    public DefaultHint setText(String text) {
        this.text = text;
        return this;
    }

    public String getTarget() {
        return target;
    }

    public DefaultHint setTarget(String target) {
        this.target = target;
        return this;
        
    }

    public boolean isRead() {
        return isRead;
    }

    public DefaultHint setIsRead(boolean isRead) {
        this.isRead = isRead;
        return this;
    }
    
    public void setRead() {
        isRead = true;
        
    }
    
    @Override
    public boolean equals(Object o) {
        
        if(o == null) return false;
        
        if(Hint.class.isAssignableFrom(o.getClass())) {
            Hint h = (Hint)o;
            
            return h.getId().equals(getId());
        }
        else return false;
    }
    
    
}
