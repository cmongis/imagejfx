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
package ijfx.examples.context;

/**
 *
 * @author Tuan anh TRINH
 */
class Category {
public String name;
public String context;
public DefaultWidget [] items;

    public Category() {
    }
    
    public Category(String name, String context, DefaultWidget [] items) {
        this.name = name;
        this.context = context;
        this.items = items;
    }

    public Category(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getContext() {
        return context;
    }

    public DefaultWidget[] getItems() {
        return items;
    }
    


}
