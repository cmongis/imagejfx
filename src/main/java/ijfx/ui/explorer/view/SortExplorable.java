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

/**
 *
 * @author Tuan anh TRINH
 */
public class SortExplorable<T> {

    List<? extends Explorable> listItems;
    List<List<? extends Explorable>> list2D = new ArrayList<>();

    public SortExplorable() {
        list2D = new ArrayList<>();

    }
    
    public SortExplorable(List<? extends Explorable> list)
    {
        this();
        listItems = list;
    }

    
    

    public void sort(MetaData metaData, List<? extends Explorable> list) {
        Comparator<Explorable> comparator = ( o1,  o2) -> {
            String s1 = getValueMetaData(o1, metaData);
            String s2 = getValueMetaData(o2, metaData);
            return s1.compareToIgnoreCase(s2);
        };
        list.sort(comparator);
    }

    public void create2DList(MetaData metaData, List<? extends Explorable> list, List<List<? extends Explorable>> list2D) {
        List<Integer> limits = new ArrayList<>();
        limits.add(0);

        for (int i = 0; i < list.size(); i++) {
            if (!getValueMetaData(list.get(i), metaData).equals(getValueMetaData(list.get(list.size() - 1), metaData))) {
                limits.add(i);
            }
        }

        for (int j = 0; j < limits.size(); j++) {
            list2D.add(list.subList(limits.get(j), limits.get(j + 1)));
        }
    }

    public void sort2DList(MetaData metaData, List<List<? extends Explorable>> list2D) {
        list2D.stream().forEach(l -> sort(metaData, l));
    }

    public String getValueMetaData(Explorable ex, MetaData metaData) {
        return MetaData.metaDataSetToMap(ex.getMetaDataSet()).get("Channel");
    }
    
    public void setItems(List<? extends Explorable> list) {
        listItems = list;
    }

    public List<? extends Explorable> getListItems() {
        return listItems;
    }

    public List<List<? extends Explorable>> getList2D() {
        return list2D;
    }
}
