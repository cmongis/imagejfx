/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ijfx.ui.filter.string;

/**
 *
 * @author tuananh
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
