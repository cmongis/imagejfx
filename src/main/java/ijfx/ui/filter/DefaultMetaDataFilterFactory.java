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

import ijfx.core.metadata.MetaData;
import ijfx.core.metadata.MetaDataOwner;
import ijfx.ui.filter.string.DefaultStringFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Pierre BONNEAU
 * @author Cyril MONGIS
 */
public class DefaultMetaDataFilterFactory implements MetaDataFilterFactory {

    @Override
    public MetaDataOwnerFilter generateFilter(Collection<? extends MetaDataOwner> ownerList, String keyName) {

        Collection<MetaData> possibleValues = getAllPossibleValues(ownerList, keyName);
        int type = checktype(ownerList, keyName);

        int differentValues = possibleValues.stream().collect(Collectors.toSet()).size();

        if (differentValues <= 1) {
            return null;
        }
        if (differentValues <= 6) {
            return createStringFilter(possibleValues, keyName);
        } else {

            MetaDataOwnerFilter ownerFilter;
            if (type == MetaData.TYPE_STRING) {
                ownerFilter = createStringFilter(possibleValues, keyName);
            } else {
                ownerFilter = createNumberFilter(possibleValues, keyName);
            }
            return ownerFilter;
        }
    }

    public Collection<MetaData> getAllPossibleValues(Collection<? extends MetaDataOwner> ownerList, String keyName) {

        return ownerList.stream()
                .map(owner -> owner.getMetaDataSet().get(keyName))
                .filter(MetaData::notNull)
                .collect(Collectors.toList());

    }

    public int checktype(Collection<? extends MetaDataOwner> ownerList, String keyName) {

        System.out.println(String.format("Checking the type for %s (%d)", keyName, ownerList.size()));
        if (ownerList.isEmpty()) {
            return MetaData.TYPE_NOT_SET;
        }

        List<MetaData> nonNullMetaData
                = ownerList
                .parallelStream()
                .map(owner -> owner.getMetaDataSet().get(keyName))
                .filter(metadata -> metadata.getValue() != null)
                .collect(Collectors.toList());

        if (nonNullMetaData.size() == nonNullMetaData.stream().filter(metadata -> metadata.getType() == MetaData.TYPE_NUMBER).count()) {
            return MetaData.TYPE_NUMBER;
        } else {
            return MetaData.TYPE_STRING;
        }

    }

    public MetaDataOwnerFilter createStringFilter(Collection<MetaData> possibleValues, String keyName) {

        Collection<String> possibleStringValues = possibleValues
                .stream()
                .filter(MetaData::notNull)
                .map(metadata -> metadata.getStringValue())
                .collect(Collectors.toList());

        StringFilter filter = new DefaultStringFilter();
        filter.setAllPossibleValues(possibleStringValues);
        MetaDataOwnerFilter wrapper = new StringFilterWrapper(filter, keyName);

        return wrapper;
    }

    public MetaDataOwnerFilter createNumberFilter(Collection<MetaData> possibleValues, String keyName) {

        // filtering and getting the doubles from the list of metadata
        Collection< ? extends Number> possibleNumberValues = possibleValues
                .stream()
                .filter(MetaData::notNull)
                .map(metadata->metadata.getDoubleValue())
                .collect(Collectors.toList());

        NumberFilter filter = new DefaultNumberFilter();
        filter.setAllPossibleValue(possibleNumberValues);
        MetaDataOwnerFilter wrapper = new NumberFilterWrapper(filter, keyName);

        return wrapper;
    }

}
