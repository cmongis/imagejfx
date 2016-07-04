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
package ijfx.ui.filter.string;

/**
 *
 * @author Tuan anh TRINH
 */
public class DefaultItem implements Item{

    private String name;
    private boolean state;
    private int number;

    public DefaultItem(String s, Integer i) {
        this.name = s;
        this.number = i;
        this.state = false;
    }
    @Override
    public String getName() {
           return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean getState() {
        return this.state;
    }

    @Override
    public void setState(boolean state) {
        this.state = state;
    }

    @Override
    public int getNumber() {
        return this.number;
    }

    @Override
    public void setNumber(int n) {
        this.number = n;
    }
    
}
