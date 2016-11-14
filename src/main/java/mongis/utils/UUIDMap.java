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
package mongis.utils;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.UUID;

/**
 *
 * @author cyril
 */
public class UUIDMap<T> extends HashMap<UUID,T>{
    
    
    
    private UUID transform(Object... object) {
        
        ByteBuffer allocate = ByteBuffer
                .allocate(object.length*4);
        for(Object o : object) {
            allocate.putInt(o.hashCode());
        }
        
        return UUID.nameUUIDFromBytes(allocate.array());
        
    }
    
    public MapPutter<T> get(Object... object) {
       UUID uuid = transform(object);
       return new DefaultPutter(this,uuid);
    }
    
    public UUID getId(Object... object) {
        return transform(object);
    }
    
    
    protected class DefaultPutter implements MapPutter<T> {
        final UUID uuid;
        final UUIDMap<T> parent;
        
        
        public DefaultPutter(UUIDMap<T> parent, UUID uuid) {
            this.uuid = uuid;
            this.parent = parent;
        }

        
        public T orPut(T t) {          
            if(containsKey(uuid) == false)  {
                put(t);
            }
            
            return get(uuid);
        }
        
        @Override
        public UUIDMap<T> put(T t) {
           parent.put(uuid,t);
           return parent;
        }

        @Override
        public boolean has() {
           return containsKey(uuid);
        }
        
        public UUID id() {
            return uuid;
        }
        
    }
    
   
    public interface MapPutter<T> {
        UUIDMap<T> put(T t);
        boolean has();
        public T orPut(T t);
        
        UUID id();
        
    }
    
    
}
