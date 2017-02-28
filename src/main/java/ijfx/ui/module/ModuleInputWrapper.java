/*
 * /*
 *     This file is part of ImageJ FX.
 *
 *     ImageJ FX is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     ImageJ FX is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with ImageJ FX.  If not, see <http://www.gnu.org/licenses/>. 
 *
 * 	Copyright 2015,2016 Cyril MONGIS, Michael Knop
 *
 */
package ijfx.ui.module;

import ijfx.service.ui.ControlableProperty;
import ijfx.ui.main.ImageJFX;
import ijfx.ui.module.input.Input;
import java.util.List;
import java.util.logging.Level;
import javafx.beans.property.Property;
import org.scijava.ItemVisibility;
import org.scijava.module.MethodCallException;
import org.scijava.module.Module;
import org.scijava.module.ModuleItem;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class ModuleInputWrapper<T extends Object> implements Input{

    private final  Module module;
    private final ModuleItem<T> moduleItem;
    
    ControlableProperty<Module, T> valueProperty;
    
    public ModuleInputWrapper(Module module, ModuleItem moduleItem) {
        this.module = module;
        this.moduleItem = moduleItem;
        
        valueProperty = new ControlableProperty<Module,T>()
                .setBean(module)
                .setBiSetter(this::setValue)
                .setCaller(this::getValue);
        
    }
    
   
    
    @Override
    public void setValue(Object value) {
        module.setInput(getName(), value);
    }
    
    private void setValue(Module module, T value) {
        moduleItem.setValue(module, value);
    }
    
    private T getValue(Module module) {
        
        T t =  moduleItem.getValue(module);
        
        T def = moduleItem.getDefaultValue();
        
        
        
        if(t == null && def != null) {
            moduleItem.setValue(module, def);
            t = def;
         }
       return t;
 
    }

    @Override
    public Object getValue() {
        return module.getInput(getName());
    }

   

    @Override
    public List<T> getChoices() {
        return moduleItem.getChoices();
    }

    @Override
    public boolean multipleChoices() {
        
        return moduleItem.getChoices() != null && moduleItem.getChoices().size() > 0;
    }

    @Override
    public String getName() {
        return moduleItem.getName();
    }

    private String transformNameToLabel(String name) {
        
        name = name.replaceAll("(\\p{Ll})(\\p{Lu})","$1 $2");
        name = name.replaceAll("(\\d+)"," $1");
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
    
    @Override
    public String getLabel() {
        
        if(moduleItem.getLabel() == null || moduleItem.getLabel().equals("")) {
            return transformNameToLabel(moduleItem.getName());
        }
        
        return moduleItem.getLabel();
    }

    @Override
    public Class getType() {
        return moduleItem.getType();
    }
    
    @Override
    public void callback() {
        try {
            moduleItem.callback(module);
            
            valueProperty.checkFromGetter();
            
        } catch (MethodCallException ex) {
            ImageJFX.getLogger().log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public boolean isMessage() {
        return moduleItem.getVisibility() == ItemVisibility.MESSAGE;
    }

    @Override
    public String getWidgetType() {
        
        return moduleItem.getWidgetStyle();
    }
    
  

    @Override
    public T getMinimumValue() {
        return moduleItem.getMinimumValue();
    }

    @Override
    public T getMaximumValue() {
        return moduleItem.getMaximumValue();
    }

    @Override
    public Property<T> valueProperty() {
       return  valueProperty;
    }
}
