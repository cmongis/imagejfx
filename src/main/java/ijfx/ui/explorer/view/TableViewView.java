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
import ijfx.core.metadata.MetaDataKeyPriority;
import ijfx.core.metadata.MetaDataSetType;
import ijfx.ui.batch.MetaDataSetOwnerHelper;
import ijfx.ui.explorer.Explorable;
import ijfx.ui.explorer.ExplorationMode;
import ijfx.ui.explorer.ExplorerSelectionChangedEvent;
import ijfx.ui.explorer.ExplorerService;
import ijfx.ui.explorer.ExplorerView;
import ijfx.ui.explorer.FolderManagerService;
import ijfx.ui.main.ImageJFX;
import java.util.List;
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
@Plugin(type = ExplorerView.class, priority = 0.9)
public class TableViewView implements ExplorerView {

    TableView<Explorable> tableView = new TableView<>();

    MetaDataSetOwnerHelper<Explorable> helper = new MetaDataSetOwnerHelper(tableView);

    @Parameter
    EventService eventService;

    @Parameter
    ExplorerService explorerService;

    @Parameter
    FolderManagerService folderService;

    List<? extends Explorable> currentItems;

    Logger logger = ImageJFX.getLogger();

    public TableViewView() {

        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tableView.getSelectionModel().getSelectedItems().addListener(this::onListChange);
        tableView.getSelectionModel().selectedItemProperty().addListener(this::onSelectedItemChanged);
        tableView.setRowFactory(this::createRow);

    }

    @Override
    public Node getNode() {
        return tableView;
    }

    String[] priority = new String[0];

    @Override
    public void setItem(List<? extends Explorable> items) {

        ImageJFX.getLogger().info(String.format("Setting %d items", items.size()));
        
        if(items == currentItems) return;
        
        if(items.size() >0)priority = MetaDataKeyPriority.getPriority(items.get(0).getMetaDataSet());
        helper.setPriority(priority);
        helper.setColumnsFromItems(items);
        helper.setItem(items);
        currentItems = items;

        List<? extends Explorable> selected = items
                .stream()
                .filter(item -> item.selectedProperty().getValue())
                .collect(Collectors.toList());

        setSelectedItem(selected);

        //tableView.getSelectionModel().getSelectedItems().addAll(selected);
    }

    @Override
    public List<? extends Explorable> getSelectedItems() {
        return tableView
                .getSelectionModel()
                .getSelectedItems()
                .stream()
                .map(i -> (Explorable) i)
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

        while (changes.next()) {

            logger.info(String.format("Selection changed : %d newly selected, %d unselected", changes.getAddedSize(), changes.getRemovedSize()));
            changes.getAddedSubList()
                    .stream()
                    .map(owner -> (Explorable) owner)
                    .forEach(explo -> explo.selectedProperty().setValue(true));

            changes.getRemoved()
                    .stream()
                    .map(owner -> (Explorable) owner)
                    .forEach(explo -> explo.selectedProperty().setValue(false));

        }
    }

    private void onSelectedItemChanged(Observable obs, Explorable oldValue, Explorable newValue) {
        currentItems.forEach(item -> {
            item.selectedProperty().setValue(tableView.getSelectionModel().getSelectedItems().contains(item));
        });

        if (eventService != null) {
            eventService.publish(new ExplorerSelectionChangedEvent().setObject(explorerService.getSelectedItems()));
        }

    }

    private TableRow<Explorable> createRow(TableView<Explorable> explorable) {

        TableRow<Explorable> row = new TableRow<>();
        row.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && row.isEmpty() == false) {
                Explorable e = row.getItem();
                explorerService.open(e);
            }
        });

        return row;
    }

    private void select(Explorable owner) {
        if (owner == null) {
            return;
        }
        ((Explorable) owner).selectedProperty().setValue(true);
    }

    private void unselect(Explorable owner) {
        if (owner == null) {
            return;
        }
        ((Explorable) owner).selectedProperty().setValue(false);
    }

    private String[] getPriority(MetaDataSetType t) {
        return priority;
    }

}
