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
import ijfx.ui.batch.MetaDataSetOwnerHelper;
import ijfx.ui.explorer.Explorable;
import ijfx.ui.explorer.ExplorationMode;
import ijfx.ui.explorer.ExplorerSelectionChangedEvent;
import ijfx.ui.explorer.ExplorerService;
import ijfx.ui.explorer.ExplorerView;
import ijfx.ui.explorer.FolderManagerService;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.beans.Observable;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author cyril
 */
@Plugin(type = ExplorerView.class)
public class ExplorerTableVie implements ExplorerView{

    TableView<Explorable> tableView = new TableView<>();
    
    MetaDataSetOwnerHelper<Explorable> helper = new MetaDataSetOwnerHelper(tableView);
    
    @Parameter
    EventService eventService;
    
    @Parameter
    ExplorerService explorerService;
    
    @Parameter
            FolderManagerService folderService;
    
    List<? extends Explorable> currentItems;
    
    private static final String[] FILE_PRIORITY = { MetaData.FILE_NAME, MetaData.WIDTH, MetaData.HEIGHT,MetaData.BITS_PER_PIXEL,MetaData.SLICE_NUMBER , MetaData.SERIE_COUNT, MetaData.SLICE_NUMBER, MetaData.ZSTACK_NUMBER, MetaData.CHANNEL_COUNT, MetaData.TIME_COUNT};
    
    private static final String[] PLANE_PRIORITY = {MetaData.FILE_NAME, MetaData.PLANE_INDEX, MetaData.CHANNEL, MetaData.TIME, MetaData.Z_POSITION};
    
    public ExplorerTableVie() {
        System.out.println("Listening now");
       tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
       tableView.getSelectionModel().getSelectedItems().addListener(this::onListChange);
       tableView.getSelectionModel().selectedItemProperty().addListener(this::onSelectedItemChanged);
       tableView.setRowFactory(this::createRow);
    }

    
    
    @Override
    public Node getNode() {
        return tableView;
    } 

    @Override
    public void setItem(List<? extends Explorable> items) {
        
        
        
        if(getPriority().length != helper.getPriority().length) {
            helper.setPriority(getPriority());
        }
        
        helper.setColumnsFromItems(items);
        helper.setItem(items);
        currentItems = items;
        
        
        List<? extends Explorable> selected = items
                .stream()
                .filter(item->item.selectedProperty().getValue())
                .collect(Collectors.toList());
                
        
        //tableView.getSelectionModel().getSelectedItems().addAll(selected);
        
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
        
        if(eventService != null) {
            eventService.publish(new ExplorerSelectionChangedEvent().setObject(explorerService.getSelectedItems()));
        }
        
    }
    
    private TableRow<Explorable> createRow(TableView<Explorable> explorable) {
        
        TableRow<Explorable> row = new TableRow<>();
        row.setOnMouseClicked(event->{
            if(event.getClickCount() == 2 && row.isEmpty() == false) {
                Explorable e = row.getItem();
                try {
                    e.open();
                } catch (Exception ex) {
                    Logger.getLogger(ExplorerTableVie.class.getName()).log(Level.SEVERE, null, ex);
                }
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
    
    
    private String[] getPriority() {
        if(explorerService == null || folderService.getCurrentExplorationMode() == ExplorationMode.FILE) {
            return FILE_PRIORITY;
        }
        else return PLANE_PRIORITY;
    }
    
}
