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
package ijfx.ui.module;

import com.google.common.collect.Lists;
import ijfx.ui.module.input.Input;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author cyril
 */
public class BeanInputWrapper<T> implements Input<T>{
    
    final Object bean;
    final Class<T> type;
    final String name;

    boolean isMultipleChoise = false;
    
    List<T> choices = null;
    
    String label;
    
    public BeanInputWrapper(Object bean, Class<T> type, String name) {
        this.bean = bean;
        this.type = type;
        this.name = name;
    }
   
 
    
    public BeanInputWrapper<T> setChoices(T... choices) {
        return setChoices(Lists.newArrayList(choices));
    }
    public BeanInputWrapper<T> setChoices(List<T> choices) {
        this.choices = choices;
        return this;
    }
            
    @Override
    public void setValue(T value) {
        
        try {
            bean.getClass().getField(name).set(bean, value);
        } catch (NoSuchFieldException ex) {
            Logger.getLogger(BeanInputWrapper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(BeanInputWrapper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(BeanInputWrapper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(BeanInputWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    @Override
    public T getValue() {
        try {
            return (T) bean.getClass().getField(name).get(bean);
        } catch (NoSuchFieldException ex) {
            Logger.getLogger(BeanInputWrapper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(BeanInputWrapper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(BeanInputWrapper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(BeanInputWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public T getDefaultValue() {
        return getValue();
    }

    @Override
    public List<T> getChoices() {
        return choices;
    }

    @Override
    public boolean multipleChoices() {
        return choices != null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getLabel() {
        return label;
    }

    public BeanInputWrapper<T> setLabel(String label) {
        this.label = label;
        return this;
    }
    
    

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public void callback() {
        
    }

    @Override
    public boolean isMessage() {
        return false;
    }

    @Override
    public String getWidgetType() {
        return "";
    }

    @Override
    public T getMinimumValue() {
        return null;
    }

    @Override
    public T getMaximumValue() {
        return null;
    }
    
}
