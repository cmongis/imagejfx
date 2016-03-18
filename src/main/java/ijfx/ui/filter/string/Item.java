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
public interface Item {
    
    public String getName();

    public void setName(String name) ;

    public boolean getState();

    public void setState(boolean state);  
    
    public int getNumber();

    public void setNumber(int n);
}
