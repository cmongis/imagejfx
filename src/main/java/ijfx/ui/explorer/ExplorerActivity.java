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

import ijfx.core.metadata.MetaData;
import ijfx.core.metadata.MetaDataOwner;
import ijfx.service.ui.LoadingScreenService;
import ijfx.service.uicontext.UiContextService;
import ijfx.ui.UiContexts;
import ijfx.ui.activity.Activity;
import ijfx.ui.explorer.cell.FolderListCellCtrl;
import ijfx.ui.explorer.event.DisplayedListChanged;
import ijfx.ui.explorer.event.FolderAddedEvent;
import ijfx.ui.explorer.event.FolderDeletedEvent;
import ijfx.ui.explorer.event.ExploredListChanged;
import ijfx.ui.explorer.event.FolderUpdatedEvent;
import ijfx.ui.filter.DefaultMetaDataFilterFactory;
import ijfx.ui.filter.MetaDataFilterFactory;
import ijfx.ui.filter.MetaDataOwnerFilter;
import ijfx.ui.main.SideMenuBinding;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import mongis.utils.AsyncCallback;
import mongis.utils.FXUtilities;
import org.reactfx.EventStreams;
import org.scijava.app.StatusService;
import org.scijava.event.EventHandler;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;

/**
 *
 * @author cyril
 */
@Plugin(type = Activity.class,name="explorerActivity")
public class ExplorerActivity extends AnchorPane implements Activity {

    @FXML
    ListView<Folder> folderListView;

    @FXML
    BorderPane contentBorderPane;

    @FXML
    ToggleButton filterToggleButton;

    @FXML
    TextField filterTextField;

    @FXML
    Accordion filterVBox;

    @FXML
    ToggleButton fileModeToggleButton;

    @FXML
    ToggleButton planeModeToggleButton;

    @FXML
    ToggleButton objectModeToggleButton;

    @FXML
     HBox viewHBox;
    
    ToggleGroup explorationModeToggleGroup;

    ToggleGroup viewToggleGroup;
    
    @Parameter
    FolderManagerService folderManagerService;

    @Parameter
    ExplorerService explorerService;

    @Parameter
    LoadingScreenService loadingScreenService;

    @Parameter
    StatusService statusService;

    @Parameter
    PluginService pluginService;
    
    @Parameter
    UiContextService uiContextService;
    
    ExplorerView view;

    List<Runnable> folderUpdateHandler = new ArrayList<>();

    Property<ExplorationMode> explorationModeProperty = new SimpleObjectProperty<>();

    public ExplorerActivity() {
        try {
            FXUtilities.injectFXML(this);

           // contentBorderPane.setCenter(view.getNode());
            folderListView.setCellFactory(this::createFolderListCell);
            folderListView.getSelectionModel().selectedItemProperty().addListener(this::onFolderSelectionChanged);

            SideMenuBinding binding = new SideMenuBinding(filterVBox);

            binding.showProperty().bind(filterToggleButton.selectedProperty());
            filterVBox.setTranslateX(-250);

            explorationModeToggleGroup = new ToggleGroup();
            explorationModeToggleGroup.getToggles().addAll(fileModeToggleButton, planeModeToggleButton, objectModeToggleButton);
            
            
            fileModeToggleButton.setUserData(ExplorationMode.FILE);
            planeModeToggleButton.setUserData(ExplorationMode.PLANE);
            objectModeToggleButton.setUserData(ExplorationMode.OBJECT);
            
            explorationModeToggleGroup.selectedToggleProperty().addListener(this::onToggleSelectionChanged);
            
            
           
            
            EventStreams.valuesOf(filterTextField.textProperty()).successionEnds(Duration.ofSeconds(1))
                    .subscribe(this::updateTextFilter);

        } catch (IOException ex) {
            Logger.getLogger(ExplorerActivity.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    
    private void init() {
        
        if(view == null) {
            
            List<ExplorerView> views = pluginService.createInstancesOfType(ExplorerView.class);
            
            
            List<ToggleButton> buttons = views.stream().map(this::createViewToggle).collect(Collectors.toList());
            
            viewToggleGroup = new ToggleGroup();
            
            viewToggleGroup.getToggles().addAll(buttons);
            
            viewToggleGroup.selectedToggleProperty().addListener(this::onViewModeChanged);
            
            viewToggleGroup.selectToggle(buttons.get(0));
            
            viewHBox.getChildren().addAll(buttons);
            
            buttons.get(0).getStyleClass().add("first");
            buttons.get(buttons.size()-1).getStyleClass().add("last");
            
        }
        
    }
    
    @Override
    public Node getContent() {
        return this;
    }

    @Override
    public Task updateOnShow() {
        
        init();
        
        explorationModeToggleGroup.selectToggle(getToggleButton(folderManagerService.getCurrentExplorationMode()));
        return new AsyncCallback<Void, List<Explorable>>()
                .run(this::update)
                .then(this::updateUi)
                .start();
    }

    // get the list of items to show from the explorer service
    public List<Explorable> update(Void v) {
        if (folderManagerService.getCurrentFolder() == null) {
            return new ArrayList<Explorable>();
        } else {
            return explorerService.getFilteredItems();
        }
    }

    public void onFolderSelectionChanged(Observable obs, Folder oldValue, Folder newValue) {
        folderManagerService.setCurrentFolder(newValue);
    }

    public void updateFolderList() {
        folderListView
                .getItems()
                .addAll(
                        folderManagerService
                        .getFolderList()
                        .stream()
                        .filter(this::isNotDisplayed)
                        .collect(Collectors.toList()));
    }

    
    public void updateExplorerView(ExplorerView view) {
        this.view = view;
        contentBorderPane.setCenter(view.getNode());
        view.setItem(explorerService.getFilteredItems());
    }
    
    public void updateUi(List<? extends Explorable> explorable) {

        updateFolderList();
        System.out.println(explorable.size());
        if (explorable == null) {
            contentBorderPane.setCenter(new Label("Drag and drop a folder containing your image to explore it"));

        } else if (explorable.size() == 0) {
            contentBorderPane.setCenter(new Label("This folder doesn't contain any images... for now"));
        } else {
            contentBorderPane.setCenter(view.getNode());
            view.setItem(explorable);

        }

    }

    // returns true if the folder is not displayed yet
    private boolean isNotDisplayed(Folder folder) {
        return !folderListView.getItems().contains(folder);
    }

    @FXML
    public void addFolder() {
        File f = FXUtilities.openFolder("Open a folder", null);

        if (f != null) {
            folderManagerService.addFolder(f);
        }
    }

    @EventHandler
    public void onFolderAdded(FolderAddedEvent event) {
        Platform.runLater(this::updateFolderList);
    }

    @EventHandler
    public void onFolderDeleted(FolderDeletedEvent event) {
        folderListView.getItems().remove(event.getObject());
    }

    @EventHandler
    public void onExploredItemListChanged(ExploredListChanged event) {
        Platform.runLater(this::updateFilters);
    }

    @EventHandler
    public void onDisplayedItemListChanged(DisplayedListChanged event) {
        Platform.runLater(() -> updateUi(event.getObject()));

    }

    @EventHandler
    public void onFolderUpdated(FolderUpdatedEvent event) {
        System.out.println("Folder updated !");
        folderUpdateHandler.forEach(handler -> handler.run());
    }

    private class FolderListCell extends ListCell<Folder> {

        FolderListCellCtrl ctrl = new FolderListCellCtrl();

        public FolderListCell() {
            super();
            getStyleClass().add("selectable");
            itemProperty().addListener(this::onItemChanged);
        }

        public void onItemChanged(Observable obs, Folder oldValue, Folder newValue) {
            if (newValue == null) {
                setGraphic(null);
            } else {

                setGraphic(ctrl);

                if (newValue != null) {
                    ctrl.setItem(newValue);
                }
            }
        }

        public void update() {

            Platform.runLater(ctrl::forceUpdate);
        }
    }

    private ListCell<Folder> createFolderListCell(ListView<Folder> listView) {
        FolderListCell cell = new FolderListCell();
        folderUpdateHandler.add(cell::update);
        return cell;
    }

    List<? extends MetaDataOwnerFilter> currentFilters = new ArrayList<>();

    public void updateFilters() {

        new AsyncCallback<List<? extends Explorable>, List<MetaDataFilterWrapper>>()
                .setInput(explorerService.getItems())
                .run(this::generateFilter)
                .then(this::replaceFilters)
                .start();

    }

    protected void replaceFilters(List<MetaDataFilterWrapper> collect) {

        // stop listening to the current filters
        currentFilters.forEach(this::stopListeningToFilter);

        // making the new set of filters
        currentFilters = collect;

        filterVBox.getPanes().clear();

        // adding all the filter to the filter things
        filterVBox.getPanes().addAll(
                currentFilters
                .stream()
                .map(filter -> (TitledPane) filter.getContent())
                .collect(Collectors.toList())
        );

        // start listening to the new set
        currentFilters.forEach(this::listenFilter);

    }

    public List<MetaDataFilterWrapper> generateFilter(List<? extends Explorable> items) {
        MetaDataFilterFactory filterFactory = new DefaultMetaDataFilterFactory();
        Set<String> keySet = new HashSet();
        items
                .stream()
                .map(owner -> owner.getMetaDataSet().keySet())
                .forEach(keys -> keySet.addAll(keys));

        return keySet
                .stream()
                .map(key -> new MetaDataFilterWrapper(key, filterFactory.generateFilter(explorerService.getItems(), key)))
                .filter(filter -> filter.getContent() != null)
                .collect(Collectors.toList());
    }

    private void listenFilter(MetaDataOwnerFilter filter) {
        filter.predicateProperty().addListener(this::onFilterChanged);
    }

    private void stopListeningToFilter(MetaDataOwnerFilter filter) {
        filter.predicateProperty().removeListener(this::onFilterChanged);
    }

    private void onFilterChanged(Observable obs, Predicate<MetaDataOwner> oldValue, Predicate<MetaDataOwner> newValue) {
        System.out.println(currentFilters.stream().filter(f -> f.predicateProperty().getValue() != null).count());

        Predicate<MetaDataOwner> predicate = e -> true;

        List<Predicate<MetaDataOwner>> predicateList = currentFilters
                .stream()
                .map(f -> f.predicateProperty().getValue())
                .filter(v -> v != null)
                .collect(Collectors.toList());

        if (predicateList.isEmpty() == false) {

            for (Predicate<MetaDataOwner> p : predicateList) {
                predicate = predicate.and(p);
            }

        }

        explorerService.applyFilter(predicate);

    }

    /*
        View related functions
    */
    
    private ToggleButton createViewToggle(ExplorerView view) {
         ToggleButton toggle =  new ToggleButton(null,view.getIcon());
         toggle.setUserData(view);
         
         return toggle;
        
    }
    
    private void onViewModeChanged(Observable obs, Toggle oldValue, Toggle newValue) {
        updateExplorerView((ExplorerView)newValue.getUserData());
    }
    
    
    private class MetaDataFilterWrapper implements MetaDataOwnerFilter {

        private final MetaDataOwnerFilter filter;
        private final String title;
        private final TitledPane pane;

        public MetaDataFilterWrapper(String title, MetaDataOwnerFilter filter) {
            this.filter = filter;
            this.title = title;
            if (filter != null) {
                pane = new TitledPane(title, filter.getContent());
                pane.setExpanded(false);
                pane.getStyleClass().add("explorer-filter");
            } else {
                pane = null;
            }
        }

        @Override
        public Node getContent() {
            return pane;
        }

        @Override
        public Property<Predicate<MetaDataOwner>> predicateProperty() {
            return filter.predicateProperty();
        }
    }

    /*
        FXML Action
     */
    @FXML
    public void selectAll() {
        explorerService
                .getFilteredItems()
                .stream()
                .forEach(item -> item.selectedProperty().setValue(true));
    }

    @FXML
    public void onSegmentButtonPressed() {
        
        uiContextService.toggleContext("segment",!uiContextService.isCurrent("segment"));
        uiContextService.toggleContext("segmentation",!uiContextService.isCurrent("segmentation"));
        uiContextService.update();
        
        
    }

    @FXML
    public void process() {

    }
    @FXML
    public void openSelection() {
        explorerService.getSelectedItems().forEach(System.out::println);
    }

    public boolean isEverythingSelected() {
        if (explorerService == null) {
            return false;
        }
        return explorerService.getFilteredItems().stream().filter(item -> item.selectedProperty().getValue()).count()
                == explorerService.getFilteredItems().size();
    }

    protected void updateTextFilter(final String query) {
        if (filterTextField.getText() != null && !filterTextField.getText().equals("")) {
            explorerService.setOptionalFilter(m -> m.getMetaDataSet().get(MetaData.FILE_NAME).getStringValue().toLowerCase().contains(query.toLowerCase()));
        }
        else {
            explorerService.setOptionalFilter(null);
        }
    }
    
    /*
        Exploration Mode Toggle related methods
    
    */
    
    
    protected ExplorationMode getExplorationMode(Toggle toggle) {
        return (ExplorationMode) toggle.getUserData();
    }
    
    protected Toggle getToggleButton(ExplorationMode mode) {
        return explorationModeToggleGroup.getToggles().stream().filter(toggle->toggle.getUserData() == mode).findFirst().orElse(fileModeToggleButton);
    }
    
    
    protected void onToggleSelectionChanged(Observable obs, Toggle oldValue, Toggle newValue) {
        if(newValue == null) return;
        folderManagerService.setExplorationMode(getExplorationMode(newValue));
    }
    
    @EventHandler
    protected void onExplorationModeChanged(ExplorationModeChangeEvent event) {
        explorationModeToggleGroup.selectToggle(getToggleButton(event.getObject()));
    }
    
    @EventHandler
    protected void onExplorerServiceSelectionChanged(ExplorerSelectionChangedEvent event) {
        if(event.getObject().size() == 0) {
            System.out.println("nothing to select ?");
        }
        else {
            System.out.println("there is "+event.getObject().size());
        }
    }
    
}
