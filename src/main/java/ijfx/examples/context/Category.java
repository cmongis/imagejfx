/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ijfx.examples.context;

/**
 *
 * @author tuananh
 */
class Category {
public String name;
public String context;
public DefaultWidget [] items;

    public Category() {
    }
    
    public Category(String name, String context, DefaultWidget [] i) {
        this.name = name;
        this.context = context;
         items = i;
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
