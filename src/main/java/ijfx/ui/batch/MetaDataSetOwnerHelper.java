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
package ijfx.ui.batch;

import ijfx.core.metadata.MetaData;
import ijfx.core.metadata.MetaDataOwner;
import ijfx.core.metadata.MetaDataSet;
import ijfx.core.metadata.MetaDataSetUtils;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 *
 * @author cyril
 */
public class MetaDataSetOwnerHelper<T extends MetaDataOwner> {

    final TableView<T> tableView;

    Set<String> currentColumns = new HashSet<>();

    LinkedHashSet<String> priority = new LinkedHashSet();
    
    public MetaDataSetOwnerHelper(TableView<T> tableView) {
        this.tableView = tableView;
    }

    public void setItem(List<? extends T> mList) {
        tableView.getItems().clear();
        tableView.getItems().addAll(mList);
        
    }

    public Set<String> getCurrentColumns() {
        return currentColumns;
    }

    public void setColumns(String... columns) {

        updateColums(columns);
    }

    public void setColums(List<String> columnList) {
        updateColumns(columnList);
    }

    public void setColumnsFromItems(List<? extends T> items) {
        List<MetaDataSet> mList = items
                .stream()
                .map(i->i.getMetaDataSet())
                .collect(Collectors.toList());
        updateColumns(MetaDataSetUtils.getAllPossibleKeys(mList).stream().filter(MetaData::canDisplay).collect(Collectors.toList()));
    }

    private void updateColums(String... columnList) {
        updateColumns(Arrays.asList(columnList));
    }

    private void updateColumns(List<String> columnList) {
        columnList.sort(this::comparePriority);
        if (Collections.disjoint(currentColumns, columnList)) {
            System.out.println("The columns are not the same, updating");
            setColumnNumber(columnList.size());
            IntStream.range(0, columnList.size()).forEach(n -> {
                tableView.getColumns().get(n).setUserData(columnList.get(n));
                tableView.getColumns().get(n).setText(columnList.get(n));
            });
        }
    }

    private void setColumnNumber(Integer number) {
        int actualSize = tableView.getColumns().size();
        System.out.println(String.format("Changing the number of column from %d to %d", actualSize, number));
        
        if(number == 0) {
            tableView.getColumns().clear();
            return;
        }
        
        if (actualSize == number) {
            return;
        }

        if (actualSize > number) {
            tableView.getColumns().removeAll(tableView.getColumns().subList(number - 1, actualSize - 1));
        } else {
            tableView.getColumns().addAll(IntStream
                    .range(0, number - actualSize)
                    .mapToObj(i -> generateColumn(""))
                    .collect(Collectors.toList()));
        }
    }

    private TableColumn<T, String> generateColumn(String key) {
        TableColumn<T, String> column = new TableColumn<>();
        column.setUserData(key);
        column.setCellValueFactory(this::getCellValueFactory);
        return column;
    }
    
    public void setPriority(Collection<String> priorityOrder) {
        
        priority = new LinkedHashSet(priorityOrder);
        
    }
    
    public void setPriority(String... keyName) {
        setPriority(Arrays.asList(keyName));
    }
    
    public String[] getPriority() {
        return priority.toArray(new String[priority.size()]);
    }
    
    public Set<String> applyPriority(Set<String> strings) {
        return strings
                .stream()
                .sorted(this::comparePriority)
                .collect(Collectors.toSet());
    }

    protected int comparePriority(String s1, String s2) {

        Integer is1 = priorityIndex(priority,s1) ;
        Integer is2 = priorityIndex(priority,s2);
        
        Integer c = s1.compareTo(s2);
        
        return 100 * (is2-is1) + c;
    }
    
    public int priorityIndex(Set<String> set, String element) {
        int i = 0;
        for(String s : set) {
            if(s.equals(element)) return 100-i;
            i++;
        }
        return 0;
    }
    
    protected ObservableValue<String> getCellValueFactory(TableColumn.CellDataFeatures<T, String> cell) {
        String key = cell.getTableColumn().getUserData().toString();
        String value = cell.getValue().getMetaDataSet().get(key).getStringValue();
        return new ReadOnlyObjectWrapper<>(value);
    }

}
