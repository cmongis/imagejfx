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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import org.apache.commons.lang.NumberUtils;

/**
 *
 * @author Tuan anh TRINH
 */
public class GroupExplorable<T> {

    private List<Explorable> listItems;
    private List<String> metaDataList;

    private List<List<? extends Explorable>> list2D;
    private List<List<List<? extends Explorable>>> list3D;

    private SortListExplorable<Explorable> sortListExplorable;
    private int size;

    public SortListExplorable<Explorable> getSortListExplorable() {
        return sortListExplorable;
    }

    public GroupExplorable() {
        list3D = new CopyOnWriteArrayList<>();
        list2D = new CopyOnWriteArrayList<>();
        listItems = new CopyOnWriteArrayList<>();
        metaDataList = new ArrayList<>();
        //TODO
        metaDataList.add("1");
        metaDataList.add("2");
        metaDataList.add("3");
        sortListExplorable = new SortListExplorable<>();

    }

    public void process() {
        List<Explorable> list1D = new CopyOnWriteArrayList<>(filterExplorableWithList(listItems, metaDataList));
        sortListExplorable.setMetaData(metaDataList.get(0), metaDataList.get(1));

        size = 0;
        list3D.clear();
        SortExplorableUtils.sort(metaDataList.get(2), list1D);
        SortExplorableUtils.create2DList(metaDataList.get(2), list2D, list1D);
        list2D.stream().forEach((l2D) -> {
            sortListExplorable.setItems(l2D);
            sortListExplorable.process();
            if (!sortListExplorable.getList2D().isEmpty()) {

                list3D.add(new CopyOnWriteArrayList<>(sortListExplorable.getList2D()));
                size = sortListExplorable.getSizeList2D() + size;
            }
        });
    }

    public boolean checkNumber(String metaData){
        if (!NumberUtils.isNumber(SortExplorableUtils.getValueMetaData(listItems.get(0), metaData))){
            return true;
        }
        else
        {
            List<Explorable> filtered = filterExplorable(listItems, metaData);
            SortExplorableUtils.sort(metaData, filtered);
            return SortExplorableUtils.findLimits(metaData, filtered).size() <= 25;
        }
    }
    
     public List<Explorable> filterExplorableWithList(List<Explorable> arrayList, List<String> metaData) {
        return arrayList.stream().filter(p -> {
            return metaData.stream().allMatch(m ->  p
                    .getMetaDataSet()
                    .containsKey(m));
        }).collect(Collectors.toList());
    }
       
    public List<Explorable> filterExplorable(List<Explorable> arrayList, String metaData) {
        return arrayList.stream().filter(p -> {
            return p.getMetaDataSet().containsKey(metaData);
        }).collect(Collectors.toList());
    }

    public List<List<List<? extends Explorable>>> getList3D() {
        return list3D;
    }

    public List<String> getMetaDataList() {
        return metaDataList;
    }

    public void setMetaDataList(List<String> metaDataList) {
        this.metaDataList = metaDataList;
    }

    public List<Explorable> getListItems() {
        return listItems;
    }

    public void setListItems(List<Explorable> listItems) {
        this.listItems = listItems;
    }

    public int getSizeList3D() {
        return size;
    }

}
