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
package ijfx.ui.explorer;

import ijfx.core.metadata.MetaDataOwner;
import ijfx.ui.explorer.event.DisplayedListChanged;
import ijfx.ui.explorer.event.ExploredListChanged;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import mongis.utils.AsyncCallback;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 *
 * @author cyril
 */
@Plugin(type = Service.class)
public class DefaultExplorerService extends AbstractService implements ExplorerService {

    List<Explorable> explorableList = new ArrayList<>();

    List<Explorable> filteredList = new ArrayList<>();

    @Parameter
    EventService eventService;

    Predicate<MetaDataOwner> lastFilter;
    Predicate<MetaDataOwner> optionalFilter;

    @Override
    public void setItems(List<Explorable> items) {

        explorableList = items;
        eventService.publish(new ExploredListChanged().setObject(items));
        applyFilter(lastFilter);

    }

    @Override
    public void applyFilter(Predicate<MetaDataOwner> predicate) {

        new AsyncCallback<Predicate<MetaDataOwner>, List<Explorable>>(predicate)
                .run(this::filter)
                .then(this::setFilteredItems)
                .start();

    }

    protected List<Explorable> filter(Predicate<MetaDataOwner> predicate) {

        if (predicate == null && optionalFilter == null) {
            return getItems();
        }
        else if(predicate == null && optionalFilter != null) {
            predicate = optionalFilter;
        }
        else if (optionalFilter != null) {
            predicate = predicate.and(optionalFilter);
        }
        return getItems().parallelStream().filter(predicate).collect(Collectors.toList());
    }

    @Override
    public List<Explorable> getItems() {
        return explorableList;
    }

    @Override
    public List<Explorable> getFilteredItems() {
        return filteredList;
    }

    protected void setFilteredItems(List<Explorable> filteredItems) {
        this.filteredList = filteredItems;
        eventService.publishLater(new DisplayedListChanged().setObject(filteredItems));
    }

    @Override
    public void setOptionalFilter(Predicate<MetaDataOwner> additionalFilter) {
       this.optionalFilter = additionalFilter;
       applyFilter(lastFilter);
    }

    @Override
    public List<? extends Explorable> getSelectedItems() {
        return filteredList
                .stream()
                .filter(item->item.selectedProperty().getValue())
                .collect(Collectors.toList());
    }

    @Override
    public void selectItem(Explorable explorable) {
        explorable.selectedProperty().setValue(true);
    }

}
