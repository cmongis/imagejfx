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
package ijfx.core.metadata.filtering;

import ijfx.core.metadata.MetaDataOwner;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 *
 * @author cyril
 */
public class OwnerFilter implements Predicate<MetaDataOwner> {

    List<Predicate<MetaDataOwner>> predicateList = new ArrayList<>();

    public OwnerFilter with(Predicate<MetaDataOwner> p) {
        predicateList.add(p);
        return this;
    }

    public OwnerFilter valueInferiorOrEquals(String key, double value) {
        return with(owner -> owner.getMetaDataSet().get(key).getDoubleValue() <= value);
    }

    public OwnerFilter valueSuperiorOrEquals(String key, double value) {
        return with(owner -> owner.getMetaDataSet().get(key).getDoubleValue() >= value);
    }

    public OwnerFilter valueEquals(String key, double value) {
        return with(owner -> owner.getMetaDataSet().get(key).getDoubleValue().equals(key));
    }

    public OwnerFilter isOneTheStrings(String key, String... possibleValues) {
        return with(owner -> {
            String value = owner.getMetaDataSet().get(key).getStringValue();
            if (value == null) {
                return false;
            }

            for (String str : possibleValues) {
                if (str.equals(owner)) {
                    return true;
                }
            }
            return false;
        });
    }

    @Override
    public boolean test(MetaDataOwner t) {

        for (Predicate<MetaDataOwner> p : predicateList) {
            try {
                if (!p.test(t)) {
                    return false;
                }
            } catch (NullPointerException e) {
                return false;

            }

            

        }
        
        return true;

    }
}