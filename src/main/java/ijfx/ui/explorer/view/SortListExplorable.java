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

import ijfx.ui.explorer.Explorable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author Tuan anh TRINH
 * @param <T>
 */
public class SortListExplorable<T extends Explorable> {

    List<T> listItems;
    List<List<T>> list2D;

    String firstMetaData;
    String secondMetaData;

    public SortListExplorable() {
        list2D = new CopyOnWriteArrayList<>();
        listItems = new CopyOnWriteArrayList<>();

    }

    public void setMetaData( String firstMetaData,String secondMetaData) {

        this.secondMetaData = secondMetaData;
        this.firstMetaData = firstMetaData;
    }

    public void process() {
        SortExplorableUtils.sort(firstMetaData, listItems);
        create2DList(firstMetaData);
        sort2DList(secondMetaData);
    }

    public SortListExplorable(List<T> list) {
        this();
        listItems = list;
    }

    public void create2DList(String metaDataName) {
        SortExplorableUtils.create2DList(metaDataName, list2D, listItems);
    }

    public void sort2DList(String metaDataName) {
        this.list2D.stream().forEach(l -> SortExplorableUtils.sort(metaDataName, l));
    }

    public void setItems(List<T> list) {
        listItems = list;
    }

    public List<T> getListItems() {
        return listItems;
    }

    public List<List<T>> getList2D() {
        return list2D;
    }

    public int getSizeList2D() {
        int size = 0;
        for (List<T> list : list2D) {
            size = size + list.size();
        }
        return size;
    }

    public String getFirstMetaData() {
        return firstMetaData;
    }

    public void setFirstMetaData(String firstMetaData) {
        this.firstMetaData = firstMetaData;
    }

    public String getSecondMetaData() {
        return secondMetaData;
    }

    public void setSecondMetaData(String secondMetaData) {
        this.secondMetaData = secondMetaData;
    }
}
