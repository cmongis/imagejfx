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
import ijfx.service.ui.ControlableProperty;
import ijfx.ui.module.input.Input;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.Property;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class BeanInputWrapper<T> implements Input<T>{
    
    final protected Object bean;
    final protected Class<T> type;
    final protected String name;

    boolean isMultipleChoise = false;
    
    List<T> choices = null;
    
    String label;
    
    Property<T> valueProperty;
    
    
    public BeanInputWrapper(Object bean, Class<T> type, String name) {
        this.bean = bean;
        this.type = type;
        this.name = name;
        
        valueProperty  = new ControlableProperty<Object, T>()
            .setBiSetter(this::setValue)
            .setCaller(this::getValue);
        
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
        
        valueProperty.setValue(value);
    }

    @Override
    public T getValue() {
        return valueProperty.getValue();
    }
    
    private T getValue(Object bean) {
        try {
            
            Object get = bean.getClass().getField(name).get(bean);
            
            return (T) get;
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
    
    private void setValue(Object bean, T value) {
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

    @Override
    public Property<T> valueProperty() {
        return valueProperty;
    }
    
}
