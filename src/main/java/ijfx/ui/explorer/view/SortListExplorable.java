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
public class SortExplorable<T> {

    List<? extends Explorable> listItems;
    List<List<? extends Explorable>> list2D;

    public SortExplorable() {
        list2D = new CopyOnWriteArrayList<>();
        listItems = new CopyOnWriteArrayList<>();
    }
    
    public SortExplorable(List<? extends Explorable> list)
    {
        this();
        listItems = list;
        List<String> t = new ArrayList<>();


    }

    
    

    public void sort(String metaDataName) {
        Comparator<Explorable> comparator = ( o1,  o2) -> {
            String s1 = getValueMetaData(o1, metaDataName);
            String s2 = getValueMetaData(o2, metaDataName);
            return s1.compareToIgnoreCase(s2);
        };
        this.listItems.sort(comparator);
    }

    public void create2DList(String metaDataName) {
        this.list2D.clear();
        List<Integer> limits = new ArrayList<>();
        limits.add(0);

        for (int i = 0; i < this.listItems.size(); i++) {
            System.out.println(i);
         if (i == this.listItems.size()-1)
            {
                limits.add(i+1);
            }
         else if ( !getValueMetaData(this.listItems.get(i), metaDataName).equals(getValueMetaData(this.listItems.get(i + 1), metaDataName))) {
                limits.add(i);
            }
        }

        for (int j = 0; j < limits.size()-1; j++) {
            List<? extends Explorable> l = new CopyOnWriteArrayList<>(this.listItems.subList(limits.get(j), limits.get(j + 1)));
            this.list2D.add(l);
        }
    }

    public void sort2DList(String metaDataName) {
        this.list2D.stream().forEach(l -> sort(metaDataName));
    }

    public String getValueMetaData(Explorable ex, String metaDataName) {
        return MetaData.metaDataSetToMap(ex.getMetaDataSet()).get(metaDataName);
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
    
    public int getSizeList2D(){
        int size = 0;
        for (List<? extends Explorable> list: list2D)
        {
            size = size + list.size();
        }
        return size;
    }
}
