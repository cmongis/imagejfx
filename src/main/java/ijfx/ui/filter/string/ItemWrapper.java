/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ijfx.ui.filter.string;

import javafx.beans.property.Property;
import javafx.beans.property.adapter.JavaBeanObjectProperty;
import javafx.beans.property.adapter.JavaBeanObjectPropertyBuilder;
import javafx.beans.property.adapter.JavaBeanStringPropertyBuilder;

/**
 *
 * @author tuananh
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
