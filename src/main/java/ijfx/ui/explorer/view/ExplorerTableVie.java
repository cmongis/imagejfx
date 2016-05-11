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

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import ijfx.core.metadata.MetaData;
import ijfx.core.metadata.MetaDataOwner;
import ijfx.ui.batch.MetaDataSetOwnerHelper;
import ijfx.ui.explorer.Explorable;
import ijfx.ui.explorer.ExplorerView;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.Observable;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = ExplorerView.class)
public class ExplorerTableVie implements ExplorerView{

    TableView<Explorable> tableView = new TableView<>();
    
    MetaDataSetOwnerHelper<Explorable> helper = new MetaDataSetOwnerHelper(tableView);
     
    
    List<? extends Explorable> currentItems;
    public ExplorerTableVie() {
        
        System.out.println("Listening now");
       tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
       tableView.getSelectionModel().getSelectedItems().addListener(this::onListChange);
       tableView.getSelectionModel().selectedItemProperty().addListener(this::onSelectedItemChanged);
       tableView.setRowFactory(this::createRow);
       helper.setPriority(MetaData.FILE_NAME,MetaData.FILE_SIZE);
        
    }
    
    @Override
    public Node getNode() {
        return tableView;
    } 

    @Override
    public void setItem(List<? extends Explorable> items) {
        
        helper.setColumnsFromItems(items);
        helper.setItem(items);
        currentItems = items;
        
        
        List<? extends Explorable> selected = items
                .stream()
                .filter(item->item.selectedProperty().getValue())
                .collect(Collectors.toList());
                
                
        tableView.getSelectionModel().getSelectedItems().addAll(selected);
        
    }

    @Override
    public List<? extends Explorable> getSelectedItems() {
        return tableView
                .getSelectionModel()
                .getSelectedItems()
                .stream()
                .map(i->(Explorable)i)
                .collect(Collectors.toList());
    }

    @Override
    public Node getIcon() {
        return new FontAwesomeIconView(FontAwesomeIcon.TABLE);
    }

    @Override
    public void setSelectedItem(List<? extends Explorable> items) {
        items.forEach(tableView.getSelectionModel()::select);
    }
    
    private void onListChange(ListChangeListener.Change<? extends Explorable> changes) {
        
        while(changes.next()) {
          
            changes.getAddedSubList()
                    .stream()
                    .map(owner->(Explorable)owner)
                    .forEach(explo->explo.selectedProperty().setValue(true));
            
            changes.getRemoved()
                    .stream()
                    .map(owner->(Explorable)owner)
                    .forEach(explo->explo.selectedProperty().setValue(false));
        }
    }
    
    private void onSelectedItemChanged(Observable obs, Explorable oldValue, Explorable newValue) {
        currentItems.forEach(item->{
            item.selectedProperty().setValue(tableView.getSelectionModel().getSelectedItems().contains(item));
        });
    }
    
    private TableRow<Explorable> createRow(TableView<Explorable> explorable) {
        
        TableRow<Explorable> row = new TableRow<>();
        row.setOnMouseClicked(event->{
            if(event.getClickCount() == 2 && row.isEmpty() == false) {
                Explorable e = row.getItem();
                e.open();
            }
        });
        
        return row;
    }
   
    
    private void select(Explorable owner) {
        if(owner==null)return;
        ((Explorable)owner).selectedProperty().setValue(true);
    }
    
    private void unselect(Explorable owner) {
        if(owner == null) return;
        ((Explorable)owner).selectedProperty().setValue(false);
    }
}
