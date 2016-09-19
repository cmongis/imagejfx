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

import ijfx.ui.explorer.Explorable;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;

/**
 *
 * @author cyril
 */
public class ExplorableTableHelper extends MetaDataSetOwnerHelper<Explorable>{
    
    
    boolean addSelectionColumn = true;
    
     private TableColumn<Explorable,Boolean> checkBoxColumn;
    
    public ExplorableTableHelper(TableView<Explorable> tableView) {
        super(tableView);
        
        tableView.getColumns().add(getCheckboxColumn());
        
        
    }
    
    
    @Override
       protected void updateColumns(List<String> columnList) {
        columnList.sort(priority);
       final int offset = getOffset();
        if (!columnList.equals(currentColumns)) {
            System.out.println("The columns are not the same, updating");
            setColumnNumber(columnList.size());
            currentColumns = new HashSet(columnList);
            IntStream.range(0, columnList.size()).forEach(n -> {
                tableView.getColumns().get(n+offset).setUserData(columnList.get(n));
                tableView.getColumns().get(n+offset).setText(columnList.get(n));
            });
            
            
            
        }
        
        
    }

    private int getOffset() {
         return addSelectionColumn ? 1 : 0;
    }   
       
    private void setColumnNumber(Integer number) {
       
       
        
        final int offset = getOffset();
        int actualSize = tableView.getColumns().size() - offset;
        
         if(actualSize < 0) {
            tableView.getColumns().add(getCheckboxColumn());
            actualSize = tableView.getColumns().size() - offset;
        }
        
         System.out.println(String.format("Changing the number of column from %d to %d", actualSize, number));
        if (number == 0) {
            tableView.getColumns().clear();
            return;
        }
       

        if (actualSize == number) {
            return;
        }

        if (actualSize > number) {
            tableView.getColumns().removeAll(tableView.getColumns().subList(number - 1 + offset, actualSize - 1 + offset));
        } else {
            tableView.getColumns().addAll(IntStream
                    .range(0, number)
                    .mapToObj(i -> generateColumn(""))
                    .collect(Collectors.toList()));
        }
    }
    
    
    
     private TableColumn<Explorable,Boolean> getCheckboxColumn() {
        if(checkBoxColumn == null) {
            checkBoxColumn = new TableColumn<>();
            checkBoxColumn.setCellValueFactory(p -> p.getValue().selectedProperty());
            checkBoxColumn.setCellFactory(p->new CheckBoxTableCell());
            checkBoxColumn.setEditable(true);
        }
        return checkBoxColumn;
    }
}
