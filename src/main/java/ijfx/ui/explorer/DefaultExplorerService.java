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
import ijfx.service.ui.LoadingScreenService;
import ijfx.ui.explorer.event.DisplayedListChanged;
import ijfx.ui.explorer.event.ExploredListChanged;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javafx.beans.Observable;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import mongis.utils.CallbackTask;
import org.reactfx.EventStream;
import org.reactfx.EventStreams;
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

    @Parameter
            LoadingScreenService loadingScreenService;
    
    Predicate<MetaDataOwner> lastFilter;
    Predicate<MetaDataOwner> optionalFilter;

    private final IntegerProperty selected = new SimpleIntegerProperty(0);
    
    EventStream<Integer> selectEvents = EventStreams.changesOf((ObservableValue)selected);
    
    
    @Override
    public void initialize() {
        selectEvents.successionEnds(Duration.ofSeconds(1)).subscribe(i->{
            eventService.publish(new ExplorerSelectionChangedEvent().setObject(getSelectedItems()));
        });
        
    }
    
    @Override
    public void setItems(List<Explorable> items) {

        if(explorableList != null) explorableList.forEach(this::stopListeningToExplorable);
        
        explorableList = items;
        
        if(explorableList != null) explorableList.forEach(this::listenToExplorableSelection);
        
        eventService.publish(new ExploredListChanged().setObject(items));
        applyFilter(lastFilter);
        
    }

    @Override
    public void applyFilter(Predicate<MetaDataOwner> predicate) {

        new CallbackTask<Predicate<MetaDataOwner>, List<Explorable>>(predicate)
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
    
    
  
    private void listenToExplorableSelection(Explorable expl) {
        expl.selectedProperty().addListener(this::onExplorableSelected);
    }
    
    private void stopListeningToExplorable(Explorable expl) {
        expl.selectedProperty().removeListener(this::onExplorableSelected);
    }
    
    private void onExplorableSelected(Observable obs, Boolean oldVAlue, Boolean newValue) {
        if(newValue) selected.add(1);
        else selected.add(-1);
    }
    
    public void open(Iconazable explorable){
        
        
        new CallbackTask<Void,Boolean>()
                    .setName("Opening file...")
                    .run(vd ->  {
                        try{ 
                        explorable.open();
                        return true;
                            }
                    catch(Exception e) {
                        return false;
                    }})
                    .then(success->{
                        if(success) {
                            
                        }
                    })
                    .submit(loadingScreenService)
                    .start();
        
        
    }

    
}
