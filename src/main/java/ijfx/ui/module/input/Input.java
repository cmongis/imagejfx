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
package ijfx.ui.module.input;

import java.util.List;

/**
 * Interface used to modify an Input
 * This interface was created to loosen the coupling with the ImageJ Module API
 * However, all the Skins are dependant to the Module API
 * @author Cyril MONGIS 2015
 */
public interface Input<T> {
    
    public void setValue(T value);
    
    public T getValue();
    
    public T getDefaultValue();
    
    public List<T> getChoices();
    
    public boolean multipleChoices();
    
    public String getName();
    
    public String getLabel();
 
    public Class<?> getType();
    
    public void callback(); 
   
    public boolean isMessage();
    
}
