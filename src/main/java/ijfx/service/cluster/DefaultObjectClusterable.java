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
package ijfx.service.cluster;

import weka.core.DenseInstance;
import weka.core.Instance;
import ijfx.ui.explorer.ObjectWrapper;

/**
 *
 * @author Tuan anh TRINH
 */
public class DefaultObjectClusterable extends DenseInstance implements ObjectClusterable{

    private Object object;

    public DefaultObjectClusterable(Object object, double weight, double[] attValues) {
        super(weight, attValues);
        this.object = object;
    }

    public DefaultObjectClusterable(Instance instance) {
        super(instance);
        if (instance instanceof DefaultObjectClusterable) {
            m_AttValues = ((DefaultObjectClusterable) instance).m_AttValues;
            this.object = ((DefaultObjectClusterable) instance).getObject();
        } else {
            m_AttValues = instance.toDoubleArray();
        }
        m_Weight = instance.weight();
        m_Dataset = null;
    }

    @Override
    public Object getObject() {
        return object;
    }

    @Override
    public Object copy() {
        DefaultObjectClusterable result = new DefaultObjectClusterable(this);
        result.m_Dataset = m_Dataset;
        return result;
    }

}
