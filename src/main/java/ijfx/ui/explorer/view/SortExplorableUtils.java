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
package ijfx.ui.explorer.view;

import ijfx.core.metadata.MetaData;
import ijfx.ui.explorer.Explorable;
import java.util.Comparator;

/**
 *
 * @author Tuan anh TRINH
 */
public class SortExplorableUtils {

    public static String getValueMetaData(Explorable ex, String metaDataName) {
        return MetaData.metaDataSetToMap(ex.getMetaDataSet()).get(metaDataName);
    }

    public static Comparator MetadataComparator(String metaDataName) {
        Comparator<Explorable> comparator = (o1, o2) -> {
            String s1 = SortExplorableUtils.getValueMetaData(o1, metaDataName);
            String s2 = SortExplorableUtils.getValueMetaData(o2, metaDataName);
            return s1.compareToIgnoreCase(s2);
        };
        return comparator;
    }
}
