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

import javafx.beans.property.Property;
import javafx.beans.property.adapter.JavaBeanObjectProperty;
import javafx.beans.property.adapter.JavaBeanObjectPropertyBuilder;
import javafx.beans.property.adapter.JavaBeanStringPropertyBuilder;

/**
 *
 * @author Tuan anh TRINH
 */
public class ItemWrapper implements Item{

    private Item item;
    private Property<String> nameProperty;
    private Property<Boolean> stateProperty;

    public ItemWrapper(String s, int i) {
        this.item = new DefaultItem(s, i);
        try {
            this.stateProperty = new JavaBeanObjectPropertyBuilder<>().bean(this.item).name("state").build();
            this.nameProperty = new JavaBeanStringPropertyBuilder().bean(this.item).name("name").build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public String getName() {
        return item.getName();
    }

    @Override
    public boolean getState() {
        return item.getState();
    }

    @Override
    public void setState(boolean isDone) {

        stateProperty.setValue(isDone);

    }

    @Override
    public void setName(String name) {
        nameProperty.setValue(name);
    }
    public Item getItem() {
        return item;
    }

    public Property<String> nameProperty() {
        return nameProperty;
    }

    public Property<Boolean> stateProperty() {
        return stateProperty;
    }

    @Override
    public int getNumber() {
        return item.getNumber();
    }

    @Override
    public void setNumber(int n) {
        item.setNumber(n);
    }
    
}
