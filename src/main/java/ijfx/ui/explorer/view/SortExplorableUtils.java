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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author Tuan anh TRINH
 */
public class SortExplorableUtils {
    
    public static String getValueMetaData(Explorable ex, String metaDataName) {
        return MetaData.metaDataSetToMap(ex.getMetaDataSet()).get(metaDataName);
    }
    
    public static Comparator MetadataComparator(String metaDataName) {
        Comparator<? extends Explorable> comparator = (o1, o2) -> {
            String s1 = SortExplorableUtils.getValueMetaData(o1, metaDataName);
            String s2 = SortExplorableUtils.getValueMetaData(o2, metaDataName);
            return s1.compareToIgnoreCase(s2);
        };
        return comparator;
    }
    
    public static void create2DList(String metaDataName, List<List<? extends Explorable>> list2D, List<? extends Explorable> listItems) {
        list2D.clear();
        List<Integer> limits = findLimits(metaDataName, listItems);
        
        for (int j = 0; j < limits.size() - 1; j++) {
            List<? extends Explorable> l = new CopyOnWriteArrayList<>(listItems.subList(limits.get(j), limits.get(j + 1)));
            if (!l.isEmpty()) {
                list2D.add(l);
                
            }
        }
    }
    
    public static List<Integer> findLimits(String metaDataName, List<? extends Explorable> listItems) {
        List<Integer> limits = new ArrayList<>();
        limits.add(0);
        
        for (int i = 0; i < listItems.size(); i++) {
            if (i == listItems.size() - 1) {
                limits.add(i + 1);
            } else if (!SortExplorableUtils.getValueMetaData(listItems.get(i), metaDataName).equals(SortExplorableUtils.getValueMetaData(listItems.get(i + 1), metaDataName))) {
                limits.add(i);
            }
        }
        return limits;
    }

    public static void sort(String metaDataName, List<? extends Explorable> listItems) {
        //Cannot use method reference, has to give the metaDataName
        listItems.sort(SortExplorableUtils.MetadataComparator(metaDataName));
    }
    
}
