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

import ijfx.core.utils.Converter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;








/**
 *
 * @author cyril
 */
public class ConvertedChoiceBeanInputWrapper<R> extends BeanInputWrapper<String>{
    
    Converter<String,R> converter;
    
    public ConvertedChoiceBeanInputWrapper(Object bean, String name, Converter<String,R> converter) {
        super(bean, String.class, name);
        this.converter = converter;
        
        if(converter instanceof Map) {
            Map<String,R> map = (Map)converter;
            setChoices(map.keySet().toArray(new String[map.size()]));
        }
        
    }
    
    @Override
    public String getValue() {
        try {
            R beanValue = (R)bean.getClass().getField(name).get(bean);
            String backward =  converter.backward(beanValue);
            return backward;
        } catch (Exception ex) {
            Logger.getLogger(ConvertedChoiceBeanInputWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    @Override
    public void setValue(String value) {
        try {
            bean.getClass().getField(name).set(bean,converter.forward(value));
        }
        catch (Exception ex) {
            Logger.getLogger(ConvertedChoiceBeanInputWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
