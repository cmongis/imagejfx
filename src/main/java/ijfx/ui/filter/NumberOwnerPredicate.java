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
package ijfx.ui.filter;

import ijfx.core.metadata.MetaDataOwner;
import java.util.function.Predicate;

/**
 *
 * @author Pierre BONNEAU
 */
public class NumberOwnerPredicate implements Predicate<MetaDataOwner>{
    
    String keyName;
    
    Double value;
    
    Predicate<Double> predicate;
    
    public NumberOwnerPredicate(String keyName, Predicate<Double> predicate){
        this.keyName = keyName;
        this.predicate = predicate;
    }
    
    @Override
    public boolean test(MetaDataOwner t) {
        value = t.getMetaDataSet().get(keyName).getDoubleValue();
        return predicate.test(value);
    }
    
    public Double getKey(){
        return this.value;
    }
   
    
    public Predicate<Double> getPredicate(){
        return this.predicate;
    }
    
    public void setPredicate(Predicate<Double> predicate){
        this.predicate = predicate;
    }
}
