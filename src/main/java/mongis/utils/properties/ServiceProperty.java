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
package mongis.utils.properties;

import java.util.function.BiConsumer;
import javafx.util.Callback;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class ServiceProperty<ITEM,VALUE> extends SimpleUpdatablePoperty<VALUE>{
    
    final private ITEM item;
    
    final private BiConsumer<ITEM, VALUE> setter;
    final private Callback<ITEM,VALUE> getter;

    public ServiceProperty(ITEM item, BiConsumer<ITEM, VALUE> setter, Callback<ITEM, VALUE> getter) {
        this.item = item;
        this.setter = setter;
        this.getter = getter;
    }
    
    
   
    
    @Override
    public VALUE get() {
        return getter.call(item);
    }
    
    @Override
    public void set(VALUE value) {
        setter.accept(item, value);
    }
}
